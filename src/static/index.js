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
let output;

window.onload = () => {
  hostingPort = document.getElementById("hosting-port");
  findingHost = document.getElementById("finding-host");
  findingPort = document.getElementById("finding-port");
  hostSendInput = document.getElementById("host-send-input");
  output = document.getElementById("output");
}

const addMessage = (message, className) => {
  const div = document.createElement('div');
  div.className = className;

  const innerDiv = document.createElement('div');
  innerDiv.innerText = message;

  div.appendChild(innerDiv);
  output.appendChild(div);
}

const createSource = (url) => {
  const source = new EventSource(url);
  source.onmessage = (event) => {
    console.log(event.data);
    addMessage(event.data, 'them');
  }  
}

const hostGame = async () => {
  const port = hostingPort.value;
  createSource(`/api/start-server?port=${port}`);
}

const findGame = () => {
  const port = findingPort.value;
  const host = findingHost.value;
  createSource(`/api/start-client?host=${host}&port=${port}`);
}

const send = async () => {
  const message = hostSendInput.value;
  const result = await fetch(`/api/send`, {
    method: 'POST',
    body: message,
  });

  if (result.status === 200) {
    addMessage(message, 'me');
  }
}
