package pl.jailbreak.mine;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import pl.jailbreak.commands.JailbreakCommand;

import java.util.concurrent.CompletableFuture;

/**
 * /minedelete --name=ironmine
 */
public class MineDeleteCommand extends JailbreakCommand {

    private final MineManager mineManager;
    private final OptionalArg<String> nameArg;

    public MineDeleteCommand(MineManager mineManager) {
        super("minedelete", "Delete a mine (--name=X)");
        requirePermission("voidcraft.admin.mine");
        this.mineManager = mineManager;
        addAliases("mineremove");
        nameArg = withOptionalArg("name", "Mine name", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!ctx.provided(nameArg)) {
            sendError(ctx, "Usage: /minedelete --name=ironmine");
            return completed();
        }

        String name = ctx.get(nameArg);

        if (mineManager.deleteMine(name)) {
            sendSuccess(ctx, "Mine '" + name + "' deleted!");
        } else {
            sendError(ctx, "Mine '" + name + "' not found!");
        }

        return completed();
    }
}
