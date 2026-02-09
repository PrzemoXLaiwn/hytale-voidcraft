package pl.jailbreak.mine;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * /mineinfo - Show info about ALL mines (no arguments needed)
 */
public class MineInfoCommand extends JailbreakCommand {

    private final MineManager mineManager;

    public MineInfoCommand(MineManager mineManager) {
        super("mineinfo", "Show all mines info");
        requirePermission("voidcraft.admin.mine");
        this.mineManager = mineManager;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        Collection<Mine> mines = mineManager.getAllMines();

        if (mines.isEmpty()) {
            sendError(ctx, "No mines exist!");
            return completed();
        }

        sendGold(ctx, "=== ALL MINES (" + mines.size() + ") ===");

        for (Mine mine : mines) {
            Vector3i min = mine.getMin();
            Vector3i max = mine.getMax();

            ctx.sendMessage(Message.raw("--- " + mine.getName() + " [" + mine.getSector() + "] ---").color("#FFD700"));
            ctx.sendMessage(Message.raw("  Pos1: " + min.x + ", " + min.y + ", " + min.z).color("#AAAAAA"));
            ctx.sendMessage(Message.raw("  Pos2: " + max.x + ", " + max.y + ", " + max.z).color("#AAAAAA"));
            ctx.sendMessage(Message.raw("  Size: " + mine.getBlockCount() + " blocks").color("#AAAAAA"));
            ctx.sendMessage(Message.raw("  Regen: " + mine.getRegenIntervalSeconds() + "s (" + formatTime(mine.getRegenIntervalSeconds()) + ")").color("#AAAAAA"));

            int timeLeft = mine.getSecondsUntilRegen();
            if (timeLeft > 0) {
                ctx.sendMessage(Message.raw("  Next regen in: " + formatTime(timeLeft)).color("#FFFF00"));
            } else {
                ctx.sendMessage(Message.raw("  Status: READY TO REGEN").color("#00FF00"));
            }
        }

        sendGold(ctx, "======================");

        return completed();
    }

    private String formatTime(int seconds) {
        if (seconds >= 3600) {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        } else if (seconds >= 60) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }
}
