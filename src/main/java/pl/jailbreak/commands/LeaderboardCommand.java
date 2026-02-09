package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.player.PlayerData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LeaderboardCommand extends JailbreakCommand {
    private final OptionalArg<String> typeArg;

    public LeaderboardCommand() {
        super("leaderboard", "View top players");
        addAliases("top", "baltop", "lb");
        typeArg = withOptionalArg("type", "balance/blocks/earned (default: balance)", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String type = ctx.provided(typeArg) ? ctx.get(typeArg).toLowerCase() : "balance";

        switch (type) {
            case "blocks":
            case "mined":
                showBlocksLeaderboard(ctx);
                break;
            case "earned":
            case "total":
                showEarnedLeaderboard(ctx);
                break;
            default:
                showBalanceLeaderboard(ctx);
        }

        return completed();
    }

    private void showBalanceLeaderboard(CommandContext ctx) {
        sendGold(ctx, "=== TOP 10 RICHEST PLAYERS ===");

        List<PlayerData> top = getPlayerManager().getTopBalances(10);

        if (top.isEmpty()) {
            sendInfo(ctx, "No players found!");
            return;
        }

        int rank = 1;
        for (PlayerData player : top) {
            String color = getRankColor(rank);
            ctx.sendMessage(Message.raw("#" + rank + " " + player.getName() + " - " + formatMoney(player.getBalance())).color(color));
            rank++;
        }

        sendGold(ctx, "==============================");
    }

    private void showBlocksLeaderboard(CommandContext ctx) {
        sendGold(ctx, "=== TOP 10 MINERS ===");

        List<PlayerData> top = getPlayerManager().getAllPlayers().stream()
            .sorted((a, b) -> Integer.compare(b.getBlocksMined(), a.getBlocksMined()))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());

        if (top.isEmpty()) {
            sendInfo(ctx, "No players found!");
            return;
        }

        int rank = 1;
        for (PlayerData player : top) {
            String color = getRankColor(rank);
            ctx.sendMessage(Message.raw("#" + rank + " " + player.getName() + " - " +
                String.format("%,d", player.getBlocksMined()) + " blocks").color(color));
            rank++;
        }

        sendGold(ctx, "=====================");
    }

    private void showEarnedLeaderboard(CommandContext ctx) {
        sendGold(ctx, "=== TOP 10 TOTAL EARNED ===");

        List<PlayerData> top = getPlayerManager().getAllPlayers().stream()
            .sorted((a, b) -> Long.compare(b.getTotalEarned(), a.getTotalEarned()))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());

        if (top.isEmpty()) {
            sendInfo(ctx, "No players found!");
            return;
        }

        int rank = 1;
        for (PlayerData player : top) {
            String color = getRankColor(rank);
            ctx.sendMessage(Message.raw("#" + rank + " " + player.getName() + " - " +
                formatMoney(player.getTotalEarned())).color(color));
            rank++;
        }

        sendGold(ctx, "===========================");
    }

    private String getRankColor(int rank) {
        switch (rank) {
            case 1: return "#FFD700"; // Gold
            case 2: return "#C0C0C0"; // Silver
            case 3: return "#CD7F32"; // Bronze
            default: return "#AAAAAA"; // Gray
        }
    }
}
