package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;
import pl.jailbreak.JailbreakPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Admin command to reset achievements for testing
 */
public class ResetAchievementsCommand extends JailbreakCommand {

    public ResetAchievementsCommand() {
        super("resetachievements", "Reset your achievements (admin)");
        requirePermission("voidcraft.admin");
        addAliases("resetach");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        try {
            com.hypixel.hytale.server.core.entity.entities.Player player = ctx.senderAs(com.hypixel.hytale.server.core.entity.entities.Player.class);
            String uuid = player.getUuid().toString();
            String name = uuid.substring(0, 8);

            PlayerManager pm = JailbreakPlugin.getPlayerManager();
            PlayerData data = pm.loadOrCreate(uuid, name);

            if (data == null) {
                sendError(ctx, "No player data!");
                return completed();
            }

            int count = data.getCompletedAchievements().size();
            data.getCompletedAchievements().clear();
            pm.savePlayer(uuid);

            sendSuccess(ctx, "Reset " + count + " achievements! They will be re-checked in 10 seconds.");
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
        return completed();
    }
}
