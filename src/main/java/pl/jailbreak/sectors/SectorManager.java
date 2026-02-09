package pl.jailbreak.sectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class SectorManager {
    
    private static final Map<String, Sector> SECTORS = new LinkedHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    
    static {
        addDefault("A", "Starter Mine", 0, "Stone, Copper");
        addDefault("B", "Iron Depths", 5000, "Iron, Copper");
        addDefault("C", "Thorium Tunnel", 15000, "Thorium, Iron");
        addDefault("D", "Golden Cave", 40000, "Gold, Silver");
        addDefault("E", "Cobalt Corridor", 100000, "Cobalt");
        addDefault("F", "Adamantite Abyss", 250000, "Adamantite");
        addDefault("G", "Diamond Grotto", 600000, "Diamond, Ruby");
        addDefault("H", "Emerald Sanctuary", 1200000, "Emerald, Sapphire");
        addDefault("I", "Crystal Chamber", 2500000, "Topaz, All Gems");
        addDefault("J", "Ancient Mine", 5000000, "All Ores");
    }
    
    private static void addDefault(String id, String name, long price, String ores) {
        SECTORS.put(id, new Sector(id, name, price, ores, 0, 64, 0));
    }
    
    public static void init(File dataFolder) {
        configFile = new File(dataFolder, "sectors.json");
        loadCoords();
    }
    
    public static void setCoords(String id, int x, int y, int z) {
        Sector old = SECTORS.get(id.toUpperCase());
        if (old != null) {
            SECTORS.put(id.toUpperCase(), new Sector(old.id, old.name, old.price, old.ores, x, y, z));
            saveCoords();
        }
    }
    
    private static void saveCoords() {
        try {
            Map<String, int[]> coordsMap = new HashMap<>();
            for (Sector s : SECTORS.values()) {
                coordsMap.put(s.id, new int[]{s.x, s.y, s.z});
            }
            try (Writer writer = new FileWriter(configFile)) {
                GSON.toJson(coordsMap, writer);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private static void loadCoords() {
        if (!configFile.exists()) { saveCoords(); return; }
        try (Reader reader = new FileReader(configFile)) {
            Map<String, int[]> coordsMap = GSON.fromJson(reader, new TypeToken<Map<String, int[]>>(){}.getType());
            if (coordsMap != null) {
                for (Map.Entry<String, int[]> entry : coordsMap.entrySet()) {
                    String id = entry.getKey();
                    int[] coords = entry.getValue();
                    Sector old = SECTORS.get(id);
                    if (old != null && coords.length >= 3) {
                        SECTORS.put(id, new Sector(old.id, old.name, old.price, old.ores, coords[0], coords[1], coords[2]));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public static Sector get(String id) { return SECTORS.get(id.toUpperCase()); }
    public static Collection<Sector> getAll() { return SECTORS.values(); }
    public static Sector getNext(String currentId) {
        Sector current = get(currentId);
        return current != null && current.hasNext() ? get(current.nextSectorId) : null;
    }
    public static boolean exists(String id) { return SECTORS.containsKey(id.toUpperCase()); }
    public static boolean canAccess(String sectorId, String playerHighestSector) {
        return sectorId.charAt(0) <= playerHighestSector.charAt(0);
    }

    public static boolean hasCoords(String id) {
        Sector s = get(id);
        if (s == null) return false;
        return !(s.x == 0 && s.y == 64 && s.z == 0);
    }
}