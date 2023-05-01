package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public abstract class DefaultHandler implements HttpHandler {

    protected final String endpoint;

    public DefaultHandler(String endpoint) {
        this.endpoint = endpoint;
    }

    protected void sendResponse(HttpExchange exchange, int code, byte[] responseContent) throws IOException {
        exchange.sendResponseHeaders(code, responseContent.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(responseContent);
        stream.flush();
        stream.close();
    }
}
