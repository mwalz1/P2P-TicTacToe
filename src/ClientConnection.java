import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class ClientConnection extends Connection {
  public static ClientConnection createConnection(int port) throws IOException {
    ServerSocket server = null;
    // TODO remove
    try {
      server = new ServerSocket(port);
      System.out.println("Port opened on port " + port);
    } catch (IOException e) {
      ClientConnection.log.severe("Could not listen on port:  " + port);
      System.exit(-1);
    }

    Socket socket = null;
    try {
      socket = server.accept();
      socket.setSoTimeout(100000);
    } catch (IOException e) {
      ClientConnection.log.severe("Accept failed: " + e.getMessage());
      System.exit(-1);
    }

    BufferedReader in = null;
    PrintWriter out = null;
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true /* autoFlush */);
    } catch (IOException e) {
      ClientConnection.log.severe("Unable to get reader/writer");
      System.exit(-1);
    }

    return new ClientConnection(socket, in, out, server);
  }

  private ServerSocket server;
  private final static Logger log = Logger.getLogger(ClientConnection.class.getName());

  private ClientConnection(Socket socket, BufferedReader in, PrintWriter out, ServerSocket server) {
    super(socket, in, out);
    this.server = server;
  }

  public void dispose() {
    super.dispose();

    try {
      server.close();
    } catch (IOException e) {
      log.severe("Unable to close server: " + e.getMessage());
    }
  }
}