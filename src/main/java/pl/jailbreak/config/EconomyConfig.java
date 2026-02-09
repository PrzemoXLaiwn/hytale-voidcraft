package pl.jailbreak.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class EconomyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    private static EconomyData data;
    
    public static void init(File dataFolder) {
        configFile = new File(dataFolder, "economy.json");
        load();
    }
    
    public static void load() {
        if (!configFile.exists()) { data = createDefault(); save(); return; }
        try (Reader reader = new FileReader(configFile)) {
            data = GSON.fromJson(reader, EconomyData.class);
        } catch (Exception e) { data = createDefault(); save(); }
    }
    
    public static void save() {
        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private static EconomyData createDefault() {
        EconomyData d = new EconomyData();
        
        // ========== ORE PRICES (real Hytale block IDs) ==========
        // Filler blocks
        d.orePrices.put("Rock_Stone", 1);
        d.orePrices.put("Rock_Stone_Cobble", 2);

        // Sector A - Copper
        d.orePrices.put("Ore_Copper_Stone", 8);

        // Sector B - Iron
        d.orePrices.put("Ore_Iron_Stone", 18);

        // Sector C - Thorium
        d.orePrices.put("Ore_Thorium_Sandstone", 35);

        // Sector D - Silver
        d.orePrices.put("Ore_Silver_Stone", 55);

        // Sector E - Gold
        d.orePrices.put("Ore_Gold_Stone", 70);

        // Sector F - Cobalt
        d.orePrices.put("Ore_Cobalt_Slate", 100);

        // Sector G-J - Adamantite
        d.orePrices.put("Ore_Adamantite_Magma", 175);

        // Required sector for each ore (minimum sector to sell)
        d.oreRequiredSector.put("Rock_Stone", "A");
        d.oreRequiredSector.put("Rock_Stone_Cobble", "A");
        d.oreRequiredSector.put("Ore_Copper_Stone", "A");
        d.oreRequiredSector.put("Ore_Iron_Stone", "B");
        d.oreRequiredSector.put("Ore_Thorium_Sandstone", "C");
        d.oreRequiredSector.put("Ore_Silver_Stone", "D");
        d.oreRequiredSector.put("Ore_Gold_Stone", "E");
        d.oreRequiredSector.put("Ore_Cobalt_Slate", "F");
        d.oreRequiredSector.put("Ore_Adamantite_Magma", "G");

        // Prestige: 5% sell bonus per prestige level
        d.prestigeBonusPercent = 5;
        // Prestige cost: must have this much money + sector J unlocked
        d.prestigeCost = 5000000;
        d.globalSellMultiplier = 1.0;
        return d;
    }
    
    public static int getOrePrice(String itemId) {
        return data.orePrices.getOrDefault(itemId, 0);
    }
    
    public static String getRequiredSector(String itemId) {
        return data.oreRequiredSector.getOrDefault(itemId, "A");
    }
    
    public static boolean canSellOre(String itemId, String playerHighestSector) {
        String required = getRequiredSector(itemId);
        return playerHighestSector.charAt(0) >= required.charAt(0);
    }
    
    public static void setOrePrice(String oreName, int price) {
        data.orePrices.put(oreName, price);
        save();
    }
    
    public static Map<String, Integer> getAllPrices() { return data.orePrices; }
    public static int getPrestigeBonusPercent() { return data.prestigeBonusPercent; }
    public static long getPrestigeCost() { return data.prestigeCost; }
    public static double getGlobalMultiplier() { return data.globalSellMultiplier; }
    
    public static void setGlobalMultiplier(double mult) {
        data.globalSellMultiplier = mult;
        save();
    }
    
    public static class EconomyData {
        public Map<String, Integer> orePrices = new LinkedHashMap<>();
        public Map<String, String> oreRequiredSector = new LinkedHashMap<>();
        public int prestigeBonusPercent = 5;
        public long prestigeCost = 1000000;
        public double globalSellMultiplier = 1.0;
    }
}