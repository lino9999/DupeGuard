package com.Lino.dupeGuard.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerListener implements Listener {

    private final DupeGuard plugin;
    private final Map<Location, Long> containerScanCache;

    public ContainerListener(DupeGuard plugin) {
        this.plugin = plugin;
        this.containerScanCache = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();

        if (player.hasPermission("dupeguard.bypass")) return;

        if (event.getView().getTitle().equals("DupeGuard Item Editor")) return;

        Inventory inventory = event.getInventory();
        InventoryType type = inventory.getType();

        boolean isContainer = isContainer(type) ||
                (inventory.getHolder() instanceof Container);

        if (isContainer) {
            Location containerLoc = getContainerLocation(inventory);

            if (containerLoc != null) {
                Long lastScan = containerScanCache.get(containerLoc);
                long currentTime = System.currentTimeMillis();
                long cacheTime = plugin.getConfigManager().getContainerCacheMinutes() * 60 * 1000L;

                if (lastScan != null && (currentTime - lastScan) < cacheTime) {
                    return;
                }

                cleanupCache();
            }

            checkContainerItems(player, inventory, event);

            if (containerLoc != null) {
                containerScanCache.put(containerLoc, System.currentTimeMillis());
            }
        }
    }

    private Location getContainerLocation(Inventory inventory) {
        if (inventory.getHolder() instanceof Container) {
            Container container = (Container) inventory.getHolder();
            return container.getLocation();
        } else if (inventory.getLocation() != null) {
            return inventory.getLocation();
        }
        return null;
    }

    private void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        long cacheTime = plugin.getConfigManager().getContainerCacheMinutes() * 60 * 1000L;

        containerScanCache.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > cacheTime
        );
    }

    private boolean isContainer(InventoryType type) {
        switch (type) {
            case CHEST:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
            case BREWING:
            case HOPPER:
            case SHULKER_BOX:
            case BARREL:
            case ENDER_CHEST:
            case BEACON:
                return true;
            default:
                return false;
        }
    }

    private void checkContainerItems(Player player, Inventory containerInventory, InventoryOpenEvent event) {
        Set<ItemStack> monitoredItems = plugin.getItemManager().getMonitoredItems();
        if (monitoredItems.isEmpty()) return;

        for (ItemStack monitoredItem : monitoredItems) {
            int count = countItemsInInventory(containerInventory, monitoredItem);

            if (count >= plugin.getConfigManager().getMaxItemsBeforeAlert()) {
                String itemName = monitoredItem.hasItemMeta() && monitoredItem.getItemMeta().hasDisplayName()
                        ? monitoredItem.getItemMeta().getDisplayName()
                        : monitoredItem.getType().toString();

                alertAdminsContainer(player, itemName, count, containerInventory.getType());

                plugin.getLogManager().logAlert(player.getName() + " (opened container)", itemName, count);

                if (count >= plugin.getConfigManager().getMaxItemsBeforeBan()) {

                    Location playerLoc = player.getLocation();
                    for (Player admin : Bukkit.getOnlinePlayers()) {
                        if (admin.hasPermission("dupeguard.alert")) {
                            admin.sendMessage(MessageUtils.colorize("&c&lWARNING: &eExtremely suspicious container found! Location: " +
                                    playerLoc.getBlockX() + ", " +
                                    playerLoc.getBlockY() + ", " +
                                    playerLoc.getBlockZ()));
                        }
                    }

                    if (plugin.getConfigManager().isAutoRemoveContainer()) {
                        Location containerLoc = null;

                        if (containerInventory.getHolder() instanceof Container) {
                            Container container = (Container) containerInventory.getHolder();
                            containerLoc = container.getLocation();
                        } else if (containerInventory.getLocation() != null) {
                            containerLoc = containerInventory.getLocation();
                        }

                        if (containerLoc != null) {
                            event.setCancelled(true);

                            Location finalLoc = containerLoc.clone();

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                player.closeInventory();

                                Block block = finalLoc.getBlock();
                                BlockState state = block.getState();

                                if (state instanceof Container) {
                                    Container cont = (Container) state;
                                    cont.getInventory().clear();
                                }

                                block.setType(Material.AIR);

                                for (Player admin : Bukkit.getOnlinePlayers()) {
                                    if (admin.hasPermission("dupeguard.alert")) {
                                        admin.sendMessage(MessageUtils.colorize("&c[DupeGuard] &eSuspicious container at &6" +
                                                finalLoc.getBlockX() + ", " + finalLoc.getBlockY() + ", " + finalLoc.getBlockZ() +
                                                " &ehas been removed!"));
                                    }
                                }

                                plugin.getLogManager().logAlert("SYSTEM", "Container removed at " +
                                        finalLoc.getWorld().getName() + " " + finalLoc.getBlockX() + "," + finalLoc.getBlockY() + "," + finalLoc.getBlockZ(), 0);
                            }, 1L);

                            break;
                        }
                    }
                }
            }
        }
    }

    private int countItemsInInventory(Inventory inventory, ItemStack targetItem) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && isSimilar(item, targetItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        if (item1.getType() != item2.getType()) return false;

        if (item1.hasItemMeta() && item2.hasItemMeta()) {
            return item1.getItemMeta().equals(item2.getItemMeta());
        }

        return !item1.hasItemMeta() && !item2.hasItemMeta();
    }

    private void alertAdminsContainer(Player player, String itemName, int count, InventoryType containerType) {
        String containerName = containerType.toString().replace("_", " ").toLowerCase();
        String message = MessageUtils.colorize("&c[DupeGuard] &ePlayer &6" + player.getName() +
                " &eopened a &6" + containerName + " &econtaining &6" + count + " &eof &6" + itemName +
                " &e(Limit: " + plugin.getConfigManager().getMaxItemsBeforeAlert() + ")");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("dupeguard.alert")) {
                admin.sendMessage(message);
                admin.playSound(admin.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.0f, 1.0f);
            }
        }
    }
}