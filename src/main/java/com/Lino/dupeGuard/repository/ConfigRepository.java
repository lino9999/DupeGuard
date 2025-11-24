package com.Lino.dupeGuard.repository;

import com.Lino.dupeGuard.DupeGuard;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigRepository {
    private final DupeGuard plugin;
    private int alertThreshold;
    private int banThreshold;
    private boolean autoBan;
    private int banDuration;
    private boolean autoRemoveContainer;
    private int scanInterval;
    private int playersPerScan;
    private String alertMessage;
    private String banMessage;

    public ConfigRepository(DupeGuard plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        FileConfiguration config = plugin.getConfig();
        alertThreshold = config.getInt("detection.max-items-before-alert", 64);
        banThreshold = config.getInt("detection.max-items-before-ban", 256);
        autoBan = config.getBoolean("auto-ban.enabled", true);
        banDuration = config.getInt("auto-ban.duration-minutes", 60);
        autoRemoveContainer = config.getBoolean("detection.auto-remove-container", false);
        scanInterval = config.getInt("detection.scan-interval-seconds", 10);
        playersPerScan = config.getInt("detection.players-per-scan", 5);
        alertMessage = config.getString("messages.alert-message", "&c[DupeGuard] &e%player% has %amount% of %item%");
        banMessage = config.getString("messages.ban-message", "Banned for duplication detected.");
    }
}