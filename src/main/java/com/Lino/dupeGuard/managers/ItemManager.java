package com.Lino.dupeGuard.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemManager {

    private final DupeGuard plugin;
    private final File itemsFile;
    private FileConfiguration itemsConfig;
    private final Set<ItemStack> monitoredItems;

    public ItemManager(DupeGuard plugin) {
        this.plugin = plugin;
        this.itemsFile = new File(plugin.getDataFolder(), "items.yml");
        this.monitoredItems = new HashSet<>();
    }

    public void loadItems() {
        if (!itemsFile.exists()) {
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        monitoredItems.clear();

        if (itemsConfig.contains("items")) {
            List<Map<?, ?>> itemList = itemsConfig.getMapList("items");
            for (Map<?, ?> itemMap : itemList) {
                ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap);
                if (item != null && item.getType() != Material.AIR) {
                    monitoredItems.add(item);
                }
            }
        }
    }

    public void saveItems() {
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (ItemStack item : monitoredItems) {
            itemList.add(item.serialize());
        }

        itemsConfig.set("items", itemList);

        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            ItemStack normalizedItem = item.clone();
            normalizedItem.setAmount(1);
            monitoredItems.add(normalizedItem);
        }
    }

    public void removeItem(ItemStack item) {
        monitoredItems.removeIf(monitored -> isSimilar(monitored, item));
    }

    public boolean isMonitored(ItemStack item) {
        return monitoredItems.stream().anyMatch(monitored -> isSimilar(monitored, item));
    }

    private boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        if (item1.getType() != item2.getType()) return false;

        if (item1.hasItemMeta() && item2.hasItemMeta()) {
            return item1.getItemMeta().equals(item2.getItemMeta());
        }

        return !item1.hasItemMeta() && !item2.hasItemMeta();
    }

    public int countItems(Player player, ItemStack targetItem) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isSimilar(item, targetItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public Set<ItemStack> getMonitoredItems() {
        return new HashSet<>(monitoredItems);
    }

    public void clearItems() {
        monitoredItems.clear();
    }

    public void listItems(Player player) {
        if (monitoredItems.isEmpty()) {
            player.sendMessage(MessageUtils.colorize("&cNo items are currently being monitored."));
            return;
        }

        player.sendMessage(MessageUtils.colorize("&6Monitored Items:"));
        int index = 1;
        for (ItemStack item : monitoredItems) {
            String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    ? item.getItemMeta().getDisplayName()
                    : item.getType().toString();
            player.sendMessage(MessageUtils.colorize("&e" + index + ". &7" + itemName));
            index++;
        }
    }
}