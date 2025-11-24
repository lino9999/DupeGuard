package com.Lino.dupeGuard.utils;

import org.bukkit.ChatColor;

public class MessageUtils {
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}