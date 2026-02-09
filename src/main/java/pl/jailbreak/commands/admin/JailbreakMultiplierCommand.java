package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.config.EconomyConfig;
import java.util.concurrent.CompletableFuture;

public class JailbreakMultiplierCommand extends JailbreakCommand {
    private final OptionalArg<Double> valueArg;

    public JailbreakMultiplierCommand() {
        super("jmultiplier", "Get/set sell multiplier");
        requirePermission("voidcraft.admin");
        valueArg = withOptionalArg("value", "Multiplier value", ArgTypes.DOUBLE);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (ctx.provided(valueArg)) {
            double value = ctx.get(valueArg);
            if (value < 0.1 || value > 100) {
                sendError(ctx, "Multiplier must be between 0.1 and 100!");
                return completed();
            }
            EconomyConfig.setGlobalMultiplier(value);
            sendSuccess(ctx, "Set multiplier to x" + value);
        } else {
            sendInfo(ctx, "Current multiplier: x" + EconomyConfig.getGlobalMultiplier());
        }
        return completed();
    }
}