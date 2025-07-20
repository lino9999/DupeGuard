package com.Lino.dupeGuard.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.utils.MessageUtils;

public class DupeGuardCommand implements CommandExecutor {

    private final DupeGuard plugin;

    public DupeGuardCommand(DupeGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("dupeguard.admin")) {
            player.sendMessage(MessageUtils.colorize("&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "edit":
                plugin.getGuiManager().openEditGui(player);
                break;
            case "reload":
                plugin.reloadPlugin();
                player.sendMessage(MessageUtils.colorize("&aDupeGuard has been reloaded!"));
                break;
            case "list":
                plugin.getItemManager().listItems(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(MessageUtils.colorize("&6DupeGuard Commands:"));
        player.sendMessage(MessageUtils.colorize("&e/dg edit &7- Open the item editor GUI"));
        player.sendMessage(MessageUtils.colorize("&e/dg reload &7- Reload the plugin"));
        player.sendMessage(MessageUtils.colorize("&e/dg list &7- List all monitored items"));
    }
}