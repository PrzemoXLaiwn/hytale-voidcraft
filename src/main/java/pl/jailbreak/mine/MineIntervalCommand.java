package pl.jailbreak.mine;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.concurrent.CompletableFuture;

/**
 * /mineinterval --name=ironmine --seconds=300
 */
public class MineIntervalCommand extends JailbreakCommand {

    private final MineManager mineManager;
    private final OptionalArg<String> nameArg;
    private final OptionalArg<Integer> secondsArg;

    public MineIntervalCommand(MineManager mineManager) {
        super("mineinterval", "Set mine regen interval (--name=X --seconds=N)");
        requirePermission("voidcraft.admin.mine");
        this.mineManager = mineManager;
        addAliases("minesetinterval");
        nameArg = withOptionalArg("name", "Mine name", ArgTypes.STRING);
        secondsArg = withOptionalArg("seconds", "Seconds between regen", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!ctx.provided(nameArg) || !ctx.provided(secondsArg)) {
            sendError(ctx, "Usage: /mineinterval --name=ironmine --seconds=300");
            return completed();
        }

        String name = ctx.get(nameArg);
        int seconds = ctx.get(secondsArg);

        Mine mine = mineManager.getMine(name);
        if (mine == null) {
            sendError(ctx, "Mine '" + name + "' not found!");
            return completed();
        }

        if (seconds < 10) {
            sendError(ctx, "Minimum interval is 10 seconds!");
            return completed();
        }

        mine.setRegenIntervalSeconds(seconds);
        mineManager.save();
        sendSuccess(ctx, "Regen interval set to " + seconds + " seconds (" + formatTime(seconds) + ")");

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
