package my.reqqpe.rlevels_and_rebirth;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Expansion  extends PlaceholderExpansion {
    private final Main plugin; //

    public Expansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "example";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        UUID uuid = player.getUniqueId();

        switch (params.toLowerCase()) {
            case "level" -> {
                return String.valueOf(getStat(uuid, "level"));
            }
            case "rebirth" -> {
                return String.valueOf(getStat(uuid, "rebirth"));
            }
            case "rebite" -> {
                return String.valueOf(getStat(uuid, "rebite"));
                }
        }
        return null; // Если идентификатор не распознан
    }

    private int getStat(UUID uuid, String type) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String column = type.equalsIgnoreCase("level") ? "level" : "rebirth";
            String sql = "SELECT " + column + " FROM player_stats WHERE uuid = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(column);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
