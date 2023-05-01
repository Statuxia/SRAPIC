package me.statuxia.srapic;

import lombok.Getter;
import me.statuxia.srapic.config.ConfigManager;
import me.statuxia.srapic.web.Server;
import me.statuxia.srapic.web.endpoint.Endpoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public final class SRAPIC extends JavaPlugin {

    @Getter
    private static SRAPIC SRAPIC;
    ConfigManager mySQL;
    ConfigManager srapicConfiguration;

    @Override
    public void onEnable() {
        SRAPIC = this;

        Path srapicPath = Path.of(Path.of(System.getProperty("user.dir"), "plugins").toString(), "srapic");
        getMySQL(srapicPath);
        getSrapicConfiguration(srapicPath);

        try {
            Server.start(srapicConfiguration.getJsonObject());
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void getMySQL(Path srapicPath) {
        try {
            mySQL = ConfigManager.of(Path.of(srapicPath.toString(), "mysql.json").toString());

            if (mySQL.isCreated()) {
                JSONObject object = new JSONObject();
                object.put("host", "localhost");
                object.put("database", "srapic");
                object.put("user", "root");
                object.put("password", "");

                mySQL.updateFile(object, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getSrapicConfiguration(Path srapicPath) {
        try {
            srapicConfiguration = ConfigManager.of(Path.of(srapicPath.toString(), "srapic.json").toString());
            if (srapicConfiguration.isCreated()) {
                JSONObject object = new JSONObject();

                for (Field field : Endpoints.class.getDeclaredFields()) {
                    if (field.get(null) instanceof String endpoint && !endpoint.equals(Endpoints.API)) {
                        object.put(endpoint.substring(endpoint.lastIndexOf("/") + 1), true);
                    }
                }

                srapicConfiguration.updateFile(object, true);
            }
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
