package pl.jailbreak.achievements;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

public class AchievementCheckTask extends TimerTask {
    private final AchievementManager achievementManager;
    private final PlayerManager playerManager;
    private World world;

    public AchievementCheckTask(AchievementManager achievementManager, PlayerManager playerManager) {
        this.achievementManager = achievementManager;
        this.playerManager = playerManager;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        if (world == null) {
            return;
        }

        try {
            world.execute(() -> {
                var players = playerManager.getAllPlayers();
                if (players.isEmpty()) {
                    return;
                }

                for (PlayerData data : players) {
                    try {
                        List<Achievement> newAchievements = achievementManager.checkAndAwardAchievements(data);

                        if (!newAchievements.isEmpty()) {
                            // Find player and notify them
                            Player player = findPlayer(data.getUuid());
                            if (player != null) {
                                for (Achievement ach : newAchievements) {
                                    notifyPlayer(player, ach);
                                }
                            } else {
                                // Player offline - achievements still awarded and saved
                                System.out.println("[Voidcraft] Player " + data.getName() + " offline but achievements awarded: " + newAchievements.size());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[Voidcraft] Error checking achievements for " + data.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error in achievement check task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Player findPlayer(String uuidStr) {
        if (world == null) return null;
        try {
            UUID uuid = UUID.fromString(uuidStr);
            for (Player player : world.getPlayers()) {
                if (player.getUuid().equals(uuid)) {
                    return player;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private void notifyPlayer(Player player, Achievement achievement) {
        player.sendMessage(Message.raw("").color("#FFFFFF"));
        player.sendMessage(Message.raw("========================================").color("#FFD700"));
        player.sendMessage(Message.raw("  ACHIEVEMENT UNLOCKED!").color("#00FF00"));
        player.sendMessage(Message.raw("  " + achievement.getName()).color("#FFD700"));
        player.sendMessage(Message.raw("  " + achievement.getDescription()).color("#AAAAAA"));
        player.sendMessage(Message.raw("  Reward: $" + String.format("%,d", achievement.getReward())).color("#00FF00"));
        player.sendMessage(Message.raw("========================================").color("#FFD700"));
        player.sendMessage(Message.raw("").color("#FFFFFF"));
    }
}
