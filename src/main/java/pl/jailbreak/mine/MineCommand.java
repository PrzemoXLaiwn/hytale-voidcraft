package pl.jailbreak.mine;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Admin command for listing mines and help
 * /mine - show help and list mines
 */
public class MineCommand extends JailbreakCommand {

    private final MineManager mineManager;

    public MineCommand(MineManager mineManager) {
        super("mine", "Show mines help");
        requirePermission("voidcraft.admin.mine");
        this.mineManager = mineManager;
        addAliases("kopalnia", "mines");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        showHelp(ctx);
        handleList(ctx);
        return completed();
    }

    private void showHelp(CommandContext ctx) {
        sendGold(ctx, "=== MINE COMMANDS ===");
        ctx.sendMessage(Message.raw("/mineregen - Regenerate ALL mines").color("#00FF00"));
        ctx.sendMessage(Message.raw("/mineinfo - Show all mines info").color("#00FF00"));
        sendGold(ctx, "--- Setup (admin) ---");
        ctx.sendMessage(Message.raw("/minepos1 --x=N --y=N --z=N").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("/minepos2 --x=N --y=N --z=N").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("/minecreate --name=X --sector=B").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("/minedelete --name=X").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("/mineinterval --name=X --seconds=300").color("#AAAAAA"));
        sendGold(ctx, "Sectors: A, B, C, D, E, F, G, H, I, J");
        sendGold(ctx, "====================");
    }

    private void handleList(CommandContext ctx) {
        Collection<Mine> mines = mineManager.getAllMines();

        if (mines.isEmpty()) {
            sendInfo(ctx, "No mines created yet.");
            return;
        }

        sendGold(ctx, "=== MINES (" + mines.size() + ") ===");
        for (Mine mine : mines) {
            int timeLeft = mine.getSecondsUntilRegen();
            String timeStr = timeLeft > 0 ? formatTime(timeLeft) : "READY";
            ctx.sendMessage(Message.raw(
                mine.getName() + " [" + mine.getSector() + "] - " +
                mine.getBlockCount() + " blocks - Regen: " + timeStr
            ).color("#AAAAAA"));
        }
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
