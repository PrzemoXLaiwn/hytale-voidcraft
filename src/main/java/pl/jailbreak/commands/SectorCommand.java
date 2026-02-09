package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

import java.util.concurrent.CompletableFuture;

/**
 * /sector - Shows current sector info + full sector list
 * No args needed. Combines old /sector + /sectorlist into one.
 */
public class SectorCommand extends JailbreakCommand {

    public SectorCommand() {
        super("sector", "View your sector info and all sectors");
        addAliases("sectors", "sectorlist");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        Sector current = SectorManager.get(data.getCurrentSector());
        Sector next = SectorManager.getNext(data.getCurrentSector());

        ctx.sendMessage(Message.raw(" "));
        sendGold(ctx, "=== YOUR SECTOR ===");
        ctx.sendMessage(Message.raw("Current: " + current.id + " - " + current.name).color("#00FF00"));
        ctx.sendMessage(Message.raw("Ores: " + current.ores).color("#AAAAAA"));

        if (next != null) {
            ctx.sendMessage(Message.raw("Next: " + next.id + " - " + next.name + " (" + formatMoney(next.price) + ")").color("#FFFF00"));
        } else {
            ctx.sendMessage(Message.raw("You have unlocked ALL sectors!").color("#FF00FF"));
        }

        ctx.sendMessage(Message.raw(" "));
        sendGold(ctx, "=== ALL SECTORS ===");

        for (Sector s : SectorManager.getAll()) {
            boolean unlocked = SectorManager.canAccess(s.id, data.getCurrentSector());
            String status = unlocked ? "[UNLOCKED]" : "[LOCKED]";
            String color = unlocked ? "#00FF00" : "#FF0000";

            String line = s.id + " - " + s.name + " " + status;
            if (!unlocked) {
                line += " (" + formatMoney(s.price) + ")";
            }

            ctx.sendMessage(Message.raw(line).color(color));
        }

        ctx.sendMessage(Message.raw(" "));
        ctx.sendMessage(Message.raw("Commands:").color("#00BFFF"));
        ctx.sendMessage(Message.raw("  /rankup - Buy next sector").color("#AAAAAA"));
        ctx.sendMessage(Message.raw("  /warp --sector=A - Teleport to sector").color("#AAAAAA"));
        sendGold(ctx, "===================");

        return completed();
    }
}
