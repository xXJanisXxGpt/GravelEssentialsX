package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;

    public DiscordCommand(GravelEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String discordLink = plugin.getConfig().getString("discord.link", "https://discord.gg/yourserver");
        String discordName = plugin.getConfig().getString("discord.name", "unserem Discord");

        Component message = plugin.getPrefix().append(
                Component.text("Komme doch gerne auf ", NamedTextColor.BLUE)
                        .append(Component.text(discordName, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl(discordLink)))
                        .append(Component.text("!", NamedTextColor.BLUE))
        );

        sender.sendMessage(message);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}