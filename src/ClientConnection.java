import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ClientConnection {
  public static ClientConnection createClient(String host, int port) {
    Socket socket = null;
    try {
      socket = new Socket(host, port);
    } catch (UnknownHostException e) {
      ClientConnection.log.severe("Unknown host: " + host);
      System.exit(-1);
    } catch (IOException e) {
      ClientConnection.log.severe("Unable to get I/O connection to: " + host + " on port: " + port);
      System.exit(-1);
    }

    BufferedReader in = null;
    PrintWriter out = null;
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true /* autoFlush */);
    } catch (IOException e) {
      ClientConnection.log.severe("Unable to get reader/writer");
    }

    return new ClientConnection(socket, in, out);
  }

  Socket socket = null;
  BufferedReader in = null;
  PrintWriter out = null;
  private final static Logger log = Logger.getLogger(ClientConnection.class.getName());

  private ClientConnection(Socket socket, BufferedReader in, PrintWriter out) {
    this.socket = socket;
    this.in = in;
    this.out = out;
  }

  public String receive() {
    String text = null;
    try {
      text = in.readLine();
    } catch (IOException e) {
      ClientConnection.log.severe("Unable to read from " + e.getMessage());
    }

    return text;
  }

  public void send(String output) {
    out.println(output);
  }

  public void dispose() {
    try {
      out.close();
      in.close();
      socket.close();
    } catch (IOException e) {
      ClientConnection.log.severe("Unable to close writer, reader, or socket: " + e.getMessage());
    }
  }
}