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
 * @type {HTMLButtonElement}
 */
let findGameButton

/**
 * @type {HTMLButtonElement}
 */
let hostGameButton


/**
 * @type {HTMLButtonElement}
 */
let resetGameButton

/**
 * @type {{ player: 'HOST' | 'OPPONENT', gameCode: string, eventSource: EventSource }}
 */
let gameState

window.onload = () => {
  accessCodeDisplay = document.getElementById("access-code-display");
  accessCode = document.getElementById("access-code");
  errorAlert = document.getElementById("error-alert");
  findGameButton = document.getElementById("find-game");
  hostGameButton = document.getElementById("host-game");
  resetGameButton = document.getElementById("reset-game");
}

const disableButtons = () => {
  findGameButton.disabled = true;
  hostGameButton.disabled = true;
  resetGameButton.disabled = false;
}

const enableButtons = () => {
  findGameButton.disabled = false;
  hostGameButton.disabled = false;
  resetGameButton.disabled = true;
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

  source.onerror = (event) => {
    console.log("onerror: " + event, source.readyState, event.eventPhase);
    if (source.readyState === EventSource.CLOSED) {
      errorAlert.innerText = "Error connecting to server.";
      resetGame();
    }
  }

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

  return source;
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
      eventSource: createSource(`/api/join-as-host?gameCode=${data.gameCode}`),
    };
    
    disableButtons();
    accessCodeDisplay.textContent = data.accessCode;
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
      eventSource: createSource(`/api/join-as-opponent?gameCode=${data.gameCode}`),
    }
    
    disableButtons();
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
    $(`#box-${x}-${y}`).removeClass("free-box").addClass("x-box");
  } else {
    $(`#box-${x}-${y}`).removeClass("free-box").addClass("o-box");
  }
}

/**
 * 
 * @param {0 | 1 | 2} x The row index.
 * @param {0 | 1 | 2} y The column index.
 */
const makePlay = async (x, y) => {
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
  $(".x-box").removeClass("x-box").addClass("free-box");
  $(".o-box").removeClass("o-box").addClass("free-box");
  enableButtons();

  // TODO: Remove
  $("#box-1-1").html("");
  accessCodeDisplay.textContent = "";
  gameState.eventSource.close();
  gameState = undefined;
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
    errorAlert.innerText = `Bad request to ${url}: ${json.error}`;
    return json;
  }

  console.log(`Data from ${url}: `, json.data);
  await onSuccess(json.data);
}
