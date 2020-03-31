'use strict';

/**
 * @type {HTMLInputElement}
 */
let accessCodeDisplay;
/**
 * @type {HTMLInputElement}
 */
let accessCode;
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
 * @type {{ player: 'host' | 'opponent', gameCode: string }}
 */
let gameState

window.onload = () => {
  accessCodeDisplay = document.getElementById("access-code-display");
  accessCode = document.getElementById("access-code");
  findingPort = document.getElementById("finding-port");
  hostSendInput = document.getElementById("host-send-input");
  output = document.getElementById("output");
}

const createSource = (url) => {
  // TODO error handling
  const source = new EventSource(url);

  source.onmessage = (event) => {
    // TODO error handling
    /**
     * @type {{ location: [number, number], move: "O" | "X" }}
     */
    const data = JSON.parse(event.data);

    // TODO handle this on the board
    console.log(data);
  }
}

/**
 * Sends a get request to the given url. Returns nothing if an error occurs (it will print the 
 * error though). If no error occurs, it returns the data in the json.
 * 
 * Expects the data to be in the following format:
 * ```
 * {
 *   result: "error";
 *   error: string;
 * }
 * 
 * // or
 * {
 *   result: "success";
 *   data: any;
 * }
 * ```
 * 
 * @param {String} url 
 * @returns {{ result: "error", error: string } | { result: "success", data: any }}
 */
const get = async (url) => {
  const response = await fetch(url);

  let json
  try {
    json = await response.json();
  } catch (e) {
    console.error(`Invalid json while fetching ${url}: ${e}`);
    return {
      result: "error",
      error: "INVALID_RESPONSE_JSON",
    };
  }

  if (json.result === "error") {
    console.error(`Bad request to ${url}: ${json.error}`)
    return json;
  }

  console.log(`Data from ${url}: `, json.data);
  return json;
}

const hostGame = async () => {
  const response = await get('/api/start-server');
  if (response.result === "error") {
    return;
  }

  gameState = {
    player: "host",
    gameCode: response.data.gameCode,
  };

  accessCodeDisplay.textContent = response.data.accessCode;
  createSource(`/api/join-as-host?gameCode=${gameState.gameCode}`);
}

const findGame = async () => {
  console.log(`Searching for game with accessCode: ${accessCode.value}`);
  const response = await get(`/api/search-for-game?accessCode=${accessCode.value}`);
  if (response.result === "error") {
    return;
  }

  gameState = {
    player: "opponent",
    gameCode: response.data.gameCode,
  }

  createSource(`/api/join-as-opponent?gameCode?=${gameState.gameCode}`);
}

/**
 * 
 * @param {0 | 1 | 2} x 
 * @param {0 | 1 | 2} y 
 * @param {"X" | "O"} state 
 */
const playRequest = async (x, y, state) => {
  // TODO Use this method to send a move to an opponent
  console.log(`Making play at ${x},${y} -> ${state}`);
  const response = await get(`/api/move?x=${x}&y=${y}&state=${state}`);
}

hostTurn = true;
gameOver = false;
boxCount = 0;

hostScore = 0;
clientScore = 0;

function makePlay(box) {
  if (!gameOver && $("#" + box.id).hasClass("free-box")) {
    if (hostTurn) {
      $("#" + box.id).removeClass("free-box").addClass("x-box");
      $("#" + box.id).removeClass("bg-dark").addClass("bg-success");
    }
    else if (!hostTurn) {
      $("#" + box.id).removeClass("free-box").addClass("o-box");
      $("#" + box.id).removeClass("bg-dark").addClass("bg-danger");
    }

    hostTurn = !hostTurn;
    boxCount++;

    checkWinner();
  }
  else if ($("#" + box.id).hasClass("restart-box")) {
    resetGame();
  }
}

function resetGame() {
  $(".x-box").removeClass("bg-success").addClass("bg-dark");
  $(".x-box").removeClass("x-box").addClass("free-box");
  $(".o-box").removeClass("bg-danger").addClass("bg-dark");
  $(".o-box").removeClass("o-box").addClass("free-box");

  // TODO: Remove
  $("#box-2-2").html("");

  gameOver = false;
  boxCount = 0;
}

function checkWinner() {
  if (boxCount >= 9) {
    gameOver = true;
  }
  if (boxCount >= 3) {
    // Do a bunch of checks
    if (
      ($("#box-1-1").hasClass("x-box") &&
      $("#box-1-2").hasClass("x-box") &&
      $("#box-1-3").hasClass("x-box")) ||
      ($("#box-2-1").hasClass("x-box") &&
      $("#box-2-2").hasClass("x-box") &&
      $("#box-2-3").hasClass("x-box")) ||
      ($("#box-3-1").hasClass("x-box") &&
      $("#box-3-2").hasClass("x-box") &&
      $("#box-3-3").hasClass("x-box")) ||
      ($("#box-1-1").hasClass("x-box") &&
      $("#box-2-1").hasClass("x-box") &&
      $("#box-3-1").hasClass("x-box")) ||
      ($("#box-1-2").hasClass("x-box") &&
      $("#box-2-2").hasClass("x-box") &&
      $("#box-3-2").hasClass("x-box")) ||
      ($("#box-1-3").hasClass("x-box") &&
      $("#box-2-3").hasClass("x-box") &&
      $("#box-3-3").hasClass("x-box")) ||
      ($("#box-1-1").hasClass("x-box") &&
      $("#box-2-2").hasClass("x-box") &&
      $("#box-3-3").hasClass("x-box")) ||
      ($("#box-1-3").hasClass("x-box") &&
      $("#box-2-2").hasClass("x-box") &&
      $("#box-3-1").hasClass("x-box"))
    ) {
      hostScore++;
      gameOver = true;
    }
    else if (
      // TODO: probs swap to an array of 0s and 1s.
      ($("#box-1-1").hasClass("o-box") &&
      $("#box-1-2").hasClass("o-box") &&
      $("#box-1-3").hasClass("o-box")) ||
      ($("#box-2-1").hasClass("o-box") &&
      $("#box-2-2").hasClass("o-box") &&
      $("#box-2-3").hasClass("o-box")) ||
      ($("#box-3-1").hasClass("o-box") &&
      $("#box-3-2").hasClass("o-box") &&
      $("#box-3-3").hasClass("o-box")) ||
      ($("#box-1-1").hasClass("o-box") &&
      $("#box-2-1").hasClass("o-box") &&
      $("#box-3-1").hasClass("o-box")) ||
      ($("#box-1-2").hasClass("o-box") &&
      $("#box-2-2").hasClass("o-box") &&
      $("#box-3-2").hasClass("o-box")) ||
      ($("#box-1-3").hasClass("o-box") &&
      $("#box-2-3").hasClass("o-box") &&
      $("#box-3-3").hasClass("o-box")) ||
      ($("#box-1-1").hasClass("o-box") &&
      $("#box-2-2").hasClass("o-box") &&
      $("#box-3-3").hasClass("o-box")) ||
      ($("#box-1-3").hasClass("o-box") &&
      $("#box-2-2").hasClass("o-box") &&
      $("#box-3-1").hasClass("o-box"))
    ) {
      clientScore++;
      gameOver = true;
    }
  }

  if (gameOver) {
    $("#box-2-2").addClass("restart-box").html("Click for new game.");

    // TODO: Remove.
    console.log("host score:" + hostScore + "\nclient score:" + clientScore);
  }
}
