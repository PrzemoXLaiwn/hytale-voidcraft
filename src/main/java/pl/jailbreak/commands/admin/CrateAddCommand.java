package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.crates.CrateLocationManager;
import pl.jailbreak.crates.CrateManager;

import java.util.concurrent.CompletableFuture;

/**
 * /crateadd --type=common --x=10 --y=65 --z=20
 * Registers a chest at given position as a crate of given type.
 */
public class CrateAddCommand extends JailbreakCommand {

    private final OptionalArg<String> typeArg;
    private final OptionalArg<Integer> xArg;
    private final OptionalArg<Integer> yArg;
    private final OptionalArg<Integer> zArg;

    public CrateAddCommand() {
        super("crateadd", "Register a chest as a crate (--type=common --x=N --y=N --z=N)");
        requirePermission("voidcraft.admin.crate");
        typeArg = withOptionalArg("type", "Crate type (common/rare/epic/legendary/void)", ArgTypes.STRING);
        xArg = withOptionalArg("x", "X coordinate", ArgTypes.INTEGER);
        yArg = withOptionalArg("y", "Y coordinate", ArgTypes.INTEGER);
        zArg = withOptionalArg("z", "Z coordinate", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!ctx.provided(typeArg) || !ctx.provided(xArg) || !ctx.provided(yArg) || !ctx.provided(zArg)) {
            sendError(ctx, "Usage: /crateadd --type=common --x=10 --y=65 --z=20");
            sendInfo(ctx, "Types: common, rare, epic, legendary, void");
            return completed();
        }

        String type = ctx.get(typeArg).toLowerCase();
        int x = ctx.get(xArg);
        int y = ctx.get(yArg);
        int z = ctx.get(zArg);

        if (!CrateManager.isValidCrate(type)) {
            sendError(ctx, "Invalid crate type! Use: common, rare, epic, legendary, void");
            return completed();
        }

        CrateLocationManager clm = JailbreakPlugin.getCrateLocationManager();
        if (clm == null) {
            sendError(ctx, "CrateLocationManager not initialized!");
            return completed();
        }

        clm.addLocation(x, y, z, type);
        String color = CrateManager.getColor(type);
        sendSuccess(ctx, "Crate added: " + CrateManager.getDisplayName(type) + " at (" + x + ", " + y + ", " + z + ")");

        return completed();
    }
}
