package my.reqqpe.rlevels_and_rebirth;

import java.io.File;
import java.sql.*;

public class DataBaseManager {
    private Connection connection;

    public void connect() throws SQLException {

        // Путь к базе данных
        File pluginFolder = new File("plugins/rlevels_and_rebirth");
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdirs()) {
                System.out.println("Папка rLevelsAndRebirths была создана.");
            } else {
                throw new SQLException("Не удалось создать папку для базы данных.");
            }
        }

        String url = "jdbc:sqlite:" + pluginFolder.getPath() + "/stats.db";
        connection = DriverManager.getConnection(url);
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS player_stats (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "level INTEGER DEFAULT 0, " +
                "rebirth INTEGER DEFAULT 0, " +
                "rebite INTEGER DEFAULT 0" +
                ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect(); // Переподключаемся, если соединение закрыто
        }
        return connection;
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
