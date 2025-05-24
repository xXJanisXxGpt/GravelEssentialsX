package com.xXJanisXx.gravelEssentialsX.mysql;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MySQLConnection {

    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean ssl;
    private final boolean autoReconnect;
    private final int timeout;
    private final Plugin plugin;

    public MySQLConnection(Plugin plugin) {
        this.plugin = plugin;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("mysql");
        if (config == null) {
            throw new IllegalStateException("MySQL-Config not in config.yml");
        }

        this.host = config.getString("host", "localhost");
        this.port = config.getInt("port", 3306);
        this.database = config.getString("database", "minecraft");
        this.username = config.getString("username", "minecraft");
        this.password = config.getString("password", "minecraft");
        this.ssl = config.getBoolean("ssl", false);
        this.autoReconnect = config.getBoolean("auto-reconnect", true);
        this.timeout = config.getInt("timeout-seconds", 30);

        if (config.getBoolean("enabled", true)) {
            connect();
        } else {
            plugin.getLogger().info("MySQL-Connection Disabled in the config.yml.");
        }
    }

    public void connect() {
        if (!isConnected()) {
            try {
                Properties properties = getProperties();

                String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
                connection = DriverManager.getConnection(url, properties);

                plugin.getLogger().info("MySQL-Connection Connect!");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error with Connection to the MySQL Class:", e);
            }
        }
    }

    private @NotNull Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("useSSL", String.valueOf(ssl));
        properties.setProperty("autoReconnect", String.valueOf(autoReconnect));
        properties.setProperty("connectTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(timeout)));
        properties.setProperty("useUnicode", "true");
        properties.setProperty("characterEncoding", "utf8");
        properties.setProperty("serverTimezone", "UTC");
        return properties;
    }

    public boolean isConnected() {
        if (connection == null) {
            return false;
        }

        try {
            return !connection.isClosed() && connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                connection = null;
                plugin.getLogger().info("MySQL-Connection Closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error on the Disabled MySQL Connection:", e);
            }
        }
    }

    public Connection getConnection() {
        if (!isConnected()) {
            connect();
        }
        return this.connection;
    }
}