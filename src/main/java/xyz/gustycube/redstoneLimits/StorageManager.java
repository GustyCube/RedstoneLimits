package xyz.gustycube.redstoneLimits;

import java.util.UUID;

public interface StorageManager {

    /**
     * Initialize the storage manager
     */
    void initialize() throws Exception;

    /**
     * Close any connections
     */
    void close();

    /**
     * Load player data from storage
     *
     * @param playerUUID The UUID of the player
     * @return PlayerData object, or null if not found
     */
    PlayerData loadPlayerData(UUID playerUUID);

    /**
     * Save player data to storage
     *
     * @param playerData The player data to save
     */
    void savePlayerData(PlayerData playerData);
}