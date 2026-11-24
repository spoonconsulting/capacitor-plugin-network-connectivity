package com.spoonconsulting.networkconnectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.Nullable;

public class NetworkMonitor {

    public interface StatusChangeListener {
        void onStatusChange(InternetStatus status);
    }

    public static class InternetStatus {
        public final boolean connected;
        public final boolean internetReachable;
        public final String connectionType;
        public final String state;

        public InternetStatus(boolean connected, boolean internetReachable, String connectionType, String state) {
            this.connected = connected;
            this.internetReachable = internetReachable;
            this.connectionType = connectionType;
            this.state = state;
        }
    }

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

    public void startMonitoring(Context context, StatusChangeListener listener) {
        connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final ConnectivityManager cm = connectivityManager;
        if (cm == null) {
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                emitIfChanged(listener);
            }

            @Override
            public void onLost(Network network) {
                emitIfChanged(listener);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                emitIfChanged(listener);
            }
        };

        try {
            cm.registerDefaultNetworkCallback(networkCallback);
        } catch (Exception ignored) {
        }
    }

    public void stopMonitoring() {
        try {
            ConnectivityManager cm = connectivityManager;
            ConnectivityManager.NetworkCallback cb = networkCallback;

            if (cm != null && cb != null) {
                cm.unregisterNetworkCallback(cb);
            }
        } catch (Exception ignored) {
        }
    }

    public InternetStatus getStatus() {
        return buildStatus();
    }

    private void emitIfChanged(StatusChangeListener listener) {
        InternetStatus status = buildStatus();

        boolean changed =
            !safeEquals(status.state, lastState) ||
            !safeEquals(status.connectionType, lastType) ||
            !safeEquals(status.internetReachable, lastReachable);

        if (changed) {
            lastState = status.state;
            lastType = status.connectionType;
            lastReachable = status.internetReachable;
            listener.onStatusChange(status);
        }
    }

    private InternetStatus buildStatus() {
        ConnectivityManager cm = connectivityManager;

        if (cm == null) {
            return new InternetStatus(false, false, "none", "offline");
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

        return new InternetStatus(connected, validated, type, state);
    }

    private boolean safeEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
