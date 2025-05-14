package com.xXJanisXx.gravelEssentialsX.managers;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import com.xXJanisXx.gravelEssentialsX.models.TPARequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {

    private final GravelEssentialsX plugin;
    private final Map<UUID, TPARequest> receivedRequests = new HashMap<>();
    private final Map<UUID, UUID> sentRequests = new HashMap<>();
    private final Map<UUID, BukkitTask> timeoutTasks = new HashMap<>();

    private final int requestTimeoutSeconds;
    private final int teleportCooldownSeconds;
    private final int teleportWarmupSeconds;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TPAManager(GravelEssentialsX plugin) {
        this.plugin = plugin;
        this.requestTimeoutSeconds = plugin.getConfigManager().getConfig().getInt("tpa.request-timeout-seconds", 60);
        this.teleportCooldownSeconds = plugin.getConfigManager().getConfig().getInt("tpa.cooldown-seconds", 60);
        this.teleportWarmupSeconds = plugin.getConfigManager().getConfig().getInt("tpa.warmup-seconds", 3);
    }

    public void sendTPARequest(Player sender, Player target) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (isOnCooldown(sender)) {
            long remainingCooldown = (cooldowns.get(senderUUID) + (teleportCooldownSeconds * 1000L) - System.currentTimeMillis()) / 1000;
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Du musst noch " + remainingCooldown + " Sekunden warten, bevor du eine neue Anfrage senden kannst!")
                            .color(NamedTextColor.RED)
            ));
            return;
        }
        if (sentRequests.containsKey(senderUUID)) {
            Player oldTarget = plugin.getServer().getPlayer(sentRequests.get(senderUUID));
            if (oldTarget != null) {
                sender.sendMessage(plugin.getPrefix().append(
                        Component.text("Du hast bereits eine Anfrage an " + oldTarget.getName() + " gesendet!")
                                .color(NamedTextColor.RED)
                ));
                return;
            } else {
                cancelTPARequest(sender);
            }
        }

        if (receivedRequests.containsKey(targetUUID)) {
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text(target.getName() + " hat bereits eine andere Teleport-Anfrage!")
                            .color(NamedTextColor.RED)
            ));
            return;
        }

        TPARequest request = new TPARequest(senderUUID, targetUUID);
        receivedRequests.put(targetUUID, request);
        sentRequests.put(senderUUID, targetUUID);

        BukkitTask timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                expireTPARequest(senderUUID, targetUUID);
            }
        }.runTaskLater(plugin, requestTimeoutSeconds * 20L);

        timeoutTasks.put(senderUUID, timeoutTask);

        sender.sendMessage(plugin.getPrefix().append(
                Component.text("Teleport-Anfrage an " + target.getName() + " gesendet!")
                        .color(NamedTextColor.GREEN)
        ));

        Component acceptButton = Component.text("[Akzeptieren]")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/tpa accept"));

        Component denyButton = Component.text("[Ablehnen]")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/tpa deny"));

        target.sendMessage(plugin.getPrefix().append(
                Component.text(sender.getName() + " möchte sich zu dir teleportieren! ")
                        .color(NamedTextColor.YELLOW)
                        .append(acceptButton)
                        .append(Component.text(" "))
                        .append(denyButton)
        ));

        target.sendMessage(Component.text("Die Anfrage läuft in " + requestTimeoutSeconds + " Sekunden ab.")
                .color(NamedTextColor.GRAY));
    }

    public void acceptTPARequest(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (!receivedRequests.containsKey(targetUUID)) {
            target.sendMessage(plugin.getPrefix().append(
                    Component.text("Du hast keine aktiven Teleport-Anfragen!")
                            .color(NamedTextColor.RED)
            ));
            return;
        }

        TPARequest request = receivedRequests.get(targetUUID);
        Player sender = plugin.getServer().getPlayer(request.getSenderUUID());

        if (sender == null || !sender.isOnline()) {
            target.sendMessage(plugin.getPrefix().append(
                    Component.text("Der Spieler ist nicht mehr online!")
                            .color(NamedTextColor.RED)
            ));
            cleanupRequest(request.getSenderUUID(), targetUUID);
            return;
        }

        if (timeoutTasks.containsKey(request.getSenderUUID())) {
            timeoutTasks.get(request.getSenderUUID()).cancel();
            timeoutTasks.remove(request.getSenderUUID());
        }

        target.sendMessage(plugin.getPrefix().append(
                Component.text("Du hast die Teleport-Anfrage von " + sender.getName() + " akzeptiert!")
                        .color(NamedTextColor.GREEN)
        ));

        sender.sendMessage(plugin.getPrefix().append(
                Component.text(target.getName() + " hat deine Teleport-Anfrage akzeptiert!")
                        .color(NamedTextColor.GREEN)
        ));

        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        if (teleportWarmupSeconds > 0) {
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Teleportieren in " + teleportWarmupSeconds + " Sekunden. Bitte bewege dich nicht!")
                            .color(NamedTextColor.YELLOW)
            ));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (sender.isOnline() && target.isOnline()) {
                        sender.teleport(target.getLocation());
                        sender.sendMessage(plugin.getPrefix().append(
                                Component.text("Du wurdest zu " + target.getName() + " teleportiert!")
                                        .color(NamedTextColor.GREEN)
                        ));
                    }
                }
            }.runTaskLater(plugin, teleportWarmupSeconds * 20L);
        } else {
            // Teleport immediately
            sender.teleport(target.getLocation());
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Du wurdest zu " + target.getName() + " teleportiert!")
                            .color(NamedTextColor.GREEN)
            ));
        }
        cleanupRequest(request.getSenderUUID(), targetUUID);
    }

    public void denyTPARequest(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (!receivedRequests.containsKey(targetUUID)) {
            target.sendMessage(plugin.getPrefix().append(
                    Component.text("Du hast keine aktiven Teleport-Anfragen!")
                            .color(NamedTextColor.RED)
            ));
            return;
        }

        TPARequest request = receivedRequests.get(targetUUID);
        Player sender = plugin.getServer().getPlayer(request.getSenderUUID());
        if (timeoutTasks.containsKey(request.getSenderUUID())) {
            timeoutTasks.get(request.getSenderUUID()).cancel();
            timeoutTasks.remove(request.getSenderUUID());
        }

        target.sendMessage(plugin.getPrefix().append(
                Component.text("Du hast die Teleport-Anfrage abgelehnt!")
                        .color(NamedTextColor.YELLOW)
        ));

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text(target.getName() + " hat deine Teleport-Anfrage abgelehnt!")
                            .color(NamedTextColor.RED)
            ));
        }

        cleanupRequest(request.getSenderUUID(), targetUUID);
    }

    public void cancelTPARequest(Player sender) {
        UUID senderUUID = sender.getUniqueId();

        if (!sentRequests.containsKey(senderUUID)) {
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Du hast keine aktiven Teleport-Anfragen gesendet!")
                            .color(NamedTextColor.RED)
            ));
            return;
        }

        UUID targetUUID = sentRequests.get(senderUUID);
        Player target = plugin.getServer().getPlayer(targetUUID);
        if (timeoutTasks.containsKey(senderUUID)) {
            timeoutTasks.get(senderUUID).cancel();
            timeoutTasks.remove(senderUUID);
        }

        sender.sendMessage(plugin.getPrefix().append(
                Component.text("Du hast deine Teleport-Anfrage zurückgezogen!")
                        .color(NamedTextColor.YELLOW)
        ));

        if (target != null && target.isOnline()) {
            target.sendMessage(plugin.getPrefix().append(
                    Component.text(sender.getName() + " hat die Teleport-Anfrage zurückgezogen!")
                            .color(NamedTextColor.YELLOW)
            ));
        }

        cleanupRequest(senderUUID, targetUUID);
    }

    private void expireTPARequest(UUID senderUUID, UUID targetUUID) {
        Player sender = plugin.getServer().getPlayer(senderUUID);
        Player target = plugin.getServer().getPlayer(targetUUID);

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getPrefix().append(
                    Component.text("Deine Teleport-Anfrage ist abgelaufen!")
                            .color(NamedTextColor.RED)
            ));
        }

        if (target != null && target.isOnline()) {
            target.sendMessage(plugin.getPrefix().append(
                    Component.text("Die Teleport-Anfrage ist abgelaufen!")
                            .color(NamedTextColor.RED)
            ));
        }

        cleanupRequest(senderUUID, targetUUID);
    }

    private void cleanupRequest(UUID senderUUID, UUID targetUUID) {
        receivedRequests.remove(targetUUID);
        sentRequests.remove(senderUUID);
        timeoutTasks.remove(senderUUID);
    }

    private boolean isOnCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }

        long lastTeleportTime = cooldowns.get(playerUUID);
        return System.currentTimeMillis() - lastTeleportTime < teleportCooldownSeconds * 1000L;
    }
}
