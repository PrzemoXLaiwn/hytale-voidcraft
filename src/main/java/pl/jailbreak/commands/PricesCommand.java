package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.config.EconomyConfig;
import pl.jailbreak.player.PlayerData;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PricesCommand extends JailbreakCommand {

    public PricesCommand() {
        super("prices", "Show ore prices");
        addAliases("ceny", "orelist", "ores");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        PlayerData data = null;
        if (ctx.isPlayer()) data = getPlayerData(ctx);
        String playerSector = data != null ? data.getCurrentSector() : "A";

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "======== ORE PRICES ========");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        double globalMult = EconomyConfig.getGlobalMultiplier();
        if (globalMult != 1.0) {
            ctx.sendMessage(Message.raw("  Global Multiplier: x" + globalMult).color("#FF69B4"));
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        }

        for (Map.Entry<String, Integer> entry : EconomyConfig.getAllPrices().entrySet()) {
            String oreId = entry.getKey();
            int price = entry.getValue();
            String required = EconomyConfig.getRequiredSector(oreId);
            boolean canSell = EconomyConfig.canSellOre(oreId, playerSector);

            String name = formatOreName(oreId);
            String priceStr = "$" + String.format("%,d", price);

            if (canSell) {
                ctx.sendMessage(Message.raw("  " + name + " - " + priceStr + " [Sector " + required + "]").color("#00FF00"));
            } else {
                ctx.sendMessage(Message.raw("  " + name + " - " + priceStr + " [LOCKED - Sector " + required + "]").color("#666666"));
            }
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        if (data != null) {
            int prestige = data.getPrestige();
            int bonus = prestige * EconomyConfig.getPrestigeBonusPercent();
            ctx.sendMessage(Message.raw("  Your sector: " + playerSector + " | Prestige bonus: +" + bonus + "%").color("#00BFFF"));
        }
        sendGold(ctx, "============================");

        return completed();
    }

    private String formatOreName(String itemId) {
        return itemId.replace("Ore_", "").replace("Rock_", "").replace("_", " ");
    }
}
