package me.statuxia.srapic.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.statuxia.srapic.SRAPIC;
import me.statuxia.srapic.database.ConnectionPool;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class MetricsHandler extends DefaultHandler {

    private static String sqlInsert;
    private static String sqlSelect;

    public MetricsHandler(String endpoint) {
        super(endpoint);
    }

    public static void collectMetrics(String table) {
        if (sqlInsert == null) {
            sqlInsert = "INSERT IGNORE INTO %1$s (update_time, online) VALUES (?, ?);".formatted(table);
            sqlSelect = "SELECT update_time, online FROM %1$s ORDER BY online DESC LIMIT 1;".formatted(table);
        }
        collectMetrics();
    }

    public static void collectMetrics() {
        if (sqlInsert == null) {
            return;
        }
        try (var agent = ConnectionPool.acquireConnection()) {
            Connection conn = agent.getConnection();

            try (PreparedStatement statement = conn.prepareStatement(sqlInsert)) {
                statement.setLong(1, System.currentTimeMillis());
                statement.setInt(2, SRAPIC.getINSTANCE().getServer().getOnlinePlayers().size());
                statement.executeUpdate();
            }

        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Cache cache = cachedContent.get(endpoint);
        if (cache != null && !cache.isOld()) {
            sendResponse(exchange, cache.code, cache.content, endpoint);
            return;
        }

        JSONObject object = new JSONObject();
        Server server = SRAPIC.getINSTANCE().getServer();

        object.put("update-time", System.currentTimeMillis());
        object.put("tps", server.getTPS());
        object.put("online", server.getOnlinePlayers().size());
        object.put("players", server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        try {
            object.put("peak-online", getMaxOnline());
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        sendResponse(exchange, 200, object.toString().getBytes(StandardCharsets.UTF_8), endpoint);
    }

    public JSONObject getMaxOnline() throws SQLException, InterruptedException {
        try (var agent = ConnectionPool.acquireConnection()) {
            Connection conn = agent.getConnection();
            try (PreparedStatement statement = conn.prepareStatement(sqlSelect)) {
                try (ResultSet set = statement.executeQuery()) {
                    JSONObject maxOnline = new JSONObject();
                    if (set.next()) {
                        maxOnline.put("update-time", set.getLong(1));
                        maxOnline.put("online", set.getInt(2));
                    }
                    return maxOnline;
                }
            }
        }
    }
}
