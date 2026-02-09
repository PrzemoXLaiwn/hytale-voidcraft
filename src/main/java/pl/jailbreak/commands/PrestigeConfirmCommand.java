package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.config.EconomyConfig;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

/**
 * /prestigeconfirm - Actually perform prestige
 */
public class PrestigeConfirmCommand extends JailbreakCommand {

    public PrestigeConfirmCommand() {
        super("prestigeconfirm", "Confirm prestige and reset progress");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }
        if (!checkCooldown(ctx, 3)) return completed();

        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No player data!"); return completed(); }

        long cost = EconomyConfig.getPrestigeCost();

        // Check requirements
        if (data.getBalance() < cost) {
            sendError(ctx, "Not enough money! Need " + formatMoney(cost) + " (have " + formatMoney(data.getBalance()) + ")");
            return completed();
        }

        if (!data.hasSectorUnlocked("J")) {
            sendError(ctx, "You need to unlock Sector J first!");
            return completed();
        }

        // Perform prestige
        int oldPrestige = data.getPrestige();
        int newPrestige = oldPrestige + 1;
        int newBonus = newPrestige * EconomyConfig.getPrestigeBonusPercent();

        data.resetForPrestige();
        getPlayerManager().savePlayer(data.getUuid());

        // Success message
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("*****************************").color("#FF00FF"));
        ctx.sendMessage(Message.raw("     PRESTIGE " + newPrestige + "!").color("#FFD700"));
        ctx.sendMessage(Message.raw("*****************************").color("#FF00FF"));
        ctx.sendMessage(Message.raw("  Sell bonus: +" + newBonus + "%").color("#00FF00"));
        ctx.sendMessage(Message.raw("  Balance reset to $0").color("#FF6666"));
        ctx.sendMessage(Message.raw("  Sectors reset to A").color("#FF6666"));
        ctx.sendMessage(Message.raw("  Enchants reset").color("#FF6666"));
        ctx.sendMessage(Message.raw("*****************************").color("#FF00FF"));

        System.out.println("[Voidcraft] " + data.getName() + " prestiged to level " + newPrestige);
        return completed();
    }
}
