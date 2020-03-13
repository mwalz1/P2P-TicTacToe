import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class HTTPTikTacToeServer {
  public static HTTPTikTacToeServer createHttpServer(final int port) throws IOException {
    final HTTPTikTacToeServer server = new HTTPTikTacToeServer(port);
    final HttpContext context = server.createContext("/");
    context.setHandler(server::handleRequest);
    return server;
  }

  private final HttpServer server;

  private HTTPTikTacToeServer(final int port) throws IOException {
    this.server = HttpServer.create(new InetSocketAddress(port), 0);
  }

  private void handleRequest(final HttpExchange exchange) throws IOException {
    final URI requestURI = exchange.getRequestURI();
    final String filePath = requestURI.getPath();
    final Path staticPath = Paths.get("static", filePath);
    final OutputStream body = exchange.getResponseBody();

    final File file = staticPath.toFile();
    if (!file.exists()) {
      byte[] response = "File not found".getBytes();
      exchange.sendResponseHeaders(404, response.length);
      body.write(response);
      body.close();
      return;
    }

    final FileInputStream stream = new FileInputStream(file);
    final byte[] response = new byte[(int) file.length()];
    stream.read(response);
    stream.close();

    exchange.sendResponseHeaders(200, response.length);
    body.write(response);
    body.close();
  }

  private void printRequestInfo(final HttpExchange exchange) {
    System.out.println("-- headers --");
    final Headers requestHeaders = exchange.getRequestHeaders();
    requestHeaders.entrySet().forEach(System.out::println);

    System.out.println("-- principle --");
    final HttpPrincipal principal = exchange.getPrincipal();
    System.out.println(principal);

    System.out.println("-- HTTP method --");
    final String requestMethod = exchange.getRequestMethod();
    System.out.println(requestMethod);

    System.out.println("-- query --");
    final URI requestURI = exchange.getRequestURI();
    final String query = requestURI.getQuery();
    System.out.println(query);
  }

  public HttpContext createContext(final String path) {
    return this.server.createContext(path);
  }

  public void start() {
    this.server.start();
  }

  public void dispose() {
    this.server.stop(0);
  }

  public static void main(String[] args) throws IOException {
    HTTPTikTacToeServer server = HTTPTikTacToeServer.createHttpServer(8500);
    server.start();
  }
}