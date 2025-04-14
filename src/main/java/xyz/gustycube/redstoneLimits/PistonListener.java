package xyz.gustycube.redstoneLimits;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class PistonListener implements Listener {

    private final RedstoneLimits plugin;

    public PistonListener(RedstoneLimits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();

        // Check if any of the blocks being moved are redstone components or hoppers
        for (Block block : blocks) {
            if (isProtectedBlock(block)) {
                event.setCancelled(true);
                plugin.debug("Prevented piston from pushing protected block: " + block.getType());
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // Only check blocks for sticky pistons
        if (!event.isSticky()) {
            return;
        }

        List<Block> blocks = event.getBlocks();

        // Check if any of the blocks being moved are redstone components or hoppers
        for (Block block : blocks) {
            if (isProtectedBlock(block)) {
                event.setCancelled(true);
                plugin.debug("Prevented piston from retracting protected block: " + block.getType());
                return;
            }
        }
    }

    private boolean isProtectedBlock(Block block) {
        Material material = block.getType();

        // Check if it's a redstone component or hopper
        if (plugin.getConfigManager().isRedstoneComponent(material) ||
                plugin.getConfigManager().isHopper(material)) {

            // If the block has owner metadata, it's protected
            return block.hasMetadata("redstonelimit-owner");
        }

        return false;
    }
}