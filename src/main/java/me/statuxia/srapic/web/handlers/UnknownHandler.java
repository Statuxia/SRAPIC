package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UnknownHandler extends DefaultHandler {

    public UnknownHandler(String endpoint) {
        super(endpoint);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject object = new JSONObject();
        object.put("error-code", 404);
        object.put("message", "Method not found");

        sendResponse(exchange, 404, object.toString().getBytes(StandardCharsets.UTF_8));
    }
}
