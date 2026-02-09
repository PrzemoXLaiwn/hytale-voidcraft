package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.vote.VoteRewardManager;

import java.util.concurrent.CompletableFuture;

/**
 * Admin command to give vote reward (for testing)
 * Usage: /givevotereward
 */
public class GiveVoteRewardCommand extends JailbreakCommand {

    public GiveVoteRewardCommand() {
        super("givevotereward", "Give yourself a vote reward (admin test)");
        requirePermission("voidcraft.admin.vote");
        addAliases("testvote", "fakevote");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        Player player = ctx.senderAs(Player.class);

        // Check permission
        if (!player.hasPermission("voidcraft.admin.vote")) {
            sendError(ctx, "No permission!");
            return completed();
        }

        // Give vote reward
        VoteRewardManager.giveRewardToPlayer(player);

        System.out.println("[Voidcraft] Admin " + player.getUuid() + " used test vote reward");

        return completed();
    }
}
