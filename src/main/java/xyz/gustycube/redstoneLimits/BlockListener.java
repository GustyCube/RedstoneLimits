package xyz.gustycube.redstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockListener implements Listener {

    private final RedstoneLimits plugin;

    public BlockListener(RedstoneLimits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        // Check if the block is a redstone component or hopper
        if (plugin.getConfigManager().isRedstoneComponent(material) || plugin.getConfigManager().isHopper(material)) {
            // Check if player has bypass permission
            if (player.hasPermission("redstonelimit.bypass")) {
                return;
            }

            // Check if player has reached their limit
            if (!plugin.getPlayerDataManager().canPlayerPlace(player, material)) {
                // Cancel the event and send a message
                event.setCancelled(true);

                String type = plugin.getConfigManager().isRedstoneComponent(material) ? "redstone" : "hopper";
                int limit = plugin.getConfigManager().isRedstoneComponent(material)
                        ? plugin.getConfigManager().getRedstoneLimit(player)
                        : plugin.getConfigManager().getHopperLimit(player);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("type", type);
                placeholders.put("limit", String.valueOf(limit));

                String message = plugin.getConfigManager().getMessage("limit-reached", placeholders);
                player.sendMessage(plugin.formatMessage(message));
                return;
            }

            // Store player data in block metadata
            block.setMetadata("redstonelimit-owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

            // Special handling for hopper chains if enabled
            if (material == Material.HOPPER && plugin.getConfigManager().isPreventHopperChains()) {
                // Only increment count if this hopper is not part of an existing chain
                if (!isConnectedToOwnedHopper(block, player.getUniqueId())) {
                    incrementBlockCount(player.getUniqueId(), material);
                }
            } else {
                // For regular components, just increment the count
                incrementBlockCount(player.getUniqueId(), material);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();

        // Check if the block is a redstone component or hopper
        if (plugin.getConfigManager().isRedstoneComponent(material) || plugin.getConfigManager().isHopper(material)) {
            // Check if block has owner metadata
            if (block.hasMetadata("redstonelimit-owner")) {
                String ownerUuidString = block.getMetadata("redstonelimit-owner").get(0).asString();
                UUID ownerUuid = UUID.fromString(ownerUuidString);

                // Special handling for hopper chains if enabled
                if (material == Material.HOPPER && plugin.getConfigManager().isPreventHopperChains()) {
                    // Only decrement count if this hopper is not part of a chain that still exists
                    if (!remainsConnectedToOwnedHopper(block, ownerUuid)) {
                        decrementBlockCount(ownerUuid, material);
                    }
                } else {
                    // For regular components, just decrement the count
                    decrementBlockCount(ownerUuid, material);
                }

                // Remove the metadata
                block.removeMetadata("redstonelimit-owner", plugin);
            }
        }
    }

    private void incrementBlockCount(UUID playerUuid, Material material) {
        if (plugin.getConfigManager().isRedstoneComponent(material)) {
            plugin.getPlayerDataManager().incrementRedstoneCount(playerUuid);
            plugin.debug("Incremented redstone count for " + playerUuid);
        } else if (plugin.getConfigManager().isHopper(material)) {
            plugin.getPlayerDataManager().incrementHopperCount(playerUuid);
            plugin.debug("Incremented hopper count for " + playerUuid);
        }
    }

    private void decrementBlockCount(UUID playerUuid, Material material) {
        if (plugin.getConfigManager().isRedstoneComponent(material)) {
            plugin.getPlayerDataManager().decrementRedstoneCount(playerUuid);
            plugin.debug("Decremented redstone count for " + playerUuid);
        } else if (plugin.getConfigManager().isHopper(material)) {
            plugin.getPlayerDataManager().decrementHopperCount(playerUuid);
            plugin.debug("Decremented hopper count for " + playerUuid);
        }
    }

    private boolean isConnectedToOwnedHopper(Block hopper, UUID playerUuid) {
        // Check adjacent blocks (above, below, and the four sides)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                // Skip diagonals and center
                if ((dx != 0 && dz != 0) || (dx == 0 && dz == 0)) {
                    continue;
                }

                Block adjacent = hopper.getRelative(dx, 0, dz);
                if (adjacent.getType() == Material.HOPPER) {
                    if (isOwnedBy(adjacent, playerUuid)) {
                        return true;
                    }
                }
            }
        }

        // Check above and below
        Block above = hopper.getRelative(0, 1, 0);
        Block below = hopper.getRelative(0, -1, 0);

        return (above.getType() == Material.HOPPER && isOwnedBy(above, playerUuid)) ||
                (below.getType() == Material.HOPPER && isOwnedBy(below, playerUuid));
    }

    private boolean remainsConnectedToOwnedHopper(Block hopper, UUID playerUuid) {
        // Create a snapshot of the block state before it's broken
        BlockState state = hopper.getState();

        // Temporarily remove the hopper to check connections
        hopper.setType(Material.AIR);

        boolean connected = false;

        // Check adjacent blocks for connections
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                // Skip diagonals and center
                if ((dx != 0 && dz != 0) || (dx == 0 && dz == 0)) {
                    continue;
                }

                Block adjacent = hopper.getRelative(dx, 0, dz);
                if (adjacent.getType() == Material.HOPPER) {
                    if (isOwnedBy(adjacent, playerUuid)) {
                        // Check if this adjacent hopper is still connected to another owned hopper
                        for (int dx2 = -1; dx2 <= 1; dx2++) {
                            for (int dz2 = -1; dz2 <= 1; dz2++) {
                                // Skip diagonals, center, and the original hopper direction
                                if ((dx2 != 0 && dz2 != 0) || (dx2 == 0 && dz2 == 0) || (dx2 == -dx && dz2 == -dz)) {
                                    continue;
                                }

                                Block adjacent2 = adjacent.getRelative(dx2, 0, dz2);
                                if (adjacent2.getType() == Material.HOPPER && isOwnedBy(adjacent2, playerUuid)) {
                                    connected = true;
                                    break;
                                }
                            }
                            if (connected) break;
                        }

                        // Also check above and below the adjacent hopper
                        Block adjacentAbove = adjacent.getRelative(0, 1, 0);
                        Block adjacentBelow = adjacent.getRelative(0, -1, 0);

                        if (!connected &&
                                ((adjacentAbove.getType() == Material.HOPPER && isOwnedBy(adjacentAbove, playerUuid)) ||
                                        (adjacentBelow.getType() == Material.HOPPER && isOwnedBy(adjacentBelow, playerUuid)))) {
                            connected = true;
                        }
                    }
                }
                if (connected) break;
            }
            if (connected) break;
        }

        // If not already found connected, check above and below
        if (!connected) {
            Block above = hopper.getRelative(0, 1, 0);
            Block below = hopper.getRelative(0, -1, 0);

            if ((above.getType() == Material.HOPPER && isOwnedBy(above, playerUuid)) ||
                    (below.getType() == Material.HOPPER && isOwnedBy(below, playerUuid))) {
                connected = true;
            }
        }

        // Restore the hopper
        state.update(true, false);

        return connected;
    }

    private boolean isOwnedBy(Block block, UUID playerUuid) {
        if (block.hasMetadata("redstonelimit-owner")) {
            String ownerUuidString = block.getMetadata("redstonelimit-owner").get(0).asString();
            return playerUuid.toString().equals(ownerUuidString);
        }
        return false;
    }
}