package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.crates.CrateLocationManager;

import java.util.concurrent.CompletableFuture;

/**
 * /cratedelete --x=10 --y=65 --z=20
 * Removes a crate location at given position.
 */
public class CrateDeleteCommand extends JailbreakCommand {

    private final OptionalArg<Integer> xArg;
    private final OptionalArg<Integer> yArg;
    private final OptionalArg<Integer> zArg;

    public CrateDeleteCommand() {
        super("cratedelete", "Remove a crate location (--x=N --y=N --z=N)");
        requirePermission("voidcraft.admin.crate");
        addAliases("crateremove");
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

        if (!ctx.provided(xArg) || !ctx.provided(yArg) || !ctx.provided(zArg)) {
            sendError(ctx, "Usage: /cratedelete --x=10 --y=65 --z=20");
            return completed();
        }

        int x = ctx.get(xArg);
        int y = ctx.get(yArg);
        int z = ctx.get(zArg);

        CrateLocationManager clm = JailbreakPlugin.getCrateLocationManager();
        if (clm == null) {
            sendError(ctx, "CrateLocationManager not initialized!");
            return completed();
        }

        if (clm.removeLocation(x, y, z)) {
            sendSuccess(ctx, "Crate removed at (" + x + ", " + y + ", " + z + ")");
        } else {
            sendError(ctx, "No crate found at (" + x + ", " + y + ", " + z + ")");
        }

        return completed();
    }
}
