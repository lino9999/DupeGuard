package com.Lino.dupeGuard.service;

import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.model.CheckItem;
import com.Lino.dupeGuard.repository.ConfigRepository;
import com.Lino.dupeGuard.repository.ItemRepository;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.Lino.dupeGuard.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DetectionService {
    private final DupeGuard plugin;
    private final ConfigRepository config;
    private final ItemRepository items;
    private final PunishmentService punishment;
    private final LogService logger;

    private final Map<UUID, Map<CheckItem, Long>> alertCooldowns;

    public DetectionService(DupeGuard plugin, ConfigRepository config, ItemRepository items,
                            PunishmentService punishment, LogService logger) {
        this.plugin = plugin;
        this.config = config;
        this.items = items;
        this.punishment = punishment;
        this.logger = logger;
        this.alertCooldowns = new ConcurrentHashMap<>();
    }

    public void scanInventory(Player player, Inventory inventory, boolean isContainer) {
        if (items.getItems().isEmpty()) return;

        Map<CheckItem, Integer> counts = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            CheckItem wrapped = new CheckItem(item);
            if (items.getItems().contains(wrapped)) {
                counts.merge(wrapped, item.getAmount(), Integer::sum);
            }
        }

        for (Map.Entry<CheckItem, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            if (count >= config.getAlertThreshold()) {
                handleViolation(player, entry.getKey(), count, isContainer);
            }
        }
    }

    private void handleViolation(Player player, CheckItem item, int count, boolean isContainer) {
        if (count >= config.getBanThreshold() && config.isAutoBan()) {
            punishment.banPlayer(player, item.getItemStack(), count);
            return;
        }

        UUID uuid = player.getUniqueId();
        Map<CheckItem, Long> playerCooldowns = alertCooldowns.computeIfAbsent(uuid, k -> new HashMap<>());

        long now = System.currentTimeMillis();
        Long lastAlert = playerCooldowns.get(item);

        if (lastAlert == null || now - lastAlert > 60000) {
            notifyAdmins(player, item.getItemStack(), count, isContainer);
            playerCooldowns.put(item, now);
            logger.log("ALERT: " + player.getName() + " has " + count + " x " + item.getItemStack().getType());
        }
    }

    private void notifyAdmins(Player violator, ItemStack item, int count, boolean isContainer) {
        String msg = config.getAlertMessage()
                .replace("%player%", violator.getName())
                .replace("%amount%", String.valueOf(count))
                .replace("%item%", item.getType().toString())
                .replace("%limit%", String.valueOf(config.getAlertThreshold()));

        if (isContainer) msg += " (Container)";

        String finalMsg = MessageUtils.colorize(msg);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("dupeguard.alert"))
                .forEach(p -> {
                    p.sendMessage(finalMsg);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                });
    }

    public void clearData(UUID uuid) {
        alertCooldowns.remove(uuid);
    }
}