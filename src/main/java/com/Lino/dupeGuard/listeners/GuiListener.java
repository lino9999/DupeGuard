package com.Lino.dupeGuard.listeners;

import com.Lino.dupeGuard.service.GuiService;
import com.Lino.dupeGuard.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {
    private final GuiService guiService;

    public GuiListener(GuiService guiService) {
        this.guiService = guiService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(GuiService.TITLE)) return;
        if (e.getClickedInventory() == null) return;

        if (e.getRawSlot() == 53) {
            e.setCancelled(true);
            guiService.saveFromInventory(e.getView().getTopInventory());
            Player p = (Player) e.getWhoClicked();
            p.sendMessage(MessageUtils.colorize("&aSaved!"));
            p.closeInventory();
        }
    }
}