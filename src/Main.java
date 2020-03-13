import java.io.IOException;
import java.util.logging.Logger;

class Main {
  private final static Logger log = Logger.getLogger(Main.class.getName());

  public static int parsePort(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      Main.log.severe("Unable to parse port number.");
      System.exit(1);
      return -1; // to satisfy the compiler
    }
  }
  
  public static void main(String args[]) throws IOException {
    int port = 3000;
    if (args.length > 0) {
      port = Main.parsePort(args[1]);
    }

    // String arg = args[0];
    // if (arg.equals("--server")) {
    //   if (args.length < 2) {
    //     Main.log.severe("Not enough args given! Please provide the port after --server.");
    //     return;
    //   }

    // int port = Main.parsePort(args[1]);

    //   ServerConnection server = ServerConnection.createServer(port);
    //   server.send("This is a test!");
    //   return;
    // } else if (arg.equals("--client")) {
    //   if (args.length < 3) {
    //     Main.log.severe("Not enough args given! Please provide the host and port after --client.");
    //     return;
    //   }

    //   String host = args[1];
    //   int port = Main.parsePort(args[2]);

    //   ClientConnection client = ClientConnection.createClient(host, port);
    //   System.out.println(client.receive());
    //   return;
    // }

    // Main.log.severe("Unknown argument: " + arg + "! Please send --server or --client.");
    

    Main.log.info("Running on port -> " + port);
    HTTPTikTacToeServer server = HTTPTikTacToeServer.createHttpServer(port);

    // Add shutdown hook to stop the server
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      Main.log.info("Shutting down server...");
      server.dispose();
    }));

    // Finally, start the server!
    server.start();
  }
}
