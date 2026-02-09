package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.concurrent.CompletableFuture;

/**
 * /vadmin - Shows admin command help
 * No args - just shows all available admin commands
 */
public class AdminCommand extends JailbreakCommand {

    public AdminCommand() {
        super("vadmin", "Voidcraft admin help");
        requirePermission("voidcraft.admin");
        addAliases("voidadmin", "va");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        sendGold(ctx, "=== VOIDCRAFT ADMIN ===");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Economy:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /jbprices - Show all ore prices").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /jsetprice --ore=X --price=Y - Set price").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /jmultiplier --value=X - Set multiplier").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /jbreload - Reload config").color("#AAAAAA"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Players:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /setbal --player=X --amount=Y - Set balance").color("#AAAAAA"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Warps:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /warpadd --name=X --x=0 --y=0 --z=0").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /warpdelete --name=X").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /warplist").color("#AAAAAA"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Sectors:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /setsector --sector=A --x=0 --y=0 --z=0").color("#AAAAAA"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Mines:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /minepos1 --x=0 --y=0 --z=0").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /minepos2 --x=0 --y=0 --z=0").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /minecreate --name=X --sector=A").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /mineregen --name=X").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /minedelete --name=X").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /mineinfo --name=X").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /mineinterval --name=X --seconds=300").color("#AAAAAA"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Protection:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /protectadd --name=X --x1=0 --y1=0 --z1=0 --x2=0 --y2=0 --z2=0").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /protectdelete --name=X").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /protectlist").color("#AAAAAA"));
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        ctx.sendMessage(Message.raw("Debug:").color("#FFD700"));
        ctx.sendMessage(Message.raw("  /whatisthis - Block ID at cursor").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /debuginv - Inventory debug").color("#AAAAAA"));
        sendGold(ctx, "======================");
        return completed();
    }
}