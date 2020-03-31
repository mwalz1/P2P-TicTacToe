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

enum PlayError {
  NOT_YOUR_TURN,
  OUT_OF_BOUNDS,
}

class Game implements Disposer {
  private final static Logger log = Logger.getLogger(Main.class.getName());
  public final String accessCode;
  public final String gameCode;
  private final State[][] game;
  private Optional<OutputStream> host;
  private Optional<OutputStream> opponent;
  private Player next = Player.HOST;

  Game() {
    State[] row1 = { State.EMPTY, State.EMPTY, State.EMPTY };
    State[] row2 = { State.EMPTY, State.EMPTY, State.EMPTY };
    State[] row3 = { State.EMPTY, State.EMPTY, State.EMPTY };

    this.game = new State[][] { row1, row2, row3 };
    this.accessCode = Utils.getAlphaNumericString(4);
    this.gameCode = Utils.getAlphaNumericString(20);
  }

  Optional<PlayError> play(Player player, int x, int y, PlayableState state) {
    if (this.next != player) {
      return Optional.of(PlayError.NOT_YOUR_TURN);
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
      return Optional.of(PlayError.NOT_YOUR_TURN);
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

    return Optional.empty();
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
}