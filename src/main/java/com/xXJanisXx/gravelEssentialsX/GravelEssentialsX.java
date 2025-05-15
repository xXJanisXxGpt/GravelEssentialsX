package com.xXJanisXx.gravelEssentialsX;

import com.xXJanisXx.gravelEssentialsX.commands.ReloadCommand;
import com.xXJanisXx.gravelEssentialsX.commands.*;
import com.xXJanisXx.gravelEssentialsX.listeners.JoinListener;
import com.xXJanisXx.gravelEssentialsX.listeners.TPAListener;
import com.xXJanisXx.gravelEssentialsX.managers.ConfigManager;
import com.xXJanisXx.gravelEssentialsX.managers.TPAManager;
import com.xXJanisXx.gravelEssentialsX.mysql.MySQLConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GravelEssentialsX extends JavaPlugin {

    private ConfigManager configManager;
    private TPAManager tpaManager;
    private MySQLConnection connection;

    @Override
    public void onEnable() {
        getLogger().info("GravelEssentials wird gestartet...");

        // Config Manager
        configManager = new ConfigManager(this);
        configManager.loadConfigurations();
        setupDefaultConfig();

        setupMySQL();

        // Managers
        tpaManager = new TPAManager(this);

        // Register commands and listeners
        registerCommands();
        registerListeners();
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        getLogger().info("GravelEssentials erfolgreich gestartet!");
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            connection.disconnect();
        }

        getLogger().info("GravelEssentials wurde gestoppt.");
    }

    private void setupDefaultConfig() {
        getConfig().addDefault("tpa.request-timeout-seconds", 60);
        getConfig().addDefault("tpa.cooldown-seconds", 60);
        getConfig().addDefault("tpa.warmup-seconds", 3);

        getConfig().addDefault("mysql.enabled", true);
        getConfig().addDefault("mysql.host", "localhost");
        getConfig().addDefault("mysql.port", 3306);
        getConfig().addDefault("mysql.database", "minecraft");
        getConfig().addDefault("mysql.username", "minecraft");
        getConfig().addDefault("mysql.password", "minecraft");
        getConfig().addDefault("mysql.ssl", false);
        getConfig().addDefault("mysql.auto-reconnect", true);
        getConfig().addDefault("mysql.timeout-seconds", 30);

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void setupMySQL() {
        if (getConfig().getBoolean("mysql.enabled", true)) {
            try {
                connection = new MySQLConnection(this);
                if (connection.isConnected()) {
                    getLogger().info("MySQL-Verbindung erfolgreich hergestellt!");
                } else {
                    getLogger().warning("MySQL-Verbindung konnte nicht hergestellt werden!");
                }
            } catch (Exception e) {
                getLogger().severe("Fehler bei der MySQL-Initialisierung: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            getLogger().info("MySQL ist in der Konfiguration deaktiviert.");
        }
    }

    private void registerCommands() {
        // Register reload command
        ReloadCommand reloadCommand = new ReloadCommand(this);
        Objects.requireNonNull(getCommand("ge")).setExecutor(reloadCommand);
        Objects.requireNonNull(getCommand("ge")).setTabCompleter(reloadCommand);

        // Register TPA command
        TPACommand tpaCommand = new TPACommand(this, tpaManager);
        Objects.requireNonNull(getCommand("tpa")).setExecutor(tpaCommand);
        Objects.requireNonNull(getCommand("tpa")).setTabCompleter(tpaCommand);
    }

    private void registerListeners() {
        TPAListener tpaListener = new TPAListener(this, tpaManager);
        getServer().getPluginManager().registerEvents(tpaListener, this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }

    public MySQLConnection getConnection() {
        return connection;
    }

    public Component getPrefix() {
        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text("GravelEssentials", NamedTextColor.AQUA))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .append(Component.text(" ", NamedTextColor.WHITE))
                .build();
    }

    public void reload() {
        // Config neu laden
        reloadConfig();

        if (connection != null) {
            connection.disconnect();
        }
        setupMySQL();

        getLogger().info("Plugin-Config and MySQL Reload!");
    }
}