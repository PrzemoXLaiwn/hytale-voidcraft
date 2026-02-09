package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

public class BuyCommand extends JailbreakCommand {

    private final RequiredArg<Integer> tierArg;

    public BuyCommand() {
        super("buy", "Buy a pickaxe from the shop");
        addAliases("kup", "purchase");
        tierArg = withRequiredArg("tier", "Pickaxe number (1-9)", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!checkCooldown(ctx, 2)) return completed();

        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        int tier = ctx.get(tierArg);
        if (tier < 1 || tier > 9) {
            sendError(ctx, "Invalid tier! Use 1-9. Type /shop to see options.");
            return completed();
        }

        try {
            Player player = ctx.senderAs(Player.class);
            JailbreakPlugin.getShopManager().buyItem(player, data, tier);
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }

        return completed();
    }
}
