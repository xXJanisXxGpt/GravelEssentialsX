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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BanCommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;
    private final BanManager banManager;

    public BanCommand(GravelEssentialsX plugin, BanManager banManager) {
        this.plugin = plugin;
        this.banManager = banManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gravelessentials.ban")) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Du hast keine Berechtigung für diesen Befehl.", NamedTextColor.RED)));
            return true;
        }

        if (args.length < 2) {
            showUsage(sender);
            return true;
        }

        String playerName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Spieler wurde nicht gefunden.", NamedTextColor.RED)));
            return true;
        }

        if (banManager.isPlayerBanned(playerName)) {
            sender.sendMessage(plugin.getPrefix().append(Component.text("Spieler ist bereits gebannt.", NamedTextColor.RED)));
            return true;
        }

        // Check if it's a template ban or custom ban
        if (args.length == 2) {
            // Template ban: /ban playername bantemplate
            String banName = args[1];
            if (!banManager.banExists(banName)) {
                sender.sendMessage(plugin.getPrefix().append(Component.text("Ban-Template '" + banName + "' existiert nicht.", NamedTextColor.RED)));
                return true;
            }

            banManager.banPlayer(playerName, banName, sender.getName());
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Spieler ", NamedTextColor.GREEN)
                            .append(Component.text(playerName, NamedTextColor.YELLOW))
                            .append(Component.text(" wurde mit Template '", NamedTextColor.GREEN))
                            .append(Component.text(banName, NamedTextColor.YELLOW))
                            .append(Component.text("' gebannt.", NamedTextColor.GREEN))
            ));
        } else {
            // Custom ban: /ban playername message y1 m2 d3 h4 min5 s6
            String message = args[1];

            int years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;

            // Parse duration arguments
            for (int i = 2; i < args.length; i++) {
                String arg = args[i].toLowerCase();
                try {
                    if (arg.startsWith("y")) {
                        years = Integer.parseInt(arg.substring(1));
                    } else if (arg.startsWith("m") && !arg.startsWith("min")) {
                        months = Integer.parseInt(arg.substring(1));
                    } else if (arg.startsWith("d")) {
                        days = Integer.parseInt(arg.substring(1));
                    } else if (arg.startsWith("h") || arg.startsWith("std")) {
                        hours = Integer.parseInt(arg.substring(arg.startsWith("std") ? 3 : 1));
                    } else if (arg.startsWith("min")) {
                        minutes = Integer.parseInt(arg.substring(3));
                    } else if (arg.startsWith("s") || arg.startsWith("sek")) {
                        seconds = Integer.parseInt(arg.substring(arg.startsWith("sek") ? 3 : 1));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getPrefix().append(Component.text("Ungültiges Zeitformat: " + arg, NamedTextColor.RED)));
                    return true;
                }
            }

            banManager.banPlayerWithCustomDuration(playerName, message, years, months, days, hours, minutes, seconds, sender.getName());

            StringBuilder durationText = new StringBuilder();
            if (years > 0) durationText.append(years).append("y ");
            if (months > 0) durationText.append(months).append("m ");
            if (days > 0) durationText.append(days).append("d ");
            if (hours > 0) durationText.append(hours).append("h ");
            if (minutes > 0) durationText.append(minutes).append("min ");
            if (seconds > 0) durationText.append(seconds).append("s ");

            if (durationText.isEmpty()) {
                durationText.append("permanent");
            }

            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Spieler ", NamedTextColor.GREEN)
                            .append(Component.text(playerName, NamedTextColor.YELLOW))
                            .append(Component.text(" wurde für ", NamedTextColor.GREEN))
                            .append(Component.text(durationText.toString().trim(), NamedTextColor.YELLOW))
                            .append(Component.text(" gebannt.", NamedTextColor.GREEN))
            ));
        }

        return true;
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage(plugin.getPrefix().append(Component.text("Ban Command Verwendung:", NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("  /ban <spieler> <template>", NamedTextColor.AQUA)
                .append(Component.text(" - Bannt mit vordefiniertem Template", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ban <spieler> <nachricht> [y#] [m#] [d#] [h#] [min#] [s#]", NamedTextColor.AQUA)
                .append(Component.text(" - Custom Ban", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("Beispiele:", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /ban Steve griefing", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  /ban Alex \"Spam im Chat\" d3 h12", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  /ban Bob Hacking y1 m6", NamedTextColor.WHITE));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("gravelessentials.ban")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete player names
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Complete ban templates or message
            String input = args[1].toLowerCase();
            List<String> templates = new ArrayList<>(banManager.getBanNames());
            templates.add("\"Custom message\"");
            return templates.stream()
                    .filter(template -> template.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length > 2) {
            // Complete duration options
            List<String> durations = Arrays.asList("y1", "m1", "d1", "h1", "std1", "min1", "s1", "sek1");
            String input = args[args.length - 1].toLowerCase();
            return durations.stream()
                    .filter(duration -> duration.startsWith(input))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}