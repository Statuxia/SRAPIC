package me.statuxia.srapic;

import lombok.Getter;
import me.statuxia.srapic.config.ConfigManager;
import me.statuxia.srapic.database.ConnectionPool;
import me.statuxia.srapic.database.Generator;
import me.statuxia.srapic.web.Server;
import me.statuxia.srapic.web.endpoint.Endpoints;
import me.statuxia.srapic.web.handlers.MetricsHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.SQLException;

public final class SRAPIC extends JavaPlugin {

    @Getter
    private static SRAPIC INSTANCE;
    private ConfigManager mySQL;
    private ConfigManager srapicConfiguration;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Path srapicPath = Path.of(Path.of(System.getProperty("user.dir"), "plugins").toString(), "srapic");

        getMySQL(srapicPath);
        prepareMySQL();

        getSrapicConfiguration(srapicPath);
        prepareServer();

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        Server.stop();
    }

    private void getMySQL(Path srapicPath) {
        try {
            mySQL = ConfigManager.of(Path.of(srapicPath.toString(), "mysql.json").toString());

            if (!mySQL.isCreated()) {
                return;
            }

            JSONObject object = new JSONObject();
            JSONObject tables = new JSONObject();

            object.put("host", "localhost");
            object.put("user", "root");
            object.put("password", "");
            object.put("database", "srapic");

            tables.put("metrics", "metrics");
            object.put("tables", tables);

            mySQL.updateFile(object, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareMySQL() {
        try {
            Generator.create(mySQL.getJsonObject());
            ConnectionPool.initialize(mySQL.getJsonObject(), 10);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        MetricsHandler.collectMetrics(mySQL.getJsonObject().getJSONObject("tables").getString("metrics"));
    }

    private void getSrapicConfiguration(Path srapicPath) {
        try {
            srapicConfiguration = ConfigManager.of(Path.of(srapicPath.toString(), "srapic.json").toString());
            if (!srapicConfiguration.isCreated()) {
                return;
            }
            JSONObject object = new JSONObject();

            for (Field field : Endpoints.class.getDeclaredFields()) {
                if (field.get(null) instanceof String endpoint && !endpoint.equals(Endpoints.API)) {
                    object.put(endpoint.substring(endpoint.lastIndexOf("/") + 1), true);
                }
            }

            srapicConfiguration.updateFile(object, true);
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareServer() {
        try {
            Server.start(srapicConfiguration.getJsonObject());
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
