import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;

class TemplateHandler extends FileHandler {
  final Path path;

  TemplateHandler(String path) {
    this.path = Path.of(path);
  }

  @Override
  protected Path getPath(HttpExchange exchange) {
    return path;
  }
}
