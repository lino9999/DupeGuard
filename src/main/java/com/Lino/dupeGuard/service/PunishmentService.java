package com.Lino.dupeGuard.service;

import com.Lino.dupeGuard.DupeGuard;
import com.Lino.dupeGuard.repository.ConfigRepository;
import com.Lino.dupeGuard.utils.MessageUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

public class PunishmentService {
    private final DupeGuard plugin;
    private final ConfigRepository config;
    private final LogService logger;

    public PunishmentService(DupeGuard plugin, ConfigRepository config, LogService logger) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
    }

    public void banPlayer(Player player, ItemStack item, int count) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Date expire = new Date(System.currentTimeMillis() + (long) config.getBanDuration() * 60 * 1000);
            String reason = MessageUtils.colorize(config.getBanMessage());

            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, expire, "DupeGuard");
            player.kickPlayer(reason);

            logger.log("BAN: " + player.getName() + " for " + count + " x " + item.getType());
        });
    }
}