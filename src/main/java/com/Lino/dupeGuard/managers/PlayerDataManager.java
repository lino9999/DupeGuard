package com.Lino.dupeGuard.managers;

import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager() {
        this.playerDataMap = new ConcurrentHashMap<>();
    }

    public void recordViolation(UUID playerUUID, ItemStack item, int count) {
        PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
        data.addViolation(item, count);
    }

    public PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.get(playerUUID);
    }

    public void clearPlayerData(UUID playerUUID) {
        playerDataMap.remove(playerUUID);
    }

    public void clearAllData() {
        playerDataMap.clear();
    }

    public static class PlayerData {
        private final Map<ItemStack, List<Violation>> violations;

        public PlayerData() {
            this.violations = new HashMap<>();
        }

        public void addViolation(ItemStack item, int count) {
            List<Violation> itemViolations = violations.computeIfAbsent(item, k -> new ArrayList<>());
            itemViolations.add(new Violation(count, System.currentTimeMillis()));

            if (itemViolations.size() > 10) {
                itemViolations.remove(0);
            }
        }

        public Map<ItemStack, List<Violation>> getViolations() {
            return new HashMap<>(violations);
        }

        public int getTotalViolations() {
            return violations.values().stream()
                    .mapToInt(List::size)
                    .sum();
        }
    }

    public static class Violation {
        private final int count;
        private final long timestamp;

        public Violation(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }

        public int getCount() {
            return count;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}