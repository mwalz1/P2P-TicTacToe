'use strict';

/**
 * @type {HTMLInputElement}
 */
let accessCodeDisplay;
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

/**
 * @type {{ accessCode: string, gameCode: string }}
 */
let gameState

window.onload = () => {
  accessCodeDisplay = document.getElementById("access-code-display");
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
  // createSource(``);
  try {
    const response = await fetch('/api/start-server');
    gameState = await response.json();
    console.log("Starting game: ", gameState);
    accessCodeDisplay.textContent = gameState.accessCode;
  } catch (e) {
    console.warn(e);
    return;
  }

  // createSource(`/api/join-as-host?gameCode?=${gameState.gameCode}`);
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
