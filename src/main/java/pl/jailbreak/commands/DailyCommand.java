package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

public class DailyCommand extends JailbreakCommand {

    // Base reward: ~5 min of sector A mining ($500)
    private static final long BASE_REWARD = 500;
    // +$250 per streak day (day 30 = $500 + 29*250 = $7,750)
    private static final long STREAK_BONUS = 250;
    // Max streak days
    private static final int MAX_STREAK = 30;
    // 24 hours in milliseconds
    private static final long DAY_MS = 24 * 60 * 60 * 1000;
    // 48 hours - if more than this, streak resets
    private static final long STREAK_RESET_MS = 48 * 60 * 60 * 1000;

    public DailyCommand() {
        super("daily", "Claim your daily reward");
        addAliases("dailyreward", "dr");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        long now = System.currentTimeMillis();
        long lastClaim = data.getLastDailyReward();
        long timeSince = now - lastClaim;

        // Check if 24 hours have passed
        if (lastClaim > 0 && timeSince < DAY_MS) {
            long remaining = DAY_MS - timeSince;
            String timeStr = formatTime(remaining);
            sendError(ctx, "You can claim your daily reward in " + timeStr + "!");
            return completed();
        }

        // Check streak
        int streak = data.getDailyStreak();
        if (lastClaim > 0 && timeSince > STREAK_RESET_MS) {
            // Streak broken - more than 48h since last claim
            streak = 0;
            data.resetDailyStreak();
            sendInfo(ctx, "Your streak was reset (more than 48h since last claim)");
        }

        // Increment streak
        streak++;
        if (streak > MAX_STREAK) streak = MAX_STREAK;
        data.setDailyStreak(streak);

        // Calculate reward
        long reward = BASE_REWARD + (STREAK_BONUS * (streak - 1));

        // Apply prestige bonus
        int prestige = data.getPrestige();
        if (prestige > 0) {
            reward = (long)(reward * (1 + prestige * 0.1));
        }

        // Give reward
        data.addBalance(reward);
        data.setLastDailyReward(now);
        getPlayerManager().savePlayer(data.getUuid());

        // Display reward
        sendGold(ctx, "=== DAILY REWARD ===");
        ctx.sendMessage(Message.raw("You received: " + formatMoney(reward)).color("#00FF00"));
        ctx.sendMessage(Message.raw("Current streak: " + streak + " day" + (streak > 1 ? "s" : "")).color("#00BFFF"));

        if (streak < MAX_STREAK) {
            long nextReward = BASE_REWARD + (STREAK_BONUS * streak);
            ctx.sendMessage(Message.raw("Tomorrow's reward: " + formatMoney(nextReward)).color("#AAAAAA"));
        } else {
            ctx.sendMessage(Message.raw("MAX STREAK REACHED!").color("#FFD700"));
        }

        if (prestige > 0) {
            ctx.sendMessage(Message.raw("Prestige bonus: +" + (prestige * 10) + "%").color("#FF00FF"));
        }

        sendGold(ctx, "====================");

        return completed();
    }

    private String formatTime(long ms) {
        long hours = ms / (60 * 60 * 1000);
        long minutes = (ms % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (ms % (60 * 1000)) / 1000;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}
