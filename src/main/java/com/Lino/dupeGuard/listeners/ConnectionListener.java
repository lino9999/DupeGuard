package com.Lino.dupeGuard.listeners;

import com.Lino.dupeGuard.service.DetectionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final DetectionService detectionService;

    public ConnectionListener(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (detectionService != null) {
            detectionService.clearData(event.getPlayer().getUniqueId());
        }
    }
}