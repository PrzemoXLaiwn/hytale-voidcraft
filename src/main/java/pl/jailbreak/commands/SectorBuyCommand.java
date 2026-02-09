package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

import java.util.concurrent.CompletableFuture;

public class SectorBuyCommand extends JailbreakCommand {

    public SectorBuyCommand() {
        super("sectorbuy", "Buy next sector");
        addAliases("buysector", "buymine");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        Sector next = SectorManager.getNext(data.getCurrentSector());

        if (next == null) {
            sendError(ctx, "You already own all sectors!");
            return completed();
        }

        // Check if sector has mines created
        if (JailbreakPlugin.getMineManager() != null
                && JailbreakPlugin.getMineManager().getMinesForSector(next.id).isEmpty()) {
            sendError(ctx, "Sector " + next.id + " is not available yet!");
            ctx.sendMessage(Message.raw("No mines have been created for it.").color("#FF6666"));
            return completed();
        }

        if (data.getBalance() < next.price) {
            sendError(ctx, "Not enough money!");
            ctx.sendMessage(Message.raw("Need: " + formatMoney(next.price)).color("#FF6600"));
            ctx.sendMessage(Message.raw("Have: " + formatMoney(data.getBalance())).color("#AAAAAA"));
            return completed();
        }

        data.setBalance(data.getBalance() - next.price);
        data.setCurrentSector(next.id);

        sendGold(ctx, "=== SECTOR PURCHASED ===");
        ctx.sendMessage(Message.raw("Unlocked: " + next.id + " - " + next.name).color("#00FF00").bold(true));
        ctx.sendMessage(Message.raw("Paid: " + formatMoney(next.price)).color("#FF6600"));
        ctx.sendMessage(Message.raw("Balance: " + formatMoney(data.getBalance())).color("#AAAAAA"));
        sendInfo(ctx, "Use /sectortp " + next.id + " to teleport!");
        sendGold(ctx, "========================");

        getPlayerManager().savePlayer(data.getUuid());
        return completed();
    }
}