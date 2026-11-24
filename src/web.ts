import { WebPlugin } from '@capacitor/core';

import type { InternetStatus, NetworkConnectivityPlugin } from './definitions';

export class NetworkConnectivityWeb
  extends WebPlugin
  implements NetworkConnectivityPlugin
{
  private online = navigator.onLine;

  constructor() {
    super();

    window.addEventListener('online', () => {
      this.online = true;
      this.notifyListeners('internetStatusChange', this.currentStatus());
    });

    window.addEventListener('offline', () => {
      this.online = false;
      this.notifyListeners('internetStatusChange', this.currentStatus());
    });
  }

  async getStatus(): Promise<InternetStatus> {
    this.online = navigator.onLine;
    return this.currentStatus();
  }

  private currentStatus(): InternetStatus {
    return {
      connected: this.online,
      internetReachable: this.online,
      connectionType: this.online ? 'unknown' : 'none',
      state: this.online ? 'online' : 'offline',
    };
  }
}
