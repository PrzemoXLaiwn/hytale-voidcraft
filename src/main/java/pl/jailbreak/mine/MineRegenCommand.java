package pl.jailbreak.mine;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * /mineregen - Regenerate ALL mines (no arguments needed)
 */
public class MineRegenCommand extends JailbreakCommand {

    private final MineManager mineManager;

    public MineRegenCommand(MineManager mineManager) {
        super("mineregen", "Regenerate all mines");
        requirePermission("voidcraft.admin.mine");
        this.mineManager = mineManager;
        addAliases("minefill");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        Player player = ctx.senderAs(Player.class);
        World world = player.getWorld();
        if (world == null) {
            sendError(ctx, "Cannot get world!");
            return completed();
        }

        // Set world for auto-regen task
        MineRegenTask regenTask = JailbreakPlugin.getMineRegenTask();
        if (regenTask != null) {
            regenTask.setWorld(world);
        }

        Collection<Mine> mines = mineManager.getAllMines();
        if (mines.isEmpty()) {
            sendError(ctx, "No mines exist! Create one first with /minecreate");
            return completed();
        }

        sendInfo(ctx, "Regenerating " + mines.size() + " mines x10...");

        world.execute(() -> {
            for (Mine mine : mines) {
                mineManager.regenerateMineMultiple(mine, world, 10);
                player.sendMessage(Message.raw("[Voidcraft] Mine '" + mine.getName() + "' [" + mine.getSector() + "] regenerated!").color("#00FF00"));
            }
            player.sendMessage(Message.raw("[Voidcraft] All mines regenerated!").color("#00FF00"));
        });

        return completed();
    }
}
