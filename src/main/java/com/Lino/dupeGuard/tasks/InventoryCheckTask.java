package com.Lino.dupeGuard.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.utils.MessageUtils;

import java.util.*;

public class InventoryCheckTask extends BukkitRunnable {

    private final DupeGuard plugin;
    private final Map<UUID, Map<ItemStack, Long>> lastAlerts;

    public InventoryCheckTask(DupeGuard plugin) {
        this.plugin = plugin;
        this.lastAlerts = new HashMap<>();
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) return;

        Set<ItemStack> monitoredItems = plugin.getItemManager().getMonitoredItems();
        if (monitoredItems.isEmpty()) return;

        for (Player player : onlinePlayers) {
            if (player.hasPermission("dupeguard.bypass")) continue;

            checkPlayerInventory(player, monitoredItems);
        }
    }

    private void checkPlayerInventory(Player player, Set<ItemStack> monitoredItems) {
        Map<ItemStack, Long> playerAlerts = lastAlerts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        for (ItemStack monitoredItem : monitoredItems) {
            int count = plugin.getItemManager().countItems(player, monitoredItem);

            if (count >= plugin.getConfigManager().getMaxItemsBeforeAlert()) {
                long currentTime = System.currentTimeMillis();
                Long lastAlert = playerAlerts.get(monitoredItem);

                if (lastAlert == null || currentTime - lastAlert > 60000) {
                    alertAdmins(player, monitoredItem, count);
                    playerAlerts.put(monitoredItem, currentTime);
                }

                if (plugin.getConfigManager().isAutoBanEnabled() &&
                        count >= plugin.getConfigManager().getMaxItemsBeforeBan()) {

                    plugin.getBanManager().banPlayer(player, monitoredItem, count);
                    plugin.getPlayerDataManager().clearPlayerData(player.getUniqueId());
                    lastAlerts.remove(player.getUniqueId());
                    break;
                }
            }
        }
    }

    private void alertAdmins(Player player, ItemStack item, int count) {
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().toString();

        String message = plugin.getConfigManager().getAlertMessage()
                .replace("%player%", player.getName())
                .replace("%amount%", String.valueOf(count))
                .replace("%item%", itemName)
                .replace("%limit%", String.valueOf(plugin.getConfigManager().getMaxItemsBeforeAlert()));

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("dupeguard.alert")) {
                admin.sendMessage(MessageUtils.colorize(message));
                admin.playSound(admin.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.0f, 1.0f);
            }
        }

        plugin.getPlayerDataManager().recordViolation(player.getUniqueId(), item, count);
        plugin.getLogManager().logAlert(player.getName(), itemName, count);
    }
}