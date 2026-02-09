package pl.jailbreak.hud;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ScoreboardHud instances for all online players.
 */
public class ScoreboardManager {

    private final Map<UUID, ScoreboardHud> huds = new ConcurrentHashMap<>();
    private final Map<UUID, Player> playerRefs = new ConcurrentHashMap<>();
    private final PlayerManager playerManager;

    public ScoreboardManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    /**
     * Create and set up the HUD for a player on connect.
     */
    @SuppressWarnings("deprecation")
    public void setupHud(Player player, PlayerData data) {
        try {
            UUID uuid = player.getUuid();
            PlayerRef playerRef = player.getPlayerRef();

            ScoreboardHud hud = new ScoreboardHud(playerRef);
            huds.put(uuid, hud);
            playerRefs.put(uuid, player);

            HudManager hudManager = player.getHudManager();
            hudManager.setCustomHud(playerRef, hud);
            hud.show();

            // Initial refresh
            hud.refresh(player, data);

            System.out.println("[Voidcraft] HUD set up for " + data.getName());
        } catch (Exception e) {
            System.out.println("[Voidcraft] HUD setup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove a player's HUD on disconnect.
     */
    public void removeHud(UUID uuid) {
        huds.remove(uuid);
        playerRefs.remove(uuid);
    }

    /**
     * Refresh a specific player's HUD.
     */
    public void refreshHud(Player player, PlayerData data) {
        if (player == null || data == null) return;
        try {
            UUID uuid = player.getUuid();
            ScoreboardHud hud = huds.get(uuid);
            if (hud != null) {
                hud.refresh(player, data);
            }
        } catch (Exception e) {
            // Silently ignore refresh errors
        }
    }

    /**
     * Get all tracked HUD UUIDs (for batch refresh).
     */
    public Map<UUID, ScoreboardHud> getHuds() {
        return huds;
    }

    /**
     * Get stored Player reference by UUID (for timer-based refresh).
     */
    public Player getPlayer(UUID uuid) {
        return playerRefs.get(uuid);
    }
}
