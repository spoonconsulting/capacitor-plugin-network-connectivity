export interface NetworkConnectivityPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
