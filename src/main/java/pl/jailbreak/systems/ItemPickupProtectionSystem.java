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

import pl.jailbreak.protection.SpawnProtection;
import pl.jailbreak.JailbreakPlugin;

/**
 * Prevents non-admin players from picking up items (F key) in protected zones.
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
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk,
                      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
                      InteractivelyPickupItemEvent event) {
        try {
            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityId);
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) return;

            // Skip for admins
            try {
                if (player.hasPermission("voidcraft.admin.bypass")) return;
            } catch (Exception ignored) {}

            // Check if player is in a protected zone
            SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
            if (protection == null) return;

            var transform = player.getTransformComponent();
            if (transform == null) return;
            var pos = transform.getPosition();
            if (pos == null) return;

            int x = (int) Math.floor(pos.getX());
            int y = (int) Math.floor(pos.getY());
            int z = (int) Math.floor(pos.getZ());

            if (protection.isProtected(x, y, z)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] ItemPickupProtection error: " + e.getMessage());
        }
    }
}
