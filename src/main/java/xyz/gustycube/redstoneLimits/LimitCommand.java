package xyz.gustycube.redstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class LimitCommand implements CommandExecutor, TabCompleter {

    private final RedstoneLimits plugin;

    public LimitCommand(RedstoneLimits plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("redstonelimit.admin")) {
            sender.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "check":
                return handleCheckCommand(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }

    private boolean handleCheckCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Self-check if sender is a player
            if (sender instanceof Player) {
                Player player = (Player) sender;
                sendLimitInfo(sender, player, player);
            } else {
                sender.sendMessage(plugin.formatMessage("&cUsage: /limit check <player>"));
            }
            return true;
        }

        // Check specific player
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("player-not-found")));
            return true;
        }

        sendLimitInfo(sender, target, sender instanceof Player ? (Player) sender : null);
        return true;
    }

    private boolean handleResetCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /limit reset <player> <redstone|hoppers|all>"));
            return true;
        }

        // Get target player
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("player-not-found")));
            return true;
        }

        // Get reset type
        String resetType = args[2].toLowerCase();
        UUID playerUUID = target.getUniqueId();

        switch (resetType) {
            case "redstone":
                plugin.getPlayerDataManager().resetRedstoneCount(playerUUID);
                sendResetMessage(sender, target, "redstone");
                break;
            case "hoppers":
                plugin.getPlayerDataManager().resetHopperCount(playerUUID);
                sendResetMessage(sender, target, "hoppers");
                break;
            case "all":
                plugin.getPlayerDataManager().resetRedstoneCount(playerUUID);
                plugin.getPlayerDataManager().resetHopperCount(playerUUID);
                sendResetMessage(sender, target, "redstone");
                sendResetMessage(sender, target, "hoppers");
                break;
            default:
                sender.sendMessage(plugin.formatMessage("&cInvalid type. Use 'redstone', 'hoppers', or 'all'."));
                break;
        }

        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("reload-success")));
        return true;
    }

    private void sendLimitInfo(CommandSender sender, Player target, Player requestingPlayer) {
        // Get limit info
        int redstoneCount = plugin.getPlayerDataManager().getRedstoneCount(target.getUniqueId());
        int hopperCount = plugin.getPlayerDataManager().getHopperCount(target.getUniqueId());

        int redstoneLimit = requestingPlayer == target ?
                plugin.getConfigManager().getRedstoneLimit(target) :
                plugin.getConfigManager().getRedstoneLimit(target);

        int hopperLimit = requestingPlayer == target ?
                plugin.getConfigManager().getHopperLimit(target) :
                plugin.getConfigManager().getHopperLimit(target);

        // Check if self or other
        String messageKey = sender == target ? "check-self" : "check-other";

        // Send redstone info
        Map<String, String> redstoneVars = new HashMap<>();
        redstoneVars.put("count", String.valueOf(redstoneCount));
        redstoneVars.put("limit", String.valueOf(redstoneLimit));
        redstoneVars.put("type", "redstone");
        redstoneVars.put("player", target.getName());

        String redstoneMessage = plugin.getConfigManager().getMessage(messageKey, redstoneVars);
        sender.sendMessage(plugin.formatMessage(redstoneMessage));

        // Send hopper info
        Map<String, String> hopperVars = new HashMap<>();
        hopperVars.put("count", String.valueOf(hopperCount));
        hopperVars.put("limit", String.valueOf(hopperLimit));
        hopperVars.put("type", "hopper");
        hopperVars.put("player", target.getName());

        String hopperMessage = plugin.getConfigManager().getMessage(messageKey, hopperVars);
        sender.sendMessage(plugin.formatMessage(hopperMessage));
    }

    private void sendResetMessage(CommandSender sender, Player target, String type) {
        Map<String, String> vars = new HashMap<>();
        vars.put("player", target.getName());
        vars.put("type", type);

        String message = plugin.getConfigManager().getMessage("reset-success", vars);
        sender.sendMessage(plugin.formatMessage(message));
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== RedstoneLimit Commands ==="));
        sender.sendMessage(plugin.formatMessage("&7/limit check [player] &f- Check limit usage"));
        sender.sendMessage(plugin.formatMessage("&7/limit reset <player> <redstone|hoppers|all> &f- Reset counts"));
        sender.sendMessage(plugin.formatMessage("&7/limit reload &f- Reload configuration"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("redstonelimit.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("check");
            completions.add("reset");
            completions.add("reload");
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("reset")) {
                return null; // Return null for player name suggestions
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reset")) {
                List<String> completions = new ArrayList<>();
                completions.add("redstone");
                completions.add("hoppers");
                completions.add("all");
                return filterCompletions(completions, args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }

        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(completion);
            }
        }

        return filtered;
    }
}