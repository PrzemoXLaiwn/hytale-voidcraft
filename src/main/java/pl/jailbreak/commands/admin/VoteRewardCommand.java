package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.concurrent.CompletableFuture;

/**
 * Vote reward command - called by HSL_Votifier when player votes
 * Usage: /votereward <player>
 *
 * Configure in HSL_Votifier rewards.json:
 * "Commands": ["votereward %player%"]
 */
public class VoteRewardCommand extends JailbreakCommand {

    private static final long VOTE_REWARD = 500;
    private final RequiredArg<String> playerArg;

    public VoteRewardCommand() {
        super("votereward", "Give vote reward to player (called by Votifier)");
        requirePermission("voidcraft.admin.vote");
        playerArg = withRequiredArg("player", "Player name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String playerName = ctx.get(playerArg);

        System.out.println("[Voidcraft] Vote reward command called for: " + playerName);

        PlayerManager playerManager = JailbreakPlugin.getPlayerManager();
        if (playerManager == null) {
            System.out.println("[Voidcraft] ERROR: PlayerManager is null!");
            return completed();
        }

        // Find player by name
        PlayerData targetData = playerManager.getPlayerByName(playerName);

        if (targetData == null) {
            System.out.println("[Voidcraft] Player not found: " + playerName + " - player may not have joined yet");
            return completed();
        }

        // Give reward
        targetData.addBalance(VOTE_REWARD);
        playerManager.savePlayer(targetData.getUuid());

        System.out.println("[Voidcraft] Vote reward given to " + playerName + ": $" + VOTE_REWARD);
        System.out.println("[Voidcraft] New balance: $" + targetData.getBalance());

        return completed();
    }
}
