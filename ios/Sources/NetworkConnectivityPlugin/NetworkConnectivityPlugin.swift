import Foundation
import Capacitor
import Network

@objc(NetworkConnectivityPlugin)
public class NetworkConnectivityPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NetworkConnectivityPlugin"
    public let jsName = "NetworkConnectivity"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getStatus", returnType: CAPPluginReturnPromise)
    ]

    private let monitor = NWPathMonitor()
    private let monitorQueue = DispatchQueue(label: "NetworkConnectivityQueue")

    private var lastState: String?
    private var lastType: String?
    private var lastReachable: Bool?

    private let probeURLs: [URL] = [
        URL(string: "https://www.google.com/generate_204")!,
        URL(string: "https://captive.apple.com")!,
        URL(string: "https://one.one.one.one")!,
        URL(string: "https://www.msftconnecttest.com/connecttest.txt")!
    ]

    private lazy var probeSession: URLSession = {
        let config = URLSessionConfiguration.ephemeral
        config.timeoutIntervalForRequest = 5
        config.timeoutIntervalForResource = 5
        config.waitsForConnectivity = false
        config.requestCachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        return URLSession(configuration: config)
    }()

    public override func load() {
        monitor.pathUpdateHandler = { [weak self] path in
            self?.handlePathUpdate(path: path)
        }
        monitor.start(queue: monitorQueue)
    }

    @objc func getStatus(_ call: CAPPluginCall) {
        let path = monitor.currentPath
        let connected = path.status == .satisfied

        if !connected {
            call.resolve(buildStatus(path: path, internetReachable: false))
            return
        }

        probeInternet { reachable in
            call.resolve(self.buildStatus(path: path, internetReachable: reachable))
        }
    }

    private func handlePathUpdate(path: NWPath) {
        let connected = path.status == .satisfied

        if !connected {
            emitIfChanged(status: buildStatus(path: path, internetReachable: false))
            return
        }

        probeInternet { [weak self] reachable in
            guard let self = self else { return }
            self.emitIfChanged(status: self.buildStatus(path: path, internetReachable: reachable))
        }
    }

    /// Fire concurrent HEAD requests to all probe URLs.
    /// Calls completion with `true` as soon as any one succeeds, or `false` if all fail.
    private func probeInternet(completion: @escaping (Bool) -> Void) {
        let group = DispatchGroup()
        var reachable = false
        let lock = NSLock()

        for url in probeURLs {
            group.enter()
            var request = URLRequest(url: url)
            request.httpMethod = "HEAD"

            probeSession.dataTask(with: request) { _, response, _ in
                if let http = response as? HTTPURLResponse, (200..<400).contains(http.statusCode) {
                    lock.lock()
                    reachable = true
                    lock.unlock()
                }
                group.leave()
            }.resume()
        }

        group.notify(queue: monitorQueue) {
            completion(reachable)
        }
    }

    private func emitIfChanged(status: [String: Any]) {
        let state = status["state"] as? String
        let type = status["connectionType"] as? String
        let reachable = status["internetReachable"] as? Bool

        let changed = state != lastState || type != lastType || reachable != lastReachable

        if changed {
            lastState = state
            lastType = type
            lastReachable = reachable

            notifyListeners("internetStatusChange", data: status)
        }
    }

    private func buildStatus(path: NWPath, internetReachable: Bool) -> [String: Any] {
        let connected = path.status == .satisfied

        let connectionType: String
        if path.usesInterfaceType(.wifi) {
            connectionType = "wifi"
        } else if path.usesInterfaceType(.cellular) {
            connectionType = "cellular"
        } else {
            connectionType = connected ? "unknown" : "none"
        }

        let state: String
        if !connected {
            state = "offline"
        } else if internetReachable {
            state = "online"
        } else {
            state = "limited"
        }

        return [
            "connected": connected,
            "internetReachable": internetReachable,
            "connectionType": connectionType,
            "state": state
        ]
    }

    deinit {
        monitor.cancel()
    }
}
