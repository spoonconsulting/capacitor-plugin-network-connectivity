import { WebPlugin } from '@capacitor/core';
import type { InternetStatus, NetworkConnectivityPlugin } from './definitions';
export declare class NetworkConnectivityWeb extends WebPlugin implements NetworkConnectivityPlugin {
    private online;
    constructor();
    getStatus(): Promise<InternetStatus>;
    private currentStatus;
}
