package com.Lino.dupeGuard.listeners;

import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.service.DetectionService;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class ContainerListener implements Listener {
    private final DupeGuard plugin;
    private final DetectionService detectionService;
    private final Map<Long, Long> cache;

    public ContainerListener(DupeGuard plugin, DetectionService detectionService) {
        this.plugin = plugin;
        this.detectionService = detectionService;
        this.cache = new HashMap<>();
    }

    private long getBlockKey(Location loc) {
        return ((long) loc.getBlockX() & 0x7FFFFFF) |
                (((long) loc.getBlockZ() & 0x7FFFFFF) << 27) |
                ((long) loc.getBlockY() << 54);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        if (p.hasPermission("dupeguard.bypass")) return;

        Inventory inv = e.getInventory();
        if (!(inv.getHolder() instanceof Container)) return;

        Container c = (Container) inv.getHolder();
        Location loc = c.getLocation();
        long key = getBlockKey(loc);
        long now = System.currentTimeMillis();

        if (cache.containsKey(key) && (now - cache.get(key) < 5000)) {
            return;
        }

        detectionService.scanInventory(p, inv, true);
        cache.put(key, now);

        if (now % 100 == 0) cache.entrySet().removeIf(entry -> now - entry.getValue() > 10000);
    }
}