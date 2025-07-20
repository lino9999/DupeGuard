package com.Lino.dupeGuard;

import com.Lino.dupeGuard.managers.*;
import com.Lino.dupeGuard.listeners.ContainerListener;
import org.bukkit.plugin.java.JavaPlugin;
import com.Lino.dupeGuard.commands.DupeGuardCommand;
import com.Lino.dupeGuard.commands.DupeGuardTabCompleter;
import com.Lino.dupeGuard.tasks.InventoryCheckTask;

public class DupeGuard extends JavaPlugin {

    private static DupeGuard instance;
    private ConfigManager configManager;
    private ItemManager itemManager;
    private GuiManager guiManager;
    private LogManager logManager;
    private PlayerDataManager playerDataManager;
    private BanManager banManager;

    @Override
    public void onEnable() {
        instance = this;

        initializeManagers();
        registerCommands();
        registerListeners();
        startTasks();

        getLogger().info("DupeGuard has been enabled!");
    }

    @Override
    public void onDisable() {
        itemManager.saveItems();
        getLogger().info("DupeGuard has been disabled!");
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        itemManager = new ItemManager(this);
        guiManager = new GuiManager(this);
        logManager = new LogManager(this);
        playerDataManager = new PlayerDataManager();
        banManager = new BanManager(this);

        configManager.loadConfig();
        itemManager.loadItems();
    }

    private void registerCommands() {
        DupeGuardCommand commandExecutor = new DupeGuardCommand(this);
        DupeGuardTabCompleter tabCompleter = new DupeGuardTabCompleter();

        getCommand("dupeguard").setExecutor(commandExecutor);
        getCommand("dupeguard").setTabCompleter(tabCompleter);
        getCommand("dg").setExecutor(commandExecutor);
        getCommand("dg").setTabCompleter(tabCompleter);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ContainerListener(this), this);
    }

    private void startTasks() {
        long intervalTicks = configManager.getScanInterval() * 20L;
        new InventoryCheckTask(this).runTaskTimer(this, intervalTicks, intervalTicks);
    }

    public void reloadPlugin() {
        configManager.loadConfig();
        itemManager.saveItems();
        itemManager.loadItems();
    }

    public static DupeGuard getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }
}