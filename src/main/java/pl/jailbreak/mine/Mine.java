package pl.jailbreak.mine;

import com.hypixel.hytale.math.vector.Vector3i;

/**
 * Represents a mine region with regenerating ores
 */
public class Mine {
    private String name;
    private String sector;
    private Vector3i pos1;
    private Vector3i pos2;
    private long lastRegenTime;
    private int regenIntervalSeconds;

    public Mine(String name, String sector, Vector3i pos1, Vector3i pos2) {
        this.name = name;
        this.sector = sector;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.lastRegenTime = System.currentTimeMillis();
        this.regenIntervalSeconds = 300; // 5 minutes default
    }

    // For deserialization
    public Mine() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public Vector3i getPos1() { return pos1; }
    public void setPos1(Vector3i pos1) { this.pos1 = pos1; }

    public Vector3i getPos2() { return pos2; }
    public void setPos2(Vector3i pos2) { this.pos2 = pos2; }

    public long getLastRegenTime() { return lastRegenTime; }
    public void setLastRegenTime(long time) { this.lastRegenTime = time; }

    public int getRegenIntervalSeconds() { return regenIntervalSeconds; }
    public void setRegenIntervalSeconds(int seconds) { this.regenIntervalSeconds = seconds; }

    /**
     * Get minimum coordinates
     */
    public Vector3i getMin() {
        return new Vector3i(
            Math.min(pos1.x, pos2.x),
            Math.min(pos1.y, pos2.y),
            Math.min(pos1.z, pos2.z)
        );
    }

    /**
     * Get maximum coordinates
     */
    public Vector3i getMax() {
        return new Vector3i(
            Math.max(pos1.x, pos2.x),
            Math.max(pos1.y, pos2.y),
            Math.max(pos1.z, pos2.z)
        );
    }

    /**
     * Get total block count
     */
    public int getBlockCount() {
        Vector3i min = getMin();
        Vector3i max = getMax();
        int width = max.x - min.x + 1;
        int height = max.y - min.y + 1;
        int depth = max.z - min.z + 1;
        return width * height * depth;
    }

    /**
     * Check if position is inside this mine
     */
    public boolean contains(int x, int y, int z) {
        Vector3i min = getMin();
        Vector3i max = getMax();
        return x >= min.x && x <= max.x &&
               y >= min.y && y <= max.y &&
               z >= min.z && z <= max.z;
    }

    /**
     * Check if position is inside this mine
     */
    public boolean contains(Vector3i pos) {
        return contains(pos.x, pos.y, pos.z);
    }

    /**
     * Check if mine needs regeneration
     */
    public boolean needsRegen() {
        long elapsed = System.currentTimeMillis() - lastRegenTime;
        return elapsed >= (regenIntervalSeconds * 1000L);
    }

    /**
     * Mark as regenerated
     */
    public void markRegenerated() {
        this.lastRegenTime = System.currentTimeMillis();
    }

    /**
     * Get time until next regen in seconds
     */
    public int getSecondsUntilRegen() {
        long elapsed = System.currentTimeMillis() - lastRegenTime;
        long remaining = (regenIntervalSeconds * 1000L) - elapsed;
        return Math.max(0, (int)(remaining / 1000));
    }
}
