package xyz.gustycube.redstoneLimits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;


import java.util.*;

public class ConfigManager {

    private final RedstoneLimits plugin;
    private final Set<Material> redstoneComponents;
    private int defaultRedstoneLimit;
    private int defaultHopperLimit;
    private boolean usePermissions;
    private boolean preventHopperChains;

    public ConfigManager(RedstoneLimits plugin) {
        this.plugin = plugin;
        this.redstoneComponents = new HashSet<>();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        loadRedstoneComponents();

        defaultRedstoneLimit = plugin.getConfig().getInt("defaults.redstone", 100);
        defaultHopperLimit = plugin.getConfig().getInt("defaults.hoppers", 50);
        usePermissions = plugin.getConfig().getBoolean("use-permissions", true);
        preventHopperChains = plugin.getConfig().getBoolean("prevent-hopper-chains", true);
    }

    private void loadRedstoneComponents() {
        redstoneComponents.clear();

        List<String> componentList = plugin.getConfig().getStringList("redstone-components");
        for (String componentName : componentList) {
            try {
                Material material = Material.valueOf(componentName.toUpperCase());
                redstoneComponents.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in redstone-components: " + componentName);
            }
        }

        // Always add HOPPER separately as it's tracked separately
        redstoneComponents.remove(Material.HOPPER);
    }

    public boolean isRedstoneComponent(Material material) {
        return redstoneComponents.contains(material);
    }

    public boolean isHopper(Material material) {
        return material == Material.HOPPER;
    }

    public int getRedstoneLimit(Player player) {
        if (player.hasPermission("redstonelimit.bypass")) {
            return Integer.MAX_VALUE;
        }

        // Check for permission-based limits
        if (usePermissions) {
            int permissionLimit = getPermissionBasedLimit(player, "redstonelimit.redstone.");
            if (permissionLimit > 0) {
                return permissionLimit;
            }
        }

        // Check for player-specific config
        String playerName = player.getName();
        ConfigurationSection playerSection = plugin.getConfig().getConfigurationSection("players." + playerName);
        if (playerSection != null && playerSection.contains("redstone")) {
            return playerSection.getInt("redstone");
        }

        // Default limit
        return defaultRedstoneLimit;
    }

    public int getHopperLimit(Player player) {
        if (player.hasPermission("redstonelimit.bypass")) {
            return Integer.MAX_VALUE;
        }

        // Check for permission-based limits
        if (usePermissions) {
            int permissionLimit = getPermissionBasedLimit(player, "redstonelimit.hoppers.");
            if (permissionLimit > 0) {
                return permissionLimit;
            }
        }

        // Check for player-specific config
        String playerName = player.getName();
        ConfigurationSection playerSection = plugin.getConfig().getConfigurationSection("players." + playerName);
        if (playerSection != null && playerSection.contains("hoppers")) {
            return playerSection.getInt("hoppers");
        }

        // Default limit
        return defaultHopperLimit;
    }

    private int getPermissionBasedLimit(Player player, String permissionPrefix) {
        int highestLimit = -1;

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith(permissionPrefix) && pai.getValue()) {
                try {
                    String limitStr = permission.substring(permissionPrefix.length());
                    int limit = Integer.parseInt(limitStr);
                    highestLimit = Math.max(highestLimit, limit);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Also check for server.redstone.X and server.hoppers.X (legacy format mentioned in requirements)
        String legacyPrefix = permissionPrefix.replace("redstonelimit.", "server.");
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith(legacyPrefix) && pai.getValue()) {
                try {
                    String limitStr = permission.substring(legacyPrefix.length());
                    int limit = Integer.parseInt(limitStr);
                    highestLimit = Math.max(highestLimit, limit);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return highestLimit;
    }

    public String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public boolean isPreventHopperChains() {
        return preventHopperChains;
    }

    public Set<Material> getRedstoneComponents() {
        return Collections.unmodifiableSet(redstoneComponents);
    }
}