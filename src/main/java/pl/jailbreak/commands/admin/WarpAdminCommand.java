package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.warp.WarpManager;
import pl.jailbreak.warp.WarpPoint;

import java.util.concurrent.CompletableFuture;

/**
 * Admin command to manage warps
 * Usage: /warpadmin list
 *        /warpadmin set spawn
 *        /warpadmin delete spawn
 */
public class WarpAdminCommand extends JailbreakCommand {

    private final OptionalArg<String> actionArg;
    private final OptionalArg<String> nameArg;
    private final OptionalArg<Double> xArg;
    private final OptionalArg<Double> yArg;
    private final OptionalArg<Double> zArg;

    public WarpAdminCommand() {
        super("warpadmin", "Manage warp points");
        requirePermission("voidcraft.admin.warp");
        addAliases("wa");
        actionArg = withOptionalArg("action", "list/set/delete", ArgTypes.STRING);
        nameArg = withOptionalArg("name", "Warp name", ArgTypes.STRING);
        xArg = withOptionalArg("x", "X coordinate", ArgTypes.DOUBLE);
        yArg = withOptionalArg("y", "Y coordinate", ArgTypes.DOUBLE);
        zArg = withOptionalArg("z", "Z coordinate", ArgTypes.DOUBLE);
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

        // Get action (default to help)
        String action = ctx.provided(actionArg) ? ctx.get(actionArg).toLowerCase() : "help";

        switch (action) {
            case "list":
                sendInfo(ctx, "=== WARP LIST ===");
                for (WarpPoint warp : warpManager.getAllWarps()) {
                    String status = warp.isEnabled() ? "[ON]" : "[OFF]";
                    sendInfo(ctx, status + " " + warp.getName() + " (" +
                        String.format("%.0f, %.0f, %.0f", warp.getX(), warp.getY(), warp.getZ()) + ")");
                }
                sendInfo(ctx, "Total: " + warpManager.getWarpCount() + " warps");
                break;

            case "set":
                if (!ctx.provided(nameArg) || !ctx.provided(xArg) || !ctx.provided(yArg) || !ctx.provided(zArg)) {
                    sendError(ctx, "Usage: /warpadmin set <name> <x> <y> <z>");
                    return completed();
                }
                String setName = ctx.get(nameArg).toLowerCase();
                double x = ctx.get(xArg);
                double y = ctx.get(yArg);
                double z = ctx.get(zArg);

                // Remove old warp if exists
                warpManager.removeWarp(setName);
                warpManager.createWarp(setName, setName, x, y, z);
                sendSuccess(ctx, "Warp '" + setName + "' set to " +
                    String.format("%.1f, %.1f, %.1f", x, y, z));
                break;

            case "delete":
            case "remove":
                if (!ctx.provided(nameArg)) {
                    sendError(ctx, "Usage: /warpadmin delete <name>");
                    return completed();
                }
                String deleteName = ctx.get(nameArg).toLowerCase();
                if (warpManager.removeWarp(deleteName)) {
                    sendSuccess(ctx, "Warp '" + deleteName + "' deleted!");
                } else {
                    sendError(ctx, "Warp '" + deleteName + "' not found!");
                }
                break;

            default:
                sendInfo(ctx, "=== WARP ADMIN ===");
                sendInfo(ctx, "/warpadmin list - Show all warps");
                sendInfo(ctx, "/warpadmin set <name> <x> <y> <z> - Create warp");
                sendInfo(ctx, "/warpadmin delete <name> - Delete warp");
                break;
        }

        return completed();
    }
}
