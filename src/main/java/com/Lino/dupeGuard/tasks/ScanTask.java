package com.Lino.dupeGuard.tasks;

import com.Lino.dupeGuard.service.DetectionService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.List;

public class ScanTask extends BukkitRunnable {
    private final DetectionService service;
    private final List<Player> queue;
    private int index;

    public ScanTask(DetectionService service) {
        this.service = service;
        this.queue = new ArrayList<>();
        this.index = 0;
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        queue.clear();
        queue.addAll(Bukkit.getOnlinePlayers());

        if (queue.isEmpty()) return;

        int batchSize = 5;
        int processed = 0;

        while (processed < batchSize) {
            if (index >= queue.size()) index = 0;

            Player p = queue.get(index);
            if (p != null && p.isOnline() && !p.hasPermission("dupeguard.bypass")) {
                service.scanInventory(p, p.getInventory(), false);
                processed++;
            }
            index++;
            if (index >= queue.size()) break;
        }
    }
}