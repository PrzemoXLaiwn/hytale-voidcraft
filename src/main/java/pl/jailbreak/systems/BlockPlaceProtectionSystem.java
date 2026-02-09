package pl.jailbreak.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import pl.jailbreak.protection.SpawnProtection;
import pl.jailbreak.JailbreakPlugin;

/**
 * ECS System that prevents block placement in protected areas
 */
public class BlockPlaceProtectionSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public BlockPlaceProtectionSystem() {
        super(PlaceBlockEvent.class);
        System.out.println("[Voidcraft] BlockPlaceProtectionSystem created!");
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk,
                      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
                      PlaceBlockEvent event) {
        try {
            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityId);
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) return;

            // Skip protection for operators (players with admin permission)
            try {
                if (player.hasPermission("voidcraft.admin.bypass")) return;
            } catch (Exception ignored) {}

            Vector3i targetBlock = event.getTargetBlock();
            if (targetBlock != null) {
                SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
                if (protection != null && protection.isProtected(targetBlock)) {
                    player.sendMessage(Message.raw("You cannot place blocks here!").color("#FF0000"));
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] BlockPlaceProtection error: " + e.getMessage());
        }
    }
}
