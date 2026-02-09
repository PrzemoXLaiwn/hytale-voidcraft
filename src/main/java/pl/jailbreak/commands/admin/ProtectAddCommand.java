package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.protection.SpawnProtection;

import java.util.concurrent.CompletableFuture;

/**
 * /protectadd <name> <x1> <y1> <z1> <x2> <y2> <z2>
 */
public class ProtectAddCommand extends JailbreakCommand {
    private final RequiredArg<String> nameArg;
    private final RequiredArg<Integer> x1Arg;
    private final RequiredArg<Integer> y1Arg;
    private final RequiredArg<Integer> z1Arg;
    private final RequiredArg<Integer> x2Arg;
    private final RequiredArg<Integer> y2Arg;
    private final RequiredArg<Integer> z2Arg;

    public ProtectAddCommand() {
        super("protectadd", "Add protected region");
        requirePermission("voidcraft.admin.protect");
        nameArg = withRequiredArg("name", "Region name", ArgTypes.STRING);
        x1Arg = withRequiredArg("x1", "X1", ArgTypes.INTEGER);
        y1Arg = withRequiredArg("y1", "Y1", ArgTypes.INTEGER);
        z1Arg = withRequiredArg("z1", "Z1", ArgTypes.INTEGER);
        x2Arg = withRequiredArg("x2", "X2", ArgTypes.INTEGER);
        y2Arg = withRequiredArg("y2", "Y2", ArgTypes.INTEGER);
        z2Arg = withRequiredArg("z2", "Z2", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
        if (protection == null) {
            sendError(ctx, "Spawn protection not initialized!");
            return completed();
        }

        String name = ctx.get(nameArg);
        int x1 = ctx.get(x1Arg);
        int y1 = ctx.get(y1Arg);
        int z1 = ctx.get(z1Arg);
        int x2 = ctx.get(x2Arg);
        int y2 = ctx.get(y2Arg);
        int z2 = ctx.get(z2Arg);

        protection.addRegion(name, x1, y1, z1, x2, y2, z2);
        sendSuccess(ctx, "Protected region '" + name + "' created!");
        sendInfo(ctx, "From: " + x1 + "," + y1 + "," + z1 + " to " + x2 + "," + y2 + "," + z2);

        return completed();
    }
}
