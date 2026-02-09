package pl.jailbreak.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;
import pl.jailbreak.warp.WarpManager;
import pl.jailbreak.warp.WarpMenuPage;

import java.util.concurrent.CompletableFuture;

/**
 * /warp - THE teleportation command. Handles everything:
 *   /warp                    → open warp menu GUI
 *   /warp --name=spawn       → teleport to warp point
 *   /warp --sector=B         → teleport to sector B
 */
public class WarpCommand extends JailbreakCommand {

    private final OptionalArg<String> warpArg;
    private final OptionalArg<String> sectorArg;

    public WarpCommand() {
        super("warp", "Teleport to a location or open warp menu");
        addAliases("warps", "tp", "teleport");
        warpArg = withOptionalArg("name", "Warp name", ArgTypes.STRING);
        sectorArg = withOptionalArg("sector", "Sector ID (A-J)", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

        if (!checkCooldown(ctx, 2)) return completed();

        Player player = ctx.senderAs(Player.class);

        // Priority: --sector first, then --name, then menu
        if (ctx.provided(sectorArg)) {
            return handleSectorTeleport(ctx, player);
        } else if (ctx.provided(warpArg)) {
            return handleWarpTeleport(ctx, player);
        } else {
            openWarpMenu(player);
            return completed();
        }
    }

    private CompletableFuture<Void> handleSectorTeleport(CommandContext ctx, Player player) {
        PlayerData data = getPlayerData(ctx);
        if (data == null) {
            sendError(ctx, "No player data!");
            return completed();
        }

        String sectorId = ctx.get(sectorArg).toUpperCase();

        if (!SectorManager.exists(sectorId)) {
            sendError(ctx, "Sector " + sectorId + " does not exist!");
            return completed();
        }

        if (!SectorManager.canAccess(sectorId, data.getCurrentSector())) {
            sendError(ctx, "You haven't unlocked sector " + sectorId + "!");
            sendInfo(ctx, "Use /rankup to buy next sector");
            return completed();
        }

        Sector sector = SectorManager.get(sectorId);

        try {
            World world = player.getWorld();

            world.execute(() -> {
                Ref<EntityStore> ref = player.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();

                Teleport teleport = Teleport.createForPlayer(
                    world,
                    new Vector3d(sector.x, sector.y, sector.z),
                    new Vector3f(0, 0, 0)
                );

                store.addComponent(ref, Teleport.getComponentType(), teleport);
            });

            sendSuccess(ctx, "Teleported to " + sector.name + "!");
        } catch (Exception e) {
            sendError(ctx, "Teleport failed: " + e.getMessage());
        }

        return completed();
    }

    private CompletableFuture<Void> handleWarpTeleport(CommandContext ctx, Player player) {
        String warpName = ctx.get(warpArg);
        WarpManager warpManager = JailbreakPlugin.getWarpManager();
        warpManager.teleportToWarp(player, warpName);
        return completed();
    }

    private void openWarpMenu(Player player) {
        try {
            World world = player.getWorld();
            if (world == null) {
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Voidcraft] Cannot open menu!").color("#FF0000"));
                return;
            }

            world.execute(() -> {
                try {
                    Ref<EntityStore> ref = player.getReference();
                    if (ref == null) return;

                    Store<EntityStore> store = ref.getStore();
                    WarpMenuPage page = new WarpMenuPage(player.getPlayerRef());
                    page.setPlayer(player);

                    player.getPageManager().openCustomPage(ref, store, page);
                } catch (Exception e) {
                    System.out.println("[Voidcraft] Error opening warp menu: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("[Voidcraft] Warp menu error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
