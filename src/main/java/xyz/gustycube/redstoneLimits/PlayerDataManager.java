package xyz.gustycube.redstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final RedstoneLimits plugin;
    private final StorageManager storageManager;
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(RedstoneLimits plugin, StorageManager storageManager) {
        this.plugin = plugin;
        this.storageManager = storageManager;
        this.playerDataMap = new ConcurrentHashMap<>();

        // Load data for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }
    }

    public void reload() {
        // Clear cache
        playerDataMap.clear();

        // Reload data for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }
    }

    public PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, this::loadPlayerData);
    }

    private PlayerData loadPlayerData(UUID playerUUID) {
        PlayerData playerData = storageManager.loadPlayerData(playerUUID);
        if (playerData == null) {
            playerData = new PlayerData(playerUUID);
        }
        return playerData;
    }

    public void savePlayerData(UUID playerUUID) {
        PlayerData playerData = playerDataMap.get(playerUUID);
        if (playerData != null) {
            storageManager.savePlayerData(playerData);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            storageManager.savePlayerData(entry.getValue());
        }
    }

    public boolean canPlayerPlace(Player player, Material material) {
        PlayerData playerData = getPlayerData(player.getUniqueId());

        if (plugin.getConfigManager().isRedstoneComponent(material)) {
            int currentCount = playerData.getRedstoneCount();
            int limit = plugin.getConfigManager().getRedstoneLimit(player);
            return currentCount < limit;
        } else if (plugin.getConfigManager().isHopper(material)) {
            int currentCount = playerData.getHopperCount();
            int limit = plugin.getConfigManager().getHopperLimit(player);
            return currentCount < limit;
        }

        return true;
    }

    public void incrementRedstoneCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.setRedstoneCount(playerData.getRedstoneCount() + 1);
        savePlayerData(playerUUID);
    }

    public void incrementHopperCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.setHopperCount(playerData.getHopperCount() + 1);
        savePlayerData(playerUUID);
    }

    public void decrementRedstoneCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        int newCount = Math.max(0, playerData.getRedstoneCount() - 1);
        playerData.setRedstoneCount(newCount);
        savePlayerData(playerUUID);
    }

    public void decrementHopperCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        int newCount = Math.max(0, playerData.getHopperCount() - 1);
        playerData.setHopperCount(newCount);
        savePlayerData(playerUUID);
    }

    public void resetRedstoneCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.setRedstoneCount(0);
        savePlayerData(playerUUID);
    }

    public void resetHopperCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.setHopperCount(0);
        savePlayerData(playerUUID);
    }

    public int getRedstoneCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.getRedstoneCount();
    }

    public int getHopperCount(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.getHopperCount();
    }
}