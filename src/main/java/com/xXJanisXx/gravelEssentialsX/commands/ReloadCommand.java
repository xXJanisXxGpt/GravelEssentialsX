package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;

    public ReloadCommand(GravelEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Überprüfe Berechtigung
        if (!sender.hasPermission("gravelessentials.reload")) {
            sender.sendMessage(
                    plugin.getPrefix().append(
                            Component.text("Du hast keine Berechtigung für diesen Befehl!", NamedTextColor.RED)
                    )
            );
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().loadConfigurations();

            sender.sendMessage(
                    plugin.getPrefix().append(
                            Component.text("Die Konfigurationsdateien wurden neu geladen!", NamedTextColor.GREEN)
                                    .decoration(TextDecoration.BOLD, true)
                    )
            );
            return true;
        }

        sender.sendMessage(
                plugin.getPrefix().append(
                        Component.text("Verwendung: /ge reload", NamedTextColor.YELLOW)
                )
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("gravelessentials.reload")) {
                return List.of("reload");
            }
        }
        return Collections.emptyList();
    }
}