package me.statuxia.srapic;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    @EventHandler
    public static void onJoin(PlayerJoinEvent event) {
    }

    @EventHandler
    public static void onLeft(PlayerQuitEvent event) {
    }
}
