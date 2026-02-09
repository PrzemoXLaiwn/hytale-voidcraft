package pl.jailbreak.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

import pl.jailbreak.protection.SpawnProtection;
import pl.jailbreak.JailbreakPlugin;

/**
 * Prevents item pickup (F key) in protected zones.
 * The event fires on the ITEM ENTITY, not the player.
 * We check the item entity's position to determine if it's in a protected zone.
 */
public class ItemPickupProtectionSystem extends EntityEventSystem<EntityStore, InteractivelyPickupItemEvent> {

    public ItemPickupProtectionSystem() {
        super(InteractivelyPickupItemEvent.class);
        System.out.println("[Voidcraft] ItemPickupProtectionSystem created!");
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    @SuppressWarnings("removal")
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk,
                      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
                      InteractivelyPickupItemEvent event) {
        try {
            SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
            if (protection == null) return;

            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityId);

            // The event fires on the ITEM entity, not the player.
            // Try to get the item entity's position via TransformComponent.
            try {
                TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
                if (transform != null) {
                    var pos = transform.getPosition();
                    if (pos != null) {
                        int x = (int) Math.floor(pos.getX());
                        int y = (int) Math.floor(pos.getY());
                        int z = (int) Math.floor(pos.getZ());

                        if (protection.isProtected(x, y, z)) {
                            event.setCancelled(true);
                            System.out.println("[Voidcraft] Blocked item pickup at " + x + "," + y + "," + z + " (protected zone)");
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[Voidcraft] ItemPickup transform check error: " + e.getMessage());
            }

            // Fallback: also check if a Player component exists on the entity (shouldn't, but just in case)
            try {
                Player player = store.getComponent(entityRef, Player.getComponentType());
                if (player != null) {
                    // Skip for admins
                    try {
                        if (player.hasPermission("voidcraft.admin.bypass")) return;
                    } catch (Exception ignored) {}

                    var transform = player.getTransformComponent();
                    if (transform != null) {
                        var pos = transform.getPosition();
                        if (pos != null) {
                            int x = (int) Math.floor(pos.getX());
                            int y = (int) Math.floor(pos.getY());
                            int z = (int) Math.floor(pos.getZ());
                            if (protection.isProtected(x, y, z)) {
                                event.setCancelled(true);
                                System.out.println("[Voidcraft] Blocked item pickup by player at " + x + "," + y + "," + z);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.out.println("[Voidcraft] ItemPickupProtection error: " + e.getMessage());
        }
    }
}
