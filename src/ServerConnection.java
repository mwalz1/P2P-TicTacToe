import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class ServerConnection {
  public static ServerConnection createServer(int port) {
    ServerSocket server = null;
    try {
      server = new ServerSocket(port);
      System.out.println("Port opened on port " + port);
    } catch (IOException e) {
      ServerConnection.log.severe("Could not listen on port:  " + port);
      System.exit(-1);
    }

    Socket socket = null;
    try {
      socket = server.accept();
    } catch (IOException e) {
      ServerConnection.log.severe("Accept failed: " + e.getMessage());
      System.exit(-1);
    }

    BufferedReader in = null;
    PrintWriter out = null;
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true /* autoFlush */);
    } catch (IOException e) {
      ServerConnection.log.severe("Unable to get reader/writer");
      System.exit(-1);
    }

    return new ServerConnection(socket, in, out, server);
  }

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private ServerSocket server;
  private final static Logger log = Logger.getLogger(ServerConnection.class.getName());

  private ServerConnection(Socket socket, BufferedReader in, PrintWriter out, ServerSocket server) {
    this.socket = socket;
    this.in = in;
    this.out = out;
    this.server = server;
  }

  public String receive() {
    String text = null;
    try {
      text = in.readLine();
      log.fine("RECEIVING: " + text);
    } catch (IOException e) {
      log.severe("Unable to read from " + e.getMessage());
    }

    return text;
  }

  public void send(String output) {
    log.fine("SENDING: " + output);
    out.println(output); // Doesn't ever throw IO exception
  }

  public void dispose() {
    try {
      server.close();
      out.close();
      in.close();
      socket.close();
    } catch (IOException e) {
      log.severe("Unable to close server, writer, reader, or socket: " + e.getMessage());
    }
  }
}