package com.Lino.dupeGuard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiManager implements Listener {

    private final DupeGuard plugin;
    public static final String TITLE = ChatColor.DARK_RED + "DupeGuard Editor";

    public GuiManager(DupeGuard plugin) {
        this.plugin = plugin;
    }

    public void openEditor(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Fill with currently monitored items
        int slot = 0;
        for (ItemStack item : plugin.getMonitoredItems()) {
            if (slot < 45) {
                inv.setItem(slot++, item);
            }
        }

        // Save Button (Emerald Block)
        ItemStack saveBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = saveBtn.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "SAVE & RELOAD");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to save monitored items"));
        saveBtn.setItemMeta(meta);

        inv.setItem(53, saveBtn);
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;

        // Prevent clicking the save button slot normally
        if (e.getRawSlot() == 53) {
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();
            saveInventory(e.getInventory());

            p.sendMessage(ChatColor.GREEN + "DupeGuard items saved and reloaded!");
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    private void saveInventory(Inventory inv) {
        List<ItemStack> itemsToSave = new ArrayList<>();

        // Loop through the main slots (0-44)
        for (int i = 0; i < 45; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                itemsToSave.add(item);
            }
        }

        plugin.updateMonitoredItems(itemsToSave);
    }
}