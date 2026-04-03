import { WebPlugin } from '@capacitor/core';
export class NetworkConnectivityWeb extends WebPlugin {
    constructor() {
        super();
        this.online = navigator.onLine;
        window.addEventListener('online', () => {
            this.online = true;
            this.notifyListeners('internetStatusChange', this.currentStatus());
        });
        window.addEventListener('offline', () => {
            this.online = false;
            this.notifyListeners('internetStatusChange', this.currentStatus());
        });
    }
    async getStatus() {
        this.online = navigator.onLine;
        return this.currentStatus();
    }
    currentStatus() {
        return {
            connected: this.online,
            internetReachable: this.online,
            connectionType: this.online ? 'unknown' : 'none',
            state: this.online ? 'online' : 'offline',
        };
    }
}
//# sourceMappingURL=web.js.map