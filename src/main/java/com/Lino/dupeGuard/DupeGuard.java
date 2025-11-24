package com.Lino.dupeGuard;

import com.Lino.dupeGuard.commands.MainCommand;
import com.Lino.dupeGuard.listeners.ConnectionListener;
import com.Lino.dupeGuard.listeners.ContainerListener;
import com.Lino.dupeGuard.listeners.GuiListener;
import com.Lino.dupeGuard.repository.ConfigRepository;
import com.Lino.dupeGuard.repository.ItemRepository;
import com.Lino.dupeGuard.service.*;
import com.Lino.dupeGuard.tasks.ScanTask;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class DupeGuard extends JavaPlugin {

    private static DupeGuard instance;
    private ConfigRepository configRepository;
    private ItemRepository itemRepository;
    private LogService logService;
    private GuiService guiService;
    private DetectionService detectionService;
    private PunishmentService punishmentService;

    @Override
    public void onEnable() {
        instance = this;

        this.configRepository = new ConfigRepository(this);
        this.logService = new LogService(this);
        this.itemRepository = new ItemRepository(this);
        this.punishmentService = new PunishmentService(this, configRepository, logService);
        this.detectionService = new DetectionService(this, configRepository, itemRepository, punishmentService, logService);
        this.guiService = new GuiService(this, itemRepository);

        getCommand("dupeguard").setExecutor(new MainCommand(this));

        getServer().getPluginManager().registerEvents(new ContainerListener(this, detectionService), this);
        getServer().getPluginManager().registerEvents(new GuiListener(guiService), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(detectionService), this);

        long interval = configRepository.getScanInterval() * 20L;
        new ScanTask(detectionService).runTaskTimer(this, interval, interval);
    }

    @Override
    public void onDisable() {
        if (itemRepository != null) itemRepository.save();
        if (logService != null) logService.shutdown();
    }

    public void reload() {
        configRepository.reload();
        itemRepository.reload();
    }
}