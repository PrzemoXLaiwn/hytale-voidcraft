package pl.jailbreak.vote;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

/**
 * Manages vote rewards for players
 * Works with Hytale Votifier plugin
 */
public class VoteRewardManager {

    private static final long VOTE_REWARD = 500; // $500 per vote

    /**
     * Process a vote for a player
     * Called when Votifier receives a vote notification
     */
    public static void processVote(String playerName, String serviceName) {
        System.out.println("[Voidcraft] Vote received for " + playerName + " from " + serviceName);

        PlayerManager playerManager = JailbreakPlugin.getPlayerManager();
        if (playerManager == null) {
            System.out.println("[Voidcraft] PlayerManager not available for vote reward");
            return;
        }

        // Log the vote - actual reward is given when player is found online
        System.out.println("[Voidcraft] Vote logged for " + playerName + " - reward: $" + VOTE_REWARD);
    }

    /**
     * Give vote reward to a player by UUID
     */
    public static boolean giveVoteRewardByUuid(String uuid) {
        PlayerManager playerManager = JailbreakPlugin.getPlayerManager();
        if (playerManager == null) return false;

        PlayerData data = playerManager.getPlayer(uuid);
        if (data != null) {
            data.addBalance(VOTE_REWARD);
            playerManager.savePlayer(uuid);
            return true;
        }

        return false;
    }

    /**
     * Send thank you message to player
     */
    private static void sendThankYouMessage(String playerName) {
        // This will be called when we can find the player object
        System.out.println("[Voidcraft] Sending thank you message to " + playerName);
    }

    /**
     * Called when a player joins - check for pending vote rewards
     * and send thank you message if they voted while offline
     */
    public static void onPlayerJoin(Player player, PlayerData data) {
        // For now, just a placeholder
        // Could implement pending rewards system later
    }

    /**
     * Give vote reward directly to an online player
     */
    public static void giveRewardToPlayer(Player player) {
        if (player == null) return;

        PlayerManager playerManager = JailbreakPlugin.getPlayerManager();
        String uuid = player.getUuid().toString();
        PlayerData data = playerManager.getPlayer(uuid);

        if (data != null) {
            data.addBalance(VOTE_REWARD);
            playerManager.savePlayer(uuid);

            // Send messages
            player.sendMessage(Message.raw(" ").color("#FFFFFF"));
            player.sendMessage(Message.raw("=========================").color("#FFD700"));
            player.sendMessage(Message.raw("   THANK YOU FOR VOTING!").color("#00FF00"));
            player.sendMessage(Message.raw("=========================").color("#FFD700"));
            player.sendMessage(Message.raw(" ").color("#FFFFFF"));
            player.sendMessage(Message.raw("You received $" + VOTE_REWARD + " as a reward!").color("#00FF00"));
            player.sendMessage(Message.raw("Your new balance: $" + data.getBalance()).color("#00BFFF"));
            player.sendMessage(Message.raw(" ").color("#FFFFFF"));

            System.out.println("[Voidcraft] Vote reward given to " + uuid + ": $" + VOTE_REWARD);
        }
    }

    /**
     * Get the current vote reward amount
     */
    public static long getVoteReward() {
        return VOTE_REWARD;
    }
}
