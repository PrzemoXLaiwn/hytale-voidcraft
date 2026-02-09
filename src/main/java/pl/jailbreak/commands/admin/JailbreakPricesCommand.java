package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.config.EconomyConfig;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JailbreakPricesCommand extends JailbreakCommand {
    public JailbreakPricesCommand() {
        super("jprices", "Show all ore prices");
        requirePermission("voidcraft.admin");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        sendGold(ctx, "=== ORE PRICES ===");
        for (Map.Entry<String, Integer> entry : EconomyConfig.getAllPrices().entrySet()) {
            ctx.sendMessage(Message.raw(entry.getKey() + ": " + entry.getValue() + " coins").color("#00FF00"));
        }
        ctx.sendMessage(Message.raw("Multiplier: x" + EconomyConfig.getGlobalMultiplier()).color("#FFFF00"));
        sendGold(ctx, "==================");
        return completed();
    }
}