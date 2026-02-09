package pl.jailbreak.achievements;

import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AchievementManager {
    private static final Map<String, Achievement> ACHIEVEMENTS = new LinkedHashMap<>();
    private final PlayerManager playerManager;

    static {
        // ======== ACHIEVEMENTS - rewards ~5-10% of current progression level ========

        // Blocks mined (main progression tracker)
        register(new Achievement("miner_1", "Beginner Miner", "Mine 100 blocks", AchievementType.BLOCKS_MINED, 100, 250));
        register(new Achievement("miner_2", "Apprentice Miner", "Mine 1,000 blocks", AchievementType.BLOCKS_MINED, 1000, 1000));
        register(new Achievement("miner_3", "Skilled Miner", "Mine 5,000 blocks", AchievementType.BLOCKS_MINED, 5000, 5000));
        register(new Achievement("miner_4", "Expert Miner", "Mine 25,000 blocks", AchievementType.BLOCKS_MINED, 25000, 20000));
        register(new Achievement("miner_5", "Master Miner", "Mine 100,000 blocks", AchievementType.BLOCKS_MINED, 100000, 75000));
        register(new Achievement("miner_6", "Legendary Miner", "Mine 500,000 blocks", AchievementType.BLOCKS_MINED, 500000, 250000));
        register(new Achievement("miner_7", "Mining God", "Mine 1,000,000 blocks", AchievementType.BLOCKS_MINED, 1000000, 750000));

        // Balance milestones (reward = ~10% of threshold)
        register(new Achievement("rich_1", "First Savings", "Have $5,000", AchievementType.BALANCE, 5000, 500));
        register(new Achievement("rich_2", "Getting Rich", "Have $50,000", AchievementType.BALANCE, 50000, 5000));
        register(new Achievement("rich_3", "Wealthy", "Have $250,000", AchievementType.BALANCE, 250000, 25000));
        register(new Achievement("rich_4", "Millionaire", "Have $1,000,000", AchievementType.BALANCE, 1000000, 100000));
        register(new Achievement("rich_5", "Multi-Millionaire", "Have $5,000,000", AchievementType.BALANCE, 5000000, 500000));

        // Total earned (cumulative, bigger goals)
        register(new Achievement("earned_1", "Money Maker", "Earn $25,000 total", AchievementType.TOTAL_EARNED, 25000, 2500));
        register(new Achievement("earned_2", "Profit Hunter", "Earn $200,000 total", AchievementType.TOTAL_EARNED, 200000, 15000));
        register(new Achievement("earned_3", "Money Machine", "Earn $2,000,000 total", AchievementType.TOTAL_EARNED, 2000000, 100000));
        register(new Achievement("earned_4", "Economy King", "Earn $20,000,000 total", AchievementType.TOTAL_EARNED, 20000000, 1000000));

        // Daily streak (loyalty rewards)
        register(new Achievement("streak_1", "Consistent", "Reach 7 day streak", AchievementType.DAILY_STREAK, 7, 3000));
        register(new Achievement("streak_2", "Dedicated", "Reach 14 day streak", AchievementType.DAILY_STREAK, 14, 10000));
        register(new Achievement("streak_3", "Committed", "Reach 30 day streak", AchievementType.DAILY_STREAK, 30, 35000));

        // Prestige (endgame achievements - reward helps restart)
        register(new Achievement("prestige_1", "Fresh Start", "Reach Prestige 1", AchievementType.PRESTIGE, 1, 25000));
        register(new Achievement("prestige_2", "Reborn", "Reach Prestige 3", AchievementType.PRESTIGE, 3, 100000));
        register(new Achievement("prestige_3", "Transcended", "Reach Prestige 5", AchievementType.PRESTIGE, 5, 300000));
        register(new Achievement("prestige_4", "Ascended", "Reach Prestige 10", AchievementType.PRESTIGE, 10, 1000000));
    }

    private static void register(Achievement achievement) {
        ACHIEVEMENTS.put(achievement.getId(), achievement);
    }

    public AchievementManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public static Map<String, Achievement> getAllAchievements() {
        return ACHIEVEMENTS;
    }

    public static Achievement getAchievement(String id) {
        return ACHIEVEMENTS.get(id);
    }

    public List<Achievement> checkAndAwardAchievements(PlayerData player) {
        List<Achievement> newlyCompleted = new ArrayList<>();

        for (Achievement achievement : ACHIEVEMENTS.values()) {
            if (player.hasAchievement(achievement.getId())) {
                continue; // Already has this achievement
            }

            boolean completed = false;

            switch (achievement.getType()) {
                case BLOCKS_MINED:
                    completed = player.getBlocksMined() >= achievement.getRequirement();
                    break;
                case BALANCE:
                    completed = player.getBalance() >= achievement.getRequirement();
                    break;
                case TOTAL_EARNED:
                    completed = player.getTotalEarned() >= achievement.getRequirement();
                    break;
                case DAILY_STREAK:
                    completed = player.getDailyStreak() >= achievement.getRequirement();
                    break;
                case PRESTIGE:
                    completed = player.getPrestige() >= achievement.getRequirement();
                    break;
                case SECTOR_UNLOCK:
                    // Check by sector letter (A=1, B=2, etc.)
                    int required = (int) achievement.getRequirement();
                    completed = player.getUnlockedSectors().size() >= required;
                    break;
                default:
                    break;
            }

            if (completed) {
                player.addAchievement(achievement.getId());
                long reward = achievement.getReward();
                player.addBalance(reward);
                newlyCompleted.add(achievement);
                System.out.println("[Voidcraft] ACHIEVEMENT UNLOCKED: " + player.getName() + " -> " + achievement.getName() + " (+$" + reward + ")");
            }
        }

        if (!newlyCompleted.isEmpty()) {
            playerManager.savePlayer(player.getUuid());
        }

        return newlyCompleted;
    }

    public int getCompletedCount(PlayerData player) {
        return player.getCompletedAchievements().size();
    }

    public int getTotalCount() {
        return ACHIEVEMENTS.size();
    }

    public double getProgress(PlayerData player, Achievement achievement) {
        long current = 0;

        switch (achievement.getType()) {
            case BLOCKS_MINED:
                current = player.getBlocksMined();
                break;
            case BALANCE:
                current = player.getBalance();
                break;
            case TOTAL_EARNED:
                current = player.getTotalEarned();
                break;
            case DAILY_STREAK:
                current = player.getDailyStreak();
                break;
            case PRESTIGE:
                current = player.getPrestige();
                break;
            case SECTOR_UNLOCK:
                current = player.getUnlockedSectors().size();
                break;
            default:
                break;
        }

        return Math.min(1.0, (double) current / achievement.getRequirement());
    }
}
