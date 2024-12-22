package my.reqqpe.rlevels_and_rebirth;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class Command implements CommandExecutor {
    private final Main plugin;
    private final DataBaseManager dataBaseManager;

    public Command(Main plugin, DataBaseManager dataBaseManager) {
        this.plugin = plugin;
        this.dataBaseManager = dataBaseManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("setstats")) {
            if (args.length < 4) {
                sender.sendMessage(plugin.getConfig().getString("messages.usage", "Usage: /setstats <rebirth/level> <add/remove/set/reset> <value> <player>"));
                return true;
            }

            String type = args[0];
            String action = args[1];
            int value;

            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Value must be a number!");
                return true;
            }

            Player target = getServer().getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage("Player not found!");
                return true;
            }
            try (Connection connection = dataBaseManager.getConnection()) {
                if (connection == null || connection.isClosed()) {
                    dataBaseManager.getConnection();
                    sender.sendMessage("Не удалось подключиться к базе данных.");
                    return false;
                }
                String column;
                switch (type.toLowerCase()) {
                    case "level" -> column = "level";
                    case "rebirth" -> column = "rebirth";
                    case "rebite" -> column = "rebite";
                    default -> {
                        sender.sendMessage("Invalid type: " + type);
                        return false;
                    }
                }

                int previousValue = getStat(target.getUniqueId(), type);


                String sql = switch (action) {
                    case "add" -> "UPDATE player_stats SET " + column + " = " + column + " + ? WHERE uuid = ?";
                    case "remove" -> "UPDATE player_stats SET " + column + " = GREATEST(" + column + " - ?, 0) WHERE uuid = ?";
                    case "set" -> "UPDATE player_stats SET " + column + " = ? WHERE uuid = ?";
                    case "reset" -> "UPDATE player_stats SET " + column + " = 0 WHERE uuid = ?";
                    default -> throw new IllegalArgumentException("Invalid action: " + action);
                };

                if (connection.isClosed()) {
                    dataBaseManager.getConnection();
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, action.equalsIgnoreCase("reset") ? 0 : value);
                    statement.setString(2, target.getUniqueId().toString());
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }

                int newValue = getStat(target.getUniqueId(), type);

                String messageKey = type.toLowerCase() + "." + action.toLowerCase();
                String message = plugin.getConfig().getString("messages." + messageKey, "{prefix} Action completed")
                        .replace("{prefix}", plugin.getConfig().getString("prefix", "[RStats]"))
                        .replace("{player}", getServer().getOfflinePlayer(target.getUniqueId()).getName())
                        .replace("{level}", String.valueOf(newValue))
                        .replace("{previous_level}", String.valueOf(previousValue))
                        .replace("{add_count}", String.valueOf(value))
                        .replace("{remove_count}", String.valueOf(value))
                        .replace("{rebirth}", String.valueOf(newValue))
                        .replace("{previous_rebirth}", String.valueOf(previousValue))
                        .replace("{rebite}", String.valueOf(newValue))
                        .replace("{previous_rebite}", String.valueOf(previousValue));

                sender.sendMessage(message);

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
//    private void updateStats(UUID uuid, String type, String action, int value, CommandSender sender) {
//        try (Connection connection = dataBaseManager.getConnection()) {
//            if (connection == null || connection.isClosed()) {
//                dataBaseManager.getConnection();
//                sender.sendMessage("Не удалось подключиться к базе данных.");
//                return;
//            }
//            String column;
//            switch (type.toLowerCase()) {
//                case "level" -> column = "level";
//                case "rebirth" -> column = "rebirth";
//                case "rebite" -> column = "rebite";
//                default -> {
//                    sender.sendMessage("Invalid type: " + type);
//                    return;
//                }
//            }
//
//            int previousValue = getStat(uuid, type, connection);
//
//
//            String sql = switch (action) {
//                case "add" -> "UPDATE player_stats SET " + column + " = " + column + " + ? WHERE uuid = ?";
//                case "remove" -> "UPDATE player_stats SET " + column + " = GREATEST(" + column + " - ?, 0) WHERE uuid = ?";
//                case "set" -> "UPDATE player_stats SET " + column + " = ? WHERE uuid = ?";
//                case "reset" -> "UPDATE player_stats SET " + column + " = 0 WHERE uuid = ?";
//                default -> throw new IllegalArgumentException("Invalid action: " + action);
//            };
//
//            if (connection.isClosed()) {
//                dataBaseManager.getConnection();
//            }
//            try (PreparedStatement statement = connection.prepareStatement(sql)) {
//                statement.setInt(1, action.equalsIgnoreCase("reset") ? 0 : value);
//                statement.setString(2, uuid.toString());
//                statement.executeUpdate();
//            }
//            catch (SQLException e) {
//                e.printStackTrace();
//            }
//
//            int newValue = getStat(uuid, type, connection);
//
//            String messageKey = type.toLowerCase() + "." + action.toLowerCase();
//            String message = plugin.getConfig().getString("messages." + messageKey, "{prefix} Action completed")
//                    .replace("{prefix}", plugin.getConfig().getString("prefix", "[RStats]"))
//                    .replace("{player}", getServer().getOfflinePlayer(uuid).getName())
//                    .replace("{level}", String.valueOf(newValue))
//                    .replace("{previous_level}", String.valueOf(previousValue))
//                    .replace("{add_count}", String.valueOf(value))
//                    .replace("{remove_count}", String.valueOf(value))
//                    .replace("{rebirth}", String.valueOf(newValue))
//                    .replace("{previous_rebirth}", String.valueOf(previousValue))
//                    .replace("{rebite}", String.valueOf(newValue))
//                    .replace("{previous_rebite}", String.valueOf(previousValue));
//
//            sender.sendMessage(message);
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
    private int getStat(UUID uuid, String type) throws SQLException {
        try (Connection connection = dataBaseManager.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                String column = type.equalsIgnoreCase("level") ? "level" : "rebirth";
                String sql = "SELECT " + column + " FROM player_stats WHERE uuid = ?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt(column);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            plugin.getLogger().severe("Подключение к базе данных прервано");
            return 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
