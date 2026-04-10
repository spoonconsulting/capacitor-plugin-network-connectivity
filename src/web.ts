import { WebPlugin } from '@capacitor/core';

import type { NetworkConnectivityPlugin } from './definitions';

export class NetworkConnectivityWeb
  extends WebPlugin
  implements NetworkConnectivityPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
