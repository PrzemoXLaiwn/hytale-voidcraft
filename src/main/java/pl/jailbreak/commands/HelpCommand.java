package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.concurrent.CompletableFuture;

/**
 * /help - Shows all player commands
 * No args needed
 */
public class HelpCommand extends JailbreakCommand {

    public HelpCommand() {
        super("help", "Show all commands");
        addAliases("commands", "cmds", "pomoc");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "========== VOIDCRAFT ==========");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Money & Economy
        ctx.sendMessage(Message.raw("Money:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /bal - Check balance").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /sell - Sell ores from inventory").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /daily - Claim daily reward").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /pay --player=X --amount=Y - Send money").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /prices - View ore prices").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Teleportation
        ctx.sendMessage(Message.raw("Teleportation:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /warp - Open warp menu").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /warp --name=spawn - Teleport to warp").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /warp --sector=B - Teleport to sector").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Sectors & Mining
        ctx.sendMessage(Message.raw("Sectors:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /sector - View sectors & info").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /rankup - Buy next sector").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Shop & Upgrades
        ctx.sendMessage(Message.raw("Shop & Upgrades:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /shop - Open pickaxe shop").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /buy --tier=1 - Buy pickaxe").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /enchants - Open enchant shop").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Crates
        ctx.sendMessage(Message.raw("Crates:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /crate - View your keys").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /crateopen --type=common - Open crate").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /crateinfo --type=common - View rewards").color("#00BFFF"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Other
        ctx.sendMessage(Message.raw("Other:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /stats - Your statistics").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /achievements - Your achievements").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /leaderboard - Top players").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /prestige - Prestige info").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /vote - Vote info").color("#00BFFF"));

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "===============================");

        return completed();
    }
}
