import Foundation
import Capacitor

@objc(NetworkConnectivityPlugin)
public class NetworkConnectivityPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NetworkConnectivityPlugin"
    public let jsName = "NetworkConnectivity"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = NetworkConnectivity()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve(["value": implementation.echo(value)])
    }
}
