package pl.jailbreak.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.crates.CrateManager;
import pl.jailbreak.crates.CrateOpenPage;
import pl.jailbreak.player.PlayerData;

import java.util.concurrent.CompletableFuture;

/**
 * /crateopen <type> - Opens a crate with CS:GO style GUI
 * Aliases: opencase, opencrate
 */
public class CrateOpenCommand extends JailbreakCommand {

    private final RequiredArg<String> typeArg;

    public CrateOpenCommand() {
        super("crateopen", "Open a crate");
        addAliases("opencase", "opencrate");
        typeArg = withRequiredArg("type", "common/rare/epic/legendary/void", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) { sendError(ctx, "Players only!"); return completed(); }

        PlayerData data = getPlayerData(ctx);
        if (data == null) { sendError(ctx, "No player data!"); return completed(); }

        String crateType = ctx.get(typeArg).toLowerCase();

        if (!CrateManager.isValidCrate(crateType)) {
            sendError(ctx, "Unknown crate: " + crateType);
            sendInfo(ctx, "Types: common, rare, epic, legendary, void");
            return completed();
        }

        if (!checkCooldown(ctx, 3)) return completed();

        int keys = data.getKeys(crateType);
        if (keys <= 0) {
            sendError(ctx, "You don't have any " + CrateManager.getDisplayName(crateType) + " keys!");
            sendInfo(ctx, "Mine ores to find keys!");
            return completed();
        }

        Player player = ctx.senderAs(Player.class);

        try {
            World world = player.getWorld();
            if (world == null) {
                CrateManager.openCrate(player, data, crateType);
                return completed();
            }

            world.execute(() -> {
                try {
                    Ref<EntityStore> ref = player.getReference();
                    if (ref == null) {
                        CrateManager.openCrate(player, data, crateType);
                        return;
                    }

                    Store<EntityStore> store = ref.getStore();
                    PlayerRef playerRef = player.getPlayerRef();
                    String uuid = player.getUuid().toString();

                    CrateOpenPage page = new CrateOpenPage(playerRef, getPlayerManager(), uuid, crateType);
                    page.setPlayer(player);

                    player.getPageManager().openCustomPage(ref, store, page);
                    System.out.println("[Voidcraft] Opened crate GUI for " + data.getName() + " (" + crateType + ")");

                } catch (Exception e) {
                    System.out.println("[Voidcraft] Error opening crate page: " + e.getMessage());
                    e.printStackTrace();
                    CrateManager.openCrate(player, data, crateType);
                }
            });

        } catch (Exception e) {
            System.out.println("[Voidcraft] Crate GUI error: " + e.getMessage());
            CrateManager.openCrate(player, data, crateType);
        }

        return completed();
    }
}
