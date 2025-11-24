package com.Lino.dupeGuard.repository;

import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.model.CheckItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemRepository {
    private final DupeGuard plugin;
    private final File file;
    private final Set<CheckItem> monitoredItems;

    public ItemRepository(DupeGuard plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "items.yml");
        this.monitoredItems = new HashSet<>();
        load();
    }

    public void load() {
        monitoredItems.clear();
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<?> list = config.getList("items");
        if (list == null) return;

        for (Object obj : list) {
            if (obj instanceof ItemStack) {
                monitoredItems.add(new CheckItem((ItemStack) obj));
            }
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        List<ItemStack> list = monitoredItems.stream()
                .map(CheckItem::getItemStack)
                .collect(Collectors.toList());
        config.set("items", list);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(ItemStack item) {
        monitoredItems.add(new CheckItem(item));
        save();
    }

    public void remove(ItemStack item) {
        monitoredItems.remove(new CheckItem(item));
        save();
    }

    public void clear() {
        monitoredItems.clear();
        save();
    }

    public Set<CheckItem> getItems() {
        return monitoredItems;
    }
}