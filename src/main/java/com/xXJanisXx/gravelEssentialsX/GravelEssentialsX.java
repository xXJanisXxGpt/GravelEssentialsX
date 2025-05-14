package com.xXJanisXx.gravelEssentialsX;

import com.xXJanisXx.gravelEssentialsX.commands.KitCommand;
import com.xXJanisXx.gravelEssentialsX.commands.ReloadCommand;
import com.xXJanisXx.gravelEssentialsX.commands.*;
import com.xXJanisXx.gravelEssentialsX.listeners.TPAListener;
import com.xXJanisXx.gravelEssentialsX.managers.ConfigManager;
import com.xXJanisXx.gravelEssentialsX.managers.TPAManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GravelEssentialsX extends JavaPlugin {

    private ConfigManager configManager;
    private TPAManager tpaManager;

    @Override
    public void onEnable() {
        getLogger().info("GravelEssentials Start");

        // Config Manager
        configManager = new ConfigManager(this);
        configManager.loadConfigurations();
        setupDefaultConfig();

        // Managers
        tpaManager = new TPAManager(this);

        // Register commands and listeners
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        getLogger().info("GravelEssentials Stop");
    }

    private void setupDefaultConfig() {
        getConfig().addDefault("tpa.request-timeout-seconds", 60);
        getConfig().addDefault("tpa.cooldown-seconds", 60);
        getConfig().addDefault("tpa.warmup-seconds", 3);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerCommands() {
        // Register reload command
        ReloadCommand reloadCommand = new ReloadCommand(this);
        Objects.requireNonNull(getCommand("ge")).setExecutor(reloadCommand);
        Objects.requireNonNull(getCommand("ge")).setTabCompleter(reloadCommand);

        KitCommand kitcommand = new KitCommand(this);
        getCommand("kit").setExecutor(kitcommand);
        // Register TPA command
        TPACommand tpaCommand = new TPACommand(this, tpaManager);
        Objects.requireNonNull(getCommand("tpa")).setExecutor(tpaCommand);
        Objects.requireNonNull(getCommand("tpa")).setTabCompleter(tpaCommand);
    }

    private void registerListeners() {
        // Register TPA listener
        TPAListener tpaListener = new TPAListener(this, tpaManager);
        getServer().getPluginManager().registerEvents(tpaListener, this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }

    public Component getPrefix() {
        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text("GravelEssentials", NamedTextColor.AQUA))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .append(Component.text(" ", NamedTextColor.WHITE))
                .build();
    }
}