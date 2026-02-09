package pl.jailbreak.mine;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.concurrent.CompletableFuture;

/**
 * /minecreate --name=ironmine --sector=B
 */
public class MineCreateCommand extends JailbreakCommand {

    private final MineManager mineManager;
    private final OptionalArg<String> nameArg;
    private final OptionalArg<String> sectorArg;

    public MineCreateCommand(MineManager mineManager) {
        super("minecreate", "Create a mine (--name=X --sector=B)");
        requirePermission("voidcraft.admin.mine");
        this.mineManager = mineManager;
        nameArg = withOptionalArg("name", "Mine name", ArgTypes.STRING);
        sectorArg = withOptionalArg("sector", "Sector A-J", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!ctx.provided(nameArg) || !ctx.provided(sectorArg)) {
            sendError(ctx, "Usage: /minecreate --name=ironmine --sector=B");
            return completed();
        }

        String name = ctx.get(nameArg);
        String sector = ctx.get(sectorArg).toUpperCase();

        if (!MineConfig.sectorExists(sector)) {
            sendError(ctx, "Invalid sector! Use: A, B, C, D, E, F, G, H, I, J");
            return completed();
        }

        if (!MinePositions.bothSet()) {
            sendError(ctx, "Set pos1 and pos2 first!");
            sendInfo(ctx, "Use /minepos1 --x=N --y=N --z=N and /minepos2 --x=N --y=N --z=N");
            return completed();
        }

        if (mineManager.getMine(name) != null) {
            sendError(ctx, "Mine '" + name + "' already exists!");
            return completed();
        }

        boolean created = mineManager.createMine(name, sector, MinePositions.getPos1(), MinePositions.getPos2());
        if (created) {
            Mine mine = mineManager.getMine(name);
            sendSuccess(ctx, "Mine '" + name + "' created for sector " + sector);
            sendInfo(ctx, "Size: " + mine.getBlockCount() + " blocks");
            sendInfo(ctx, "Use /mineregen --name=" + name + " to fill it with ores");
            MinePositions.clear();
        } else {
            sendError(ctx, "Failed to create mine!");
        }

        return completed();
    }
}
