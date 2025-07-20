package com.Lino.dupeGuard.managers;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.Lino.dupeGuard.DupeGuard;

import java.util.Date;

public class BanManager {

    private final DupeGuard plugin;

    public BanManager(DupeGuard plugin) {
        this.plugin = plugin;
    }

    public void banPlayer(Player player, ItemStack item, int count) {
        Location loc = player.getLocation();
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().toString();

        plugin.getLogManager().logBan(
                player.getName(),
                player.getUniqueId().toString(),
                loc,
                itemName,
                count,
                player.getAddress().getAddress().getHostAddress()
        );

        removePlayerItems(player);

        Date banExpiration = new Date(System.currentTimeMillis() +
                (plugin.getConfigManager().getBanDuration() * 60 * 1000L));

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                plugin.getConfigManager().getBanMessage(),
                banExpiration,
                "DupeGuard"
        );

        player.kickPlayer(plugin.getConfigManager().getBanMessage());
    }

    private void removePlayerItems(Player player) {
        for (ItemStack item : plugin.getItemManager().getMonitoredItems()) {
            player.getInventory().remove(item.getType());
        }
    }
}