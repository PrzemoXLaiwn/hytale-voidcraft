package pl.jailbreak.shop;

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
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

public class ShopPage extends InteractiveCustomUIPage<ShopPage.ShopEventData> {

    private final PlayerManager playerManager;
    private final String playerUuid;
    private Player player;
    private int selectedTier = 0; // 0 = nothing selected

    public static class ShopEventData {
        public static final BuilderCodec<ShopEventData> CODEC = BuilderCodec.builder(ShopEventData.class, ShopEventData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                (data, value) -> data.action = value, data -> data.action).add()
            .append(new KeyedCodec<>("ItemId", Codec.STRING),
                (data, value) -> data.itemId = value, data -> data.itemId).add()
            .build();

        public String action = "";
        public String itemId = "";
    }

    public ShopPage(PlayerRef playerRef, PlayerManager playerManager, String playerUuid) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, ShopEventData.CODEC);
        this.playerManager = playerManager;
        this.playerUuid = playerUuid;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder,
                      UIEventBuilder uiEventBuilder, Store<EntityStore> store) {

        System.out.println("[ShopPage] Building UI...");

        // Load UI file
        uiCommandBuilder.append("Shop.ui");

        // Update balance and sector display
        PlayerData data = playerManager.getPlayer(playerUuid);
        if (data != null) {
            uiCommandBuilder.set("#BalanceLabel.TextSpans", Message.raw("Balance: $" + formatNumber(data.getBalance())));
            uiCommandBuilder.set("#SectorLabel.TextSpans", Message.raw("Sector: " + data.getCurrentSector()));
        }

        // Register button click events for all 10 pickaxes (select action)
        for (int i = 1; i <= 10; i++) {
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#Item" + i,
                EventData.of("Action", "select").append("ItemId", String.valueOf(i)),
                false
            );
        }

        // Register BUY button
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#BuyButton",
            EventData.of("Action", "buy"),
            false
        );

        // Register CLOSE button
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            EventData.of("Action", "close"),
            false
        );

        System.out.println("[ShopPage] UI built successfully");
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, ShopEventData data) {
        System.out.println("[ShopPage] Event received - action: " + data.action + ", itemId: " + data.itemId);

        if (data.action == null || data.action.isEmpty()) {
            sendUpdate();
            return;
        }

        // Handle close button
        if ("close".equals(data.action)) {
            close();
            return;
        }

        // Handle select action - show item info
        if ("select".equals(data.action) && data.itemId != null && !data.itemId.isEmpty()) {
            try {
                int tier = Integer.parseInt(data.itemId);
                selectItem(tier);
            } catch (NumberFormatException e) {
                System.out.println("[ShopPage] Invalid itemId: " + data.itemId);
            }
            return;
        }

        // Handle buy action
        if ("buy".equals(data.action)) {
            buySelectedItem();
            return;
        }

        sendUpdate();
    }

    private void selectItem(int tier) {
        ShopConfig.ShopItem item = ShopConfig.getItemByTier(tier);
        if (item == null) {
            System.out.println("[ShopPage] Item not found for tier: " + tier);
            return;
        }

        selectedTier = tier;

        PlayerData pData = playerManager.getPlayer(playerUuid);
        String playerSector = pData != null ? pData.getCurrentSector() : "A";
        long balance = pData != null ? pData.getBalance() : 0;

        // Determine status
        String status;
        String statusColor;
        if (!item.canBuy(playerSector)) {
            status = "LOCKED - Need Sector " + item.requiredSector;
            statusColor = "#FF4444";
        } else if (balance < item.price) {
            status = "NOT ENOUGH MONEY";
            statusColor = "#FF6666";
        } else {
            status = "AVAILABLE - Click BUY";
            statusColor = "#00FF00";
        }

        // Update info panel
        UICommandBuilder updateBuilder = new UICommandBuilder();
        updateBuilder.set("#SelectedName.TextSpans", Message.raw(item.displayName));
        updateBuilder.set("#SelectedPrice.TextSpans", Message.raw("Price: " + (item.price == 0 ? "FREE" : "$" + formatNumber(item.price))));
        updateBuilder.set("#SelectedDurability.TextSpans", Message.raw("Durability: " + item.durability));
        updateBuilder.set("#SelectedSpeed.TextSpans", Message.raw("Mining Speed: " + item.miningSpeed + "x"));
        updateBuilder.set("#SelectedRarity.TextSpans", Message.raw("Rarity: " + item.rarity));
        updateBuilder.set("#SelectedSector.TextSpans", Message.raw("Required Sector: " + item.requiredSector));
        updateBuilder.set("#SelectedStatus.TextSpans", Message.raw(status).color(statusColor));

        sendUpdate(updateBuilder, false);
        System.out.println("[ShopPage] Selected item: " + item.displayName);
    }

    private void buySelectedItem() {
        if (selectedTier == 0) {
            if (player != null) {
                player.sendMessage(Message.raw("Select a pickaxe first!").color("#FF0000"));
            }
            return;
        }

        if (player == null) {
            System.out.println("[ShopPage] Player is null!");
            return;
        }

        PlayerData data = playerManager.getPlayer(playerUuid);
        if (data == null) {
            player.sendMessage(Message.raw("Error: Player data not found!").color("#FF0000"));
            return;
        }

        ShopConfig.ShopItem item = ShopConfig.getItemByTier(selectedTier);
        if (item == null) {
            player.sendMessage(Message.raw("Invalid item!").color("#FF0000"));
            return;
        }

        String playerSector = data.getCurrentSector();
        if (!item.canBuy(playerSector)) {
            player.sendMessage(Message.raw("You need Sector " + item.requiredSector + " to buy this!").color("#FF0000"));
            return;
        }

        long balance = data.getBalance();
        if (balance < item.price) {
            player.sendMessage(Message.raw("Not enough money! You need $" + formatNumber(item.price)).color("#FF0000"));
            return;
        }

        // Take money
        if (item.price > 0) {
            data.removeBalance(item.price);
            playerManager.savePlayer(playerUuid);
        }

        // Give item
        try {
            player.getInventory().getHotbar().addItemStack(new ItemStack(item.itemId, 1));
            player.sendMessage(Message.raw("Purchased " + item.displayName + " for $" + formatNumber(item.price) + "!").color("#00FF00"));
            player.sendMessage(Message.raw("New balance: $" + formatNumber(data.getBalance())).color("#00BFFF"));

            // Update UI - balance and status
            UICommandBuilder updateBuilder = new UICommandBuilder();
            updateBuilder.set("#BalanceLabel.TextSpans", Message.raw("Balance: $" + formatNumber(data.getBalance())));
            updateBuilder.set("#SelectedStatus.TextSpans", Message.raw("PURCHASED!").color("#00FF00"));
            sendUpdate(updateBuilder, false);

        } catch (Exception e) {
            data.addBalance(item.price);
            playerManager.savePlayer(playerUuid);
            player.sendMessage(Message.raw("Error! Money refunded.").color("#FF0000"));
        }
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%,d", number).replace(",", ",");
        } else if (number >= 1_000) {
            return String.format("%,d", number).replace(",", ",");
        }
        return String.valueOf(number);
    }
}
