package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.protection.SpawnProtection;

import java.util.concurrent.CompletableFuture;

/**
 * /protectlist - list all protected regions
 */
public class ProtectListCommand extends JailbreakCommand {

    public ProtectListCommand() {
        super("protectlist", "List protected regions");
        requirePermission("voidcraft.admin.protect");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
        if (protection == null) {
            sendError(ctx, "Spawn protection not initialized!");
            return completed();
        }

        var names = protection.getRegionNames();
        if (names.isEmpty()) {
            sendInfo(ctx, "No protected regions.");
        } else {
            sendGold(ctx, "=== Protected Regions ===");
            for (String name : names) {
                sendInfo(ctx, protection.getRegionInfo(name));
            }
        }

        return completed();
    }
}
