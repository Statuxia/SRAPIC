package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.statuxia.srapic.SRAPIC;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class MetricsHandler extends DefaultHandler {

    public MetricsHandler(String endpoint) {
        super(endpoint);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject object = new JSONObject();
        Server server = SRAPIC.getSRAPIC().getServer();

        object.put("update-time", System.currentTimeMillis());
        object.put("tps", server.getTPS());
        object.put("online", server.getOnlinePlayers().size());
        object.put("players", server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));

        sendResponse(exchange, 200, object.toString().getBytes(StandardCharsets.UTF_8));
    }
}
