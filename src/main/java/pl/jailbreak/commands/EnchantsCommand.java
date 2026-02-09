package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

/**
 * Command to open enchant shop
 * Usage: /enchants
 */
public class EnchantsCommand extends JailbreakCommand {

    public EnchantsCommand() {
        super("enchants", "Open the enchantment shop");
        addAliases("enchant", "ench", "e");
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!checkCooldown(ctx, 1)) return completed();

        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        try {
            Player player = ctx.senderAs(Player.class);
            JailbreakPlugin.getEnchantManager().openEnchantShop(player, data);
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }

        return completed();
    }
}
