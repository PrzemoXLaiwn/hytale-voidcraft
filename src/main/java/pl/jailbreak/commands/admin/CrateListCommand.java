package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.crates.CrateLocationManager;
import pl.jailbreak.crates.CrateManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * /cratelist - Show all registered crate locations
 */
public class CrateListCommand extends JailbreakCommand {

    public CrateListCommand() {
        super("cratelist", "Show all crate locations");
        requirePermission("voidcraft.admin.crate");
        addAliases("cratelocations");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        CrateLocationManager clm = JailbreakPlugin.getCrateLocationManager();
        if (clm == null) {
            sendError(ctx, "CrateLocationManager not initialized!");
            return completed();
        }

        Collection<CrateLocationManager.CrateLocationData> locs = clm.getAllLocations();
        if (locs.isEmpty()) {
            sendInfo(ctx, "No crate locations registered.");
            sendInfo(ctx, "Use /crateadd --type=common --x=N --y=N --z=N");
            return completed();
        }

        sendGold(ctx, "=== CRATE LOCATIONS (" + locs.size() + ") ===");
        for (CrateLocationManager.CrateLocationData loc : locs) {
            String color = CrateManager.getColor(loc.crateType);
            String display = CrateManager.getDisplayName(loc.crateType);
            ctx.sendMessage(Message.raw(display + " at (" + loc.x + ", " + loc.y + ", " + loc.z + ")").color(color));
        }

        return completed();
    }
}
