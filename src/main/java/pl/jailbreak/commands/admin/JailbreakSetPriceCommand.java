package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.config.EconomyConfig;
import java.util.concurrent.CompletableFuture;

public class JailbreakSetPriceCommand extends JailbreakCommand {
    private final RequiredArg<String> oreArg;
    private final RequiredArg<Integer> priceArg;

    public JailbreakSetPriceCommand() {
        super("jsetprice", "Set ore price");
        requirePermission("voidcraft.admin");
        oreArg = withRequiredArg("ore", "Ore name", ArgTypes.STRING);
        priceArg = withRequiredArg("price", "Price", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String ore = ctx.get(oreArg);
        int price = ctx.get(priceArg);

        if (ore == null || ore.trim().isEmpty()) {
            sendError(ctx, "Ore name cannot be empty!");
            return completed();
        }

        if (price < 0) {
            sendError(ctx, "Price cannot be negative!");
            return completed();
        }

        if (price > 1000000000) {
            sendError(ctx, "Price too high! Max: 1,000,000,000");
            return completed();
        }

        EconomyConfig.setOrePrice(ore, price);
        sendSuccess(ctx, "Set " + ore + " price to $" + String.format("%,d", price));
        return completed();
    }
}