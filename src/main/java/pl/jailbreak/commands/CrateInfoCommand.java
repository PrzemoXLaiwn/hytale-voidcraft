package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.crates.CrateManager;

import java.util.concurrent.CompletableFuture;

/**
 * /crateinfo <type> - Shows crate rewards info
 */
public class CrateInfoCommand extends JailbreakCommand {

    private final RequiredArg<String> typeArg;

    public CrateInfoCommand() {
        super("crateinfo", "View crate rewards");
        typeArg = withRequiredArg("type", "common/rare/epic/legendary/void", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }

        String crateType = ctx.get(typeArg).toLowerCase();

        if (!CrateManager.isValidCrate(crateType)) {
            sendError(ctx, "Unknown crate: " + crateType);
            sendInfo(ctx, "Types: common, rare, epic, legendary, void");
            return completed();
        }

        String color = CrateManager.getColor(crateType);
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("=== " + CrateManager.getDisplayName(crateType) + " ===").color(color));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  Possible rewards:").color("#FFFFFF"));

        for (CrateManager.CrateReward reward : CrateManager.getRewards(crateType)) {
            ctx.sendMessage(Message.raw("  - " + reward.name).color(reward.color));
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("  /crateopen " + crateType).color("#FFFF00"));
        ctx.sendMessage(Message.raw("=========================").color(color));

        return completed();
    }
}
