package pl.jailbreak.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

import java.util.concurrent.CompletableFuture;

public class SectorTpCommand extends JailbreakCommand {

    private final RequiredArg<String> sectorArg;

    public SectorTpCommand() {
        super("sectortp", "Teleport to sector");
        addAliases("tpsector", "minetp");
        sectorArg = withRequiredArg("sector", "A-J", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            sendError(ctx, "Players only!");
            return completed();
        }

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
            sendInfo(ctx, "Use /sectorbuy to buy next sector");
            return completed();
        }

        Sector sector = SectorManager.get(sectorId);

        try {
            Player player = ctx.senderAs(Player.class);
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
}