package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

import java.util.concurrent.CompletableFuture;

/**
 * /setsector --sector=B --x=-188 --y=245 --z=2071
 */
public class SetSectorCommand extends JailbreakCommand {

    private final OptionalArg<String> sectorArg;
    private final OptionalArg<Integer> xArg;
    private final OptionalArg<Integer> yArg;
    private final OptionalArg<Integer> zArg;

    public SetSectorCommand() {
        super("setsector", "Set sector teleport coords (--sector=B --x=0 --y=64 --z=0)");
        requirePermission("voidcraft.admin.setsector");
        sectorArg = withOptionalArg("sector", "Sector ID (A-J)", ArgTypes.STRING);
        xArg = withOptionalArg("x", "X coord", ArgTypes.INTEGER);
        yArg = withOptionalArg("y", "Y coord", ArgTypes.INTEGER);
        zArg = withOptionalArg("z", "Z coord", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.provided(sectorArg) || !ctx.provided(xArg) || !ctx.provided(yArg) || !ctx.provided(zArg)) {
            sendError(ctx, "Usage: /setsector --sector=B --x=-188 --y=245 --z=2071");
            return completed();
        }

        String sectorId = ctx.get(sectorArg).toUpperCase();

        if (!SectorManager.exists(sectorId)) {
            sendError(ctx, "Sector " + sectorId + " does not exist! Use A-J");
            return completed();
        }

        int x = ctx.get(xArg);
        int y = ctx.get(yArg);
        int z = ctx.get(zArg);

        SectorManager.setCoords(sectorId, x, y, z);

        Sector sector = SectorManager.get(sectorId);
        sendSuccess(ctx, "Sector " + sectorId + " (" + sector.name + ") coords set!");
        sendInfo(ctx, "Coords: " + x + ", " + y + ", " + z);

        return completed();
    }
}
