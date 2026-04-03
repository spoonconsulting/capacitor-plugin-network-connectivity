'use strict';

var core = require('@capacitor/core');

const NetworkConnectivity = core.registerPlugin('NetworkConnectivity', {
    web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.NetworkConnectivityWeb()),
});

class NetworkConnectivityWeb extends core.WebPlugin {
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

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    NetworkConnectivityWeb: NetworkConnectivityWeb
});

exports.NetworkConnectivity = NetworkConnectivity;
//# sourceMappingURL=plugin.cjs.js.map
