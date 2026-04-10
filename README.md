# @spoonconsulting/capacitor-plugin-network-connectivity

Capacitor plugin that detects connection type and verifies actual internet access in real time.

## Install

To use npm

```bash
npm install @spoonconsulting/capacitor-plugin-network-connectivity
```

To use yarn

```bash
yarn add @spoonconsulting/capacitor-plugin-network-connectivity
```

Sync native files

```bash
npx cap sync
```

## API

<docgen-index>

* [`getStatus()`](#getstatus)
* [`addListener('internetStatusChange', ...)`](#addlistenerinternetstatuschange-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getStatus()

```typescript
getStatus() => Promise<InternetStatus>
```

Read the current network status once.

**Returns:** <code>Promise&lt;<a href="#internetstatus">InternetStatus</a>&gt;</code>

**Since:** 0.0.1

--------------------


### addListener('internetStatusChange', ...)

```typescript
addListener(eventName: 'internetStatusChange', listenerFunc: (status: InternetStatus) => void) => Promise<PluginListenerHandle>
```

Emits `internetStatusChange` when connectivity, reachability, or connection type changes.

| Param              | Type                                                                           |
| ------------------ | ------------------------------------------------------------------------------ |
| **`eventName`**    | <code>'internetStatusChange'</code>                                            |
| **`listenerFunc`** | <code>(status: <a href="#internetstatus">InternetStatus</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 0.0.1

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin.

**Since:** 0.0.1

--------------------


### Interfaces


#### InternetStatus

| Prop                    | Type                                                     | Description                                                                                                                                                          |
| ----------------------- | -------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`connected`**         | <code>boolean</code>                                     | True when there is an active network path or interface (platform-specific).                                                                                          |
| **`internetReachable`** | <code>boolean</code>                                     | True when the OS considers the network validated for general internet use. On Android this uses `NET_CAPABILITY_VALIDATED`; on iOS it follows `NWPath` satisfaction. |
| **`connectionType`**    | <code>'wifi' \| 'cellular' \| 'none' \| 'unknown'</code> | Active transport when known: Wi‑Fi, cellular, none, or unknown.                                                                                                      |
| **`state`**             | <code>'offline' \| 'limited' \| 'online'</code>          | High-level state: fully offline, connected but not validated (`limited`), or `online`.                                                                               |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
