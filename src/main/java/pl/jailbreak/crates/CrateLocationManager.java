package pl.jailbreak.crates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.math.vector.Vector3i;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps chest block positions on spawn to crate types.
 * Admin places a chest, runs /crateadd --type=common, and that chest becomes a Common Crate.
 */
public class CrateLocationManager {

    private final Map<String, CrateLocationData> locations = new ConcurrentHashMap<>();
    private File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void init(File dataFolder) {
        dataFile = new File(dataFolder, "crate_locations.json");
        load();
        System.out.println("[Voidcraft] CrateLocationManager loaded with " + locations.size() + " crate locations");
    }

    public void load() {
        if (!dataFile.exists()) return;
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, CrateLocationData>>(){}.getType();
            Map<String, CrateLocationData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                locations.clear();
                locations.putAll(loaded);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error loading crate locations: " + e.getMessage());
        }
    }

    public void save() {
        try {
            dataFile.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(dataFile)) {
                gson.toJson(locations, writer);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error saving crate locations: " + e.getMessage());
        }
    }

    /**
     * Add a crate location
     */
    public void addLocation(int x, int y, int z, String crateType) {
        String key = posKey(x, y, z);
        locations.put(key, new CrateLocationData(x, y, z, crateType));
        save();
    }

    /**
     * Remove a crate location
     */
    public boolean removeLocation(int x, int y, int z) {
        String key = posKey(x, y, z);
        CrateLocationData removed = locations.remove(key);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    /**
     * Get crate type at position, or null if not a crate
     */
    public String getCrateTypeAt(int x, int y, int z) {
        CrateLocationData loc = locations.get(posKey(x, y, z));
        return loc != null ? loc.crateType : null;
    }

    /**
     * Get crate type at position
     */
    public String getCrateTypeAt(Vector3i pos) {
        return getCrateTypeAt(pos.x, pos.y, pos.z);
    }

    /**
     * Get all crate locations
     */
    public Collection<CrateLocationData> getAllLocations() {
        return locations.values();
    }

    private String posKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public static class CrateLocationData {
        public int x, y, z;
        public String crateType;

        public CrateLocationData() {}

        public CrateLocationData(int x, int y, int z, String crateType) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.crateType = crateType;
        }
    }
}
