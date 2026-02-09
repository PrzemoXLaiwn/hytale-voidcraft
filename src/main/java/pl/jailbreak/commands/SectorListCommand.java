package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

import java.util.concurrent.CompletableFuture;

public class SectorListCommand extends JailbreakCommand {

    public SectorListCommand() {
        super("sectorlist", "Show all sectors");
        addAliases("sectors");
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

        sendGold(ctx, "===================");
        return completed();
    }
}