package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.concurrent.CompletableFuture;

/**
 * Admin command to show all available commands
 * /cmd - Shows all server commands with descriptions
 */
public class CmdListCommand extends JailbreakCommand {

    public CmdListCommand() {
        super("cmd", "Show all admin commands");
        requirePermission("voidcraft.admin");
        addAliases("admincmds");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        Player player = ctx.senderAs(Player.class);
        if (!player.hasPermission("voidcraft.admin.cmd")) {
            sendError(ctx, "No permission!");
            return completed();
        }

        sendGold(ctx, "==========================================");
        sendGold(ctx, "        VOIDCRAFT - COMMAND LIST");
        sendGold(ctx, "==========================================");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== PLAYER COMMANDS ===");
        sendCmd(ctx, "/help - Show player commands");
        sendCmd(ctx, "/bal - Check balance");
        sendCmd(ctx, "/sell - Sell ores");
        sendCmd(ctx, "/daily - Daily reward");
        sendCmd(ctx, "/pay --player=X --amount=Y - Send money");
        sendCmd(ctx, "/prices - Ore prices");
        sendCmd(ctx, "/warp - Warp menu");
        sendCmd(ctx, "/warp --name=spawn - Teleport to warp");
        sendCmd(ctx, "/warp --sector=B - Teleport to sector");
        sendCmd(ctx, "/sector - Sector info + list");
        sendCmd(ctx, "/rankup - Buy next sector");
        sendCmd(ctx, "/shop - Pickaxe shop");
        sendCmd(ctx, "/buy --tier=1 - Buy pickaxe");
        sendCmd(ctx, "/enchants - Enchant shop");
        sendCmd(ctx, "/crate - View keys");
        sendCmd(ctx, "/crateopen --type=common - Open");
        sendCmd(ctx, "/crateinfo --type=common - Rewards");
        sendCmd(ctx, "/stats - Statistics");
        sendCmd(ctx, "/achievements - Achievements");
        sendCmd(ctx, "/leaderboard - Top players");
        sendCmd(ctx, "/prestige - Prestige info");
        sendCmd(ctx, "/prestigeconfirm - Execute prestige");
        sendCmd(ctx, "/vote - Vote info");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== ADMIN COMMANDS ===");
        sendCmd(ctx, "/vadmin - Admin help panel");
        sendCmd(ctx, "/setbal --player=X --amount=Y");
        sendCmd(ctx, "/jbprices - Ore prices");
        sendCmd(ctx, "/jsetprice --ore=X --price=Y");
        sendCmd(ctx, "/jmultiplier --value=X");
        sendCmd(ctx, "/jbreload - Reload config");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== WARP ADMIN ===");
        sendCmd(ctx, "/warpadd --name=X --x=0 --y=0 --z=0");
        sendCmd(ctx, "/warpdelete --name=X");
        sendCmd(ctx, "/warplist");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== SECTOR ADMIN ===");
        sendCmd(ctx, "/setsector --sector=A --x=0 --y=0 --z=0");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== MINE ADMIN ===");
        sendCmd(ctx, "/mine - Mine list");
        sendCmd(ctx, "/minepos1 --x=0 --y=0 --z=0");
        sendCmd(ctx, "/minepos2 --x=0 --y=0 --z=0");
        sendCmd(ctx, "/minecreate --name=X --sector=A");
        sendCmd(ctx, "/mineregen --name=X");
        sendCmd(ctx, "/minedelete --name=X");
        sendCmd(ctx, "/mineinfo --name=X");
        sendCmd(ctx, "/mineinterval --name=X --seconds=300");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== PROTECTION ADMIN ===");
        sendCmd(ctx, "/protectadd --name=X --x1=0 ... --z2=0");
        sendCmd(ctx, "/protectdelete --name=X");
        sendCmd(ctx, "/protectlist");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "=== DEBUG ===");
        sendCmd(ctx, "/whatisthis - Block ID");
        sendCmd(ctx, "/debuginv - Inventory debug");
        sendCmd(ctx, "/resetachievements");
        sendCmd(ctx, "/givevotereward - Test vote");

        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw(" "));
        sendGold(ctx, "==========================================");

        return completed();
    }

    private void sendCmd(CommandContext ctx, String text) {
        ctx.sendMessage(com.hypixel.hytale.server.core.Message.raw("  " + text).color("#00BFFF"));
    }
}
