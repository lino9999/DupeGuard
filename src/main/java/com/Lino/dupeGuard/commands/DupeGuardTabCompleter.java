package com.Lino.dupeGuard.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DupeGuardTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = Arrays.asList("edit", "reload", "list");
            List<String> completions = new ArrayList<>();

            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(suggestion);
                }
            }

            return completions;
        }

        return new ArrayList<>();
    }
}