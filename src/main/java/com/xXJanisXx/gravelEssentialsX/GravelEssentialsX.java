package com.xXJanisXx.gravelEssentialsX;

import com.xXJanisXx.gravelEssentialsX.commands.*;
import com.xXJanisXx.gravelEssentialsX.listeners.JoinListener;
import com.xXJanisXx.gravelEssentialsX.listeners.LeaveListener;
import com.xXJanisXx.gravelEssentialsX.listeners.TPAListener;
import com.xXJanisXx.gravelEssentialsX.managers.BanManager;
import com.xXJanisXx.gravelEssentialsX.managers.ConfigManager;
import com.xXJanisXx.gravelEssentialsX.managers.KitManager;
import com.xXJanisXx.gravelEssentialsX.managers.TPAManager;
import com.xXJanisXx.gravelEssentialsX.mysql.MySQLConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class GravelEssentialsX extends JavaPlugin {

    private ConfigManager configManager;
    private TPAManager tpaManager;
    private KitManager kitManager;
    private BanManager banManager;
    private MySQLConnection connection;

    @Override
    public void onEnable() {
        sendStartupMessage();

        getLogger().info("Gravel Api Start from com.xXJanisXx.gravelEssentialsX.api.gravellib!");

        configManager = new ConfigManager(this);
        configManager.loadConfigurations();
        setupDefaultConfig();
        setupMySQL();

        tpaManager = new TPAManager(this);
        kitManager = new KitManager(this);
        banManager = new BanManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("Gravel Api Started!");
    }

    @Override
    public void onDisable() {
        sendShutdownMessage();

        if (connection != null) {
            connection.disconnect();
        }

        getLogger().info("Gravel Api Stopped");
    }

    private void setupDefaultConfig() {
        getConfig().addDefault("tpa.request-timeout-seconds", 60);
        getConfig().addDefault("tpa.cooldown-seconds", 60);
        getConfig().addDefault("tpa.warmup-seconds", 3);
        getConfig().addDefault("discord.link", "https://discord.gg/yourserver");
        getConfig().addDefault("discord.name", "GravelMC Discord");
        getConfig().addDefault("kits.starter.items.0.material", "BREAD");
        getConfig().addDefault("kits.starter.items.0.amount", 5);
        getConfig().addDefault("kits.starter.items.0.name", "<yellow>Magisches Brot</yellow>");
        getConfig().addDefault("kits.starter.items.0.lore", List.of("<gray>Ein besonders leckeres Brot</gray>"));
        getConfig().addDefault("kits.starter.items.1.material", "STONE_SWORD");
        getConfig().addDefault("kits.starter.items.1.amount", 1);
        getConfig().addDefault("kits.starter.items.1.name", "<red>Starter Schwert</red>");
        getConfig().addDefault("kits.starter.items.1.lore", List.of("<gray>Nicht besonders scharf</gray>"));
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
                getLogger().severe("Error with MySQL Connection: " + e.getMessage());
                if (e.getCause() != null) {
                    getLogger().severe("Caused by: " + e.getCause().getMessage());
                }
            }
        } else {
            getLogger().info("MySQL ist deaktiviert.");
        }
    }

    private void registerCommands() {
        InfoCommand infoCommand = new InfoCommand(this);
        Objects.requireNonNull(getCommand("info")).setExecutor(infoCommand);
        Objects.requireNonNull(getCommand("info")).setTabCompleter(infoCommand);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        Objects.requireNonNull(getCommand("ge")).setExecutor(reloadCommand);
        Objects.requireNonNull(getCommand("ge")).setTabCompleter(reloadCommand);

        // TPA Command
        TPACommand tpaCommand = new TPACommand(this, tpaManager);
        Objects.requireNonNull(getCommand("tpa")).setExecutor(tpaCommand);
        Objects.requireNonNull(getCommand("tpa")).setTabCompleter(tpaCommand);

        // Discord Command
        DiscordCommand discordCommand = new DiscordCommand(this);
        Objects.requireNonNull(getCommand("discord")).setExecutor(discordCommand);
        Objects.requireNonNull(getCommand("discord")).setTabCompleter(discordCommand);

        // Kit Command
        KitCommand kitCommand = new KitCommand(this);
        Objects.requireNonNull(getCommand("kit")).setExecutor(kitCommand);
        Objects.requireNonNull(getCommand("kit")).setTabCompleter(kitCommand);

        // Ban Command
        BanCommand banCommand = new BanCommand(this, banManager);
        Objects.requireNonNull(getCommand("ban")).setExecutor(banCommand);
        Objects.requireNonNull(getCommand("ban")).setTabCompleter(banCommand);

        // Unban Command
        UnbanCommand unbanCommand = new UnbanCommand(this, banManager);
        Objects.requireNonNull(getCommand("unban")).setExecutor(unbanCommand);
        Objects.requireNonNull(getCommand("unban")).setTabCompleter(unbanCommand);
    }

    private void registerListeners() {
        TPAListener tpaListener = new TPAListener(this, tpaManager);
        getServer().getPluginManager().registerEvents(tpaListener, this);

        JoinListener joinListener = new JoinListener(this);
        getServer().getPluginManager().registerEvents(joinListener, this);

        LeaveListener leaveListener = new LeaveListener(this);
        getServer().getPluginManager().registerEvents(leaveListener, this);

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public BanManager getBanManager() {
        return banManager;
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
        reloadConfig();

        // MySQL neu verbinden
        if (connection != null) {
            connection.disconnect();
        }
        setupMySQL();

        // Manager neu laden
        kitManager.loadKits();
        banManager.loadBans();

        getLogger().info("Plugin-Config, MySQL und Ban-System wurden neu geladen!");
    }

    private void sendStartupMessage() {
        // ASCII Art für "GE" mit Adventure API
        Bukkit.getConsoleSender().sendMessage(
                Component.text("   ██████╗ ███████╗", NamedTextColor.AQUA)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ██╔════╝ ██╔════╝", NamedTextColor.AQUA)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ██║  ███╗█████╗  ", NamedTextColor.AQUA)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ██║   ██║██╔══╝  ", NamedTextColor.AQUA)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ╚██████╔╝███████╗", NamedTextColor.AQUA)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("   ╚═════╝ ╚══════╝", NamedTextColor.AQUA)
        );

        Bukkit.getConsoleSender().sendMessage(Component.empty());

        Bukkit.getConsoleSender().sendMessage(
                Component.text(" » GravelEssentials ", NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text("v1.0 [Alpha]", NamedTextColor.GRAY))
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text(" » https://discord.gg/UGMF3DAQ", NamedTextColor.DARK_GRAY)
        );
    }

    private void sendShutdownMessage() {
        Bukkit.getConsoleSender().sendMessage(Component.empty());

        Bukkit.getConsoleSender().sendMessage(
                Component.text("   ██████╗ ███████╗", NamedTextColor.GRAY)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ██╔════╝ ██╔════╝", NamedTextColor.GRAY)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ██║  ███╗█████╗  ", NamedTextColor.DARK_GRAY)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ██║   ██║██╔══╝  ", NamedTextColor.DARK_GRAY)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  ╚██████╔╝███████╗", NamedTextColor.BLACK)
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("   ╚═════╝ ╚══════╝", NamedTextColor.BLACK)
        );

        Bukkit.getConsoleSender().sendMessage(Component.empty());

        Bukkit.getConsoleSender().sendMessage(
                Component.text("  » ", NamedTextColor.YELLOW)
                        .append(Component.text("GravelEssentials  ", NamedTextColor.GRAY)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text("wurde gestoppt", NamedTextColor.DARK_GRAY))
        );
        Bukkit.getConsoleSender().sendMessage(
                Component.text("  » Auf Wiedersehen!", NamedTextColor.DARK_GRAY)
        );
    }
}