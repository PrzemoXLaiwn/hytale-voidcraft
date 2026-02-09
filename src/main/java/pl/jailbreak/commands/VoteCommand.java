package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.vote.VoteRewardManager;

import java.util.concurrent.CompletableFuture;

public class VoteCommand extends JailbreakCommand {

    public VoteCommand() {
        super("vote", "Vote for the server and get rewards");
        addAliases("glosuj");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        PlayerData data = getPlayerData(ctx);

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "====== VOTE FOR VOIDCRAFT ======");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Vote and receive $" + VoteRewardManager.getVoteReward() + " per vote!").color("#00FF00"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  1. Go to: hyghest.com").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  2. Find VOIDCRAFT server").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  3. Click VOTE").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  4. Reward is given automatically!").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Link: https://hyghest.com/servers/voidcraft/vote").color("#9400D3"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        if (data != null) {
            ctx.sendMessage(Message.raw("  Your balance: " + formatMoney(data.getBalance())).color("#00FF00"));
            ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        }

        sendGold(ctx, "================================");

        return completed();
    }
}
