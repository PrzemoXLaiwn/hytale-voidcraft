package pl.jailbreak.crates;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.*;

public class CrateManager {

    private static final Random RANDOM = new Random();

    // Key drop chance per ore mined (~1 key every 50 ores = 2%)
    public static final double KEY_DROP_CHANCE = 0.02;

    // Crate types
    public static final String COMMON = "common";
    public static final String RARE = "rare";
    public static final String EPIC = "epic";
    public static final String LEGENDARY = "legendary";
    public static final String VOID = "void";

    public static class CrateReward {
        public final String name;
        public final String color;
        public final long moneyReward;
        public final int keyReward; // bonus keys
        public final String keyType; // type of bonus key
        public final double weight; // higher = more common

        public CrateReward(String name, String color, long moneyReward, double weight) {
            this.name = name;
            this.color = color;
            this.moneyReward = moneyReward;
            this.keyReward = 0;
            this.keyType = null;
            this.weight = weight;
        }

        public CrateReward(String name, String color, long moneyReward, int keyReward, String keyType, double weight) {
            this.name = name;
            this.color = color;
            this.moneyReward = moneyReward;
            this.keyReward = keyReward;
            this.keyType = keyType;
            this.weight = weight;
        }
    }

    private static final Map<String, List<CrateReward>> CRATE_REWARDS = new LinkedHashMap<>();
    private static final Map<String, String> CRATE_COLORS = new LinkedHashMap<>();
    private static final Map<String, String> CRATE_DISPLAY = new LinkedHashMap<>();

    static {
        // ============ CRATE REWARDS ============
        // Balanced to economy: common = early game help, void = late game boost
        // Avg common value ~$350, avg rare ~$2K, avg epic ~$15K, legendary ~$80K, void ~$500K

        // Common crate (green) - sectors A-C level rewards
        CRATE_COLORS.put(COMMON, "#00FF00");
        CRATE_DISPLAY.put(COMMON, "Common Crate");
        List<CrateReward> commonRewards = new ArrayList<>();
        commonRewards.add(new CrateReward("$50", "#AAAAAA", 50, 35));
        commonRewards.add(new CrateReward("$150", "#AAAAAA", 150, 25));
        commonRewards.add(new CrateReward("$400", "#FFFFFF", 400, 18));
        commonRewards.add(new CrateReward("$1,000", "#00FF00", 1000, 12));
        commonRewards.add(new CrateReward("$2,500", "#00FF00", 2500, 6));
        commonRewards.add(new CrateReward("$5,000", "#FFD700", 5000, 2));
        commonRewards.add(new CrateReward("Rare Key x1", "#0088FF", 0, 1, RARE, 2));
        CRATE_REWARDS.put(COMMON, commonRewards);

        // Rare crate (blue) - sectors C-E level rewards
        CRATE_COLORS.put(RARE, "#0088FF");
        CRATE_DISPLAY.put(RARE, "Rare Crate");
        List<CrateReward> rareRewards = new ArrayList<>();
        rareRewards.add(new CrateReward("$500", "#AAAAAA", 500, 28));
        rareRewards.add(new CrateReward("$1,500", "#FFFFFF", 1500, 25));
        rareRewards.add(new CrateReward("$3,000", "#00FF00", 3000, 20));
        rareRewards.add(new CrateReward("$7,500", "#00FF00", 7500, 12));
        rareRewards.add(new CrateReward("$15,000", "#FFD700", 15000, 8));
        rareRewards.add(new CrateReward("$30,000", "#FFD700", 30000, 4));
        rareRewards.add(new CrateReward("Epic Key x1", "#9400D3", 0, 1, EPIC, 3));
        CRATE_REWARDS.put(RARE, rareRewards);

        // Epic crate (purple) - sectors E-G level rewards
        CRATE_COLORS.put(EPIC, "#9400D3");
        CRATE_DISPLAY.put(EPIC, "Epic Crate");
        List<CrateReward> epicRewards = new ArrayList<>();
        epicRewards.add(new CrateReward("$5,000", "#FFFFFF", 5000, 25));
        epicRewards.add(new CrateReward("$12,000", "#00FF00", 12000, 22));
        epicRewards.add(new CrateReward("$25,000", "#FFD700", 25000, 20));
        epicRewards.add(new CrateReward("$50,000", "#FFD700", 50000, 15));
        epicRewards.add(new CrateReward("$100,000", "#FF6600", 100000, 8));
        epicRewards.add(new CrateReward("$200,000", "#FF0000", 200000, 5));
        epicRewards.add(new CrateReward("Legendary Key x1", "#FF0000", 0, 1, LEGENDARY, 3));
        epicRewards.add(new CrateReward("Void Key x1", "#FF00FF", 0, 1, VOID, 2));
        CRATE_REWARDS.put(EPIC, epicRewards);

        // Legendary crate (red/gold) - sectors G-I level rewards
        CRATE_COLORS.put(LEGENDARY, "#FF0000");
        CRATE_DISPLAY.put(LEGENDARY, "Legendary Crate");
        List<CrateReward> legendaryRewards = new ArrayList<>();
        legendaryRewards.add(new CrateReward("$25,000", "#00FF00", 25000, 20));
        legendaryRewards.add(new CrateReward("$50,000", "#FFD700", 50000, 22));
        legendaryRewards.add(new CrateReward("$100,000", "#FF6600", 100000, 20));
        legendaryRewards.add(new CrateReward("$200,000", "#FF0000", 200000, 15));
        legendaryRewards.add(new CrateReward("$400,000", "#FF0000", 400000, 10));
        legendaryRewards.add(new CrateReward("$750,000", "#FF00FF", 750000, 6));
        legendaryRewards.add(new CrateReward("Void Key x2", "#FF00FF", 0, 2, VOID, 4));
        legendaryRewards.add(new CrateReward("JACKPOT $1,500,000", "#FFD700", 1500000, 3));
        CRATE_REWARDS.put(LEGENDARY, legendaryRewards);

        // Void crate (magenta/pink) - endgame rewards, prestige-level money
        CRATE_COLORS.put(VOID, "#FF00FF");
        CRATE_DISPLAY.put(VOID, "VOID Crate");
        List<CrateReward> voidRewards = new ArrayList<>();
        voidRewards.add(new CrateReward("$100,000", "#FFD700", 100000, 18));
        voidRewards.add(new CrateReward("$250,000", "#FF6600", 250000, 22));
        voidRewards.add(new CrateReward("$500,000", "#FF0000", 500000, 22));
        voidRewards.add(new CrateReward("$1,000,000", "#FF00FF", 1000000, 18));
        voidRewards.add(new CrateReward("$2,000,000", "#FF00FF", 2000000, 10));
        voidRewards.add(new CrateReward("$3,500,000", "#FFD700", 3500000, 5));
        voidRewards.add(new CrateReward("JACKPOT $5,000,000", "#FFD700", 5000000, 3));
        voidRewards.add(new CrateReward("MEGA JACKPOT $10,000,000", "#FFD700", 10000000, 2));
        CRATE_REWARDS.put(VOID, voidRewards);
    }

    /**
     * Roll a random reward from a crate
     */
    public static CrateReward rollReward(String crateType) {
        List<CrateReward> rewards = CRATE_REWARDS.get(crateType);
        if (rewards == null || rewards.isEmpty()) return null;

        double totalWeight = rewards.stream().mapToDouble(r -> r.weight).sum();
        double roll = RANDOM.nextDouble() * totalWeight;

        double cumulative = 0;
        for (CrateReward reward : rewards) {
            cumulative += reward.weight;
            if (roll <= cumulative) return reward;
        }

        return rewards.get(rewards.size() - 1);
    }

    /**
     * Get random key type to drop from mining
     */
    public static String rollKeyDrop() {
        double roll = RANDOM.nextDouble();
        if (roll < 0.55) return COMMON;      // 55%
        if (roll < 0.85) return RARE;         // 30%
        if (roll < 0.95) return EPIC;         // 10%
        if (roll < 0.99) return LEGENDARY;    // 4%
        return VOID;                           // 1%
    }

    /**
     * Open a crate for a player - CS:GO style rolling in chat
     */
    public static void openCrate(Player player, PlayerData data, String crateType) {
        if (!isValidCrate(crateType)) return;

        if (!data.useKey(crateType)) {
            player.sendMessage(Message.raw("[Voidcraft] You don't have a " + getDisplayName(crateType) + " key!").color("#FF0000"));
            return;
        }

        String color = getColor(crateType);
        List<CrateReward> rewards = CRATE_REWARDS.get(crateType);
        CrateReward finalReward = rollReward(crateType);
        if (finalReward == null) return;

        // CS:GO style - show rolling animation in chat
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("*************************************").color(color));
        player.sendMessage(Message.raw("  Opening " + getDisplayName(crateType) + "...").color(color));
        player.sendMessage(Message.raw("*************************************").color(color));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Show 5 "rolling" items
        for (int i = 0; i < 5; i++) {
            CrateReward fake = rewards.get(RANDOM.nextInt(rewards.size()));
            player.sendMessage(Message.raw("  >> " + fake.name).color("#666666"));
        }

        // The REAL reward
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("  >>>  " + finalReward.name + "  <<<").color(finalReward.color));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Give reward
        if (finalReward.moneyReward > 0) {
            data.addBalance(finalReward.moneyReward);
            player.sendMessage(Message.raw("  +" + String.format("$%,d", finalReward.moneyReward) + " added to balance!").color("#00FF00"));
        }
        if (finalReward.keyReward > 0 && finalReward.keyType != null) {
            data.addKey(finalReward.keyType, finalReward.keyReward);
            player.sendMessage(Message.raw("  +" + finalReward.keyReward + "x " + getDisplayName(finalReward.keyType) + " Key!").color(getColor(finalReward.keyType)));
        }

        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("  Balance: $" + String.format("%,d", data.getBalance())).color("#00BFFF"));
        player.sendMessage(Message.raw("*************************************").color(color));

        // Save
        PlayerManager pm = JailbreakPlugin.getPlayerManager();
        if (pm != null) pm.savePlayer(data.getUuid());

        System.out.println("[Voidcraft] " + data.getName() + " opened " + crateType + " crate -> " + finalReward.name);
    }

    public static boolean isValidCrate(String type) {
        return CRATE_REWARDS.containsKey(type);
    }

    public static String getColor(String type) {
        return CRATE_COLORS.getOrDefault(type, "#FFFFFF");
    }

    public static String getDisplayName(String type) {
        return CRATE_DISPLAY.getOrDefault(type, type);
    }

    public static Set<String> getCrateTypes() {
        return CRATE_REWARDS.keySet();
    }

    public static List<CrateReward> getRewards(String crateType) {
        return CRATE_REWARDS.getOrDefault(crateType, Collections.emptyList());
    }
}
