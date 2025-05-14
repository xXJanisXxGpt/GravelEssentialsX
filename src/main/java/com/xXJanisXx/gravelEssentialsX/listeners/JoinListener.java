package com.xXJanisXx.gravelEssentialsX.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.joinMessage(null);

        final TextComponent joinMessage = Component.text("[")
                .color(NamedTextColor.GRAY)
                .append(Component.text("+")
                        .color(NamedTextColor.GREEN))
                .append(Component.text("]")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(" ")
                        .color(NamedTextColor.GRAY))
                .append(Component.text(player.getName())
                        .color(NamedTextColor.GRAY));

        player.getServer().sendMessage(joinMessage);

        final TextComponent welcomeMessage = Component.text("Welcome on %Server.Name%")
                .color(TextColor.color(0x55FFFF));

        player.sendMessage(welcomeMessage);
    }
}