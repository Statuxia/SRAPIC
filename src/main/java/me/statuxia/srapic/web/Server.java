package me.statuxia.srapic.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.statuxia.srapic.web.endpoint.Endpoint;
import me.statuxia.srapic.web.endpoint.Endpoints;
import me.statuxia.srapic.web.service.HandlerService;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class Server {

    public static boolean isStarted = false;
    private static HttpServer server;

    private Server() {
    }

    public synchronized static void start(JSONObject object) throws IOException, InvocationTargetException, IllegalAccessException {
        if (isStarted) {
            return;
        }
        isStarted = true;

        server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);

        for (Method method : HandlerService.class.getDeclaredMethods()) {
            Endpoint endpoint = method.getAnnotation(Endpoint.class);

            String value = endpoint.value();

            if (value.equals(Endpoints.API)) {
                server.createContext(value, (HttpHandler) method.invoke(endpoint, value));
                continue;
            }

            String endpointMethod = value.substring(value.lastIndexOf("/") + 1);

            if (object.has(endpointMethod) && object.getBoolean(endpointMethod)) {
                server.createContext(value, (HttpHandler) method.invoke(endpoint, value));
            }

        }

        server.setExecutor(null);
        server.start();
    }

    public synchronized static void stop() {
        if (!isStarted) {
            return;
        }
        isStarted = false;

        server.stop(0);
        server = null;
    }
}
