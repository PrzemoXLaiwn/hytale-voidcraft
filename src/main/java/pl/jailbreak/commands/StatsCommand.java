package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.achievements.AchievementManager;
import pl.jailbreak.JailbreakPlugin;
import java.util.concurrent.CompletableFuture;

public class StatsCommand extends JailbreakCommand {
    public StatsCommand() {
        super("stats", "Show your statistics");
        addAliases("statystyki", "info");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No player data!"); return completed(); }

        int totalAchievements = 0;
        try {
            AchievementManager am = JailbreakPlugin.getAchievementManager();
            if (am != null) totalAchievements = am.getTotalCount();
        } catch (Exception ignored) {}

        int completedCount = data.getCompletedAchievements().size();
        int sectorCount = data.getUnlockedSectors().size();

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "======= YOUR STATS =======");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Balance: " + formatMoney(data.getBalance())).color("#00FF00"));
        ctx.sendMessage(Message.raw("  Prestige: " + data.getPrestige()).color("#FF00FF"));
        ctx.sendMessage(Message.raw("  Sector: " + data.getCurrentSector() + " (" + sectorCount + "/10 unlocked)").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Blocks Mined: " + String.format("%,d", data.getBlocksMined())).color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  Total Earned: " + formatMoney(data.getTotalEarned())).color("#00FF00"));
        ctx.sendMessage(Message.raw("  Total Spent: " + formatMoney(data.getTotalSpent())).color("#FF6666"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Daily Streak: " + data.getDailyStreak() + " days").color("#FFD700"));
        ctx.sendMessage(Message.raw("  Achievements: " + completedCount + "/" + totalAchievements).color("#9400D3"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "==========================");

        return completed();
    }
}
