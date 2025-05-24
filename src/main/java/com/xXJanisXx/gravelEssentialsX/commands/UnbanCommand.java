package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import com.xXJanisXx.gravelEssentialsX.managers.BanManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;
    private final BanManager banManager;

    public UnbanCommand(GravelEssentialsX plugin, BanManager banManager) {
        this.plugin = plugin;
        this.banManager = banManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gravelessentials.unban")) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Du hast keine Berechtigung für diesen Befehl.", NamedTextColor.RED)));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Verwendung: /unban <spieler>", NamedTextColor.RED)));
            return true;
        }

        String playerName = args[0];

        if (!banManager.isPlayerBanned(playerName)) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Spieler ist nicht gebannt.", NamedTextColor.RED)));
            return true;
        }

        if (banManager.unbanPlayer(playerName)) {
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Spieler ", NamedTextColor.GREEN)
                            .append(Component.text(playerName, NamedTextColor.YELLOW))
                            .append(Component.text(" wurde erfolgreich entbannt.", NamedTextColor.GREEN))
            ));
        } else {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Fehler beim Entbannen des Spielers.", NamedTextColor.RED)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("gravelessentials.unban")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            // Return all player names that might be banned (we can't easily get banned players from BanList without iterating)
            return Bukkit.getOfflinePlayers().length > 50
                    ? new ArrayList<>() // Don't show too many completions
                    : java.util.Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(name -> name != null && name.toLowerCase().startsWith(input))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}