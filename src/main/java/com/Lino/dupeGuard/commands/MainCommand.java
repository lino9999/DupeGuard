package com.Lino.dupeGuard.commands;

import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {

    private final DupeGuard plugin;

    public MainCommand(DupeGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dupeguard.admin")) {
            sender.sendMessage(MessageUtils.colorize("&cYou don't have permission."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(MessageUtils.colorize("&6DupeGuard &7- &e/dg <edit|reload>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reload();
                sender.sendMessage(MessageUtils.colorize("&aConfiguration and items reloaded."));
                break;
            case "edit":
                if (sender instanceof Player) {
                    plugin.getGuiService().open((Player) sender);
                } else {
                    sender.sendMessage("Only players can use the GUI.");
                }
                break;
            default:
                sender.sendMessage(MessageUtils.colorize("&cUnknown command. Use /dg edit or /dg reload"));
                break;
        }
        return true;
    }
}