import { registerPlugin } from '@capacitor/core';
const NetworkConnectivity = registerPlugin('NetworkConnectivity', {
    web: () => import('./web').then((m) => new m.NetworkConnectivityWeb()),
});
export * from './definitions';
export { NetworkConnectivity };
//# sourceMappingURL=index.js.map