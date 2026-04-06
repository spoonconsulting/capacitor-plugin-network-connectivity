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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@CapacitorPlugin(name = "NetworkConnectivity")
public class NetworkConnectivityPlugin extends Plugin {

    private static final String[] PROBE_URLS = {
        "https://www.google.com/generate_204",
        "https://captive.apple.com",
        "https://one.one.one.one",
        "https://www.msftconnecttest.com/connecttest.txt"
    };

    private static final int PROBE_TIMEOUT_MS = 5000;

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

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void load() {
        connectivityManager =
            (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        startMonitoring();
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        executor.execute(() -> {
            JSObject status = buildStatus();
            call.resolve(status);
        });
    }

    private void startMonitoring() {
        final ConnectivityManager cm = connectivityManager;
        if (cm == null) {
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                executor.execute(() -> emitIfChanged());
            }

            @Override
            public void onLost(Network network) {
                executor.execute(() -> emitIfChanged());
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                executor.execute(() -> emitIfChanged());
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

        boolean internetReachable = connected && probeInternet();

        String state;
        if (!connected) {
            state = "offline";
        } else if (internetReachable) {
            state = "online";
        } else {
            state = "limited";
        }

        ret.put("connected", connected);
        ret.put("internetReachable", internetReachable);
        ret.put("connectionType", type);
        ret.put("state", state);

        return ret;
    }

    /**
     * Fire concurrent HEAD requests to all probe URLs.
     * Returns true as soon as any one succeeds, or false if all fail / timeout.
     */
    private boolean probeInternet() {
        final AtomicBoolean reachable = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(PROBE_URLS.length);

        for (final String urlStr : PROBE_URLS) {
            executor.execute(() -> {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    conn.setRequestMethod("HEAD");
                    conn.setConnectTimeout(PROBE_TIMEOUT_MS);
                    conn.setReadTimeout(PROBE_TIMEOUT_MS);
                    conn.setUseCaches(false);

                    int code = conn.getResponseCode();
                    conn.disconnect();

                    if (code >= 200 && code < 400) {
                        reachable.set(true);
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(PROBE_TIMEOUT_MS + 1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        return reachable.get();
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
