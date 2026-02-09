package pl.jailbreak.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import pl.jailbreak.protection.SpawnProtection;
import pl.jailbreak.JailbreakPlugin;

/**
 * Blocks UseBlockEvent interactions (F key "Use", item pickup prompts) in protected zones.
 * Only blocks Use/Pick/Pickup interaction types — Secondary (right-click) is handled elsewhere.
 */
public class BlockUseProtectionSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public BlockUseProtectionSystem() {
        super(UseBlockEvent.Pre.class);
        System.out.println("[Voidcraft] BlockUseProtectionSystem created!");
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk,
                      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
                      UseBlockEvent.Pre event) {
        try {
            // Only block Use-type interactions (F key), not right-click (Secondary)
            InteractionType type = event.getInteractionType();
            if (type == InteractionType.Secondary) return;

            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityId);
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) return;

            // Skip for admins
            try {
                if (player.hasPermission("voidcraft.admin.bypass")) return;
            } catch (Exception ignored) {}

            // Check if target block is in a protected zone
            Vector3i targetBlock = event.getTargetBlock();
            if (targetBlock == null) return;

            SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
            if (protection != null && protection.isProtected(targetBlock)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] BlockUseProtection error: " + e.getMessage());
        }
    }
}
