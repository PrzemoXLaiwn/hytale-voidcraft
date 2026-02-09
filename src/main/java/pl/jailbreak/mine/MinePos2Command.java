package pl.jailbreak.mine;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.concurrent.CompletableFuture;

/**
 * /minepos2 --x=150 --y=80 --z=250
 */
public class MinePos2Command extends JailbreakCommand {

    private final OptionalArg<Integer> xArg;
    private final OptionalArg<Integer> yArg;
    private final OptionalArg<Integer> zArg;

    public MinePos2Command() {
        super("minepos2", "Set mine corner 2 (--x=N --y=N --z=N)");
        requirePermission("voidcraft.admin.mine");
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
            sendError(ctx, "Usage: /minepos2 --x=150 --y=80 --z=250");
            return completed();
        }

        int x = ctx.get(xArg);
        int y = ctx.get(yArg);
        int z = ctx.get(zArg);

        MinePositions.setPos2(new Vector3i(x, y, z));
        sendSuccess(ctx, "Pos2 set to: " + x + ", " + y + ", " + z);

        if (MinePositions.getPos1() != null) {
            sendInfo(ctx, "Both positions set! Use /minecreate --name=X --sector=B");
        }

        return completed();
    }
}
