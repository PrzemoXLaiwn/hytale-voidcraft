package pl.jailbreak.enchants;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

/**
 * Manages enchantment shop and enchant effects
 */
public class EnchantManager {

    private final PlayerManager playerManager;

    public EnchantManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    /**
     * Opens the enchant shop UI for a player
     */
    public void openEnchantShop(Player player, PlayerData data) {
        String uuid = data.getUuid();

        try {
            World world = player.getWorld();
            if (world == null) {
                System.out.println("[EnchantManager] World is null, using chat");
                openChatEnchantShop(player, data);
                return;
            }

            world.execute(() -> {
                try {
                    Ref<EntityStore> ref = player.getReference();
                    if (ref == null) {
                        System.out.println("[EnchantManager] Player ref is null");
                        openChatEnchantShop(player, data);
                        return;
                    }

                    Store<EntityStore> store = ref.getStore();
                    PlayerRef playerRef = player.getPlayerRef();

                    EnchantShopPage shopPage = new EnchantShopPage(playerRef, playerManager, uuid);
                    shopPage.setPlayer(player);

                    player.getPageManager().openCustomPage(ref, store, shopPage);
                    System.out.println("[EnchantManager] Opened enchant shop for " + data.getName());

                } catch (Exception e) {
                    System.out.println("[EnchantManager] Error opening page: " + e.getMessage());
                    e.printStackTrace();
                    openChatEnchantShop(player, data);
                }
            });

        } catch (Exception e) {
            System.out.println("[EnchantManager] Error: " + e.getMessage());
            e.printStackTrace();
            openChatEnchantShop(player, data);
        }
    }

    /**
     * Fallback chat-based enchant shop
     */
    private void openChatEnchantShop(Player player, PlayerData data) {
        // Read enchants from held pickaxe
        ItemStack heldPickaxe = PickaxeEnchantUtil.getHeldPickaxe(player);
        PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(heldPickaxe);
        String playerSector = data.getCurrentSector();
        long balance = data.getBalance();

        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("========== ENCHANT SHOP ==========").color("#9966CC"));
        player.sendMessage(Message.raw("Balance: $" + balance).color("#00FF00"));
        player.sendMessage(Message.raw("Sector: " + playerSector).color("#00BFFF"));
        player.sendMessage(Message.raw("----------------------------------").color("#9966CC"));

        for (EnchantType type : EnchantType.values()) {
            int level = enchants.getLevel(type);
            int nextLevel = level + 1;
            boolean isMaxed = level >= type.maxLevel;

            String status;
            String color;

            if (isMaxed) {
                status = "[MAX]";
                color = "#FFD700";
            } else {
                long price = EnchantConfig.getPrice(type, nextLevel);
                String reqSector = EnchantConfig.getRequiredSector(type, nextLevel);

                if (!EnchantConfig.canBuy(type, nextLevel, playerSector)) {
                    status = "[LOCKED - Sector " + reqSector + "]";
                    color = "#666666";
                } else if (balance < price) {
                    status = "[NEED $" + EnchantConfig.formatPrice(price) + "]";
                    color = "#FF6666";
                } else {
                    status = "[AVAILABLE - $" + EnchantConfig.formatPrice(price) + "]";
                    color = "#00FF00";
                }
            }

            String levelStr = level > 0 ? " " + type.getDisplayWithLevel(level) : "";
            player.sendMessage(Message.raw(
                type.displayName + levelStr + " " + status
            ).color(color));
        }

        player.sendMessage(Message.raw("----------------------------------").color("#9966CC"));
        player.sendMessage(Message.raw("Use: /upgrade <enchant> to upgrade").color("#FFFF00"));
        player.sendMessage(Message.raw("==================================").color("#9966CC"));
    }

    /**
     * Upgrade an enchant via command
     */
    public boolean upgradeEnchant(Player player, PlayerData data, String enchantName) {
        EnchantType type = null;
        for (EnchantType t : EnchantType.values()) {
            if (t.name().equalsIgnoreCase(enchantName) ||
                t.displayName.equalsIgnoreCase(enchantName) ||
                t.displayName.replace("-", "").equalsIgnoreCase(enchantName.replace("-", ""))) {
                type = t;
                break;
            }
        }

        if (type == null) {
            player.sendMessage(Message.raw("Unknown enchant! Available: fortune, luck, efficiency, multi-drop, auto-sell").color("#FF0000"));
            return false;
        }

        // Read enchants from held pickaxe
        ItemStack heldPickaxe = PickaxeEnchantUtil.getHeldPickaxe(player);
        if (heldPickaxe == null) {
            player.sendMessage(Message.raw("You must hold a pickaxe to upgrade enchants!").color("#FF0000"));
            return false;
        }

        PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(heldPickaxe);
        int currentLevel = enchants.getLevel(type);
        int nextLevel = currentLevel + 1;

        if (currentLevel >= type.maxLevel) {
            player.sendMessage(Message.raw(type.displayName + " is already at max level!").color("#FF0000"));
            return false;
        }

        String playerSector = data.getCurrentSector();
        if (!EnchantConfig.canBuy(type, nextLevel, playerSector)) {
            String reqSector = EnchantConfig.getRequiredSector(type, nextLevel);
            player.sendMessage(Message.raw("You need Sector " + reqSector + " to upgrade " + type.displayName + "!").color("#FF0000"));
            return false;
        }

        long price = EnchantConfig.getPrice(type, nextLevel);
        if (data.getBalance() < price) {
            player.sendMessage(Message.raw("Not enough money! Need $" + price + ", have $" + data.getBalance()).color("#FF0000"));
            return false;
        }

        // Take money and upgrade
        data.removeBalance(price);
        enchants.upgrade(type);

        // Write enchants back to pickaxe and replace in inventory
        ItemStack updatedPickaxe = PickaxeEnchantUtil.writeEnchants(heldPickaxe, enchants);
        PickaxeEnchantUtil.replaceHeldPickaxe(player, updatedPickaxe);

        playerManager.savePlayer(data.getUuid());

        player.sendMessage(Message.raw("Upgraded " + type.displayName + " to " + type.getDisplayWithLevel(nextLevel) + "!").color("#00FF00"));
        player.sendMessage(Message.raw("New balance: $" + data.getBalance()).color("#00BFFF"));
        return true;
    }

    /**
     * Calculate money with enchant bonuses
     */
    public long calculateMoney(PlayerData data, long baseAmount) {
        PlayerEnchants enchants = data.getEnchants();

        // Apply fortune multiplier (+10% per level, max 50%)
        double fortune = enchants.getFortuneMultiplier();
        long amount = (long) (baseAmount * fortune);

        // Apply luck (chance for x2 money, 5% per level, max 25%)
        double luckChance = enchants.getLuckChance();
        if (luckChance > 0 && Math.random() < luckChance) {
            amount *= 2;
        }

        // Apply prestige bonus
        int prestige = data.getPrestige();
        if (prestige > 0) {
            amount = (long) (amount * (1.0 + prestige * 0.05)); // 5% per prestige
        }

        return amount;
    }

    /**
     * Check if player has auto-sell enabled
     */
    public boolean hasAutoSell(PlayerData data) {
        return data.getEnchants().hasAutoSell();
    }
}
