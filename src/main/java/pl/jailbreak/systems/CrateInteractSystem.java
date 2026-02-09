package pl.jailbreak.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;

import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.crates.CrateLocationManager;
import pl.jailbreak.crates.CrateManager;
import pl.jailbreak.crates.CrateOpenPage;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts UseBlockEvent.Pre (right-click) on chest blocks registered as crate locations.
 * Cancels default chest open and opens the crate UI instead.
 */
public class CrateInteractSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    private final PlayerManager playerManager;
    private final CrateLocationManager crateLocationManager;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000;

    public CrateInteractSystem(PlayerManager playerManager, CrateLocationManager crateLocationManager) {
        super(UseBlockEvent.Pre.class);
        this.playerManager = playerManager;
        this.crateLocationManager = crateLocationManager;
        System.out.println("[Voidcraft] CrateInteractSystem created!");
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk,
                      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
                      UseBlockEvent.Pre event) {
        try {
            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityId);
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) return;

            Vector3i targetBlock = event.getTargetBlock();
            if (targetBlock == null) return;

            // Check if this block position is a registered crate
            String crateType = crateLocationManager.getCrateTypeAt(targetBlock);
            if (crateType == null) return;

            // It's a crate! Cancel the default chest open
            event.setCancelled(true);

            UUID playerUuid = player.getUuid();

            // Cooldown
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(playerUuid);
            if (lastUse != null && (now - lastUse) < COOLDOWN_MS) {
                return;
            }
            cooldowns.put(playerUuid, now);

            String uuid = playerUuid.toString();
            PlayerData data = playerManager.getPlayer(uuid);
            if (data == null) return;

            int keys = data.getKeys(crateType);
            String color = CrateManager.getColor(crateType);
            String displayName = CrateManager.getDisplayName(crateType);

            // Show key count message
            if (keys <= 0) {
                player.sendMessage(Message.raw("[Voidcraft] " + displayName + " - You have 0 keys!").color("#FF6666"));
                player.sendMessage(Message.raw("[Voidcraft] Mine ores to find keys!").color("#AAAAAA"));
                return;
            }

            // Open crate UI
            player.sendMessage(Message.raw("[Voidcraft] Opening " + displayName + "... (" + keys + " keys)").color(color));

            try {
                World world = player.getWorld();
                if (world != null) {
                    world.execute(() -> {
                        try {
                            Ref<EntityStore> pRef = player.getReference();
                            if (pRef != null) {
                                Store<EntityStore> pStore = pRef.getStore();
                                CrateOpenPage page = new CrateOpenPage(
                                    player.getPlayerRef(), playerManager, uuid, crateType
                                );
                                page.setPlayer(player);
                                player.getPageManager().openCustomPage(pRef, pStore, page);
                            }
                        } catch (Exception e) {
                            System.out.println("[Voidcraft] Error opening crate page: " + e.getMessage());
                            CrateManager.openCrate(player, data, crateType);
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("[Voidcraft] Crate interact error: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("[Voidcraft] CrateInteract error: " + e.getMessage());
        }
    }
}
