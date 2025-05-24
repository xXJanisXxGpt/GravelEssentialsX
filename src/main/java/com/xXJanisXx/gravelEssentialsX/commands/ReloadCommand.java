package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;
    private final List<String> subCommands = Arrays.asList("reload", "version", "help");

    public ReloadCommand(GravelEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gravelessentials.admin")) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Du hast keine Berechtigung für diesen Befehl.", NamedTextColor.RED)));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reload();
                sender.sendMessage(plugin.getPrefix().append(Component.text("Konfiguration erfolgreich neu geladen!", NamedTextColor.GREEN)));
                break;

            case "version":
                sender.sendMessage(plugin.getPrefix().append(
                        Component.text("Version: ", NamedTextColor.YELLOW)
                                .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.GREEN))
                ));
                break;

            case "help":
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getPrefix().append(Component.text("Verfügbare Befehle:", NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("  /ge reload", NamedTextColor.AQUA).append(Component.text(" - Lädt die Config neu", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ge version", NamedTextColor.AQUA).append(Component.text(" - Zeigt die Plugin-Version an", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ban <spieler> <template>", NamedTextColor.AQUA).append(Component.text(" - Bannt Spieler mit Template", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ban <spieler> <msg> [zeit]", NamedTextColor.AQUA).append(Component.text(" - Custom Ban mit Zeitangabe", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /unban <spieler>", NamedTextColor.AQUA).append(Component.text(" - Entbannt einen Spieler", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("Ban Zeitformat:", NamedTextColor.YELLOW).append(Component.text(" y# m# d# h# min# s#", NamedTextColor.WHITE)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("gravelessentials.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}