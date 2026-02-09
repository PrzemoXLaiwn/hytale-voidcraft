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
 * /warpdelete --name=spawn
 */
public class WarpDeleteCommand extends JailbreakCommand {

    private final RequiredArg<String> nameArg;

    public WarpDeleteCommand() {
        super("warpdelete", "Delete a warp point");
        requirePermission("voidcraft.admin.warp");
        addAliases("warpremove", "delwarp");
        nameArg = withRequiredArg("name", "Warp name", ArgTypes.STRING);
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

        if (warpManager.removeWarp(name)) {
            sendSuccess(ctx, "Warp '" + name + "' deleted!");
        } else {
            sendError(ctx, "Warp '" + name + "' not found!");
        }

        return completed();
    }
}
