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
let errorAlert

/**
 * @type {{ player: 'HOST' | 'OPPONENT', gameCode: string }}
 */
let gameState

window.onload = () => {
  accessCodeDisplay = document.getElementById("access-code-display");
  accessCode = document.getElementById("access-code");
  errorAlert = document.getElementById("error-alert");
}

/**
 * Create a SSE listener to listen for events from the game server. Each event represents a move 
 * made by the opposing player.
 * 
 * @param {String} url 
 */
const createSource = (url) => {
  // TODO error handling what if the url is bad?
  const source = new EventSource(url);

  source.onmessage = (event) => {
    /**
     * @type {{ location: [number, number] }}
     */
    const data = JSON.parse(event.data);
    console.log(data);

    const [x, y] = data.location;
    if (gameState.player === "HOST") {
      placeMarker(x, y, "O");
    } else {
      placeMarker(x, y, "X");
    }
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
 * @template T
 * @param {String} url 
 * @param {(data: T) => void} onSuccess 
 * @returns {{ result: "error", error: string } | { result: "success", data: T }}
 */
const get = async (url, onSuccess) => {
  const response = await fetch(url);

  /**
   * @type {{ result: "error", error: string } | { result: "success", data: T }}
   */
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
    errorAlert.innerText = `Bad request to ${url}: ${json.error}`;
    return json;
  }

  console.log(`Data from ${url}: `, json.data);
  await onSuccess(json.data);
}

/**
 * Start a game, set the text content to display the access code so the opponent can join and then
 * create an SSE stream.
 */
const hostGame = async () => {
  await get('/api/start-server', (data) => {
    // data is { gameCode: string, accessCode: string }

    gameState = {
      player: "HOST",
      gameCode: data.gameCode,
    };
  
    accessCodeDisplay.textContent = data.accessCode;
    createSource(`/api/join-as-host?gameCode=${gameState.gameCode}`);
  });
}

/**
 * Find a game using an access code. If the access code is valid, immediately use the game code
 * to create a SSE stream.
 */
const findGame = async () => {
  console.log(`Searching for game with accessCode: ${accessCode.value}`);
  await get(`/api/search-for-game?accessCode=${accessCode.value}`, (data) => {
    // data is { gameCode: string }

    gameState = {
      player: "OPPONENT",
      gameCode: data.gameCode,
    }
  
    createSource(`/api/join-as-opponent?gameCode=${gameState.gameCode}`);
  });
}

/**
 * 
 * @param {0 | 1 | 2} x 
 * @param {0 | 1 | 2} y 
 * @param {"X" | "O"} state 
 */
const placeMarker = async (x, y, state) => {
  console.log(`Placing ${state} at ${x},${y}.`);
  if (state === "X") {
    $(`#box-${x}-${y}`).removeClass("bg-dark").addClass("bg-success");
  } else {
    $(`#box-${x}-${y}`).removeClass("bg-dark").addClass("bg-danger");
  }
}

let hostTurn = true;
let gameOver = false;
let boxCount = 0;

let hostScore = 0;
let clientScore = 0;

/**
 * 
 * @param {HTMLElement} box 
 * @param {0 | 1 | 2} x The row index.
 * @param {0 | 1 | 2} y The column index.
 */
const makePlay = async (box, x, y) => {
  if (!gameState) {
    errorAlert.innerText = "The game has not yet started!";
    return;
  }

  await get(
    `/api/move?x=${x}&y=${y}&player=${gameState.player}&gameCode=${gameState.gameCode}`,
    (data) => {
      if (gameState.player === "HOST") {
        placeMarker(x, y, "X");
      } else {
        placeMarker(x, y, "O");
      }
      // data is { finished: 'yes' | 'no' }
      // TODO Use the data to set the game to finished or not maybe?
    }
  );
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
