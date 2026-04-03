import { registerPlugin } from '@capacitor/core';

import type { NetworkConnectivityPlugin } from './definitions';

const NetworkConnectivity = registerPlugin<NetworkConnectivityPlugin>(
  'NetworkConnectivity',
  {
    web: () =>
      import('./web').then((m) => new m.NetworkConnectivityWeb()),
  },
);

export * from './definitions';
export { NetworkConnectivity };
