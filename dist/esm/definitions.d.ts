import type { PluginListenerHandle } from '@capacitor/core';
export interface InternetStatus {
    /** True when there is an active network path or interface (platform-specific). */
    connected: boolean;
    /**
     * True when the OS considers the network validated for general internet use.
     * On Android this uses `NET_CAPABILITY_VALIDATED`; on iOS it follows `NWPath` satisfaction.
     */
    internetReachable: boolean;
    /** Active transport when known: Wi‑Fi, cellular, none, or unknown. */
    connectionType: 'wifi' | 'cellular' | 'none' | 'unknown';
    /** High-level state: fully offline, connected but not validated (`limited`), or `online`. */
    state: 'offline' | 'limited' | 'online';
}
export interface NetworkConnectivityPlugin {
    /**
     * Read the current network status once.
     *
     * @since 0.0.1
     */
    getStatus(): Promise<InternetStatus>;
    /**
     * Emits `internetStatusChange` when connectivity, reachability, or connection type changes.
     *
     * @since 0.0.1
     */
    addListener(eventName: 'internetStatusChange', listenerFunc: (status: InternetStatus) => void): Promise<PluginListenerHandle>;
    /**
     * Remove all listeners for this plugin.
     *
     * @since 0.0.1
     */
    removeAllListeners(): Promise<void>;
}
