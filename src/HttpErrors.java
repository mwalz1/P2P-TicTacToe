import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

class HttpError extends RuntimeException {
  private static final long serialVersionUID = 3903228837194891673L;

  private final int status;
  private final String errorCode;

  protected HttpError(int status, String errorCode) {
    this.status = status;
    this.errorCode = errorCode;
  }

  static HttpHandler withErrorHandler(HttpHandler handler) {
    return (HttpExchange exchange) -> {
      try {
        handler.handle(exchange);
      } catch (HttpError e) {
        Map<String, String> body = new HashMap<>();
        body.put("result", "error");
        body.put("error", e.errorCode);

        Utils.sendResponse(exchange, e.status, Utils.mapToJSONString(body));
      }
    };
  }
}

class HttpError400 extends HttpError {
  private static final long serialVersionUID = 3903228837194891673L;

  HttpError400(String errorCode) {
    super(400, errorCode);
  }
}
