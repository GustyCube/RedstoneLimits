# RedstoneLimit

A powerful and configurable Minecraft plugin that limits the number of redstone components and hoppers each player can place, helping to prevent server lag and improve performance.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
  - [Default Configuration](#default-configuration)
  - [Customization Options](#customization-options)
- [For Developers](#for-developers)
  - [Plugin Structure](#plugin-structure)
  - [API Usage](#api-usage)
  - [Extending the Plugin](#extending-the-plugin)

## Features

### Core Functionality
- **Separate Redstone & Hopper Limits**: Track and limit redstone components and hoppers independently
- **Player-Specific Limits**: Configure different limits for individual players
- **Permission-Based Limits**: Set varying limits based on player permissions
- **Ownership Tracking**: All placed components are tracked to their respective owners

### Anti-Exploits
- **Piston Protection**: Prevents players from using pistons to move tracked components (configurable)
- **Explosion Handling**: Properly tracks components destroyed by explosions
- **Hopper Chain Detection**: Option to count connected hoppers as a single entity to prevent limit bypassing

### Administration
- **Check Usage**: View current component usage for any player
- **Reset Counters**: Reset redstone or hopper counters for specific players
- **Bypass Permission**: Allow certain players or groups to bypass limits entirely

### Technical Features
- **YAML Storage**: Store player data in YAML files (MySQL support prepared for future releases)
- **Color Code Support**: All messages support color codes using `&` notation
- **Debug Mode**: Optional debug logging for troubleshooting

## Installation

1. Download the latest version of RedstoneLimit
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or use `/reload confirm`
4. Configure the plugin in the generated `config.yml` file
5. Use `/limit reload` to apply changes

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/limit check [player]` | Check redstone and hopper usage for yourself or another player | `redstonelimit.admin` |
| `/limit reset <player> <redstone\|hoppers\|all>` | Reset component counts for a player | `redstonelimit.admin` |
| `/limit reload` | Reload the configuration | `redstonelimit.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `redstonelimit.admin` | Access to all RedstoneLimit commands | `op` |
| `redstonelimit.bypass` | Bypass all redstone and hopper limits | `false` |
| `redstonelimit.redstone.<number>` | Set a custom redstone limit | - |
| `redstonelimit.hoppers.<number>` | Set a custom hopper limit | - |
| `server.redstone.<number>` | Legacy format for custom redstone limit | - |
| `server.hoppers.<number>` | Legacy format for custom hopper limit | - |

## Configuration

### Default Configuration

The plugin generates a comprehensive `config.yml` file with these default values:

```yaml
# Default limits
defaults:
  redstone: 100
  hoppers: 50

# Use permissions for limits
use-permissions: true

# Whether to prevent piston pushing of redstone components
prevent-piston-push: true

# Whether to count connected hoppers as one chain
prevent-hopper-chains: true

# Debug mode (enables additional logging)
debug: false

# Storage type: YAML or MYSQL
storage: YAML
```

### Customization Options

#### Player-Specific Limits

Override default limits for specific players:

```yaml
players:
  PlayerName:
    redstone: 200
    hoppers: 100
```

#### Custom Messages

All messages can be customized with color codes:

```yaml
messages:
  prefix: "&8[&cRedstoneLimit&8] &r"
  limit-reached: "&cYou have reached your limit of {type} components ({limit})."
  check-self: "&7You have placed &6{count}&7 out of &6{limit}&7 {type} components."
  # ...more messages
```

#### Redstone Component List

Configure which blocks count as redstone components:

```yaml
redstone-components:
  - REDSTONE_WIRE
  - REPEATER
  - COMPARATOR
  # ...and more
```

## For Developers

### Plugin Structure

RedstoneLimit follows a clean, modular architecture:

- **RedstoneLimits**: Main plugin class and entry point
- **ConfigManager**: Handles configuration and permissions
- **PlayerDataManager**: Manages player data and limits
- **StorageManager**: Interface for data storage (YAML/MySQL)
- **Listeners**: Event handlers for block placement, explosions, etc.

### API Usage

To integrate with RedstoneLimit in your own plugin:

```java
// Get the plugin instance
RedstoneLimits redstoneLimit = (RedstoneLimits) Bukkit.getPluginManager().getPlugin("RedstoneLimit");

// Check if a player is at their limit
Player player = ...;
Material material = Material.REDSTONE;
boolean canPlace = redstoneLimit.getPlayerDataManager().canPlayerPlace(player, material);

// Get a player's current usage
int redstoneCount = redstoneLimit.getPlayerDataManager().getRedstoneCount(player.getUniqueId());
int hopperCount = redstoneLimit.getPlayerDataManager().getHopperCount(player.getUniqueId());

// Get a player's limits
int redstoneLimit = redstoneLimit.getConfigManager().getRedstoneLimit(player);
int hopperLimit = redstoneLimit.getConfigManager().getHopperLimit(player);
```

### Extending the Plugin

RedstoneLimit is designed for extension:

1. **Custom Storage**: Implement the `StorageManager` interface to add new storage options
2. **Additional Limits**: Extend the plugin to limit other block types by following the pattern in `BlockListener`
3. **Integration**: Hook into the player data tracking to integrate with other plugins

Example custom storage implementation:

```java
public class CustomStorageManager implements StorageManager {
    @Override
    public void initialize() throws Exception {
        // Initialize your storage
    }
    
    @Override
    public void close() {
        // Close connections
    }
    
    @Override
    public PlayerData loadPlayerData(UUID playerUUID) {
        // Load and return player data
    }
    
    @Override
    public void savePlayerData(PlayerData playerData) {
        // Save player data
    }
}
```

## Support

If you encounter any issues or have feature suggestions, please open an issue on our GitHub repository.

## License

This plugin is released under the [MIT License](LICENSE).

---

*Developed by GustyCube*
