package com.Lino.dupeGuard.managers;

import org.bukkit.Location;
import com.Lino.dupeGuard.DupeGuard;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private final DupeGuard plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat;

    public LogManager(DupeGuard plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.logFile = new File(plugin.getDataFolder(), "bans.log");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create ban log file: " + e.getMessage());
            }
        }
    }

    public void logBan(String playerName, String uuid, Location location, String itemName,
                       int itemCount, String ipAddress) {

        try (FileWriter writer = new FileWriter(logFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            String timestamp = dateFormat.format(new Date());
            String worldName = location.getWorld() != null ? location.getWorld().getName() : "Unknown";

            StringBuilder logEntry = new StringBuilder();
            logEntry.append("=== BAN ENTRY ===\n");
            logEntry.append("Timestamp: ").append(timestamp).append("\n");
            logEntry.append("Player: ").append(playerName).append("\n");
            logEntry.append("UUID: ").append(uuid).append("\n");
            logEntry.append("IP Address: ").append(ipAddress).append("\n");
            logEntry.append("Item: ").append(itemName).append("\n");
            logEntry.append("Item Count: ").append(itemCount).append("\n");
            logEntry.append("Location: ").append(worldName)
                    .append(" X:").append(location.getBlockX())
                    .append(" Y:").append(location.getBlockY())
                    .append(" Z:").append(location.getBlockZ()).append("\n");
            logEntry.append("================\n\n");

            bufferedWriter.write(logEntry.toString());
            bufferedWriter.flush();

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write to ban log: " + e.getMessage());
        }
    }

    public void logAlert(String playerName, String itemName, int itemCount) {
        String timestamp = dateFormat.format(new Date());
        plugin.getLogger().warning(String.format("[%s] Alert: %s has %d of %s",
                timestamp, playerName, itemCount, itemName));
    }
}