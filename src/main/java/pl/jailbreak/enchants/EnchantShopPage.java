package pl.jailbreak.enchants;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

public class EnchantShopPage extends InteractiveCustomUIPage<EnchantShopPage.EnchantEventData> {

    private final PlayerManager playerManager;
    private final String playerUuid;
    private Player player;
    private EnchantType selectedEnchant = null;

    public static class EnchantEventData {
        public static final BuilderCodec<EnchantEventData> CODEC = BuilderCodec.builder(EnchantEventData.class, EnchantEventData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                (data, value) -> data.action = value, data -> data.action).add()
            .append(new KeyedCodec<>("EnchantId", Codec.STRING),
                (data, value) -> data.enchantId = value, data -> data.enchantId).add()
            .build();

        public String action = "";
        public String enchantId = "";
    }

    public EnchantShopPage(PlayerRef playerRef, PlayerManager playerManager, String playerUuid) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EnchantEventData.CODEC);
        this.playerManager = playerManager;
        this.playerUuid = playerUuid;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder,
                      UIEventBuilder uiEventBuilder, Store<EntityStore> store) {

        System.out.println("[EnchantShopPage] Building UI...");

        uiCommandBuilder.append("EnchantShop.ui");

        PlayerData data = playerManager.getPlayer(playerUuid);
        if (data != null) {
            uiCommandBuilder.set("#BalanceLabel.TextSpans", Message.raw("Balance: $" + formatNumber(data.getBalance())));
            uiCommandBuilder.set("#SectorLabel.TextSpans", Message.raw("Sector: " + data.getCurrentSector()));
        }

        // Register enchant buttons
        EnchantType[] types = EnchantType.values();
        for (int i = 0; i < types.length && i < 6; i++) {
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#Enchant" + (i + 1),
                EventData.of("Action", "select").append("EnchantId", types[i].name()),
                false
            );
        }

        // Register upgrade and close buttons
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#UpgradeButton",
            EventData.of("Action", "upgrade"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            EventData.of("Action", "close"),
            false
        );

        System.out.println("[EnchantShopPage] UI built successfully");
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, EnchantEventData data) {
        System.out.println("[EnchantShopPage] Event: action=" + data.action + ", enchantId=" + data.enchantId);

        if (data.action == null || data.action.isEmpty()) {
            sendUpdate();
            return;
        }

        if ("close".equals(data.action)) {
            close();
            return;
        }

        if ("select".equals(data.action) && data.enchantId != null && !data.enchantId.isEmpty()) {
            try {
                EnchantType type = EnchantType.valueOf(data.enchantId);
                selectEnchant(type);
            } catch (IllegalArgumentException e) {
                System.out.println("[EnchantShopPage] Invalid enchant: " + data.enchantId);
            }
            return;
        }

        if ("upgrade".equals(data.action)) {
            upgradeEnchant();
            return;
        }

        sendUpdate();
    }

    private void selectEnchant(EnchantType type) {
        selectedEnchant = type;

        PlayerData pData = playerManager.getPlayer(playerUuid);
        if (pData == null) return;

        // Read enchants from held pickaxe
        ItemStack heldPickaxe = (player != null) ? PickaxeEnchantUtil.getHeldPickaxe(player) : null;
        PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(heldPickaxe);
        int currentLevel = enchants.getLevel(type);
        int nextLevel = currentLevel + 1;
        boolean isMaxed = currentLevel >= type.maxLevel;

        String playerSector = pData.getCurrentSector();
        long balance = pData.getBalance();

        // Get next level info
        long price = isMaxed ? 0 : EnchantConfig.getPrice(type, nextLevel);
        String reqSector = isMaxed ? "-" : EnchantConfig.getRequiredSector(type, nextLevel);
        boolean canAfford = balance >= price;
        boolean hasAccess = EnchantConfig.canBuy(type, nextLevel, playerSector);

        // Determine status
        String status;
        String statusColor;
        if (isMaxed) {
            status = "MAX LEVEL REACHED";
            statusColor = "#FFD700";
        } else if (!hasAccess) {
            status = "LOCKED - Need Sector " + reqSector;
            statusColor = "#FF4444";
        } else if (!canAfford) {
            status = "NOT ENOUGH MONEY";
            statusColor = "#FF6666";
        } else {
            status = "AVAILABLE - Click UPGRADE";
            statusColor = "#00FF00";
        }

        // Calculate effect description
        String effectDesc = getEffectDescription(type, currentLevel, nextLevel);

        // Update UI
        UICommandBuilder update = new UICommandBuilder();
        update.set("#EnchantName.TextSpans", Message.raw(type.displayName).color(type.color));
        update.set("#EnchantDesc.TextSpans", Message.raw(type.description));
        update.set("#CurrentLevel.TextSpans", Message.raw("Current Level: " + (currentLevel == 0 ? "None" : type.getDisplayWithLevel(currentLevel))));
        update.set("#MaxLevel.TextSpans", Message.raw("Max Level: " + type.maxLevel));
        update.set("#NextLevelPrice.TextSpans", Message.raw("Upgrade Cost: " + (isMaxed ? "N/A" : "$" + formatNumber(price))));
        update.set("#RequiredSector.TextSpans", Message.raw("Required Sector: " + reqSector));
        update.set("#EffectInfo.TextSpans", Message.raw(effectDesc));
        update.set("#StatusLabel.TextSpans", Message.raw(status).color(statusColor));

        sendUpdate(update, false);
        System.out.println("[EnchantShopPage] Selected: " + type.displayName);
    }

    private void upgradeEnchant() {
        if (selectedEnchant == null) {
            if (player != null) {
                player.sendMessage(Message.raw("Select an enchant first!").color("#FF0000"));
            }
            return;
        }

        if (player == null) return;

        PlayerData pData = playerManager.getPlayer(playerUuid);
        if (pData == null) {
            player.sendMessage(Message.raw("Error: Player data not found!").color("#FF0000"));
            return;
        }

        // Read enchants from held pickaxe
        ItemStack heldPickaxe = PickaxeEnchantUtil.getHeldPickaxe(player);
        if (heldPickaxe == null) {
            player.sendMessage(Message.raw("You must hold a pickaxe to upgrade enchants!").color("#FF0000"));
            return;
        }

        PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(heldPickaxe);
        int currentLevel = enchants.getLevel(selectedEnchant);
        int nextLevel = currentLevel + 1;

        if (currentLevel >= selectedEnchant.maxLevel) {
            player.sendMessage(Message.raw("This enchant is already at max level!").color("#FF0000"));
            return;
        }

        String playerSector = pData.getCurrentSector();
        if (!EnchantConfig.canBuy(selectedEnchant, nextLevel, playerSector)) {
            String reqSector = EnchantConfig.getRequiredSector(selectedEnchant, nextLevel);
            player.sendMessage(Message.raw("You need Sector " + reqSector + " to upgrade this!").color("#FF0000"));
            return;
        }

        long price = EnchantConfig.getPrice(selectedEnchant, nextLevel);
        if (pData.getBalance() < price) {
            player.sendMessage(Message.raw("Not enough money! Need $" + formatNumber(price)).color("#FF0000"));
            return;
        }

        // Take money and upgrade
        pData.removeBalance(price);
        enchants.upgrade(selectedEnchant);

        // Write enchants back to pickaxe and replace in inventory
        ItemStack updatedPickaxe = PickaxeEnchantUtil.writeEnchants(heldPickaxe, enchants);
        PickaxeEnchantUtil.replaceHeldPickaxe(player, updatedPickaxe);

        playerManager.savePlayer(playerUuid);

        player.sendMessage(Message.raw("Upgraded " + selectedEnchant.displayName + " to level " + nextLevel + "!").color("#00FF00"));
        player.sendMessage(Message.raw("New balance: $" + formatNumber(pData.getBalance())).color("#00BFFF"));

        // Refresh the display
        selectEnchant(selectedEnchant);

        // Update balance
        UICommandBuilder update = new UICommandBuilder();
        update.set("#BalanceLabel.TextSpans", Message.raw("Balance: $" + formatNumber(pData.getBalance())));
        sendUpdate(update, false);
    }

    private String getEffectDescription(EnchantType type, int currentLevel, int nextLevel) {
        switch (type) {
            case FORTUNE:
                // +10% sell value per level (max 50% at level 5)
                double currentFortune = currentLevel > 0 ? (1 + type.multiplierPerLevel * currentLevel) : 1.0;
                double nextFortune = nextLevel <= type.maxLevel ? (1 + type.multiplierPerLevel * nextLevel) : currentFortune;
                return String.format("Effect: %.0f%% -> %.0f%% sell value", currentFortune * 100, nextFortune * 100);

            case LUCK:
                // 5% x2 money chance per level (max 25% at level 5)
                double currentLuck = currentLevel > 0 ? (type.multiplierPerLevel * currentLevel * 100) : 0;
                double nextLuck = nextLevel <= type.maxLevel ? (type.multiplierPerLevel * nextLevel * 100) : currentLuck;
                return String.format("Effect: %.0f%% -> %.0f%% x2 money chance", currentLuck, nextLuck);

            case EFFICIENCY:
                // Level 1 = no bonus, Level 2 = +1 block, Level 3 = +2, etc.
                int currentBonus = currentLevel > 1 ? currentLevel - 1 : 0;
                int nextBonus = nextLevel > 1 && nextLevel <= type.maxLevel ? nextLevel - 1 : currentBonus;
                if (currentLevel == 0) {
                    return String.format("Effect: +0 -> +%d extra blocks", nextBonus);
                }
                return String.format("Effect: +%d -> +%d extra blocks", currentBonus, nextBonus);

            case MULTI_DROP:
                // 5% bonus ore drop chance per level (max 25% at level 5)
                double currentDrop = currentLevel > 0 ? (type.multiplierPerLevel * currentLevel * 100) : 0;
                double nextDrop = nextLevel <= type.maxLevel ? (type.multiplierPerLevel * nextLevel * 100) : currentDrop;
                return String.format("Effect: %.0f%% -> %.0f%% bonus drop", currentDrop, nextDrop);

            case AUTO_SELL:
                return currentLevel > 0 ? "Effect: AUTO-SELL ACTIVE" : "Effect: Instantly sell mined ores";

            default:
                return "Effect: Unknown";
        }
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%,d", number);
        } else if (number >= 1_000) {
            return String.format("%,d", number);
        }
        return String.valueOf(number);
    }
}
