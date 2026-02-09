package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.protection.SpawnProtection;

import java.util.concurrent.CompletableFuture;

/**
 * /protectdelete <name>
 */
public class ProtectDeleteCommand extends JailbreakCommand {
    private final RequiredArg<String> nameArg;

    public ProtectDeleteCommand() {
        super("protectdelete", "Delete protected region");
        requirePermission("voidcraft.admin.protect");
        nameArg = withRequiredArg("name", "Region name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
        if (protection == null) {
            sendError(ctx, "Spawn protection not initialized!");
            return completed();
        }

        String name = ctx.get(nameArg);
        if (protection.removeRegion(name)) {
            sendSuccess(ctx, "Region '" + name + "' deleted!");
        } else {
            sendError(ctx, "Region '" + name + "' not found!");
        }

        return completed();
    }
}
