package com.Lino.dupeGuard.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.utils.MessageUtils;

import java.util.*;

public class GuiManager implements Listener {

    private final DupeGuard plugin;
    private final Map<UUID, Inventory> openInventories;
    private final String GUI_TITLE = "DupeGuard Item Editor";

    public GuiManager(DupeGuard plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openEditGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        Set<ItemStack> monitoredItems = plugin.getItemManager().getMonitoredItems();
        int slot = 0;
        for (ItemStack item : monitoredItems) {
            if (slot < 45) {
                gui.setItem(slot, item.clone());
                slot++;
            }
        }

        ItemStack saveButton = createButton(Material.EMERALD_BLOCK, "&aSave & Reload",
                Arrays.asList("&7Click to save changes", "&7and reload the plugin"));
        ItemStack clearButton = createButton(Material.REDSTONE_BLOCK, "&cClear All",
                Arrays.asList("&7Remove all monitored items"));
        ItemStack infoButton = createButton(Material.BOOK, "&eInformation",
                Arrays.asList("&7Add items to monitor", "&7Remove items by clicking them"));

        gui.setItem(49, saveButton);
        gui.setItem(53, clearButton);
        gui.setItem(45, infoButton);

        player.openInventory(gui);
        openInventories.put(player.getUniqueId(), gui);
    }

    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(MessageUtils.colorize(name));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(MessageUtils.colorize(line));
        }
        meta.setLore(coloredLore);

        button.setItemMeta(meta);
        return button;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        int slot = event.getRawSlot();

        if (slot == 49) {
            event.setCancelled(true);
            saveAndReload(player);
        } else if (slot == 53) {
            event.setCancelled(true);
            clearAllItems(inventory);
            player.sendMessage(MessageUtils.colorize("&cAll monitored items have been cleared!"));
        } else if (slot == 45) {
            event.setCancelled(true);
        } else if (slot < 45) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    event.setCancelled(true);
                    inventory.setItem(slot, cursorItem.clone());
                    player.setItemOnCursor(null);
                }
            } else {
                event.setCancelled(true);
                inventory.setItem(slot, null);
                player.sendMessage(MessageUtils.colorize("&eItem removed from monitoring list."));
            }
        } else if (slot >= 54) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                for (int i = 0; i < 45; i++) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        inventory.setItem(i, cursorItem.clone());
                        player.setItemOnCursor(null);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        if (openInventories.containsKey(player.getUniqueId())) {
            openInventories.remove(player.getUniqueId());
        }
    }

    private void saveAndReload(Player player) {
        Inventory gui = openInventories.get(player.getUniqueId());
        if (gui == null) return;

        plugin.getItemManager().clearItems();

        for (int i = 0; i < 45; i++) {
            ItemStack item = gui.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                plugin.getItemManager().addItem(item);
            }
        }

        plugin.getItemManager().saveItems();
        plugin.reloadPlugin();

        player.closeInventory();
        player.sendMessage(MessageUtils.colorize("&aItems saved and plugin reloaded successfully!"));
    }

    private void clearAllItems(Inventory inventory) {
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, null);
        }
    }
}