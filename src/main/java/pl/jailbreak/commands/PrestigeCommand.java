package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.config.EconomyConfig;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

/**
 * /prestige - Shows prestige info
 * Use /prestigeconfirm to actually prestige
 */
public class PrestigeCommand extends JailbreakCommand {

    public PrestigeCommand() {
        super("prestige", "Prestige to reset progress for permanent bonuses");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }

        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No player data!"); return completed(); }

        long cost = EconomyConfig.getPrestigeCost();
        int currentPrestige = data.getPrestige();
        int nextPrestige = currentPrestige + 1;
        int currentBonus = currentPrestige * EconomyConfig.getPrestigeBonusPercent();
        int nextBonus = nextPrestige * EconomyConfig.getPrestigeBonusPercent();

        // Show prestige info
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "======= PRESTIGE =======");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Current Prestige: " + currentPrestige).color("#FF00FF"));
        ctx.sendMessage(Message.raw("  Current Bonus: +" + currentBonus + "% sell value").color("#00FF00"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Next Prestige: " + nextPrestige).color("#FFD700"));
        ctx.sendMessage(Message.raw("  Next Bonus: +" + nextBonus + "% sell value").color("#FFD700"));
        ctx.sendMessage(Message.raw("  Cost: " + formatMoney(cost)).color("#FF6600"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  WHAT RESETS:").color("#FF0000"));
        ctx.sendMessage(Message.raw("  - Balance resets to $0").color("#FF6666"));
        ctx.sendMessage(Message.raw("  - Sectors reset to A").color("#FF6666"));
        ctx.sendMessage(Message.raw("  - Enchants reset").color("#FF6666"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  WHAT YOU KEEP:").color("#00FF00"));
        ctx.sendMessage(Message.raw("  - Achievements").color("#00FF00"));
        ctx.sendMessage(Message.raw("  - Daily Streak").color("#00FF00"));
        ctx.sendMessage(Message.raw("  - Crate Keys").color("#00FF00"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        boolean canPrestige = data.getBalance() >= cost && data.hasSectorUnlocked("J");
        if (canPrestige) {
            ctx.sendMessage(Message.raw("  Type /prestigeconfirm").color("#FFFF00"));
        } else {
            if (data.getBalance() < cost) {
                ctx.sendMessage(Message.raw("  Need " + formatMoney(cost) + " (have " + formatMoney(data.getBalance()) + ")").color("#FF6666"));
            }
            if (!data.hasSectorUnlocked("J")) {
                ctx.sendMessage(Message.raw("  Need to unlock Sector J first!").color("#FF6666"));
            }
        }

        sendGold(ctx, "========================");
        return completed();
    }
}
