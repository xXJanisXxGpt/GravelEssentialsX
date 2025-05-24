package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfoCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;

    public InfoCommand(GravelEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String Info = plugin.getConfig().getString("info.message", "Test");
        Component message = plugin.getPrefix().append(
                Component.text("Unsere Infos:", NamedTextColor.BLUE)
                        .append(Component.text(Info, NamedTextColor.AQUA))
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}