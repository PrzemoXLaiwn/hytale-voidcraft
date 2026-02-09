package pl.jailbreak.systems;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.protection.SpawnProtection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Periodic task that makes players invulnerable in protected zones.
 * Adds/removes the Invulnerable ECS component based on player position.
 * Runs every 1 second. Uses world.getPlayers() to get online players.
 */
public class SpawnProtectionHealTask extends TimerTask {

    private World world;
    private final Set<UUID> invulnerablePlayers = ConcurrentHashMap.newKeySet();

    public SpawnProtectionHealTask() {
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void run() {
        try {
            SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
            if (protection == null) return;

            World w = this.world;
            if (w == null) return;

            Collection<Player> players = w.getPlayers();
            if (players == null || players.isEmpty()) return;

            for (Player player : players) {
                try {
                    if (player == null) continue;

                    UUID uuid = player.getUuid();

                    // Skip admins
                    boolean isAdmin = false;
                    try { isAdmin = player.hasPermission("voidcraft.admin.bypass"); } catch (Exception ignored) {}
                    if (isAdmin) continue;

                    var transform = player.getTransformComponent();
                    if (transform == null) continue;
                    var pos = transform.getPosition();
                    if (pos == null) continue;

                    int x = (int) Math.floor(pos.getX());
                    int y = (int) Math.floor(pos.getY());
                    int z = (int) Math.floor(pos.getZ());

                    boolean inProtectedZone = protection.isProtected(x, y, z);
                    boolean currentlyInvulnerable = invulnerablePlayers.contains(uuid);

                    if (inProtectedZone && !currentlyInvulnerable) {
                        // Entering protected zone — make invulnerable
                        w.execute(() -> {
                            try {
                                Ref<EntityStore> ref = player.getReference();
                                if (ref == null) return;
                                Store<EntityStore> store = ref.getStore();
                                store.addComponent(ref, Invulnerable.getComponentType(), Invulnerable.INSTANCE);
                                invulnerablePlayers.add(uuid);
                            } catch (Exception e) {
                                System.out.println("[Voidcraft] Error setting invulnerable: " + e.getMessage());
                            }
                        });
                    } else if (!inProtectedZone && currentlyInvulnerable) {
                        // Leaving protected zone — remove invulnerability
                        w.execute(() -> {
                            try {
                                Ref<EntityStore> ref = player.getReference();
                                if (ref == null) return;
                                Store<EntityStore> store = ref.getStore();
                                store.removeComponentIfExists(ref, Invulnerable.getComponentType());
                                invulnerablePlayers.remove(uuid);
                            } catch (Exception e) {
                                System.out.println("[Voidcraft] Error removing invulnerable: " + e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    // Skip this player
                }
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }

    /**
     * Clean up when player disconnects.
     */
    public void removePlayer(UUID uuid) {
        invulnerablePlayers.remove(uuid);
    }
}
