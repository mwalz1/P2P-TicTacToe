import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;

class Utils {
  private final static String LETTERS_NUMBERS = 
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
    "0123456789";

  private final static Logger log = Logger.getLogger(Utils.class.getName());

  public static String inputStreamToString(InputStream is) {
    return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
  }

  public static String getAlphaNumericString(int n) { 
    // create StringBuffer size of AlphaNumericString 
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      // generate a random number between 
      // 0 to AlphaNumericString variable length 
      int index = (int)(Utils.LETTERS_NUMBERS.length() * Math.random()); 

      // add Character one by one in end of sb 
      sb.append(Utils.LETTERS_NUMBERS.charAt(index)); 
    } 

    return sb.toString(); 
  } 
  

  public static int parsePort(String s, int defaultValue) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static Map<String, String> queryToMap(final String query) {
    Map<String, String> result = new HashMap<>();
    for (String param : query.split("&")) {
      String[] entry = param.split("=");
      if (entry.length > 1) {
        result.put(entry[0], entry[1]);
      } else {
        result.put(entry[0], "");
      }
    }

    return result;
  }

  public static Map<String, String> exchangeToParamMap(final HttpExchange exchange) {
    return Utils.queryToMap(exchange.getRequestURI().getQuery());
  }

  public static String mapToJSONString(Map<String, String> map) {
    StringBuilder b = new StringBuilder();
    b.append("{ ");
    boolean first = true;
    for (String key : map.keySet()) {
      if (!first) {
        b.append(", ");
      }

      b.append(String.format("\"%s\": \"%s\"", key, map.get(key)));
      first = false;
    }

    b.append(" }");
    return b.toString();
  }

  public static void sendSuccess(HttpExchange exchange, Optional<Map<String, String>> response) {
    String data = "null";
    try {
      data = Utils.mapToJSONString(response.get());
    } catch (NoSuchElementException e) {
      // do nothing
    }

    StringBuilder body = new StringBuilder();
    body.append("{ ");
    body.append("\"result\": \"success\", ");
    body.append(String.format("\"data\": %s", data));
    body.append(" }");

    Utils.sendResponse(exchange, 200, body.toString());
  }

  // TODO maybe just combine the two sendSuccess methods??
  public static void sendSuccess(HttpExchange exchange, Map<String, String> response) {
    Utils.sendSuccess(exchange, Optional.of(response));
  }

  public static void tryToClose(Closeable o) {
    try {
      o.close();
    } catch (IOException e) {
      // ignore
    }
  }

  public static <T> T getOrThrow(Optional<T> optional, String errorCode) {
    try {
      return optional.get();
    } catch (NoSuchElementException e) {
      throw new HttpError400(errorCode);
    }
  }

  public static void sendSseStream(HttpExchange exchange) {
    Headers headers = exchange.getResponseHeaders();
    headers.set("Content-Type", "text/event-stream");
    headers.set("Cache-Control", "no-cache");
    headers.set("Connection", "keep-alive");
    try {
      exchange.sendResponseHeaders(200, 0);
    } catch (IOException e) {
      Utils.log.severe("Unable to send response to " + exchange.getRemoteAddress());
      Utils.log.severe(e.toString());
    }
  }

  public static void sendResponse(HttpExchange exchange, int code, String response) {
    final OutputStream body = exchange.getResponseBody();
    byte[] bytes = response.getBytes();
    try {
      exchange.sendResponseHeaders(code, bytes.length);
      body.write(bytes);
      body.close();
    } catch (IOException e) {
      Utils.log.severe("Unable to send response to " + exchange.getRemoteAddress());
      Utils.log.severe(e.toString());
    }
  }

  public static HttpHandler handleGet(HttpHandler handler) {
    return (HttpExchange exchange) -> {
      if (!exchange.getRequestMethod().equals("GET")) {
        throw new HttpError405();
      }

      handler.handle(exchange);
    };
  }
}