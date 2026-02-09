package pl.jailbreak.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class StarterKit {

    private static final String PICKAXE_ID = "Tool_Pickaxe_Wood";
    private static final String HELMET_ID = "Armor_Copper_Head";
    private static final String CHEST_ID = "Armor_Copper_Chest";
    private static final String LEGS_ID = "Armor_Copper_Legs";
    private static final String HANDS_ID = "Armor_Copper_Hands";

    public static void giveStarterKit(Player player) {
        try {
            Inventory inventory = player.getInventory();

            inventory.getHotbar().addItemStack(new ItemStack(PICKAXE_ID, 1));
            inventory.getStorage().addItemStack(new ItemStack(HELMET_ID, 1));
            inventory.getStorage().addItemStack(new ItemStack(CHEST_ID, 1));
            inventory.getStorage().addItemStack(new ItemStack(LEGS_ID, 1));
            inventory.getStorage().addItemStack(new ItemStack(HANDS_ID, 1));

            System.out.println("[Voidcraft] Gave starter kit to player");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Starter kit error: " + e.getMessage());
        }
    }

    public static void sendWelcomeMessage(Player player) {
        String playerName = "Player";
        try {
            playerName = player.getDisplayName();
            if (playerName == null || playerName.isEmpty()) {
                playerName = "Player";
            }
        } catch (Exception ignored) {}

        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("=====================================").color("#9400D3"));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("    WELCOME TO VOIDCRAFT").color("#9400D3").bold(true));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("=====================================").color("#9400D3"));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("  Hello, " + playerName + "!").color("#FFFFFF"));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("  You received a STARTER KIT:").color("#00BFFF"));
        player.sendMessage(Message.raw("    - Wooden Pickaxe").color("#AAAAAA"));
        player.sendMessage(Message.raw("    - Copper Armor Set").color("#AAAAAA"));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("  Useful Commands:").color("#FFD700"));
        player.sendMessage(Message.raw("    /balance - Check your balance").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /sell - Sell your ores").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /shop - Browse the shop").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /enchants - View your enchants").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /stats - View your statistics").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /daily - Claim daily reward").color("#AAAAAA"));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("  Start mining and become rich!").color("#00FF00"));
        player.sendMessage(Message.raw(" ").color("#FFFFFF"));
        player.sendMessage(Message.raw("=====================================").color("#9400D3"));
    }
}
