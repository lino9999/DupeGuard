package com.Lino.dupeGuard.service;

import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.model.CheckItem;
import com.Lino.dupeGuard.repository.ItemRepository;
import com.Lino.dupeGuard.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class GuiService {
    private final DupeGuard plugin;
    private final ItemRepository itemRepository;
    public static final String TITLE = "DupeGuard Editor";

    public GuiService(DupeGuard plugin, ItemRepository itemRepository) {
        this.plugin = plugin;
        this.itemRepository = itemRepository;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        int i = 0;
        for (CheckItem item : itemRepository.getItems()) {
            if (i < 45) inv.setItem(i++, item.getItemStack());
        }

        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = save.getItemMeta();
        meta.setDisplayName(MessageUtils.colorize("&aSave"));
        meta.setLore(Arrays.asList(MessageUtils.colorize("&7Click to save")));
        save.setItemMeta(meta);

        inv.setItem(53, save);
        p.openInventory(inv);
    }

    public void saveFromInventory(Inventory inv) {
        itemRepository.clear();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                itemRepository.add(item);
            }
        }
        itemRepository.save();
    }
}