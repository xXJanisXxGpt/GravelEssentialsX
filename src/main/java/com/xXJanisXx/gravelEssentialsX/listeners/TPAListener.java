package com.xXJanisXx.gravelEssentialsX.listeners;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import com.xXJanisXx.gravelEssentialsX.managers.TPAManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TPAListener implements Listener {

    private final GravelEssentialsX plugin;
    private final TPAManager tpaManager;

    public TPAListener(GravelEssentialsX plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        tpaManager.cancelTPARequest(player);
    }

    public GravelEssentialsX getPlugin() {
        return plugin;
    }
}