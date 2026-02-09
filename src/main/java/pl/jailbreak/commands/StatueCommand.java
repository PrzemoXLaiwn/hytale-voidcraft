package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.statue.StatueData;
import pl.jailbreak.statue.StatueManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * /statue --action=refresh|list|info
 */
public class StatueCommand extends JailbreakCommand {

    private final OptionalArg<String> actionArg;

    public StatueCommand() {
        super("statue", "Manage TOP 4 balance statues");
        addAliases("statues", "statuetka");
        actionArg = withOptionalArg("action", "refresh/list/info", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        String action = ctx.provided(actionArg) ? ctx.get(actionArg).toLowerCase() : "help";

        switch (action) {
            case "refresh":
                return executeRefresh(ctx);
            case "list":
                return executeList(ctx);
            case "info":
                return executeInfo(ctx);
            default:
                showHelp(ctx);
                return completed();
        }
    }

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
        }

        return completed();
    }

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

            var config = manager.getConfig();
            int interval = config.getUpdateIntervalMinutes();
            ctx.sendMessage(Message.raw("Update interval: " + interval + " minutes").color("#AAAAAA"));

            String modelKey = config.getNpcModelKey();
            ctx.sendMessage(Message.raw("NPC Model: " + modelKey).color("#AAAAAA"));
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "==========================");

        return completed();
    }

    private void showHelp(CommandContext ctx) {
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=== STATUE COMMANDS ===");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("/statue --action=refresh - Manually refresh statues").color("#FFFF00"));
        ctx.sendMessage(Message.raw("/statue --action=list - Show current TOP 4").color("#FFFF00"));
        ctx.sendMessage(Message.raw("/statue --action=info - Show system status").color("#FFFF00"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=======================");
    }

    private String getRankColor(int rank) {
        switch (rank) {
            case 1: return "#FFD700";
            case 2: return "#C0C0C0";
            case 3: return "#CD7F32";
            default: return "#FFFFFF";
        }
    }

    private String getRankIcon(int rank) {
        switch (rank) {
            case 1: return "*";
            case 2: return "+";
            case 3: return "-";
            default: return ".";
        }
    }
}
