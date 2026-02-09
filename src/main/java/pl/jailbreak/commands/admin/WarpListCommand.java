package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.warp.WarpManager;
import pl.jailbreak.warp.WarpPoint;

import java.util.concurrent.CompletableFuture;

/**
 * /warplist - Show all warps
 */
public class WarpListCommand extends JailbreakCommand {

    public WarpListCommand() {
        super("warplist", "List all warp points");
        requirePermission("voidcraft.admin.warp");
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

        sendGold(ctx, "=== WARP LIST ===");
        for (WarpPoint warp : warpManager.getAllWarps()) {
            String status = warp.isEnabled() ? "[ON]" : "[OFF]";
            sendInfo(ctx, status + " " + warp.getName() + " (" +
                String.format("%.0f, %.0f, %.0f", warp.getX(), warp.getY(), warp.getZ()) + ")");
        }
        sendInfo(ctx, "Total: " + warpManager.getWarpCount() + " warps");
        sendGold(ctx, "=================");

        return completed();
    }
}
