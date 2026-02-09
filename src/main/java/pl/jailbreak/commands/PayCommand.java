package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

public class PayCommand extends JailbreakCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> amountArg;

    public PayCommand() {
        super("pay", "Send money to another player");
        addAliases("transfer", "send");
        playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to send", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }
        if (!checkCooldown(ctx, 3)) return completed();

        PlayerData sender = getPlayerData(ctx);
        if (sender == null) { sendError(ctx, "No player data!"); return completed(); }

        String targetName = ctx.get(playerArg);
        long amount = ctx.get(amountArg).longValue();

        if (amount <= 0) {
            sendError(ctx, "Amount must be positive!");
            return completed();
        }

        if (amount < 10) {
            sendError(ctx, "Minimum transfer: $10");
            return completed();
        }

        if (targetName.equalsIgnoreCase(sender.getName())) {
            sendError(ctx, "You can't pay yourself!");
            return completed();
        }

        PlayerData target = getPlayerManager().getPlayerByName(targetName);
        if (target == null) {
            sendError(ctx, "Player '" + targetName + "' not found!");
            sendInfo(ctx, "Player must be online.");
            return completed();
        }

        if (sender.getBalance() < amount) {
            sendError(ctx, "Not enough money!");
            ctx.sendMessage(Message.raw("  Have: " + formatMoney(sender.getBalance())).color("#FF6666"));
            ctx.sendMessage(Message.raw("  Need: " + formatMoney(amount)).color("#FF6666"));
            return completed();
        }

        // Transfer
        sender.removeBalance(amount);
        target.addBalance(amount);
        getPlayerManager().savePlayer(sender.getUuid());
        getPlayerManager().savePlayer(target.getUuid());

        // Notify sender
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "====== TRANSFER ======");
        ctx.sendMessage(Message.raw("  Sent " + formatMoney(amount) + " to " + target.getName()).color("#00FF00"));
        ctx.sendMessage(Message.raw("  Your balance: " + formatMoney(sender.getBalance())).color("#00BFFF"));
        sendGold(ctx, "======================");

        System.out.println("[Voidcraft] " + sender.getName() + " paid " + target.getName() + " $" + amount);

        return completed();
    }
}
