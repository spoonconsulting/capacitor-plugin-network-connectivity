package com.spoonconsulting.networkconnectivity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NetworkConnectivity")
public class NetworkConnectivityPlugin extends Plugin {

    private final NetworkMonitor implementation = new NetworkMonitor();

    @Override
    public void load() {
        implementation.startMonitoring(getContext(), status ->
            notifyListeners("internetStatusChange", toJSObject(status))
        );
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        call.resolve(toJSObject(implementation.getStatus()));
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        implementation.stopMonitoring();
    }

    private JSObject toJSObject(NetworkMonitor.InternetStatus status) {
        JSObject ret = new JSObject();
        ret.put("connected", status.connected);
        ret.put("internetReachable", status.internetReachable);
        ret.put("connectionType", status.connectionType);
        ret.put("state", status.state);
        return ret;
    }
}
