package com.Lino.dupeGuard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DupeGuard extends JavaPlugin {

    private static DupeGuard instance;
    private final Set<ItemStack> monitoredItems = new HashSet<>();
    private final Map<UUID, Map<Material, Long>> alertCooldowns = new HashMap<>();
    private File itemsFile;

    // Config settings
    private int alertThreshold;
    private String alertMessage;
    private boolean debugMode;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Setup Config
        saveDefaultConfig();
        loadConfigValues();

        // 2. Setup Items File
        itemsFile = new File(getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            try { itemsFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        loadItems();

        // 3. Register Commands & Events
        getCommand("dupeguard").setExecutor(new MainCommand(this));
        getServer().getPluginManager().registerEvents(new GuiManager(this), this);

        // 4. Start Scanning Task (Runs every 20 ticks = 1 second)
        // Adjust interval in config if needed, hardcoded to 20L for stability
        new BukkitRunnable() {
            @Override
            public void run() {
                scanOnlinePlayers();
            }
        }.runTaskTimer(this, 100L, 20L); // Starts after 5s, runs every 1s

        getLogger().info("DupeGuard Lite enabled successfully!");
    }

    public static DupeGuard getInstance() {
        return instance;
    }

    public void loadConfigValues() {
        reloadConfig();
        FileConfiguration config = getConfig();
        alertThreshold = config.getInt("detection.max-items-before-alert", 64);
        alertMessage = config.getString("messages.alert-message", "&c[DupeGuard] &e%player% has &6%amount% &eof &6%item%");
        debugMode = config.getBoolean("debug", false);
    }

    // --- ITEM MANAGEMENT ---

    public void loadItems() {
        monitoredItems.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
        List<?> list = config.getList("monitored-items");
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof ItemStack) {
                    monitoredItems.add((ItemStack) obj);
                }
            }
        }
        getLogger().info("Loaded " + monitoredItems.size() + " items to monitor.");
    }

    public void saveItems() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("monitored-items", new ArrayList<>(monitoredItems));
        try {
            config.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<ItemStack> getMonitoredItems() {
        return monitoredItems;
    }

    public void addMonitoredItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemStack clone = item.clone();
        clone.setAmount(1);
        monitoredItems.add(clone);
        saveItems();
    }

    public void updateMonitoredItems(List<ItemStack> newItems) {
        monitoredItems.clear();
        for (ItemStack item : newItems) {
            if (item != null && item.getType() != Material.AIR) {
                ItemStack clone = item.clone();
                clone.setAmount(1);
                monitoredItems.add(clone);
            }
        }
        saveItems();
    }

    // --- DETECTION LOGIC ---

    private void scanOnlinePlayers() {
        if (monitoredItems.isEmpty()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("dupeguard.bypass")) continue;
            checkInventory(player);
        }
    }

    private void checkInventory(Player player) {
        Inventory inv = player.getInventory();

        // Temporary map to count items for this player
        Map<ItemStack, Integer> counts = new HashMap<>();

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            // Check if this item matches any monitored item
            for (ItemStack monitored : monitoredItems) {
                if (item.isSimilar(monitored)) {
                    counts.put(monitored, counts.getOrDefault(monitored, 0) + item.getAmount());
                    break; // Stop checking against other monitored items
                }
            }
        }

        // Check thresholds
        for (Map.Entry<ItemStack, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= alertThreshold) {
                triggerAlert(player, entry.getKey(), entry.getValue());
            }
        }
    }

    private void triggerAlert(Player player, ItemStack item, int amount) {
        UUID uuid = player.getUniqueId();
        Material type = item.getType();

        // Cooldown Logic (Prevent spam)
        alertCooldowns.putIfAbsent(uuid, new HashMap<>());
        Map<Material, Long> playerCooldowns = alertCooldowns.get(uuid);

        long lastAlert = playerCooldowns.getOrDefault(type, 0L);
        long now = System.currentTimeMillis();

        // 60 seconds cooldown per item type
        if (now - lastAlert > 60000) {
            String msg = ChatColor.translateAlternateColorCodes('&', alertMessage
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%item%", type.toString()));

            // Notify Admins
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("dupeguard.alert"))
                    .forEach(p -> {
                        p.sendMessage(msg);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    });

            // Console Log
            getLogger().warning("ALERT: " + player.getName() + " has " + amount + " of " + type);

            // Update Cooldown
            playerCooldowns.put(type, now);
        }
    }
}