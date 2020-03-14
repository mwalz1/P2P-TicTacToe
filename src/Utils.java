import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

class Utils {
  private final static Logger log = Logger.getLogger(Utils.class.getName());

  public static String inputStreamToString(InputStream is) {
    return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
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

  public static void sendSuccess(HttpExchange exchange) {
    Utils.sendResponse(exchange, 200, "");
  }

  public static void sendResponse(HttpExchange exchange, String response) {
    Utils.sendResponse(exchange, 200, response);
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
}