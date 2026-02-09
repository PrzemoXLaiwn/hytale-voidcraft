package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;
import java.util.concurrent.CompletableFuture;

public class BalanceCommand extends JailbreakCommand {
    public BalanceCommand() {
        super("bal", "Check your balance");
        addAliases("balance", "money", "cash");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }
        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No data!"); return completed(); }
        ctx.sendMessage(Message.raw(" "));
        sendGold(ctx, "=== YOUR BALANCE ===");
        ctx.sendMessage(Message.raw("Money: " + formatMoney(data.getBalance())).color(COLOR_MONEY));
        if (data.getPrestige() > 0) {
            ctx.sendMessage(Message.raw("Prestige bonus: +" + (data.getPrestige() * 5) + "%").color("#FF00FF"));
        }
        sendGold(ctx, "====================");
        return completed();
    }
}