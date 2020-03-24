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
