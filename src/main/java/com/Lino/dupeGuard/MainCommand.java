package com.Lino.dupeGuard;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {

    private final DupeGuard plugin;
    private final GuiManager guiManager;

    public MainCommand(DupeGuard plugin) {
        this.plugin = plugin;
        this.guiManager = new GuiManager(plugin); // Create instance solely for opening GUI
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dupeguard.admin")) {
            sender.sendMessage(ChatColor.RED + "No Permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Usage: /dg <edit|reload>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfigValues();
            plugin.loadItems();
            sender.sendMessage(ChatColor.GREEN + "DupeGuard reloaded.");
        }
        else if (args[0].equalsIgnoreCase("edit")) {
            if (sender instanceof Player) {
                guiManager.openEditor((Player) sender);
            } else {
                sender.sendMessage("Console cannot use GUI.");
            }
        }

        return true;
    }
}