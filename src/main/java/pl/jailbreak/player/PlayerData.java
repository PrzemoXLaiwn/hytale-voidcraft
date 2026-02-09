package pl.jailbreak.player;

import pl.jailbreak.enchants.PlayerEnchants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData {
    private String uuid;
    private String name;
    private long balance;
    private int prestige;
    private long totalEarned;
    private long totalSpent;
    private int blocksMined;
    private String currentSector;
    private List<String> unlockedSectors;
    private boolean firstJoin;
    private PlayerEnchants enchants;

    // Daily rewards
    private long lastDailyReward;
    private int dailyStreak;

    // Achievements
    private List<String> completedAchievements;

    // Crate keys
    private Map<String, Integer> crateKeys;

    public PlayerData(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.balance = 0;
        this.prestige = 0;
        this.totalEarned = 0;
        this.totalSpent = 0;
        this.blocksMined = 0;
        this.currentSector = "A";
        this.unlockedSectors = new ArrayList<>();
        this.unlockedSectors.add("A");
        this.firstJoin = true;
        this.enchants = new PlayerEnchants();
        this.lastDailyReward = 0;
        this.dailyStreak = 0;
        this.completedAchievements = new ArrayList<>();
        this.crateKeys = new HashMap<>();
    }

    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public long getBalance() { return balance; }
    public int getPrestige() { return prestige; }
    public long getTotalEarned() { return totalEarned; }
    public long getTotalSpent() { return totalSpent; }
    public int getBlocksMined() { return blocksMined; }
    public String getCurrentSector() { return currentSector; }
    public List<String> getUnlockedSectors() { return unlockedSectors; }
    public boolean isFirstJoin() { return firstJoin; }

    public void setName(String name) { this.name = name; }
    public void setBalance(long balance) { this.balance = balance; }
    public void setPrestige(int prestige) { this.prestige = prestige; }
    public void setCurrentSector(String sector) { this.currentSector = sector; }
    public void setUnlockedSectors(List<String> sectors) { this.unlockedSectors = sectors; }
    public void setFirstJoin(boolean firstJoin) { this.firstJoin = firstJoin; }

    public void addBalance(long amount) { this.balance += amount; this.totalEarned += amount; }
    public boolean removeBalance(long amount) {
        if (this.balance >= amount) { this.balance -= amount; this.totalSpent += amount; return true; }
        return false;
    }
    public void unlockSector(String sector) {
        if (!unlockedSectors.contains(sector)) { unlockedSectors.add(sector); currentSector = sector; }
    }
    public boolean hasSectorUnlocked(String sector) { return unlockedSectors.contains(sector); }
    public void resetForPrestige() {
        this.balance = 0;
        this.currentSector = "A";
        this.unlockedSectors.clear();
        this.unlockedSectors.add("A");
        // Enchants are now stored on the pickaxe, not the player account.
        // Player loses their pickaxe on prestige and gets a new Wood Pickaxe (no enchants).
        this.prestige++;
    }

    // Enchant methods
    public PlayerEnchants getEnchants() {
        if (enchants == null) {
            enchants = new PlayerEnchants();
        }
        return enchants;
    }

    public void setEnchants(PlayerEnchants enchants) {
        this.enchants = enchants;
    }

    // Daily rewards methods
    public long getLastDailyReward() { return lastDailyReward; }
    public void setLastDailyReward(long time) { this.lastDailyReward = time; }
    public int getDailyStreak() { return dailyStreak; }
    public void setDailyStreak(int streak) { this.dailyStreak = streak; }
    public void incrementDailyStreak() { this.dailyStreak++; }
    public void resetDailyStreak() { this.dailyStreak = 0; }

    // Achievement methods
    public List<String> getCompletedAchievements() {
        if (completedAchievements == null) completedAchievements = new ArrayList<>();
        return completedAchievements;
    }
    public boolean hasAchievement(String id) {
        return getCompletedAchievements().contains(id);
    }
    public void addAchievement(String id) {
        if (!hasAchievement(id)) getCompletedAchievements().add(id);
    }

    public void addBlocksMined(int amount) { this.blocksMined += amount; }

    // Crate key methods
    public Map<String, Integer> getCrateKeys() {
        if (crateKeys == null) crateKeys = new HashMap<>();
        return crateKeys;
    }
    public int getKeys(String crateType) {
        return getCrateKeys().getOrDefault(crateType, 0);
    }
    public void addKey(String crateType, int amount) {
        getCrateKeys().merge(crateType, amount, Integer::sum);
    }
    public boolean useKey(String crateType) {
        int current = getKeys(crateType);
        if (current <= 0) return false;
        getCrateKeys().put(crateType, current - 1);
        return true;
    }
    public int getTotalKeys() {
        return getCrateKeys().values().stream().mapToInt(Integer::intValue).sum();
    }
}