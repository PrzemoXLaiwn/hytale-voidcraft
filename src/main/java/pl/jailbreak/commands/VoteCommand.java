package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.vote.VoteRewardManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class VoteCommand extends JailbreakCommand {

    private static final long VOTE_COOLDOWN_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final Map<String, Long> lastVoteTime = new ConcurrentHashMap<>();

    public VoteCommand() {
        super("vote", "Vote for the server and get $500 reward");
        addAliases("glosuj");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        Player player = ctx.senderAs(Player.class);
        String uuid = player.getUuid().toString();
        PlayerData data = getPlayerData(ctx);

        if (data == null) {
            sendError(ctx, "Player data not found!");
            return completed();
        }

        // Check cooldown (24 hours)
        long now = System.currentTimeMillis();
        Long lastVote = lastVoteTime.get(uuid);
        if (lastVote != null && (now - lastVote) < VOTE_COOLDOWN_MS) {
            long remaining = VOTE_COOLDOWN_MS - (now - lastVote);
            long hoursLeft = remaining / (60 * 60 * 1000);
            long minsLeft = (remaining % (60 * 60 * 1000)) / (60 * 1000);

            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            sendGold(ctx, "====== VOTE FOR VOIDCRAFT ======");
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            sendError(ctx, "You already voted today!");
            ctx.sendMessage(Message.raw("  Next vote in: " + hoursLeft + "h " + minsLeft + "m").color("#FFA500"));
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
            ctx.sendMessage(Message.raw("  Vote link: hyghest.com/server/voidcraft").color("#00BFFF"));
            ctx.sendMessage(Message.raw("  Your balance: " + formatMoney(data.getBalance())).color("#00FF00"));
            sendGold(ctx, "================================");
            return completed();
        }

        // Give reward
        long reward = VoteRewardManager.getVoteReward();
        data.addBalance(reward);
        lastVoteTime.put(uuid, now);

        var pm = getPlayerManager();
        if (pm != null) pm.savePlayer(uuid);

        // Send reward message
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "=== THANK YOU FOR VOTING! ===");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  You received " + formatMoney(reward) + "!").color("#00FF00"));
        ctx.sendMessage(Message.raw("  New balance: " + formatMoney(data.getBalance())).color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Vote link: hyghest.com/server/voidcraft").color("#9400D3"));
        ctx.sendMessage(Message.raw("  Come back in 24h for another reward!").color("#FFA500"));
        sendGold(ctx, "=============================");

        System.out.println("[Voidcraft] Vote reward given to " + data.getName() + ": $" + reward);

        return completed();
    }
}
