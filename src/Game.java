import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.logging.Logger;

enum State {
  X, O, EMPTY
}

enum PlayableState {
  X, O
}

enum Player {
  HOST,
  OPPONENT,
}

enum PlayResult {
  NOT_YOUR_TURN,
  OUT_OF_BOUNDS,
  GAME_ALREADY_FINISHED,
  GAME_FINISHED,
  GAME_NOT_FINISHED,
  PLACEMENT_CONFLICT,
}

class Game implements Disposer {
  private final static Logger log = Logger.getLogger(Main.class.getName());
  public final String accessCode;
  public final String gameCode;
  private final State[][] game;
  private Optional<OutputStream> host = Optional.empty();
  private Optional<OutputStream> opponent = Optional.empty();
  private Player next = Player.HOST;
  private boolean finished = false;

  Game() {
    State[] row1 = { State.EMPTY, State.EMPTY, State.EMPTY };
    State[] row2 = { State.EMPTY, State.EMPTY, State.EMPTY };
    State[] row3 = { State.EMPTY, State.EMPTY, State.EMPTY };

    this.game = new State[][] { row1, row2, row3 };

    // TODO could we just have one short identifier?
    this.accessCode = Utils.getAlphaNumericString(4);
    this.gameCode = Utils.getAlphaNumericString(20);
  }

  PlayResult play(Player player, int x, int y, PlayableState state) {
    if (this.finished) {
      return PlayResult.GAME_ALREADY_FINISHED;
    }

    if (this.next != player) {
      return PlayResult.NOT_YOUR_TURN;
    }

    OutputStream stream;
    if (this.next == Player.HOST) {
      this.next = Player.OPPONENT;
      stream = Utils.getOrThrow(this.opponent, "NO_OPPONENT");
    } else {
      this.next = Player.HOST;
      stream = Utils.getOrThrow(this.host, "NO_HOST");
    }

    if (x > 2 || x < 0 || y > 2 || y < 0) {
      return PlayResult.OUT_OF_BOUNDS;
    }

    if (this.game[x][y] != State.EMPTY) {
      return PlayResult.PLACEMENT_CONFLICT;
    }

    if (state == PlayableState.O) {
      this.game[x][y] = State.O;
    } else {
      this.game[x][y] = State.X;
    }

    Game.log.info(player.toString() + " played " + state.toString() + " at " + x + ", " + y);
    String message = String.format(
      "data: { \"location\": [%d, %d], \"move\": \"%s\" }\n\n", 
      x, 
      y, 
      state.toString()
    );

    try {
      stream.write(message.getBytes());
      stream.flush();
    } catch (IOException e) {
      Game.log.severe("Unknown IOException while writing to SSE stream: " + e.toString());
    }

    this.checkFinished();

    if (this.finished) {
      return PlayResult.GAME_FINISHED;
    } else {
      return PlayResult.GAME_NOT_FINISHED;
    }
  }

  public void setHost(OutputStream host) {
    this.host = Optional.of(host);
  }

  public void setOpponent(OutputStream opponent) {
    this.opponent = Optional.of(opponent);
  }

  public void dispose() {
    this.host.ifPresent(Utils::tryToClose);
    this.opponent.ifPresent(Utils::tryToClose);
  }

  private void checkFinished() {
    this.finished = this.checkWon(State.X) || this.checkWon(State.O);
  }

  private boolean checkWon(State state) {
    for (int i = 0; i < 3; i++) {
      if (
        this.game[i][0] == state &&
        this.game[i][1] == state &&
        this.game[i][2] == state
      ) {
        return true;
      }
    }

    for (int j = 0; j < 3; j++) {
      if (
        this.game[0][j] == state &&
        this.game[1][j] == state &&
        this.game[2][j] == state
      ) {
        return true;
      }
    }

    if (
      this.game[0][0] == state &&
      this.game[1][1] == state &&
      this.game[2][2] == state
    ) {
      return true;
    }

    if (
      this.game[2][0] == state &&
      this.game[1][1] == state &&
      this.game[0][2] == state
    ) {
      return true;
    }

    return false;
  }
}