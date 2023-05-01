package me.statuxia.srapic.web.service;

import com.sun.net.httpserver.HttpHandler;
import me.statuxia.srapic.web.endpoint.Endpoint;
import me.statuxia.srapic.web.endpoint.Endpoints;
import me.statuxia.srapic.web.handlers.InfoHandler;
import me.statuxia.srapic.web.handlers.MetricsHandler;
import me.statuxia.srapic.web.handlers.UnknownHandler;
import me.statuxia.srapic.web.handlers.WorldHandler;

public class HandlerService {

    @Endpoint(value = Endpoints.API)
    public static HttpHandler unknown(String endpoint) {
        return new UnknownHandler(endpoint);
    }

    @Endpoint(value = Endpoints.METRICS)
    public static HttpHandler metrics(String endpoint) {
        return new MetricsHandler(endpoint);
    }

    @Endpoint(value = Endpoints.INFO)
    public static HttpHandler info(String endpoint) {
        return new InfoHandler(endpoint);
    }

    @Endpoint(value = Endpoints.WORLD)
    public static HttpHandler world(String endpoint) {
        return new WorldHandler(endpoint);
    }
}
