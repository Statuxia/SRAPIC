package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.statuxia.srapic.SRAPIC;
import org.bukkit.GameRule;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class WorldHandler extends DefaultHandler {

    public WorldHandler(String endpoint) {
        super(endpoint);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String endpoint = exchange.getRequestURI().getPath();
        Server server = SRAPIC.getSRAPIC().getServer();
        JSONObject object = null;

        List<String> worlds = server.getWorlds().stream().map(World::getName).toList();
        for (String stringWorld : worlds) {
            if ((this.endpoint + "/"+ stringWorld).equals(endpoint)) {
                World world = server.getWorld(stringWorld);
                if (world == null) {
                    continue;
                }
                object = getWorldInfo(world);
                break;
            }
        }

        if (object == null) {
            object = new JSONObject();
            object.put("worlds", server.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        }

        sendResponse(exchange, 200, object.toString().getBytes(StandardCharsets.UTF_8));
    }

    private JSONObject getWorldInfo(World world) {
        JSONObject worldObject = new JSONObject();
        worldObject.put("name", world.getName());
        worldObject.put("difficulty", world.getDifficulty().toString().toLowerCase());
        worldObject.put("full-time", world.getFullTime());
        worldObject.put("game-time", world.getGameTime());
        worldObject.put("online", world.getPlayerCount());
        worldObject.put("players", world.getPlayers().stream().map(Player::getName).collect(Collectors.toList()));
        worldObject.put("pvp", world.getPVP());
        worldObject.put("weather", world.isClearWeather() ? "clear" : world.isThundering() ? "thunder" : "rain");
        worldObject.put("time", world.getTime());

        JSONObject gamerules = new JSONObject();
        for (GameRule rule : GameRule.values()) {
            gamerules.put(rule.getName(), world.getGameRuleValue(rule));
        }
        worldObject.put("gamerules", gamerules);

        return worldObject;
    }
}
