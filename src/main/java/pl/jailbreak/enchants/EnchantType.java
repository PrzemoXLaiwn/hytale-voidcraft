package pl.jailbreak.enchants;

/**
 * Types of enchantments available for pickaxes
 */
public enum EnchantType {
    // Fortune - increases sell value (+10% per level, max 50%)
    FORTUNE("Fortune", "Increases sell value", "#FFD700", 5, 0.10),

    // Luck - chance for double money on sell (5% per level, max 25%)
    LUCK("Luck", "Chance for x2 money on sell", "#FF69B4", 5, 0.05),

    // Efficiency - breaks extra blocks (level = total blocks mined)
    EFFICIENCY("Efficiency", "Mine multiple blocks at once", "#00FFFF", 5, 1.0),

    // Multi-Drop - chance for extra ore drop (5% per level, max 25%)
    MULTI_DROP("Multi-Drop", "Chance for bonus ore drop", "#9966CC", 5, 0.05),

    // Auto-Sell - automatically sells mined ores instantly
    AUTO_SELL("Auto-Sell", "Instantly sell mined ores", "#00FF00", 1, 0);

    public final String displayName;
    public final String description;
    public final String color;
    public final int maxLevel;
    public final double multiplierPerLevel;

    EnchantType(String displayName, String description, String color, int maxLevel, double multiplierPerLevel) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.maxLevel = maxLevel;
        this.multiplierPerLevel = multiplierPerLevel;
    }

    /**
     * Get effect value for given level
     */
    public double getEffect(int level) {
        if (level <= 0) return 0;
        return multiplierPerLevel * level;
    }

    /**
     * Get display string for level (e.g., "Fortune III")
     */
    public String getDisplayWithLevel(int level) {
        return displayName + " " + toRoman(level);
    }

    private String toRoman(int num) {
        if (num <= 0) return "";
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (num <= 10) return romans[num - 1];
        return String.valueOf(num);
    }
}
