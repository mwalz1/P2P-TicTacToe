import java.io.IOException;
import java.util.logging.Logger;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

class Main {
  private final static Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String args[]) throws IOException {
    int port = 3000;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        Main.log.severe("Unable to parse port number.");
        System.exit(1);
      }
    }

    Main.log.info("Running on port -> " + port);
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", new TemplateHandler("static/index.html"));
    server.createContext("/static/", new StaticHandler("/static/"));

    GameManager manager = new GameManager();
    server.createContext("/api/start-server", manager::startServer);
    server.createContext("/api/start-client", manager::startClient);
    server.createContext("/api/move", manager::move);
    server.createContext("/api/test", manager::testSse);

    // Add shutdown hook to stop the server
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      Main.log.info("Shutting down server...");
      server.stop(0);
      manager.dispose();
    }));

    // Finally, start the server!
    server.start();
  }
}
