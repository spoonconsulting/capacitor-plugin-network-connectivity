import Foundation
import Network

public struct InternetStatus {
    public let connected: Bool
    public let internetReachable: Bool
    public let connectionType: String
    public let state: String

    func toDictionary() -> [String: Any] {
        return [
            "connected": connected,
            "internetReachable": internetReachable,
            "connectionType": connectionType,
            "state": state
        ]
    }
}

public class NetworkMonitor {

    public typealias StatusChangeHandler = (InternetStatus) -> Void

    private let monitor = NWPathMonitor()
    private let monitorQueue = DispatchQueue(label: "NetworkConnectivityQueue")

    private var lastState: String?
    private var lastType: String?
    private var lastReachable: Bool?

    private var onChange: StatusChangeHandler?

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

    public func startMonitoring(onChange: @escaping StatusChangeHandler) {
        self.onChange = onChange
        monitor.pathUpdateHandler = { [weak self] path in
            self?.handlePathUpdate(path: path)
        }
        monitor.start(queue: monitorQueue)
    }

    public func stopMonitoring() {
        monitor.cancel()
    }

    public func getStatus(completion: @escaping (InternetStatus) -> Void) {
        let path = monitor.currentPath
        let connected = path.status == .satisfied

        if !connected {
            completion(buildStatus(path: path, internetReachable: false))
            return
        }

        probeInternet { reachable in
            completion(self.buildStatus(path: path, internetReachable: reachable))
        }
    }

    private func handlePathUpdate(path: NWPath) {
        let connected = path.status == .satisfied

        if !connected {
            emitIfChanged(buildStatus(path: path, internetReachable: false))
            return
        }

        probeInternet { [weak self] reachable in
            guard let self = self else { return }
            self.emitIfChanged(self.buildStatus(path: path, internetReachable: reachable))
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

    private func emitIfChanged(_ status: InternetStatus) {
        let changed = status.state != lastState
            || status.connectionType != lastType
            || status.internetReachable != lastReachable

        if changed {
            lastState = status.state
            lastType = status.connectionType
            lastReachable = status.internetReachable
            onChange?(status)
        }
    }

    private func buildStatus(path: NWPath, internetReachable: Bool) -> InternetStatus {
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

        return InternetStatus(
            connected: connected,
            internetReachable: internetReachable,
            connectionType: connectionType,
            state: state
        )
    }
}
