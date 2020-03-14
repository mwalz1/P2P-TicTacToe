import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ServerConnection extends Connection {
  public static ServerConnection createConnection(String host, int port) throws UnknownHostException, IOException {
    Socket socket = null;
    try {
      socket = new Socket(host, port);
      // 
      socket.setSoTimeout(100000);
    } catch (UnknownHostException e) {
      // TODO remove these
      ServerConnection.log.severe("Unknown host: " + host);
      System.exit(-1);
    } catch (IOException e) {
      ServerConnection.log.severe("Unable to get I/O connection to: " + host + " on port: " + port);
      System.exit(-1);
    }    

    BufferedReader in = null;
    PrintWriter out = null;
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true /* autoFlush */);
    } catch (IOException e) {
      ServerConnection.log.severe("Unable to get reader/writer");
    }

    return new ServerConnection(socket, in, out);
  }

  private final static Logger log = Logger.getLogger(ServerConnection.class.getName());

  private ServerConnection(Socket socket, BufferedReader in, PrintWriter out) {
    super(socket, in, out);
  }
}