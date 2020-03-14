import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

enum ReceiveError {
  SocketTimeoutException, IOException,
}

abstract class Connection implements Disposer {
  private final static Logger log = Logger.getLogger(ClientConnection.class.getName());
  final private Socket socket;
  final private BufferedReader in;
  final private PrintWriter out;

  Connection(Socket socket, BufferedReader in, PrintWriter out) {
    this.socket = socket;
    this.in = in;
    this.out = out;
  }

  // TODO docs
  public Either<String, ReceiveError> receive() {
    String text = null;
    try {
      text = in.readLine();
    } catch (SocketTimeoutException e) {
      return Either.right(ReceiveError.SocketTimeoutException);
    } catch (IOException e) {
      Connection.log.severe("Unable to receive: " + e.getMessage());
      // TODO return more information
      return Either.right(ReceiveError.IOException);
    }

    return Either.left(text);
  }

  // TODO docs
  public boolean send(String output) {
    out.println(output);
    return out.checkError();
  }

  public void dispose() {
    try {
      this.out.close();
      this.in.close();
      this.socket.close();
    } catch (IOException e) {
      log.severe("Unable to close server, writer, reader, or socket: " + e.getMessage());
    }
  }
}
