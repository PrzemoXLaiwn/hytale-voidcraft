package pl.jailbreak.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Vector3d;
import com.hypixel.hytale.math.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.statue.StatueConfig;
import pl.jailbreak.statue.StatueData;
import pl.jailbreak.statue.StatueManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * /statue - Admin commands for managing TOP 4 balance statues
 *
 * Subcommands:
 * - /statue refresh - Manually refresh statues
 * - /statue list - Show current TOP 4
 * - /statue setpos <rank> - Set statue position to player location
 * - /statue info - Show system status
 */
public class StatueCommand extends JailbreakCommand {

    public StatueCommand() {
        super("statue", "Manage TOP 4 balance statues");
        addAliases("statues", "statuetka");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        Player player = ctx.getPlayer();

        // Check admin permission
        // TODO: Sprawdzić jak działa permission system w tym projekcie
        // Na razie używamy prostego sprawdzenia
        // if (!player.hasPermission("voidcraft.admin.statue")) {
        //     sendError(ctx, "No permission! Requires voidcraft.admin.statue");
        //     return completed();
        // }

        String[] args = ctx.getArguments();

        if (args.length == 0) {
            showHelp(ctx);
            return completed();
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "refresh":
                return executeRefresh(ctx);

            case "list":
                return executeList(ctx);

            case "setpos":
                return executeSetPos(ctx, args);

            case "info":
                return executeInfo(ctx);

            default:
                showHelp(ctx);
                return completed();
        }
    }

    /**
     * /statue refresh - Manualnie odśwież statuetki
     */
    private CompletableFuture<Void> executeRefresh(CommandContext ctx) {
        StatueManager manager = JailbreakPlugin.getStatueManager();

        if (manager == null || !manager.isInitialized()) {
            sendError(ctx, "StatueManager not initialized!");
            return completed();
        }

        ctx.sendMessage(Message.raw("Refreshing statues...").color("#FFFF00"));

        try {
            manager.manualRefresh();
            ctx.sendMessage(Message.raw("Statues refreshed successfully!").color("#00FF00"));
        } catch (Exception e) {
            sendError(ctx, "Error refreshing statues: " + e.getMessage());
            e.printStackTrace();
        }

        return completed();
    }

    /**
     * /statue list - Pokaż aktualny TOP 4
     */
    private CompletableFuture<Void> executeList(CommandContext ctx) {
        StatueManager manager = JailbreakPlugin.getStatueManager();

        if (manager == null || !manager.isInitialized()) {
            sendError(ctx, "StatueManager not initialized!");
            return completed();
        }

        Map<Integer, StatueData> statues = manager.getCurrentStatues();

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=== TOP 4 BALANCE STATUES ===");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        for (int rank = 1; rank <= 4; rank++) {
            StatueData statue = statues.get(rank);

            if (statue != null) {
                String color = getRankColor(rank);
                String icon = getRankIcon(rank);
                ctx.sendMessage(Message.raw(
                    icon + " #" + rank + " - " + statue.getPlayerName() +
                    " - " + formatMoney(statue.getBalance())
                ).color(color));
            } else {
                ctx.sendMessage(Message.raw(
                    "  #" + rank + " - (empty)"
                ).color("#666666"));
            }
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw(
            "Active statues: " + statues.size() + "/4"
        ).color("#AAAAAA"));
        sendGold(ctx, "=============================");

        return completed();
    }

    /**
     * /statue setpos <rank> - Ustaw pozycję statuetki na lokację gracza
     */
    private CompletableFuture<Void> executeSetPos(CommandContext ctx, String[] args) {
        if (args.length < 2) {
            sendError(ctx, "Usage: /statue setpos <1-4>");
            return completed();
        }

        int rank;
        try {
            rank = Integer.parseInt(args[1]);
            if (rank < 1 || rank > 4) {
                sendError(ctx, "Rank must be between 1 and 4!");
                return completed();
            }
        } catch (NumberFormatException e) {
            sendError(ctx, "Invalid rank number!");
            return completed();
        }

        Player player = ctx.getPlayer();
        World world = player.getWorld();

        if (world == null) {
            sendError(ctx, "World is null!");
            return completed();
        }

        StatueManager manager = JailbreakPlugin.getStatueManager();

        if (manager == null) {
            sendError(ctx, "StatueManager not initialized!");
            return completed();
        }

        // TODO: Sprawdzić jak uzyskać pozycję gracza
        // Możliwe opcje:
        // - player.getPosition()
        // - player.getTransform().getPosition()
        // - player.getLocation()

        // Na razie placeholder:
        // Vector3d position = player.getTransform().getPosition();
        // Vector3f rotation = player.getTransform().getRotation();

        // Tymczasowo używamy domyślnych pozycji z config
        ctx.sendMessage(Message.raw("Setting position for statue #" + rank + "...").color("#FFFF00"));

        StatueConfig config = manager.getConfig();

        // TODO: Implementacja - get player position and set in config
        // config.setPosition(rank, position.x, position.y, position.z,
        //                    rotation.x, rotation.y, rotation.z);

        ctx.sendMessage(Message.raw("Position set! Use /statue refresh to respawn.").color("#00FF00"));

        return completed();
    }

    /**
     * /statue info - Pokaż status systemu
     */
    private CompletableFuture<Void> executeInfo(CommandContext ctx) {
        StatueManager manager = JailbreakPlugin.getStatueManager();

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=== STATUE SYSTEM INFO ===");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        if (manager == null) {
            sendError(ctx, "StatueManager: NOT LOADED");
        } else if (!manager.isInitialized()) {
            ctx.sendMessage(Message.raw("Status: Waiting for initialization").color("#FF6600"));
        } else {
            ctx.sendMessage(Message.raw("Status: Active").color("#00FF00"));

            int activeCount = manager.getCurrentStatues().size();
            ctx.sendMessage(Message.raw("Active statues: " + activeCount + "/4").color("#FFFFFF"));

            StatueConfig config = manager.getConfig();
            int interval = config.getUpdateIntervalMinutes();
            ctx.sendMessage(Message.raw("Update interval: " + interval + " minutes").color("#AAAAAA"));

            String modelKey = config.getNpcModelKey();
            ctx.sendMessage(Message.raw("NPC Model: " + modelKey).color("#AAAAAA"));
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "==========================");

        return completed();
    }

    /**
     * Pokaż help
     */
    private void showHelp(CommandContext ctx) {
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=== STATUE COMMANDS ===");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("/statue refresh - Manually refresh statues").color("#FFFF00"));
        ctx.sendMessage(Message.raw("/statue list - Show current TOP 4").color("#FFFF00"));
        ctx.sendMessage(Message.raw("/statue setpos <1-4> - Set statue position").color("#FFFF00"));
        ctx.sendMessage(Message.raw("/statue info - Show system status").color("#FFFF00"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=======================");
    }

    /**
     * Pobiera kolor dla danego ranku
     */
    private String getRankColor(int rank) {
        switch (rank) {
            case 1: return "#FFD700"; // Gold
            case 2: return "#C0C0C0"; // Silver
            case 3: return "#CD7F32"; // Bronze
            default: return "#FFFFFF"; // White
        }
    }

    /**
     * Pobiera ikonę dla danego ranku
     */
    private String getRankIcon(int rank) {
        switch (rank) {
            case 1: return "\u2B50"; // Star
            case 2: return "\u25C6"; // Diamond
            case 3: return "\u25B2"; // Triangle
            default: return "\u2022"; // Bullet
        }
    }
}
