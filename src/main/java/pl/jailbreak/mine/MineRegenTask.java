package pl.jailbreak.mine;

import com.hypixel.hytale.server.core.universe.world.World;

import java.util.TimerTask;

/**
 * Timer task that checks and regenerates mines automatically
 * World reference must be set when a player joins
 */
public class MineRegenTask extends TimerTask {

    private final MineManager mineManager;
    private volatile World world;

    public MineRegenTask(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    /**
     * Set the world reference (call when player joins)
     */
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        if (world == null) {
            System.out.println("[Voidcraft] MineRegenTask: World is null, skipping");
            return;
        }

        try {
            // Execute on world thread for thread safety
            world.execute(() -> {
                try {
                    int mineCount = 0;
                    for (Mine mine : mineManager.getAllMines()) {
                        mineCount++;
                        if (mine.needsRegen()) {
                            System.out.println("[Voidcraft] Auto-regenerating mine: " + mine.getName());
                            mineManager.regenerateMineMultiple(mine, world, 10);
                        }
                    }
                    if (mineCount > 0) {
                        System.out.println("[Voidcraft] MineRegenTask checked " + mineCount + " mines");
                    }
                } catch (Exception e) {
                    System.out.println("[Voidcraft] Error in mine regen task: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error executing mine regen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
