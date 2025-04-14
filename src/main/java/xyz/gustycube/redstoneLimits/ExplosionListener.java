package xyz.gustycube.redstoneLimits;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.UUID;

public class ExplosionListener implements Listener {

    private final RedstoneLimits plugin;

    public ExplosionListener(RedstoneLimits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplodedBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplodedBlocks(event.blockList());
    }

    private void handleExplodedBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            Material material = block.getType();

            // Check if the block is a redstone component or hopper
            if (plugin.getConfigManager().isRedstoneComponent(material) ||
                    plugin.getConfigManager().isHopper(material)) {

                // Check if block has owner metadata
                if (block.hasMetadata("redstonelimit-owner")) {
                    String ownerUuidString = block.getMetadata("redstonelimit-owner").get(0).asString();
                    UUID ownerUuid = UUID.fromString(ownerUuidString);

                    // Decrement the appropriate counter
                    if (plugin.getConfigManager().isRedstoneComponent(material)) {
                        plugin.getPlayerDataManager().decrementRedstoneCount(ownerUuid);
                        plugin.debug("Decremented redstone count for " + ownerUuid + " due to explosion");
                    } else if (plugin.getConfigManager().isHopper(material)) {
                        plugin.getPlayerDataManager().decrementHopperCount(ownerUuid);
                        plugin.debug("Decremented hopper count for " + ownerUuid + " due to explosion");
                    }

                    // Remove the metadata (not strictly necessary as the block is being destroyed)
                    block.removeMetadata("redstonelimit-owner", plugin);
                }
            }
        }
    }
}