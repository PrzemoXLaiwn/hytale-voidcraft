package pl.jailbreak.protection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.math.vector.Vector3i;
import pl.jailbreak.mine.MineManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages spawn protection zones where players cannot break/place blocks
 * Mines inside protected regions are excluded from protection (players can break blocks there)
 */
public class SpawnProtection {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private List<ProtectedRegion> regions = new ArrayList<>();
    private File configFile;
    private MineManager mineManager;

    public SpawnProtection() {
    }

    /**
     * Set the mine manager reference to exclude mines from protection
     */
    public void setMineManager(MineManager mineManager) {
        this.mineManager = mineManager;
        System.out.println("[Voidcraft] SpawnProtection: MineManager linked - mines will be excluded from protection");
    }

    public void init(File dataFolder) {
        configFile = new File(dataFolder, "spawn_protection.json");
        load();
    }

    public void load() {
        if (configFile == null || !configFile.exists()) {
            // Create default with empty regions
            save();
            return;
        }

        try (Reader reader = new FileReader(configFile)) {
            SpawnProtectionData data = gson.fromJson(reader, SpawnProtectionData.class);
            if (data != null && data.regions != null) {
                regions = data.regions;
            }
            System.out.println("[Voidcraft] Loaded " + regions.size() + " protected regions");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error loading spawn protection: " + e.getMessage());
        }
    }

    public void save() {
        if (configFile == null) return;

        try {
            configFile.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(configFile)) {
                SpawnProtectionData data = new SpawnProtectionData();
                data.regions = regions;
                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error saving spawn protection: " + e.getMessage());
        }
    }

    /**
     * Check if a position is protected
     * Returns false if position is inside a mine (mines are excluded from protection)
     */
    public boolean isProtected(int x, int y, int z) {
        // First check if position is inside a mine - mines are NOT protected
        if (mineManager != null && mineManager.getMineAt(x, y, z) != null) {
            return false; // Inside mine = not protected = can break blocks
        }

        // Check if position is in any protected region
        for (ProtectedRegion region : regions) {
            if (region.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a position is protected
     */
    public boolean isProtected(Vector3i pos) {
        return isProtected(pos.x, pos.y, pos.z);
    }

    /**
     * Add a new protected region
     */
    public void addRegion(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
        // Normalize coordinates (min/max)
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        ProtectedRegion region = new ProtectedRegion();
        region.name = name;
        region.minX = minX;
        region.minY = minY;
        region.minZ = minZ;
        region.maxX = maxX;
        region.maxY = maxY;
        region.maxZ = maxZ;

        regions.add(region);
        save();

        System.out.println("[Voidcraft] Added protected region: " + name);
    }

    /**
     * Remove a protected region by name
     */
    public boolean removeRegion(String name) {
        boolean removed = regions.removeIf(r -> r.name.equalsIgnoreCase(name));
        if (removed) {
            save();
            System.out.println("[Voidcraft] Removed protected region: " + name);
        }
        return removed;
    }

    /**
     * Get all region names
     */
    public List<String> getRegionNames() {
        List<String> names = new ArrayList<>();
        for (ProtectedRegion region : regions) {
            names.add(region.name);
        }
        return names;
    }

    /**
     * Get region info
     */
    public String getRegionInfo(String name) {
        for (ProtectedRegion region : regions) {
            if (region.name.equalsIgnoreCase(name)) {
                return region.name + ": (" + region.minX + "," + region.minY + "," + region.minZ +
                       ") to (" + region.maxX + "," + region.maxY + "," + region.maxZ + ")";
            }
        }
        return null;
    }

    // Data classes for JSON
    private static class SpawnProtectionData {
        List<ProtectedRegion> regions = new ArrayList<>();
    }

    public static class ProtectedRegion {
        String name;
        int minX, minY, minZ;
        int maxX, maxY, maxZ;

        public boolean contains(int x, int y, int z) {
            return x >= minX && x <= maxX &&
                   y >= minY && y <= maxY &&
                   z >= minZ && z <= maxZ;
        }
    }
}
