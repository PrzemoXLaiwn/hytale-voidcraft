package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

import java.util.concurrent.CompletableFuture;

public class RankupCommand extends JailbreakCommand {

    public RankupCommand() {
        super("rankup", "Buy the next sector");
        addAliases("ru", "nextmine", "upgrade");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }
        if (!checkCooldown(ctx, 2)) return completed();

        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No player data!"); return completed(); }

        Sector next = SectorManager.getNext(data.getCurrentSector());

        if (next == null) {
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            sendGold(ctx, "====== MAX SECTOR ======");
            ctx.sendMessage(Message.raw("  You own all sectors!").color("#FF00FF"));
            ctx.sendMessage(Message.raw("  Try /prestige for bonuses!").color("#00BFFF"));
            sendGold(ctx, "========================");
            return completed();
        }

        // Check if next sector has mines created
        if (JailbreakPlugin.getMineManager() != null
                && JailbreakPlugin.getMineManager().getMinesForSector(next.id).isEmpty()) {
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            sendGold(ctx, "======= RANKUP =======");
            ctx.sendMessage(Message.raw("  Sector " + next.id + " - " + next.name).color("#FFFF00"));
            ctx.sendMessage(Message.raw("  This sector is not available yet!").color("#FF6666"));
            ctx.sendMessage(Message.raw("  No mines have been created for it.").color("#FF6666"));
            sendGold(ctx, "======================");
            return completed();
        }

        if (data.getBalance() < next.price) {
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            sendGold(ctx, "======= RANKUP =======");
            ctx.sendMessage(Message.raw("  Next: " + next.id + " - " + next.name).color("#FFFF00"));
            ctx.sendMessage(Message.raw("  Cost: " + formatMoney(next.price)).color("#FF6600"));
            ctx.sendMessage(Message.raw("  Have: " + formatMoney(data.getBalance())).color("#FF6666"));
            long needed = next.price - data.getBalance();
            ctx.sendMessage(Message.raw("  Need: " + formatMoney(needed) + " more").color("#FF0000"));
            sendGold(ctx, "======================");
            return completed();
        }

        // Buy it
        data.setBalance(data.getBalance() - next.price);
        data.unlockSector(next.id);
        getPlayerManager().savePlayer(data.getUuid());

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("*****************************").color("#00FF00"));
        ctx.sendMessage(Message.raw("     RANKUP! Sector " + next.id).color("#FFD700"));
        ctx.sendMessage(Message.raw("     " + next.name).color("#FFFF00"));
        ctx.sendMessage(Message.raw("*****************************").color("#00FF00"));
        ctx.sendMessage(Message.raw("  New ores: " + next.ores).color("#00BFFF"));
        ctx.sendMessage(Message.raw("  Balance: " + formatMoney(data.getBalance())).color("#AAAAAA"));

        Sector nextNext = SectorManager.getNext(next.id);
        if (nextNext != null) {
            ctx.sendMessage(Message.raw("  Next rankup: " + nextNext.id + " - " + formatMoney(nextNext.price)).color("#666666"));
        } else {
            ctx.sendMessage(Message.raw("  MAX SECTOR! Try /prestige").color("#FF00FF"));
        }
        ctx.sendMessage(Message.raw("*****************************").color("#00FF00"));

        System.out.println("[Voidcraft] " + data.getName() + " ranked up to sector " + next.id);
        return completed();
    }
}
