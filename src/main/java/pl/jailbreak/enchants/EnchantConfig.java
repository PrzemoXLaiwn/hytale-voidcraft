package pl.jailbreak.enchants;

import java.util.*;

/**
 * Configuration for enchantment prices and requirements
 */
public class EnchantConfig {

    private static final Map<EnchantType, long[]> PRICES = new HashMap<>();
    private static final Map<EnchantType, String[]> REQUIRED_SECTORS = new HashMap<>();

    static {
        // Fortune (1-5) - +10% sell value per level = huge long-term ROI
        // Prices: 2K, 12K, 50K, 200K, 800K (total: ~1M for max)
        PRICES.put(EnchantType.FORTUNE, new long[]{2000, 12000, 50000, 200000, 800000});
        REQUIRED_SECTORS.put(EnchantType.FORTUNE, new String[]{"A", "B", "D", "F", "H"});

        // Luck (1-5) - 5% x2 money chance per level = gambling boost
        // Prices: 3K, 18K, 75K, 300K, 1.2M (total: ~1.6M for max)
        PRICES.put(EnchantType.LUCK, new long[]{3000, 18000, 75000, 300000, 1200000});
        REQUIRED_SECTORS.put(EnchantType.LUCK, new String[]{"A", "C", "E", "G", "I"});

        // Efficiency (1-5) - mine extra blocks = directly multiplies income
        // Most expensive because it multiplies everything
        // Prices: 5K, 25K, 100K, 400K, 1.5M (total: ~2M for max)
        PRICES.put(EnchantType.EFFICIENCY, new long[]{5000, 25000, 100000, 400000, 1500000});
        REQUIRED_SECTORS.put(EnchantType.EFFICIENCY, new String[]{"B", "C", "E", "G", "I"});

        // Multi-Drop (1-5) - 5% bonus drop chance per level
        // Prices: 4K, 20K, 80K, 350K, 1M (total: ~1.45M for max)
        PRICES.put(EnchantType.MULTI_DROP, new long[]{4000, 20000, 80000, 350000, 1000000});
        REQUIRED_SECTORS.put(EnchantType.MULTI_DROP, new String[]{"B", "D", "F", "H", "I"});

        // Auto-Sell (1 level) - huge QoL, unlocks at sector D
        // Price: 25K (affordable mid-game, saves tons of manual /sell)
        PRICES.put(EnchantType.AUTO_SELL, new long[]{25000});
        REQUIRED_SECTORS.put(EnchantType.AUTO_SELL, new String[]{"D"});
    }

    /**
     * Get price for upgrading enchant to specific level
     */
    public static long getPrice(EnchantType type, int level) {
        long[] prices = PRICES.get(type);
        if (prices == null || level < 1 || level > prices.length) return -1;
        return prices[level - 1];
    }

    /**
     * Get required sector for specific enchant level
     */
    public static String getRequiredSector(EnchantType type, int level) {
        String[] sectors = REQUIRED_SECTORS.get(type);
        if (sectors == null || level < 1 || level > sectors.length) return "A";
        return sectors[level - 1];
    }

    /**
     * Check if player can buy enchant level
     */
    public static boolean canBuy(EnchantType type, int level, String playerSector) {
        String required = getRequiredSector(type, level);
        if (playerSector == null || playerSector.isEmpty()) {
            return required.equals("A");
        }
        return playerSector.charAt(0) >= required.charAt(0);
    }

    /**
     * Get all enchant types
     */
    public static EnchantType[] getAllTypes() {
        return EnchantType.values();
    }

    /**
     * Format price for display
     */
    public static String formatPrice(long price) {
        if (price >= 1_000_000) {
            return String.format("%.1fM", price / 1_000_000.0);
        } else if (price >= 1_000) {
            return String.format("%.1fK", price / 1_000.0);
        }
        return String.valueOf(price);
    }
}
