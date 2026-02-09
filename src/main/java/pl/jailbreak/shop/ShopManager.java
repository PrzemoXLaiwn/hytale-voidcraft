package pl.jailbreak.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ShopManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PlayerManager playerManager;
    private final Set<String> shopNpcIds = new HashSet<>();
    private final File npcFile;

    public ShopManager(PlayerManager playerManager, File dataFolder) {
        this.playerManager = playerManager;
        this.npcFile = new File(dataFolder, "shop_npcs.json");
        loadNpcs();
    }

    private void loadNpcs() {
        if (!npcFile.exists()) return;
        try (Reader reader = new FileReader(npcFile)) {
            Type type = new TypeToken<Set<String>>(){}.getType();
            Set<String> loaded = GSON.fromJson(reader, type);
            if (loaded != null) shopNpcIds.addAll(loaded);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveNpcs() {
        try (Writer writer = new FileWriter(npcFile)) {
            GSON.toJson(shopNpcIds, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void registerShopNpc(String npcId) {
        shopNpcIds.add(npcId);
        saveNpcs();
    }

    public void unregisterShopNpc(String npcId) {
        shopNpcIds.remove(npcId);
        saveNpcs();
    }

    public boolean isShopNpc(String npcId) {
        return shopNpcIds.contains(npcId);
    }

    /**
     * Opens shop UI page for player
     */
    public void openShop(Player player, PlayerData data) {
        String uuid = data.getUuid();

        try {
            World world = player.getWorld();
            if (world == null) {
                System.out.println("[ShopManager] World is null, using chat");
                openChatShop(player, data);
                return;
            }

            world.execute(() -> {
                try {
                    Ref<EntityStore> ref = player.getReference();
                    if (ref == null) {
                        System.out.println("[ShopManager] Player ref is null");
                        openChatShop(player, data);
                        return;
                    }

                    Store<EntityStore> store = ref.getStore();
                    PlayerRef playerRef = player.getPlayerRef();

                    ShopPage shopPage = new ShopPage(playerRef, playerManager, uuid);
                    shopPage.setPlayer(player);

                    player.getPageManager().openCustomPage(ref, store, shopPage);
                    System.out.println("[ShopManager] Opened shop page for " + data.getName());

                } catch (Exception e) {
                    System.out.println("[ShopManager] Error opening page: " + e.getMessage());
                    e.printStackTrace();
                    openChatShop(player, data);
                }
            });

        } catch (Exception e) {
            System.out.println("[ShopManager] Error: " + e.getMessage());
            e.printStackTrace();
            openChatShop(player, data);
        }
    }

    /**
     * Fallback chat-based shop
     */
    private void openChatShop(Player player, PlayerData data) {
        String playerSector = data.getCurrentSector();
        long balance = data.getBalance();

        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("========== PICKAXE SHOP ==========").color("#FFD700"));
        player.sendMessage(Message.raw("Balance: $" + balance).color("#00FF00"));
        player.sendMessage(Message.raw("Sector: " + playerSector).color("#00BFFF"));
        player.sendMessage(Message.raw("----------------------------------").color("#FFD700"));

        for (ShopConfig.ShopItem item : ShopConfig.getItems()) {
            String status;
            String color;

            if (!item.canBuy(playerSector)) {
                status = "[LOCKED - Sector " + item.requiredSector + "]";
                color = "#666666";
            } else if (balance < item.price) {
                status = "[NOT ENOUGH $]";
                color = "#FF6666";
            } else {
                status = "[AVAILABLE]";
                color = "#00FF00";
            }

            String priceStr = item.price == 0 ? "FREE" : "$" + item.price;
            player.sendMessage(Message.raw(
                item.tier + ". " + item.displayName + " - " + priceStr + " " + status
            ).color(color));
        }

        player.sendMessage(Message.raw("----------------------------------").color("#FFD700"));
        player.sendMessage(Message.raw("Use: /buy <number> to purchase").color("#FFFF00"));
        player.sendMessage(Message.raw("==================================").color("#FFD700"));
    }

    /**
     * Buys pickaxe for player
     */
    public boolean buyItem(Player player, PlayerData data, int tier) {
        ShopConfig.ShopItem item = ShopConfig.getItemByTier(tier);

        if (item == null) {
            player.sendMessage(Message.raw("Invalid item number!").color("#FF0000"));
            return false;
        }

        String playerSector = data.getCurrentSector();
        if (!item.canBuy(playerSector)) {
            player.sendMessage(Message.raw("You need Sector " + item.requiredSector + "!").color("#FF0000"));
            return false;
        }

        long balance = data.getBalance();
        if (balance < item.price) {
            player.sendMessage(Message.raw("Not enough money! Need $" + item.price + ", have $" + balance).color("#FF0000"));
            return false;
        }

        // Take money
        if (item.price > 0) {
            data.removeBalance(item.price);
            playerManager.savePlayer(data.getUuid());
        }

        // Give item
        try {
            player.getInventory().getHotbar().addItemStack(new ItemStack(item.itemId, 1));
            player.sendMessage(Message.raw("Purchased " + item.displayName + " for $" + item.price + "!").color("#00FF00"));
            player.sendMessage(Message.raw("New balance: $" + data.getBalance()).color("#00BFFF"));
            return true;
        } catch (Exception e) {
            // Refund on error
            data.addBalance(item.price);
            playerManager.savePlayer(data.getUuid());
            player.sendMessage(Message.raw("Error! Money refunded.").color("#FF0000"));
            return false;
        }
    }
}
