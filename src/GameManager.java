import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

class GameManager {
  private final static Logger log = Logger.getLogger(Main.class.getName());
  State[][] game = null;
  private Optional<Connection> connection = Optional.empty();

  public void startServer(HttpExchange exchange) {
    this.resetConnection();

    final Map<String, String> params = Utils.exchangeToParamMap(exchange);
    final String stringPort = params.getOrDefault("port", "");
    final int port = Utils.parsePort(stringPort, 8000);

    GameManager.log.info("Hosting server on port " + port);
    try {
      this.connection = Optional.of(ClientConnection.createConnection(port));
    } catch (IOException e) {
      GameManager.log.severe(e.toString());
      Utils.sendResponse(exchange, 500, "Unknown error");
      return;
    }

    GameManager.log.info("Successfully connected to a client!");
    Utils.sendSseStream(exchange);

    // this.initGame();
    final OutputStream body = exchange.getResponseBody();
    this.createMessageThread(body);
  }

  public void startClient(HttpExchange exchange) {
    this.resetConnection();

    final Map<String, String> params = Utils.exchangeToParamMap(exchange);
    final String host = params.getOrDefault("host", "localhost");
    final String stringPort = params.getOrDefault("port", "");
    final int port = Utils.parsePort(stringPort, 8000);

    try {
      this.connection = Optional.of(ServerConnection.createConnection(host, port));
    } catch (UnknownHostException e) {
      Utils.sendResponse(exchange, 400, "Unknown host: " + host + ":" + port);
    } catch (IOException e) {
      GameManager.log.severe(e.toString());
      Utils.sendResponse(exchange, 500, "Unknown error");
      return;
    }

    GameManager.log.info("Successfully connected to the server!");
    Utils.sendSseStream(exchange);
    
    final OutputStream body = exchange.getResponseBody();
    this.createMessageThread(body);
  }

  public void move(HttpExchange exchange) {
    Utils.sendSuccess(exchange);
  }

  public void send(HttpExchange exchange) {
    this.connection.ifPresentOrElse((connection) -> {
      String body = Utils.inputStreamToString(exchange.getRequestBody());
      connection.send(body);
      Utils.sendSuccess(exchange);
    }, () -> {
      Utils.sendResponse(exchange, 400, "No connection present.");
    });
  }

  public void dispose() {
    this.resetConnection();
  }

  private void createMessageThread(OutputStream body) {
    Thread t = new Thread(() -> {
      this.connection.ifPresent((server) -> {
        Either<String, ReceiveError> result = server.receive();
        
        result.ifLeft((message) -> {
          GameManager.log.info("Received: " + message);
          message = "data: " + message + "\n\n";
          try {
            body.write(message.getBytes());
            body.flush();
            this.createMessageThread(body);
          } catch (IOException e) {
            GameManager.log.severe("Unknown IOException while writing to SSE stream: " + e.toString());
          }
        });

        result.ifRight((error) -> {
          if (error == ReceiveError.SocketTimeoutException) {
            GameManager.log.info("Socket timeout! Not creating another message thread.");
          } else {
            GameManager.log.severe("Unknown IOException occurred while receiving message from peer.");
          }
        });
      });
    });

    t.start();
  }

  private void resetConnection() {
    this.connection.ifPresent((connection) -> {
      connection.dispose();
      this.connection = Optional.empty();
    });
  }

  private void initGame() {
    State[] row1 = { State.EMPTY, State.EMPTY, State.EMPTY };
    State[] row2 = { State.EMPTY, State.EMPTY, State.EMPTY };
    State[] row3 = { State.EMPTY, State.EMPTY, State.EMPTY };

    this.game = new State[][] { row1, row2, row3 };
  }
}

enum State {
  X, O, EMPTY
}
