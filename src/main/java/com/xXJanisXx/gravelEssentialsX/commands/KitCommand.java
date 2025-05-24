package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;

    public KitCommand(GravelEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Dieser Befehl kann nur von Spielern genutzt werden!", NamedTextColor.RED)));
            return true;
        }

        if (args.length < 1) {
            List<String> kitNames = plugin.getKitManager().getKitNames();

            if (kitNames.isEmpty()) {
                player.sendMessage(plugin.getPrefix().append(Component.text("Es sind keine Kits verfügbar.", NamedTextColor.RED)));
                return true;
            }

            Component message = plugin.getPrefix().append(Component.text("Verfügbare Kits: ", NamedTextColor.YELLOW));
            for (int i = 0; i < kitNames.size(); i++) {
                message = message.append(Component.text(kitNames.get(i), NamedTextColor.GREEN));
                if (i < kitNames.size() - 1) {
                    message = message.append(Component.text(", ", NamedTextColor.GRAY));
                }
            }

            player.sendMessage(message);
            return true;
        }

        String kitName = args[0].toLowerCase();

        if (!plugin.getKitManager().getKitNames().contains(kitName)) {
            player.sendMessage(plugin.getPrefix().append(Component.text("Das Kit " + kitName + " existiert nicht!", NamedTextColor.RED)));
            return true;
        }

        if (plugin.getKitManager().giveKit(player, kitName)) {
            player.sendMessage(plugin.getPrefix().append(Component.text("Du hast das Kit " + kitName + " erhalten!", NamedTextColor.GREEN)));
        } else {
            player.sendMessage(plugin.getPrefix().append(Component.text("Beim Geben des Kits ist ein Fehler aufgetreten!", NamedTextColor.RED)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialKit = args[0].toLowerCase();
            for (String kitName : plugin.getKitManager().getKitNames()) {
                if (kitName.toLowerCase().startsWith(partialKit)) {
                    completions.add(kitName);
                }
            }
        }

        return completions;
    }
}