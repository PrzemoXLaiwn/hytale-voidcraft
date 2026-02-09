package pl.jailbreak.mine;

import java.util.*;

/**
 * Configuration for ore distribution in mines per sector.
 * Each sector has ONE ore type (100%).
 *
 * Real Hytale block IDs:
 * - Ore_Copper_Stone
 * - Ore_Iron_Stone
 * - Ore_Thorium_Sandstone
 * - Ore_Silver_Stone
 * - Ore_Gold_Stone
 * - Ore_Cobalt_Slate
 * - Ore_Adamantite_Magma
 * - Rock_Stone
 * - Rock_Stone_Cobble
 */
public class MineConfig {

    // Ore distribution per sector (ore ID -> percentage chance)
    private static final Map<String, Map<String, Integer>> SECTOR_ORES = new LinkedHashMap<>();

    static {
        // Sector A - Copper Mine
        Map<String, Integer> sectorA = new LinkedHashMap<>();
        sectorA.put("Ore_Copper_Stone", 100);
        SECTOR_ORES.put("A", sectorA);

        // Sector B - Iron Mine
        Map<String, Integer> sectorB = new LinkedHashMap<>();
        sectorB.put("Ore_Iron_Stone", 100);
        SECTOR_ORES.put("B", sectorB);

        // Sector C - Thorium Mine
        Map<String, Integer> sectorC = new LinkedHashMap<>();
        sectorC.put("Ore_Thorium_Sandstone", 100);
        SECTOR_ORES.put("C", sectorC);

        // Sector D - Silver Mine
        Map<String, Integer> sectorD = new LinkedHashMap<>();
        sectorD.put("Ore_Silver_Stone", 100);
        SECTOR_ORES.put("D", sectorD);

        // Sector E - Gold Mine
        Map<String, Integer> sectorE = new LinkedHashMap<>();
        sectorE.put("Ore_Gold_Stone", 100);
        SECTOR_ORES.put("E", sectorE);

        // Sector F - Cobalt Mine
        Map<String, Integer> sectorF = new LinkedHashMap<>();
        sectorF.put("Ore_Cobalt_Slate", 100);
        SECTOR_ORES.put("F", sectorF);

        // Sector G - Adamantite Mine
        Map<String, Integer> sectorG = new LinkedHashMap<>();
        sectorG.put("Ore_Adamantite_Magma", 100);
        SECTOR_ORES.put("G", sectorG);

        // Sector H - Adamantite Mine (higher tier)
        Map<String, Integer> sectorH = new LinkedHashMap<>();
        sectorH.put("Ore_Adamantite_Magma", 100);
        SECTOR_ORES.put("H", sectorH);

        // Sector I - Adamantite Mine (highest tier)
        Map<String, Integer> sectorI = new LinkedHashMap<>();
        sectorI.put("Ore_Adamantite_Magma", 100);
        SECTOR_ORES.put("I", sectorI);

        // Sector J - Adamantite Mine (legendary)
        Map<String, Integer> sectorJ = new LinkedHashMap<>();
        sectorJ.put("Ore_Adamantite_Magma", 100);
        SECTOR_ORES.put("J", sectorJ);
    }

    private static final Random RANDOM = new Random();

    /**
     * Get random ore for a sector
     */
    public static String getRandomOre(String sector) {
        Map<String, Integer> ores = SECTOR_ORES.get(sector);
        if (ores == null || ores.isEmpty()) {
            ores = SECTOR_ORES.get("A");
        }

        int roll = RANDOM.nextInt(100);
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : ores.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        return "Rock_Stone";
    }

    /**
     * Get all ores for a sector
     */
    public static Map<String, Integer> getOresForSector(String sector) {
        Map<String, Integer> ores = SECTOR_ORES.get(sector);
        return ores != null ? new LinkedHashMap<>(ores) : SECTOR_ORES.get("A");
    }

    /**
     * Get all sectors
     */
    public static Set<String> getAllSectors() {
        return SECTOR_ORES.keySet();
    }

    /**
     * Check if sector exists
     */
    public static boolean sectorExists(String sector) {
        return SECTOR_ORES.containsKey(sector.toUpperCase());
    }
}
