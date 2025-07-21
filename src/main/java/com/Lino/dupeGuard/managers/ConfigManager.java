package com.Lino.dupeGuard.managers;

import org.bukkit.configuration.file.FileConfiguration;
import com.Lino.dupeGuard.DupeGuard;

public class ConfigManager {

    private final DupeGuard plugin;
    private FileConfiguration config;

    private int maxItemsBeforeAlert;
    private int maxItemsBeforeBan;
    private boolean autoBanEnabled;
    private int banDuration;
    private String banMessage;
    private String alertMessage;
    private boolean autoRemoveContainer;
    private int scanInterval;
    private int playersPerScan;
    private int containerCacheMinutes;

    public ConfigManager(DupeGuard plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        maxItemsBeforeAlert = config.getInt("detection.max-items-before-alert", 64);
        maxItemsBeforeBan = config.getInt("detection.max-items-before-ban", 256);
        autoBanEnabled = config.getBoolean("auto-ban.enabled", true);
        banDuration = config.getInt("auto-ban.duration-minutes", 60);
        banMessage = config.getString("messages.ban-message", "You have been temporarily banned for suspicious item duplication!");
        alertMessage = config.getString("messages.alert-message", "&c[DupeGuard] &ePlayer &6%player% &ehas &6%amount% &eof &6%item% &e(Limit: %limit%)");
        autoRemoveContainer = config.getBoolean("detection.auto-remove-container", false);
        scanInterval = config.getInt("detection.scan-interval-seconds", 5);
        playersPerScan = config.getInt("detection.players-per-scan", 1);
        containerCacheMinutes = config.getInt("detection.container-cache-minutes", 5);

        createDefaultConfig();
    }

    private void createDefaultConfig() {
        config.addDefault("detection.max-items-before-alert", 64);
        config.addDefault("detection.max-items-before-ban", 256);
        config.addDefault("detection.auto-remove-container", false);
        config.addDefault("detection.scan-interval-seconds", 5);
        config.addDefault("detection.players-per-scan", 1);
        config.addDefault("detection.container-cache-minutes", 5);
        config.addDefault("auto-ban.enabled", true);
        config.addDefault("auto-ban.duration-minutes", 60);
        config.addDefault("messages.ban-message", "You have been temporarily banned for suspicious item duplication!");
        config.addDefault("messages.alert-message", "&c[DupeGuard] &ePlayer &6%player% &ehas &6%amount% &eof &6%item% &e(Limit: %limit%)");
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public int getMaxItemsBeforeAlert() {
        return maxItemsBeforeAlert;
    }

    public int getMaxItemsBeforeBan() {
        return maxItemsBeforeBan;
    }

    public boolean isAutoBanEnabled() {
        return autoBanEnabled;
    }

    public int getBanDuration() {
        return banDuration;
    }

    public String getBanMessage() {
        return banMessage;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public boolean isAutoRemoveContainer() {
        return autoRemoveContainer;
    }

    public int getScanInterval() {
        return scanInterval;
    }

    public int getPlayersPerScan() {
        return playersPerScan;
    }

    public int getContainerCacheMinutes() {
        return containerCacheMinutes;
    }
}