import { NetworkConnectivity } from '@spoonconsulting/capacitor-plugin-network-connectivity';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    NetworkConnectivity.echo({ value: inputValue })
}
