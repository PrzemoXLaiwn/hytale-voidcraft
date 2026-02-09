package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class JailbreakCommand extends AbstractCommand {

    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    protected static final String PREFIX = "[Voidcraft] ";
    protected static final String COLOR_SUCCESS = "#00FF00";
    protected static final String COLOR_ERROR = "#FF0000";
    protected static final String COLOR_INFO = "#00BFFF";
    protected static final String COLOR_GOLD = "#FFD700";
    protected static final String COLOR_MONEY = "#00FF00";

    public JailbreakCommand(String name, String desc) {
        super(name, desc);
        // Make all player commands accessible to everyone (Default group = all players)
        // Admin commands override this by calling requirePermission() in their constructor
        setPermissionGroups("Default");
    }

    protected CompletableFuture<Void> completed() {
        return CompletableFuture.completedFuture(null);
    }

    protected PlayerManager getPlayerManager() {
        return JailbreakPlugin.getPlayerManager();
    }

    protected PlayerData getPlayerData(CommandContext ctx) {
        if (!ctx.isPlayer()) return null;
        try {
            Player player = ctx.senderAs(Player.class);
            String uuid = player.getUuid().toString();
            return getPlayerManager().getPlayer(uuid);
        } catch (Exception e) { return null; }
    }

    protected boolean checkCooldown(CommandContext ctx, int seconds) {
        String id = ctx.isPlayer() ? ctx.senderAs(Player.class).getUuid().toString() : "console";
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(id);
        if (last != null && now - last < seconds * 1000L) {
            sendError(ctx, "Wait " + seconds + "s!");
            return false;
        }
        cooldowns.put(id, now);
        return true;
    }

    protected void sendSuccess(CommandContext ctx, String msg) {
        ctx.sendMessage(Message.raw(PREFIX + msg).color(COLOR_SUCCESS));
    }

    protected void sendError(CommandContext ctx, String msg) {
        ctx.sendMessage(Message.raw(PREFIX + msg).color(COLOR_ERROR));
    }

    protected void sendInfo(CommandContext ctx, String msg) {
        ctx.sendMessage(Message.raw(PREFIX + msg).color(COLOR_INFO));
    }

    protected void sendGold(CommandContext ctx, String msg) {
        ctx.sendMessage(Message.raw(msg).color(COLOR_GOLD));
    }

    protected String formatMoney(long amount) {
        return "$" + String.format("%,d", amount);
    }
}