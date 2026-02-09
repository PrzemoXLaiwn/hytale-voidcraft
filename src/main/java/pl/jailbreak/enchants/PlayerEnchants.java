package pl.jailbreak.enchants;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores enchantment levels for a player
 */
public class PlayerEnchants {
    private Map<String, Integer> enchants = new HashMap<>();

    public PlayerEnchants() {
        // Initialize all enchants at level 0
        for (EnchantType type : EnchantType.values()) {
            enchants.put(type.name(), 0);
        }
    }

    /**
     * Get level of specific enchant
     */
    public int getLevel(EnchantType type) {
        if (enchants == null) {
            enchants = new HashMap<>();
            for (EnchantType t : EnchantType.values()) {
                enchants.put(t.name(), 0);
            }
        }
        Integer level = enchants.get(type.name());
        return level != null ? level : 0;
    }

    /**
     * Set level of specific enchant
     */
    public void setLevel(EnchantType type, int level) {
        enchants.put(type.name(), Math.min(level, type.maxLevel));
    }

    /**
     * Upgrade enchant by 1 level
     * @return new level, or -1 if max level reached
     */
    public int upgrade(EnchantType type) {
        int current = getLevel(type);
        if (current >= type.maxLevel) {
            return -1;
        }
        int newLevel = current + 1;
        setLevel(type, newLevel);
        return newLevel;
    }

    /**
     * Check if enchant is at max level
     */
    public boolean isMaxLevel(EnchantType type) {
        return getLevel(type) >= type.maxLevel;
    }

    /**
     * Get the enchants map for serialization
     */
    public Map<String, Integer> getEnchantsMap() {
        return enchants;
    }

    /**
     * Set the enchants map from deserialization
     */
    public void setEnchantsMap(Map<String, Integer> map) {
        if (map != null) {
            this.enchants = new HashMap<>(map);
        }
    }

    /**
     * Check if player has auto-sell enabled
     */
    public boolean hasAutoSell() {
        return getLevel(EnchantType.AUTO_SELL) > 0;
    }

    /**
     * Get fortune multiplier (1.0 = no bonus)
     * +10% per level (max 50% at level 5)
     */
    public double getFortuneMultiplier() {
        int level = getLevel(EnchantType.FORTUNE);
        if (level <= 0) return 1.0;
        return 1.0 + (EnchantType.FORTUNE.multiplierPerLevel * level);
    }

    /**
     * Get luck chance for x2 money (0.0 - 0.25)
     * +5% per level (max 25% at level 5)
     */
    public double getLuckChance() {
        int level = getLevel(EnchantType.LUCK);
        if (level <= 0) return 0.0;
        return EnchantType.LUCK.multiplierPerLevel * level;
    }

    /**
     * Get efficiency level (blocks mined per hit)
     * Level 1 = 1 block, Level 5 = 5 blocks
     */
    public int getEfficiencyLevel() {
        return getLevel(EnchantType.EFFICIENCY);
    }

    /**
     * Get multi-drop chance (0.0 - 0.25)
     * +5% per level (max 25% at level 5)
     */
    public double getMultiDropChance() {
        int level = getLevel(EnchantType.MULTI_DROP);
        if (level <= 0) return 0.0;
        return EnchantType.MULTI_DROP.multiplierPerLevel * level;
    }

    /**
     * Get total enchant value (sum of all levels)
     */
    public int getTotalEnchantValue() {
        int total = 0;
        for (EnchantType type : EnchantType.values()) {
            total += getLevel(type);
        }
        return total;
    }
}
