import Foundation
import Capacitor

@objc(NetworkConnectivityPlugin)
public class NetworkConnectivityPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NetworkConnectivityPlugin"
    public let jsName = "NetworkConnectivity"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getStatus", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = NetworkMonitor()

    public override func load() {
        implementation.startMonitoring { [weak self] status in
            self?.notifyListeners("internetStatusChange", data: status.toDictionary())
        }
    }

    @objc func getStatus(_ call: CAPPluginCall) {
        implementation.getStatus { status in
            call.resolve(status.toDictionary())
        }
    }

    deinit {
        implementation.stopMonitoring()
    }
}
