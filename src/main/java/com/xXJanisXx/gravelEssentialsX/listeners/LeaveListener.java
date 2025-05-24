package com.xXJanisXx.gravelEssentialsX.listeners;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private final GravelEssentialsX plugin;

    public LeaveListener(GravelEssentialsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.quitMessage(null);

        final TextComponent quitMessage = Component.text("[")
                .color(NamedTextColor.GRAY)
                .append(Component.text("-")
                        .color(NamedTextColor.RED))
                .append(Component.text("]")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(" ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(player.getName())
                        .color(NamedTextColor.GRAY));

        player.getServer().sendMessage(quitMessage);

    }
}