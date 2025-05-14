package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import com.xXJanisXx.gravelEssentialsX.managers.TPAManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TPACommand implements CommandExecutor, TabCompleter {

    private final GravelEssentialsX plugin;
    private final TPAManager tpaManager;

    public TPACommand(GravelEssentialsX plugin, TPAManager tpaManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "send", "request" -> handleTPARequest(player, args);
            case "accept" -> handleTPAAccept(player);
            case "deny" -> handleTPADeny(player);
            case "cancel" -> handleTPACancel(player);
            default -> sendHelpMessage(player);
        }

        return true;
    }

    private void handleTPARequest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix().append(Component.text("Bitte gib einen Spielernamen an: /tpa send <Spieler>").color(NamedTextColor.RED)));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getPrefix().append(Component.text("Spieler nicht gefunden!").color(NamedTextColor.RED)));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getPrefix().append(Component.text("Du kannst keine TPA an dich selbst senden!").color(NamedTextColor.RED)));
            return;
        }

        tpaManager.sendTPARequest(player, target);
    }

    private void handleTPAAccept(Player player) {
        tpaManager.acceptTPARequest(player);
    }

    private void handleTPADeny(Player player) {
        tpaManager.denyTPARequest(player);
    }

    private void handleTPACancel(Player player) {
        tpaManager.cancelTPARequest(player);
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(plugin.getPrefix().append(Component.text("TPA Befehle:").color(NamedTextColor.YELLOW)));
        player.sendMessage(Component.text("/tpa send <Spieler> - Sende eine Teleportanfrage").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("/tpa accept - Akzeptiere eine Teleportanfrage").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("/tpa deny - Lehne eine Teleportanfrage ab").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("/tpa cancel - Brich eine gesendete Anfrage ab").color(NamedTextColor.GRAY));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("send");
            completions.add("accept");
            completions.add("deny");
            completions.add("cancel");
            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equals(sender.getName()))
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}