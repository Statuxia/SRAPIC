package me.statuxia.srapic;

import me.statuxia.srapic.web.handlers.MetricsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {

    @EventHandler
    public static void onJoin(PlayerJoinEvent event) {
        MetricsHandler.collectMetrics();
    }
}
