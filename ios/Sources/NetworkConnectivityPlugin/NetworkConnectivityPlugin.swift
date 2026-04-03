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

    public override func load() {
        monitor.pathUpdateHandler = { [weak self] path in
            self?.emitIfChanged(path: path)
        }
        monitor.start(queue: monitorQueue)
    }

    @objc func getStatus(_ call: CAPPluginCall) {
        call.resolve(buildStatus(path: monitor.currentPath))
    }

    private func emitIfChanged(path: NWPath) {
        let status = buildStatus(path: path)

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

    private func buildStatus(path: NWPath) -> [String: Any] {
        let connected = path.status == .satisfied
        let reachable = path.status == .satisfied

        let connectionType: String
        if path.usesInterfaceType(.wifi) {
            connectionType = "wifi"
        } else if path.usesInterfaceType(.cellular) {
            connectionType = "cellular"
        } else {
            connectionType = connected ? "unknown" : "none"
        }

        let state: String
        switch path.status {
        case .satisfied:
            state = "online"
        case .requiresConnection:
            state = "limited"
        case .unsatisfied:
            state = "offline"
        @unknown default:
            state = "limited"
        }

        return [
            "connected": connected,
            "internetReachable": reachable,
            "connectionType": connectionType,
            "state": state
        ]
    }

    deinit {
        monitor.cancel()
    }
}
