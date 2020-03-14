/**
 * @type {HTMLInputElement}
 */
let hostingPort;
/**
 * @type {HTMLInputElement}
 */
let findingHost;
/**
 * @type {HTMLInputElement}
 */
let findingPort;
/**
 * @type {HTMLInputElement}
 */
let hostSendInput
/**
 * @type {HTMLInputElement}
 */
let findOutput;

window.onload = () => {
  hostingPort = document.getElementById("hosting-port");
  findingHost = document.getElementById("finding-host");
  findingPort = document.getElementById("finding-port");
  hostSendInput = document.getElementById("host-send-input");
  findOutput = document.getElementById("find-output");
}

const hostGame = async () => {
  // const result = await fetch(`/api/start-server?port=${port}`);
  // console.log(result);

  console.log('HELLO')
  const port = hostingPort.value;
  const source = new EventSource(`/api/start-server?port=${port}`);

  source.onmessage = (event) => {
    console.log(event);
    findOutput.innerText += "message: " + event.data;
  }
}

const findGame = () => {
  console.log('HELLO')
  const port = findingPort.value;
  const host = findingHost.value;
  const source = new EventSource(`/api/start-client?host=${host}&port=${port}`);

  source.onmessage = (event) => {
    console.log(event);
    findOutput.innerText += "message: " + event.data;
  }
}

const send = () => {
  //
}
