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
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.protocol.InteractionType;

import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.crates.CrateLocationManager;
import pl.jailbreak.enchants.PickaxeEnchantUtil;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts UseBlockEvent.Pre when player is holding a pickaxe.
 * Opens the enchant shop UI on right-click (unless the target is a crate chest).
 */
public class PickaxeInteractSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    private final PlayerManager playerManager;
    private final CrateLocationManager crateLocationManager;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000;

    public PickaxeInteractSystem(PlayerManager playerManager, CrateLocationManager crateLocationManager) {
        super(UseBlockEvent.Pre.class);
        this.playerManager = playerManager;
        this.crateLocationManager = crateLocationManager;
        System.out.println("[Voidcraft] PickaxeInteractSystem created!");
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

            // Only react to right-click (Secondary), not F key (Use/Pickup) or left-click (Primary)
            InteractionType interactionType = event.getInteractionType();
            if (interactionType != InteractionType.Secondary) return;

            // Check if player is holding a pickaxe
            Inventory inv = player.getInventory();
            if (inv == null) return;
            ItemStack heldItem = inv.getItemInHand();
            if (!PickaxeEnchantUtil.isPickaxe(heldItem)) return;

            // If target block is a crate location, skip (let CrateInteractSystem handle it)
            Vector3i targetBlock = event.getTargetBlock();
            if (targetBlock != null && crateLocationManager != null) {
                String crateType = crateLocationManager.getCrateTypeAt(targetBlock);
                if (crateType != null) return;
            }

            // Cancel the default block use action
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

            // Open enchant shop
            JailbreakPlugin.getEnchantManager().openEnchantShop(player, data);

        } catch (Exception e) {
            System.out.println("[Voidcraft] PickaxeInteract error: " + e.getMessage());
        }
    }
}
