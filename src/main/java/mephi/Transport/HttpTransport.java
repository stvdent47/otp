package mephi.Transport;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class HttpTransport {
    private static final Logger logger = LoggerFactory.getLogger(HttpTransport.class);

    public static <T> void sendResponse(
        HttpExchange exchange,
        int responseCode,
        ResponseBody<T> responseBody
    ) {
        try {
            exchange.sendResponseHeaders(responseCode, 0);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(new Gson().toJson(responseBody).getBytes());
            }

            exchange.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static ResponseBody<Object> getResponseEmptySuccess() {
        return new ResponseBody<>(
            ResponseBodyStatus.success,
            null,
            null
        );
    }

    public static <T> ResponseBody<T> getResponseSuccess(T data) {
        return new ResponseBody<>(
            ResponseBodyStatus.success,
            data,
            null
        );
    }

    public static ResponseBody<Object> getResponseError(String reason) {
        return new ResponseBody<>(
            ResponseBodyStatus.error,
            null,
            reason
        );
    }
}
