package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand implements CommandExecutor {

    private final GravelEssentialsX plugin = new GravelEssentialsX();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(plugin.getPrefix().append(Component.text("Komme doch gerne auf unsern Discord: %Discord_Link%", NamedTextColor.BLUE)));
        return false;
    }
}