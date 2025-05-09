package com.xXJanisXx.gravelEssentialsX;

import com.xXJanisXx.gravelEssentialsX.commands.ReloadCommand;
import com.xXJanisXx.gravelEssentialsX.managers.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GravelEssentialsX extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getLogger().info("GravelEssentials Start");

        // Config Manager
        configManager = new ConfigManager(this);
        configManager.loadConfigurations();

        // Commands register
        registerCommands();
    }

    @Override
    public void onDisable() {
        getLogger().info("GravelEssentials Stop");
    }

    private void registerCommands() {
        ReloadCommand reloadCommand = new ReloadCommand(this);
        Objects.requireNonNull(getCommand("ge")).setExecutor(reloadCommand);
        Objects.requireNonNull(getCommand("ge")).setTabCompleter(reloadCommand);
    }

    public ConfigManager getConfigManager() {
        return configManager;
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