import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

abstract class FileHandler implements HttpHandler {
  protected abstract Path getPath(HttpExchange exchange);

  public void handle(HttpExchange exchange) throws IOException {
    final Path filePath = this.getPath(exchange);
    final OutputStream body = exchange.getResponseBody();

    final File file = filePath.toFile();
    if (!file.exists()) {
      byte[] response = ("File not found").getBytes();
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
}