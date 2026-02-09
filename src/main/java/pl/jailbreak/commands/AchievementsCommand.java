package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.achievements.Achievement;
import pl.jailbreak.achievements.AchievementManager;
import pl.jailbreak.player.PlayerData;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AchievementsCommand extends JailbreakCommand {
    private final OptionalArg<Integer> pageArg;

    public AchievementsCommand() {
        super("achievements", "View your achievements");
        addAliases("ach", "achieve");
        pageArg = withOptionalArg("page", "Page number", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        AchievementManager manager = JailbreakPlugin.getAchievementManager();
        int page = ctx.provided(pageArg) ? ctx.get(pageArg) : 1;
        int perPage = 8;

        Map<String, Achievement> all = AchievementManager.getAllAchievements();
        int total = all.size();
        int maxPages = (int) Math.ceil((double) total / perPage);

        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        int completed = manager.getCompletedCount(data);

        sendGold(ctx, "=== ACHIEVEMENTS (" + completed + "/" + total + ") ===");

        int skip = (page - 1) * perPage;
        int count = 0;
        int shown = 0;

        for (Achievement ach : all.values()) {
            if (count < skip) {
                count++;
                continue;
            }
            if (shown >= perPage) break;

            boolean hasIt = data.hasAchievement(ach.getId());
            String status = hasIt ? "[COMPLETED]" : "[" + getProgressBar(manager.getProgress(data, ach)) + "]";
            String color = hasIt ? "#00FF00" : "#AAAAAA";

            ctx.sendMessage(Message.raw(status + " " + ach.getName()).color(color));
            ctx.sendMessage(Message.raw("  " + ach.getDescription() + " - Reward: " + formatMoney(ach.getReward())).color("#888888"));

            count++;
            shown++;
        }

        if (maxPages > 1) {
            sendInfo(ctx, "Page " + page + "/" + maxPages + " - Use /achievements <page>");
        }

        sendGold(ctx, "================================");

        return completed();
    }

    private String getProgressBar(double progress) {
        int filled = (int) (progress * 10);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "|" : ".");
        }
        return bar.toString() + " " + (int)(progress * 100) + "%";
    }
}
