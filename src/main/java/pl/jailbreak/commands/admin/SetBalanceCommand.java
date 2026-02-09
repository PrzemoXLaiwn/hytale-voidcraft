package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.player.PlayerData;
import java.util.concurrent.CompletableFuture;

public class SetBalanceCommand extends JailbreakCommand {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> amountArg;

    public SetBalanceCommand() {
        super("setbal", "Set player balance");
        requirePermission("voidcraft.admin.setbal");
        playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "New balance", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String name = ctx.get(playerArg);
        int amount = ctx.get(amountArg);

        if (name == null || name.trim().isEmpty()) {
            sendError(ctx, "Player name cannot be empty!");
            return completed();
        }

        if (amount < 0) {
            sendError(ctx, "Balance cannot be negative!");
            return completed();
        }

        // Try exact match first, then partial match
        PlayerData target = getPlayerManager().getPlayerByName(name);
        if (target == null) {
            // Try partial match (case insensitive)
            for (pl.jailbreak.player.PlayerData p : getPlayerManager().getAllPlayers()) {
                if (p.getName().toLowerCase().contains(name.toLowerCase())) {
                    target = p;
                    break;
                }
            }
        }
        if (target == null) {
            sendError(ctx, "Player '" + name + "' not found! (Try partial name or check if online)");
            return completed();
        }

        long old = target.getBalance();
        target.setBalance(amount);
        getPlayerManager().savePlayer(target.getUuid());
        sendSuccess(ctx, name + ": " + formatMoney(old) + " -> " + formatMoney(amount));
        return completed();
    }
}