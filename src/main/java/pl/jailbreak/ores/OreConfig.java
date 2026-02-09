package pl.jailbreak.ores;

import java.util.HashMap;
import java.util.Map;

/**
 * Konfiguracja rud Hytale - nazwy i ceny
 */
public class OreConfig {

    private static final Map<String, Integer> ORE_PRICES = new HashMap<>();
    private static final Map<String, String> ORE_SECTORS = new HashMap<>();

    static {
        // === SEKTOR A (poczatkowy) ===
        addOre("copper", 10, "A");
        addOre("copper_ore", 10, "A");
        addOre("stone", 1, "A");
        addOre("cobble", 1, "A");
        addOre("rock_stone_cobble", 1, "A");

        // === SEKTOR B ===
        addOre("iron", 25, "B");
        addOre("iron_ore", 25, "B");

        // === SEKTOR C ===
        addOre("thorium", 40, "C");
        addOre("thorium_ore", 40, "C");

        // === SEKTOR D ===
        addOre("gold", 75, "D");
        addOre("gold_ore", 75, "D");
        addOre("silver", 60, "D");
        addOre("silver_ore", 60, "D");

        // === SEKTOR E ===
        addOre("cobalt", 100, "E");
        addOre("cobalt_ore", 100, "E");

        // === SEKTOR F ===
        addOre("adamantite", 200, "F");
        addOre("adamantite_ore", 200, "F");

        // === SEKTOR G+ (gemstones) ===
        addOre("diamond", 500, "G");
        addOre("ruby", 400, "G");
        addOre("sapphire", 350, "G");
        addOre("emerald", 450, "H");
        addOre("topaz", 300, "G");
    }

    private static void addOre(String name, int price, String sector) {
        ORE_PRICES.put(name.toLowerCase(), price);
        ORE_SECTORS.put(name.toLowerCase(), sector);
    }

    public static int getPrice(String blockName) {
        if (blockName == null) return 0;
        String name = normalize(blockName);
        return ORE_PRICES.getOrDefault(name, 0);
    }

    public static boolean isOre(String blockName) {
        return getPrice(blockName) > 0;
    }

    public static String getRequiredSector(String blockName) {
        if (blockName == null) return "A";
        String name = normalize(blockName);
        return ORE_SECTORS.getOrDefault(name, "A");
    }

    public static boolean canMine(String blockName, String playerSector) {
        String required = getRequiredSector(blockName);
        return playerSector.charAt(0) >= required.charAt(0);
    }

    private static String normalize(String blockName) {
        String name = blockName.toLowerCase();
        // Usun prefix namespace (hytale:, minecraft:, etc)
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        // Usun _ore suffix jesli jest w mapie bez niego
        return name;
    }
}