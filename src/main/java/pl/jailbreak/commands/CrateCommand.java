package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.crates.CrateManager;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

/**
 * /crate - Shows your keys and all crate info
 * Aliases: crates, skrzynka, keys
 */
public class CrateCommand extends JailbreakCommand {

    public CrateCommand() {
        super("crate", "View your crate keys");
        addAliases("crates", "skrzynka", "keys");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }

        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No player data!"); return completed(); }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        sendGold(ctx, "======== YOUR KEYS ========");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        boolean hasAny = false;
        for (String type : CrateManager.getCrateTypes()) {
            int count = data.getKeys(type);
            String color = count > 0 ? CrateManager.getColor(type) : "#666666";
            ctx.sendMessage(Message.raw("  " + CrateManager.getDisplayName(type) + ": " + count + " keys").color(color));
            if (count > 0) hasAny = true;
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));

        // Show all crate types
        sendGold(ctx, "======== CRATES ========");
        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        for (String type : CrateManager.getCrateTypes()) {
            int keys = data.getKeys(type);
            String color = CrateManager.getColor(type);
            String keysStr = keys > 0 ? " [" + keys + " keys]" : "";
            ctx.sendMessage(Message.raw("  " + CrateManager.getDisplayName(type) + keysStr).color(color));
        }

        ctx.sendMessage(Message.raw(" ").color("#FFFFFF"));
        if (hasAny) {
            ctx.sendMessage(Message.raw("  /crateopen <type> - Open a crate").color("#FFFF00"));
        } else {
            ctx.sendMessage(Message.raw("  Mine ores to find keys!").color("#AAAAAA"));
        }
        ctx.sendMessage(Message.raw("  /crateinfo <type> - See rewards").color("#AAAAAA"));
        sendGold(ctx, "===========================");

        return completed();
    }
}
