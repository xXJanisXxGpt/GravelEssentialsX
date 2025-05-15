package com.xXJanisXx.gravelEssentialsX.mysql;

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MySQLTable {

    private final MySQLConnection connection;
    private final String name;
    private final Map<String, MySQLDataType> columns;
    private final Plugin plugin;

    public MySQLTable(Plugin plugin, MySQLConnection connection, String name, Map<String, MySQLDataType> columns) {
        this.plugin = plugin;
        this.connection = connection;
        this.name = name;
        this.columns = columns;
        createTable();
    }

    public void createTable() {
        if (!connection.isConnected()) {
            plugin.getLogger().warning("MySQL nicht verbunden! Tabelle " + name + " konnte nicht erstellt werden.");
            return;
        }

        try (Connection conn = connection.getConnection()) {
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + name + "` (");
            StringBuilder primaryKeys = new StringBuilder();

            for (Map.Entry<String, MySQLDataType> entry : columns.entrySet()) {
                String columnName = entry.getKey();
                MySQLDataType dataType = entry.getValue();

                sql.append("`").append(columnName).append("` ").append(dataType.toSQL());

                if (columnName.endsWith("_pk")) {
                    if (!primaryKeys.isEmpty()) {
                        primaryKeys.append(", ");
                    }
                    primaryKeys.append("`").append(columnName).append("`");
                    sql.append(" NOT NULL");
                }

                sql.append(", ");
            }

            if (!primaryKeys.isEmpty()) {
                sql.append("PRIMARY KEY (").append(primaryKeys).append(")");
            } else {
                sql.delete(sql.length() - 2, sql.length());
            }

            sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(sql.toString());
                plugin.getLogger().info("Tabelle " + name + " erfolgreich erstellt oder überprüft!");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Tabelle " + name + ":", e);
        }
    }

    public void verifyTable() {
        if (!connection.isConnected()) {
            return;
        }

        try (Connection conn = connection.getConnection()) {
            // Bestehende Spalten abfragen
            Map<String, String> existingColumns = new HashMap<>();
            String sql = "SHOW COLUMNS FROM `" + name + "`;";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    existingColumns.put(rs.getString("Field"), rs.getString("Type"));
                }
            }

            for (Map.Entry<String, MySQLDataType> entry : columns.entrySet()) {
                String columnName = entry.getKey();

                if (!existingColumns.containsKey(columnName)) {
                    String alterSql = "ALTER TABLE `" + name + "` ADD COLUMN `" +
                            columnName + "` " + entry.getValue().toSQL() + ";";

                    try (Statement alterStmt = conn.createStatement()) {
                        alterStmt.executeUpdate(alterSql);
                        plugin.getLogger().info("Spalte " + columnName + " zur Tabelle " + name + " hinzugefügt.");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler bei der Tabellenüberprüfung " + name + ":", e);
        }
    }

    public Set<String> getColumnNames() {
        return columns.keySet();
    }

    public MySQLDataType getType(String columnName) {
        return columns.get(columnName);
    }

    public static class Condition {
        private final String columnName;
        private final Object value;
        private final String operator;

        public Condition(String columnName, Object value) {
            this(columnName, value, "=");
        }

        public Condition(String columnName, Object value, String operator) {
            this.columnName = columnName;
            this.value = value;
            this.operator = operator;
        }

        public String getColumnName() {
            return columnName;
        }

        public Object getValue() {
            return value;
        }

        public String getOperator() {
            return operator;
        }
    }

    public void set(String columnName, Object value, Condition condition) {
        if (!connection.isConnected()) {
            plugin.getLogger().warning("MySQL nicht verbunden! Konnte Wert nicht setzen.");
            return;
        }

        try (Connection conn = connection.getConnection()) {
            if (value == null) {
                remove(condition);
                return;
            }

            if (exists(condition)) {
                String sql = "UPDATE `" + this.name + "` SET `" + columnName + "`=? WHERE `" +
                        condition.getColumnName() + "`" + condition.getOperator() + "?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, value);
                    ps.setObject(2, condition.getValue());
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO `" + this.name + "` (`" + columnName + "`, `" +
                        condition.getColumnName() + "`) VALUES (?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, value);
                    ps.setObject(2, condition.getValue());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Setzen von Daten:", e);
        }
    }

    public void setMultiple(Map<String, Object> values, Condition condition) {
        if (!connection.isConnected() || values.isEmpty()) {
            return;
        }

        try (Connection conn = connection.getConnection()) {
            if (exists(condition)) {
                StringBuilder sql = new StringBuilder("UPDATE `" + this.name + "` SET ");
                List<Object> params = new ArrayList<>();

                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    sql.append("`").append(entry.getKey()).append("`=?, ");
                    params.add(entry.getValue());
                }

                sql.delete(sql.length() - 2, sql.length());

                sql.append(" WHERE `").append(condition.getColumnName())
                        .append("`").append(condition.getOperator()).append("?");

                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    int index = 1;
                    for (Object param : params) {
                        ps.setObject(index++, param);
                    }
                    ps.setObject(index, condition.getValue());
                    ps.executeUpdate();
                }
            } else {
                StringBuilder columns = new StringBuilder();
                StringBuilder placeholders = new StringBuilder();
                List<Object> params = new ArrayList<>(values.size() + 1);

                columns.append("`").append(condition.getColumnName()).append("`, ");
                placeholders.append("?, ");
                params.add(condition.getValue());

                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    columns.append("`").append(entry.getKey()).append("`, ");
                    placeholders.append("?, ");
                    params.add(entry.getValue());
                }

                columns.delete(columns.length() - 2, columns.length());
                placeholders.delete(placeholders.length() - 2, placeholders.length());

                String sql = "INSERT INTO `" + this.name + "` (" + columns + ") VALUES (" + placeholders + ")";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int index = 1;
                    for (Object param : params) {
                        ps.setObject(index++, param);
                    }
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Setzen mehrerer Werte:", e);
        }
    }

    public boolean remove(Condition condition) {
        if (!connection.isConnected()) {
            return false;
        }

        try (Connection conn = connection.getConnection()) {
            String sql = "DELETE FROM `" + this.name + "` WHERE `" +
                    condition.getColumnName() + "`" + condition.getOperator() + "?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, condition.getValue());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Entfernen von Daten:", e);
            return false;
        }
    }

    private PreparedStatement select(String columnName, Condition condition) {
        if (!connection.isConnected()) {
            return null;
        }

        try {
            String sql = "SELECT `" + columnName + "` FROM `" + this.name + "` WHERE `" +
                    condition.getColumnName() + "`" + condition.getOperator() + "?";

            PreparedStatement ps = connection.getConnection().prepareStatement(sql);
            ps.setObject(1, condition.getValue());
            return ps;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der SELECT-Anweisung:", e);
            return null;
        }
    }

    public String getString(String columnName, Condition condition) {
        PreparedStatement ps = select(columnName, condition);
        if (ps == null) {
            return null;
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(columnName);
            }
            return null;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Lesen eines String-Werts:", e);
            return null;
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                // Ignorieren
            }
        }
    }

    public int getInt(String columnName, Condition condition) {
        PreparedStatement ps = select(columnName, condition);
        if (ps == null) {
            return 0;
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(columnName);
            }
            return 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Lesen eines Integer-Werts:", e);
            return 0;
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                // Ignorieren
            }
        }
    }

    public boolean getBoolean(String columnName, Condition condition) {
        PreparedStatement ps = select(columnName, condition);
        if (ps == null) {
            return false;
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean(columnName);
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Lesen eines Boolean-Werts:", e);
            return false;
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                // Ignorieren
            }
        }
    }

    public boolean exists(Condition condition) {
        if (!connection.isConnected()) {
            return false;
        }

        try (Connection conn = connection.getConnection()) {
            String sql = "SELECT 1 FROM `" + this.name + "` WHERE `" +
                    condition.getColumnName() + "`" + condition.getOperator() + "? LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, condition.getValue());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Prüfen der Existenz:", e);
            return false;
        }
    }

    public int count(Condition condition) {
        if (!connection.isConnected()) {
            return 0;
        }

        try (Connection conn = connection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM `" + this.name + "` WHERE `" +
                    condition.getColumnName() + "`" + condition.getOperator() + "?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, condition.getValue());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Zählen der Datensätze:", e);
            return 0;
        }
    }
}