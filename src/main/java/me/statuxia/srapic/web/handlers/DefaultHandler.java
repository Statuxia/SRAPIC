package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class DefaultHandler implements HttpHandler {

    public final Map<String, Cache> cachedContent = new HashMap<>();
    protected final String endpoint;

    public DefaultHandler(String endpoint) {
        this.endpoint = endpoint;
    }

    protected void sendResponse(HttpExchange exchange, int code, byte[] responseContent, String endpoint) throws IOException {
        Cache cache = cachedContent.get(endpoint);
        if (cache == null || cache.isOld()) {
            cachedContent.put(endpoint, new Cache(code, responseContent));
        }
        exchange.sendResponseHeaders(code, responseContent.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(responseContent);
        stream.flush();
        stream.close();
    }

    protected static class Cache {
        public int code;
        public byte[] content;
        public int requestTime;

        public Cache(int code, byte[] content) {
            this.code = code;
            this.content = content;
            this.requestTime = (int) (System.currentTimeMillis() / 1000L);
        }

        public boolean isOld() {
            return this.requestTime + 60 < System.currentTimeMillis() / 1000L;
        }
    }

}
