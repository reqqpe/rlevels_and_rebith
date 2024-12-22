package my.reqqpe.rlevels_and_rebirth;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public final class Main extends JavaPlugin {

    private DataBaseManager databaseManager;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        new Expansion(this).register();
        databaseManager = new DataBaseManager();
        try {
            databaseManager.connect();
            getLogger().info("Database connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Failed to connect to the database!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("setstats").setExecutor(new Command(this, databaseManager));
        getLogger().info("RStats Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        try {
            databaseManager.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info("RStats Plugin Disabled!");
    }

    public DataBaseManager getDatabaseManager() {
        return databaseManager;
    }
}