package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import pl.jailbreak.config.EconomyConfig;
import pl.jailbreak.enchants.PickaxeEnchantUtil;
import pl.jailbreak.enchants.PlayerEnchants;
import pl.jailbreak.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SellCommand extends JailbreakCommand {

    public SellCommand() {
        super("sell", "Sell ores from inventory");
        addAliases("sprzedaj", "sellall");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!checkCooldown(ctx, 2)) return completed();

        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        try {
            Player player = ctx.senderAs(Player.class);
            Inventory inventory = player.getInventory();
            String playerSector = data.getCurrentSector();

            Map<String, Integer> soldItems = new HashMap<>();
            Map<String, Integer> blockedItems = new HashMap<>();
            long totalValue = 0;

            // Scan hotbar and storage
            totalValue += scanAndSell(inventory.getHotbar(), soldItems, blockedItems, playerSector);
            totalValue += scanAndSell(inventory.getStorage(), soldItems, blockedItems, playerSector);

            // Show blocked items first
            if (!blockedItems.isEmpty()) {
                sendError(ctx, "BLOCKED (unlock sector first):");
                for (Map.Entry<String, Integer> entry : blockedItems.entrySet()) {
                    String required = EconomyConfig.getRequiredSector(entry.getKey());
                    ctx.sendMessage(Message.raw("  " + formatOreName(entry.getKey()) + " x" + entry.getValue() + " (needs sector " + required + ")").color("#FF6666"));
                }
            }

            if (soldItems.isEmpty()) {
                if (blockedItems.isEmpty()) {
                    sendError(ctx, "Nothing to sell!");
                    sendInfo(ctx, "Mine some ores first!");
                }
                return completed();
            }

            // Apply enchant bonuses from held pickaxe
            ItemStack heldPickaxe = PickaxeEnchantUtil.getHeldPickaxe(player);
            PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(heldPickaxe);

            // Fortune bonus (+10% per level, max 50%)
            double fortuneMultiplier = enchants.getFortuneMultiplier();
            totalValue = (long)(totalValue * fortuneMultiplier);

            // Luck bonus (chance for x2 money)
            double luckChance = enchants.getLuckChance();
            boolean luckyDouble = luckChance > 0 && Math.random() < luckChance;
            if (luckyDouble) {
                totalValue *= 2;
            }

            // Apply prestige bonus
            int prestigeBonus = data.getPrestige() * EconomyConfig.getPrestigeBonusPercent();
            long bonusValue = (totalValue * prestigeBonus) / 100;
            long finalValue = totalValue + bonusValue;

            // Apply global multiplier
            finalValue = (long)(finalValue * EconomyConfig.getGlobalMultiplier());

            // Add money
            data.addBalance(finalValue);
            getPlayerManager().savePlayer(data.getUuid());

            // Send result
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            sendGold(ctx, "========== SOLD ==========");
            int totalItems = 0;
            for (Map.Entry<String, Integer> entry : soldItems.entrySet()) {
                int price = EconomyConfig.getOrePrice(entry.getKey());
                long itemTotal = (long) entry.getValue() * price;
                totalItems += entry.getValue();
                ctx.sendMessage(Message.raw("  " + formatOreName(entry.getKey()) + " x" + entry.getValue() + " = $" + String.format("%,d", itemTotal)).color("#AAAAAA"));
            }
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            if (fortuneMultiplier > 1.0) {
                ctx.sendMessage(Message.raw("  Fortune +" + (int)((fortuneMultiplier - 1) * 100) + "%").color("#FFD700"));
            }
            if (luckyDouble) {
                ctx.sendMessage(Message.raw("  LUCKY x2!").color("#FF69B4"));
            }
            if (prestigeBonus > 0) {
                ctx.sendMessage(Message.raw("  Prestige +" + prestigeBonus + "%: +$" + String.format("%,d", bonusValue)).color("#FF00FF"));
            }
            ctx.sendMessage(Message.raw("  Items sold: " + totalItems).color("#AAAAAA"));
            ctx.sendMessage(Message.raw("  Earned: $" + String.format("%,d", finalValue)).color("#00FF00"));
            ctx.sendMessage(Message.raw("  Balance: $" + String.format("%,d", data.getBalance())).color("#00BFFF"));
            sendGold(ctx, "==========================");

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }

        return completed();
    }

    private String formatOreName(String itemId) {
        return itemId.replace("Ore_", "").replace("Rock_", "").replace("_", " ");
    }

    private long scanAndSell(ItemContainer container, Map<String, Integer> soldItems, Map<String, Integer> blockedItems, String playerSector) {
        long value = 0;
        short capacity = container.getCapacity();

        for (short slot = 0; slot < capacity; slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack == null || !stack.isValid()) continue;

            String itemId = stack.getItemId();
            if (itemId == null) continue;

            int price = EconomyConfig.getOrePrice(itemId);
            if (price <= 0) continue;

            int quantity = stack.getQuantity();

            // Check if player can sell this ore
            if (EconomyConfig.canSellOre(itemId, playerSector)) {
                value += (long) price * quantity;
                soldItems.merge(itemId, quantity, Integer::sum);
                container.removeItemStackFromSlot(slot);
            } else {
                // Blocked - player doesn't have required sector
                blockedItems.merge(itemId, quantity, Integer::sum);
            }
        }
        return value;
    }
}
