package xyz.gustycube.redstoneLimits;

import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;
    private int redstoneCount;
    private int hopperCount;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.redstoneCount = 0;
        this.hopperCount = 0;
    }

    public PlayerData(UUID playerUUID, int redstoneCount, int hopperCount) {
        this.playerUUID = playerUUID;
        this.redstoneCount = redstoneCount;
        this.hopperCount = hopperCount;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getRedstoneCount() {
        return redstoneCount;
    }

    public void setRedstoneCount(int redstoneCount) {
        this.redstoneCount = Math.max(0, redstoneCount);
    }

    public int getHopperCount() {
        return hopperCount;
    }

    public void setHopperCount(int hopperCount) {
        this.hopperCount = Math.max(0, hopperCount);
    }
}