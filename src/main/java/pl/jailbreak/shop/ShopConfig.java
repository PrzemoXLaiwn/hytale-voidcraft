package pl.jailbreak.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class ShopConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    private static ShopData data;

    public static void init(File dataFolder) {
        configFile = new File(dataFolder, "shop.json");
        load();
    }

    public static void load() {
        // Always use hardcoded values - ignore shop.json to ensure correct item IDs
        data = createDefault();
        save();
    }

    public static void save() {
        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static ShopData createDefault() {
        ShopData d = new ShopData();

        // Pickaxes - prices balanced so each is ~10-20% of sector unlock cost
        // Tier 1: free starter, Tier 2: small upgrade early game
        // Higher tiers are investments that boost mining speed significantly
        d.items.add(new ShopItem("Tool_Pickaxe_Wood", "Wood Pickaxe", 0, "A", 1, 60, 1.0, "Common"));
        d.items.add(new ShopItem("Tool_Pickaxe_Crude", "Crude Pickaxe", 750, "A", 2, 100, 1.2, "Common"));
        d.items.add(new ShopItem("Tool_Pickaxe_Copper", "Copper Pickaxe", 3000, "B", 3, 150, 1.5, "Uncommon"));
        d.items.add(new ShopItem("Tool_Pickaxe_Iron", "Iron Pickaxe", 10000, "C", 4, 250, 2.0, "Uncommon"));
        d.items.add(new ShopItem("Tool_Pickaxe_Scrap", "Scrap Pickaxe", 30000, "D", 5, 400, 2.5, "Rare"));
        d.items.add(new ShopItem("Tool_Pickaxe_Thorium", "Thorium Pickaxe", 75000, "E", 6, 600, 3.0, "Rare"));
        d.items.add(new ShopItem("Tool_Pickaxe_Mithril", "Mithril Pickaxe", 200000, "F", 7, 900, 4.0, "Epic"));
        d.items.add(new ShopItem("Tool_Pickaxe_Cobalt", "Cobalt Pickaxe", 500000, "G", 8, 1200, 5.0, "Epic"));
        d.items.add(new ShopItem("Tool_Pickaxe_Adamantite", "Adamantite Pickaxe", 1500000, "H", 9, 1800, 6.5, "Legendary"));
        d.items.add(new ShopItem("Tool_Pickaxe_Onyxium", "Onyxium Pickaxe", 3500000, "I", 10, 2500, 8.0, "Legendary"));

        return d;
    }

    public static List<ShopItem> getItems() {
        return data.items;
    }

    public static ShopItem getItem(String itemId) {
        for (ShopItem item : data.items) {
            if (item.itemId.equals(itemId)) return item;
        }
        return null;
    }

    public static ShopItem getItemByTier(int tier) {
        for (ShopItem item : data.items) {
            if (item.tier == tier) return item;
        }
        return null;
    }

    public static class ShopData {
        public List<ShopItem> items = new ArrayList<>();
    }

    public static class ShopItem {
        public String itemId;
        public String displayName;
        public long price;
        public String requiredSector;
        public int tier;
        public int durability;
        public double miningSpeed;
        public String rarity;

        public ShopItem() {}

        public ShopItem(String itemId, String displayName, long price, String requiredSector, int tier,
                        int durability, double miningSpeed, String rarity) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.price = price;
            this.requiredSector = requiredSector;
            this.tier = tier;
            this.durability = durability;
            this.miningSpeed = miningSpeed;
            this.rarity = rarity;
        }

        public boolean canBuy(String playerSector) {
            if (playerSector == null || playerSector.isEmpty()) return requiredSector.equals("A");
            return playerSector.charAt(0) >= requiredSector.charAt(0);
        }
    }
}
