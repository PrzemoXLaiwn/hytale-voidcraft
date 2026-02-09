package pl.jailbreak.commands.admin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import pl.jailbreak.commands.JailbreakCommand;
import pl.jailbreak.config.EconomyConfig;
import java.util.concurrent.CompletableFuture;

public class JailbreakReloadCommand extends JailbreakCommand {

    public JailbreakReloadCommand() {
        super("jreload", "Reload config");
        requirePermission("voidcraft.admin");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        EconomyConfig.load();
        sendSuccess(ctx, "Config reloaded!");
        return completed();
    }
}