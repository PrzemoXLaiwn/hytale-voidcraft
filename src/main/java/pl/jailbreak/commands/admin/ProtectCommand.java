package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.protection.SpawnProtection;

import java.util.concurrent.CompletableFuture;

/**
 * Admin command to manage spawn protection
 * /protect add <name> <x1> <y1> <z1> <x2> <y2> <z2> - create region
 * /protect delete <name> - delete region
 * /protect list - list all regions
 */
public class ProtectCommand extends JailbreakCommand {
    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> argsArg;

    public ProtectCommand() {
        super("protect", "Manage spawn protection");
        requirePermission("jailbreak.admin.protect");
        actionArg = withRequiredArg("action", "add/delete/list", ArgTypes.STRING);
        argsArg = withRequiredArg("args", "arguments", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        String action = ctx.get(actionArg).toLowerCase();
        String args = ctx.provided(argsArg) ? ctx.get(argsArg) : "";

        SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
        if (protection == null) {
            sendError(ctx, "Spawn protection not initialized!");
            return completed();
        }

        switch (action) {
            case "add":
            case "create":
                handleAdd(ctx, protection, args);
                break;

            case "delete":
            case "remove":
                handleDelete(ctx, protection, args);
                break;

            case "list":
                handleList(ctx, protection);
                break;

            default:
                sendError(ctx, "Unknown action! Use: add, delete, list");
                sendInfo(ctx, "/protect add <name> <x1> <y1> <z1> <x2> <y2> <z2>");
                sendInfo(ctx, "/protect delete <name>");
                sendInfo(ctx, "/protect list");
                break;
        }

        return completed();
    }

    private void handleAdd(CommandContext ctx, SpawnProtection protection, String args) {
        // Parse: name x1 y1 z1 x2 y2 z2
        String[] parts = args.split("\\s+");
        if (parts.length < 7) {
            sendError(ctx, "Usage: /protect add <name> <x1> <y1> <z1> <x2> <y2> <z2>");
            sendInfo(ctx, "Example: /protect add spawn 30 32 -2 311 205 229");
            return;
        }

        String name = parts[0];
        try {
            int x1 = Integer.parseInt(parts[1]);
            int y1 = Integer.parseInt(parts[2]);
            int z1 = Integer.parseInt(parts[3]);
            int x2 = Integer.parseInt(parts[4]);
            int y2 = Integer.parseInt(parts[5]);
            int z2 = Integer.parseInt(parts[6]);

            protection.addRegion(name, x1, y1, z1, x2, y2, z2);
            sendSuccess(ctx, "Protected region '" + name + "' created!");
            sendInfo(ctx, "From: " + x1 + "," + y1 + "," + z1 + " to " + x2 + "," + y2 + "," + z2);
        } catch (NumberFormatException e) {
            sendError(ctx, "Invalid coordinates! All coordinates must be integers.");
        }
    }

    private void handleDelete(CommandContext ctx, SpawnProtection protection, String args) {
        if (args.isEmpty()) {
            sendError(ctx, "Usage: /protect delete <name>");
            return;
        }
        String name = args.split("\\s+")[0];
        if (protection.removeRegion(name)) {
            sendSuccess(ctx, "Region '" + name + "' deleted!");
        } else {
            sendError(ctx, "Region '" + name + "' not found!");
        }
    }

    private void handleList(CommandContext ctx, SpawnProtection protection) {
        var names = protection.getRegionNames();
        if (names.isEmpty()) {
            sendInfo(ctx, "No protected regions.");
        } else {
            sendGold(ctx, "=== Protected Regions ===");
            for (String name : names) {
                sendInfo(ctx, protection.getRegionInfo(name));
            }
        }
    }
}
