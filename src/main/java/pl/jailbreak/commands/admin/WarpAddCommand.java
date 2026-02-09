package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.warp.WarpManager;

import java.util.concurrent.CompletableFuture;

/**
 * /warpadd --name=spawn --x=-187 --y=245 --z=2071
 */
public class WarpAddCommand extends JailbreakCommand {

    private final RequiredArg<String> nameArg;
    private final RequiredArg<Integer> xArg;
    private final RequiredArg<Integer> yArg;
    private final RequiredArg<Integer> zArg;

    public WarpAddCommand() {
        super("warpadd", "Create a warp point");
        requirePermission("voidcraft.admin.warp");
        addAliases("warpset", "setwarp");
        nameArg = withRequiredArg("name", "Warp name", ArgTypes.STRING);
        xArg = withRequiredArg("x", "X coordinate", ArgTypes.INTEGER);
        yArg = withRequiredArg("y", "Y coordinate", ArgTypes.INTEGER);
        zArg = withRequiredArg("z", "Z coordinate", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        Player player = ctx.senderAs(Player.class);
        if (!player.hasPermission("voidcraft.admin.warp")) {
            sendError(ctx, "No permission!");
            return completed();
        }

        WarpManager warpManager = JailbreakPlugin.getWarpManager();

        String name = ctx.get(nameArg).toLowerCase();
        int x = ctx.get(xArg);
        int y = ctx.get(yArg);
        int z = ctx.get(zArg);

        warpManager.removeWarp(name);
        warpManager.createWarp(name, name, x, y, z);
        sendSuccess(ctx, "Warp '" + name + "' set to " + x + ", " + y + ", " + z);

        return completed();
    }
}
