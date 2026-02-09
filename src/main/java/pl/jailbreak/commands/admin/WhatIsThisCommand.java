package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import pl.jailbreak.commands.JailbreakCommand;
import java.util.concurrent.CompletableFuture;

public class WhatIsThisCommand extends JailbreakCommand {
    public WhatIsThisCommand() {
        super("jwhat", "Show item in hand");
        requirePermission("voidcraft.admin");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        try {
            Player player = ctx.senderAs(Player.class);
            Inventory inventory = player.getInventory();
            ItemStack inHand = inventory.getItemInHand();
            
            if (inHand == null || !inHand.isValid()) {
                sendError(ctx, "Nothing in hand!");
                return completed();
            }
            
            sendGold(ctx, "=== ITEM INFO ===");
            ctx.sendMessage(Message.raw("ID: " + inHand.getItemId()).color("#00FFFF"));
            ctx.sendMessage(Message.raw("Quantity: " + inHand.getQuantity()).color("#AAAAAA"));
            ctx.sendMessage(Message.raw("Durability: " + inHand.getDurability() + "/" + inHand.getMaxDurability()).color("#AAAAAA"));
            sendGold(ctx, "=================");
            
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }

        return completed();
    }
}