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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    public static boolean isStarted = false;
    private static HttpServer server;

    private Server() {
    }

    public synchronized static void start(JSONObject configuration) throws IOException, InvocationTargetException, IllegalAccessException {
        if (isStarted) {
            return;
        }
        isStarted = true;

        server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        invokeEndpointContents(configuration);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.setExecutor(executor);
        server.start();
    }

    public static void invokeEndpointContents(JSONObject configuration) throws InvocationTargetException, IllegalAccessException {
        for (Method method : HandlerService.class.getDeclaredMethods()) {
            Endpoint endpoint = method.getAnnotation(Endpoint.class);

            String value = endpoint.value();
            if (value.equals(Endpoints.API)) {
                server.createContext(value, (HttpHandler) method.invoke(endpoint, value));
                continue;
            }

            String endpointMethod = value.substring(value.lastIndexOf("/") + 1);
            if (configuration.has(endpointMethod) && configuration.getBoolean(endpointMethod)) {
                server.createContext(value, (HttpHandler) method.invoke(endpoint, value));
            }
        }
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
