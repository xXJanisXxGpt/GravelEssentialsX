package com.xXJanisXx.gravelEssentialsX.managers;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class BanManager {

    private final GravelEssentialsX plugin;
    private File bansFile;
    private FileConfiguration bansConfig;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BanManager(GravelEssentialsX plugin) {
        this.plugin = plugin;
        setupBansFile();
        loadBans();
    }

    private void setupBansFile() {
        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!bansFile.exists()) {
            bansFile.getParentFile().mkdirs();
            try {
                bansFile.createNewFile();
                createDefaultBans();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create bans.yml file: " + e.getMessage());
            }
        }
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    private void createDefaultBans() {
        bansConfig.set("exampleban.duration.years", 0);
        bansConfig.set("exampleban.duration.months", 0);
        bansConfig.set("exampleban.duration.days", 3);
        bansConfig.set("exampleban.duration.hours", 15);
        bansConfig.set("exampleban.duration.minutes", 23);
        bansConfig.set("exampleban.duration.seconds", 4);

        List<String> messages = Arrays.asList(
                "&6MineAttack",
                "&5Du wurdest gebannt",
                "",
                "Website: https://mineattack.de/ - Discord: discord.gg/mineattack"
        );
        bansConfig.set("exampleban.message", messages);

        bansConfig.set("griefing.duration.years", 0);
        bansConfig.set("griefing.duration.months", 0);
        bansConfig.set("griefing.duration.days", 7);
        bansConfig.set("griefing.duration.hours", 0);
        bansConfig.set("griefing.duration.minutes", 0);
        bansConfig.set("griefing.duration.seconds", 0);

        List<String> griefingMessages = Arrays.asList(
                "&cDu wurdest wegen Griefing gebannt!",
                "",
                "&7Grund: Zerstörung fremder Bauwerke",
                "&7Dauer: 7 Tage"
        );
        bansConfig.set("griefing.message", griefingMessages);

        saveBansConfig();
    }

    public void loadBans() {
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        plugin.getLogger().info("Ban-Konfiguration wurde geladen.");
    }

    private void saveBansConfig() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bans.yml: " + e.getMessage());
        }
    }

    public boolean banExists(String banName) {
        return bansConfig.contains(banName.toLowerCase());
    }

    public Set<String> getBanNames() {
        return bansConfig.getKeys(false);
    }

    public void banPlayer(String playerName, String banName, String bannedBy) {
        if (!banExists(banName)) {
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        ConfigurationSection banSection = bansConfig.getConfigurationSection(banName.toLowerCase());

        if (banSection == null) {
            return;
        }

        Date expirationDate = calculateExpirationDate(banSection);

        List<String> messageLines = banSection.getStringList("message");
        String banMessage = String.join("\n", messageLines);
        banMessage = banMessage.replace("&", "§");

        banMessage += "\n\n§7Gebannt von: §e" + bannedBy;
        if (expirationDate != null) {
            banMessage += "\n§7Läuft ab: §e" + expirationDate.toString();
        } else {
            banMessage += "\n§7Dauer: §cPermanent";
        }

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        banList.addBan(player.getName(), banMessage, expirationDate, bannedBy);

        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                Component kickMessage = miniMessage.deserialize(banMessage.replace("§", "&"));
                onlinePlayer.kick(kickMessage);
            }
        }

        plugin.getLogger().info(playerName + " wurde mit Ban-Template '" + banName + "' von " + bannedBy + " gebannt.");
    }

    public void banPlayerWithCustomDuration(String playerName, String message,
                                            int years, int months, int days,
                                            int hours, int minutes, int seconds,
                                            String bannedBy) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now
                .plusYears(years)
                .plusMonths(months)
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

        Date expirationDate = null;
        if (years > 0 || months > 0 || days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
            expirationDate = Date.from(expiration.toInstant(ZoneOffset.UTC));
        }

        String banMessage = message.replace("&", "§");
        banMessage += "\n\n§7Gebannt von: §e" + bannedBy;
        if (expirationDate != null) {
            banMessage += "\n§7Läuft ab: §e" + expirationDate.toString();
        } else {
            banMessage += "\n§7Dauer: §cPermanent";
        }

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        banList.addBan(player.getName(), banMessage, expirationDate, bannedBy);
        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                Component kickMessage = miniMessage.deserialize(banMessage.replace("§", "&"));
                onlinePlayer.kick(kickMessage);
            }
        }

        plugin.getLogger().info(playerName + " wurde mit custom Ban von " + bannedBy + " gebannt.");
    }

    public boolean unbanPlayer(String playerName) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (banList.isBanned(playerName)) {
            banList.pardon(playerName);
            plugin.getLogger().info(playerName + " wurde entbannt.");
            return true;
        }
        return false;
    }

    public boolean isPlayerBanned(String playerName) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        return banList.isBanned(playerName);
    }

    private Date calculateExpirationDate(ConfigurationSection banSection) {
        ConfigurationSection duration = banSection.getConfigurationSection("duration");
        if (duration == null) {
            return null;
        }

        int years = duration.getInt("years", 0);
        int months = duration.getInt("months", 0);
        int days = duration.getInt("days", 0);
        int hours = duration.getInt("hours", 0);
        int minutes = duration.getInt("minutes", 0);
        int seconds = duration.getInt("seconds", 0);

        // If all are 0, it's a permanent ban
        if (years == 0 && months == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now
                .plusYears(years)
                .plusMonths(months)
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

        return Date.from(expiration.toInstant(ZoneOffset.UTC));
    }
}