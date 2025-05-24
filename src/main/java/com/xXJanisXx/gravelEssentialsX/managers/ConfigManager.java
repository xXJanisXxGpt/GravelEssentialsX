package com.xXJanisXx.gravelEssentialsX.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final JavaPlugin plugin;
    private File configFile;
    private File messagesFile;
    private File bansFile;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration bans;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        createMessagesFile();
        createBansFile();
    }

    public void loadConfigurations() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        bans = YamlConfiguration.loadConfiguration(bansFile);

        plugin.getLogger().info("Alle Configs wurden neu geladen.");
    }

    private void createMessagesFile() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }
    }

    private void createBansFile() {
        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!bansFile.exists()) {
            bansFile.getParentFile().mkdirs();
            try {
                bansFile.createNewFile();
                plugin.getLogger().info("bans.yml wurde erstellt.");
            } catch (IOException e) {
                plugin.getLogger().severe("Fehler beim Erstellen der bans.yml: " + e.getMessage());
            }
        }
    }

    public void saveConfigurations() {
        try {
            config.save(configFile);
            messages.save(messagesFile);
            bans.save(bansFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Configs: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getMessages() {
        return this.messages;
    }

    public FileConfiguration getBans() {
        return this.bans;
    }

    public String getMessage(String path, String defaultValue) {
        return messages.getString(path, defaultValue);
    }

    public void reloadBans() {
        if (bansFile != null) {
            bans = YamlConfiguration.loadConfiguration(bansFile);
            plugin.getLogger().info("bans.yml wurde neu geladen.");
        }
    }
}