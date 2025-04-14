package xyz.gustycube.redstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class RedstoneLimits extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        // Initialize config
        saveDefaultConfig();

        // Create default playerdata.yml reference
        if (!new File(getDataFolder(), "playerdata.yml").exists()) {
            saveResource("playerdata.yml", false);
        }

        configManager = new ConfigManager(this);

        // Initialize storage
        initializeStorage();

        // Initialize player data manager
        playerDataManager = new PlayerDataManager(this, storageManager);

        // Register event listeners
        registerListeners();

        // Register commands
        registerCommands();

        getLogger().info("RedstoneLimit has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        // Close storage connections
        if (storageManager != null) {
            storageManager.close();
        }

        getLogger().info("RedstoneLimit has been disabled!");
    }

    private void initializeStorage() {
        String storageType = getConfig().getString("storage", "YAML");
        if (storageType.equalsIgnoreCase("MYSQL")) {
            // MySQL implementation would go here
            // storageManager = new MySqlStorageManager(this);
            getLogger().warning("MySQL storage is not implemented yet, falling back to YAML storage.");
            storageManager = new YamlStorageManager(this);
        } else {
            storageManager = new YamlStorageManager(this);
        }

        try {
            storageManager.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize storage manager", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        // Register piston listener if piston protection is enabled
        if (getConfig().getBoolean("prevent-piston-push", true)) {
            getServer().getPluginManager().registerEvents(new PistonListener(this), this);
        }

        // Register explosion listener to handle TNT, creepers, etc.
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
    }

    private void registerCommands() {
        LimitCommand limitCommand = new LimitCommand(this);
        getCommand("limit").setExecutor(limitCommand);
    }

    public void reload() {
        // Reload configuration
        reloadConfig();
        configManager.reload();

        // Save and reload player data
        playerDataManager.saveAll();
        playerDataManager.reload();

        getLogger().info("RedstoneLimit has been reloaded!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public String formatMessage(String message) {
        String prefix = getConfig().getString("messages.prefix", "&8[&cRedstoneLimit&8] &r");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public void debug(String message) {
        if (getConfig().getBoolean("debug", false)) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}