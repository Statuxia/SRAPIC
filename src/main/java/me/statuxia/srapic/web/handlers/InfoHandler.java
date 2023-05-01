package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.statuxia.srapic.SRAPIC;
import org.bukkit.Server;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InfoHandler extends DefaultHandler {

    public InfoHandler(String endpoint) {
        super(endpoint);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject object = new JSONObject();
        Server server = SRAPIC.getSRAPIC().getServer();

        object.put("name", server.getName());
        object.put("motd", server.getMotd());
        object.put("version", server.getVersion());
        object.put("minecraft-version", server.getMinecraftVersion());
        object.put("bukkit-version", server.getBukkitVersion());
        object.put("total", String.format("%1$d/%2$d", server.getOnlinePlayers().size(), server.getMaxPlayers()));
        object.put("spawn-radius", server.getSpawnRadius());
        object.put("simulation-distance", server.getSimulationDistance());
        object.put("view-distance", server.getViewDistance());
        object.put("world-size", server.getMaxWorldSize());

        sendResponse(exchange, 200, object.toString().getBytes(StandardCharsets.UTF_8));
    }
}
