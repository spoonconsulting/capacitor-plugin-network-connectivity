package com.spoonconsulting.networkconnectivity;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.Nullable;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NetworkConnectivity")
public class NetworkConnectivityPlugin extends Plugin {

    @Nullable
    private ConnectivityManager connectivityManager;

    @Nullable
    private ConnectivityManager.NetworkCallback networkCallback;

    @Nullable
    private String lastState;

    @Nullable
    private String lastType;

    @Nullable
    private Boolean lastReachable;

    @Override
    public void load() {
        connectivityManager =
            (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        startMonitoring();
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        call.resolve(buildStatus());
    }

    private void startMonitoring() {
        final ConnectivityManager cm = connectivityManager;
        if (cm == null) {
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                emitIfChanged();
            }

            @Override
            public void onLost(Network network) {
                emitIfChanged();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                emitIfChanged();
            }
        };

        try {
            cm.registerDefaultNetworkCallback(networkCallback);
        } catch (Exception ignored) {
        }
    }

    private void emitIfChanged() {
        JSObject status = buildStatus();

        String state = status.getString("state");
        String type = status.getString("connectionType");
        Boolean reachable = status.has("internetReachable")
            ? status.getBool("internetReachable")
            : null;

        boolean changed =
            !safeEquals(state, lastState) ||
            !safeEquals(type, lastType) ||
            !safeEquals(reachable, lastReachable);

        if (changed) {
            lastState = state;
            lastType = type;
            lastReachable = reachable;
            notifyListeners("internetStatusChange", status);
        }
    }

    private JSObject buildStatus() {
        ConnectivityManager cm = connectivityManager;
        JSObject ret = new JSObject();

        if (cm == null) {
            ret.put("connected", false);
            ret.put("internetReachable", false);
            ret.put("connectionType", "none");
            ret.put("state", "offline");
            return ret;
        }

        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);

        boolean connected = activeNetwork != null && caps != null;
        boolean validated =
            caps != null &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        boolean isWifi =
            caps != null &&
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);

        boolean isCellular =
            caps != null &&
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

        String type;
        if (!connected) {
            type = "none";
        } else if (isWifi) {
            type = "wifi";
        } else if (isCellular) {
            type = "cellular";
        } else {
            type = "unknown";
        }

        String state;
        if (!connected) {
            state = "offline";
        } else if (validated) {
            state = "online";
        } else {
            state = "limited";
        }

        ret.put("connected", connected);
        ret.put("internetReachable", validated);
        ret.put("connectionType", type);
        ret.put("state", state);

        return ret;
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();

        try {
            ConnectivityManager cm = connectivityManager;
            ConnectivityManager.NetworkCallback cb = networkCallback;

            if (cm != null && cb != null) {
                cm.unregisterNetworkCallback(cb);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean safeEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
