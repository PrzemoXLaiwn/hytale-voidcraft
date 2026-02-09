package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import pl.jailbreak.commands.JailbreakCommand;
import java.util.concurrent.CompletableFuture;

public class DebugInventoryCommand extends JailbreakCommand {
    public DebugInventoryCommand() {
        super("jdebug", "Debug inventory items");
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
            
            sendGold(ctx, "=== HOTBAR ===");
            debugContainer(ctx, inventory.getHotbar(), "Hotbar");
            
            sendGold(ctx, "=== STORAGE ===");
            debugContainer(ctx, inventory.getStorage(), "Storage");
            
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }

        return completed();
    }
    
    private void debugContainer(CommandContext ctx, ItemContainer container, String name) {
        short capacity = container.getCapacity();
        int found = 0;
        
        for (short slot = 0; slot < capacity; slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack == null) continue;
            if (!stack.isValid()) continue;
            
            String itemId = stack.getItemId();
            int qty = stack.getQuantity();
            
            ctx.sendMessage(Message.raw("Slot " + slot + ": " + itemId + " x" + qty).color("#00FFFF"));
            found++;
        }
        
        if (found == 0) {
            ctx.sendMessage(Message.raw("Empty").color("#888888"));
        }
    }
}