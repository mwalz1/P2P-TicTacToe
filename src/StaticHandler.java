import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

class StaticHandler extends FileHandler {
  final private String rootURI;

  StaticHandler(String rootURI) {
    this.rootURI = rootURI;
  }

  @Override
  protected Path getPath(HttpExchange exchange) {
    final URI requestURI = exchange.getRequestURI();
    final String filePath = requestURI.getPath();
    // replaceFirst removes the rootURI from the beginning of the string since we don't care about it
    return Paths.get("static", filePath.replaceFirst("^" + this.rootURI, ""));
  }
}