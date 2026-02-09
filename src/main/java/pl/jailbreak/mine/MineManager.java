package pl.jailbreak.mine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all mines - creation, deletion, regeneration
 */
public class MineManager {

    private final Map<String, Mine> mines = new ConcurrentHashMap<>();
    private final Map<UUID, Vector3i> pos1Selections = new ConcurrentHashMap<>();
    private final Map<UUID, Vector3i> pos2Selections = new ConcurrentHashMap<>();
    private File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void init(File dataFolder) {
        dataFile = new File(dataFolder, "mines.json");
        load();
        System.out.println("[Voidcraft] MineManager initialized with " + mines.size() + " mines");
    }

    /**
     * Load mines from file
     */
    public void load() {
        if (!dataFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<MineData>(){}.getType();
            MineData data = gson.fromJson(reader, MineData.class);
            if (data != null && data.mines != null) {
                for (MineJsonData mineData : data.mines) {
                    Mine mine = new Mine();
                    mine.setName(mineData.name);
                    mine.setSector(mineData.sector);
                    mine.setPos1(new Vector3i(mineData.x1, mineData.y1, mineData.z1));
                    mine.setPos2(new Vector3i(mineData.x2, mineData.y2, mineData.z2));
                    mine.setRegenIntervalSeconds(mineData.regenInterval);
                    mine.setLastRegenTime(mineData.lastRegen);
                    mines.put(mine.getName().toLowerCase(), mine);
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error loading mines: " + e.getMessage());
        }
    }

    /**
     * Save mines to file
     */
    public void save() {
        try {
            dataFile.getParentFile().mkdirs();
            MineData data = new MineData();
            data.mines = new ArrayList<>();

            for (Mine mine : mines.values()) {
                MineJsonData mineData = new MineJsonData();
                mineData.name = mine.getName();
                mineData.sector = mine.getSector();
                mineData.x1 = mine.getPos1().x;
                mineData.y1 = mine.getPos1().y;
                mineData.z1 = mine.getPos1().z;
                mineData.x2 = mine.getPos2().x;
                mineData.y2 = mine.getPos2().y;
                mineData.z2 = mine.getPos2().z;
                mineData.regenInterval = mine.getRegenIntervalSeconds();
                mineData.lastRegen = mine.getLastRegenTime();
                data.mines.add(mineData);
            }

            try (Writer writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error saving mines: " + e.getMessage());
        }
    }

    /**
     * Set pos1 for player
     */
    public void setPos1(UUID playerUuid, Vector3i pos) {
        pos1Selections.put(playerUuid, pos);
    }

    /**
     * Set pos2 for player
     */
    public void setPos2(UUID playerUuid, Vector3i pos) {
        pos2Selections.put(playerUuid, pos);
    }

    /**
     * Get pos1 for player
     */
    public Vector3i getPos1(UUID playerUuid) {
        return pos1Selections.get(playerUuid);
    }

    /**
     * Get pos2 for player
     */
    public Vector3i getPos2(UUID playerUuid) {
        return pos2Selections.get(playerUuid);
    }

    /**
     * Create a new mine
     */
    public boolean createMine(String name, String sector, Vector3i pos1, Vector3i pos2) {
        if (mines.containsKey(name.toLowerCase())) {
            return false;
        }

        Mine mine = new Mine(name, sector.toUpperCase(), pos1, pos2);
        mines.put(name.toLowerCase(), mine);
        save();
        return true;
    }

    /**
     * Delete a mine
     */
    public boolean deleteMine(String name) {
        Mine removed = mines.remove(name.toLowerCase());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    /**
     * Get mine by name
     */
    public Mine getMine(String name) {
        return mines.get(name.toLowerCase());
    }

    /**
     * Get all mines
     */
    public Collection<Mine> getAllMines() {
        return mines.values();
    }

    /**
     * Get mine at position
     */
    public Mine getMineAt(int x, int y, int z) {
        for (Mine mine : mines.values()) {
            if (mine.contains(x, y, z)) {
                return mine;
            }
        }
        return null;
    }

    /**
     * Get mine at position
     */
    public Mine getMineAt(Vector3i pos) {
        return getMineAt(pos.x, pos.y, pos.z);
    }

    /**
     * Regenerate a mine (fill with ores)
     * Simple approach - just place all blocks
     */
    public int regenerateMine(Mine mine, World world) {
        if (world == null || mine == null) return 0;

        Vector3i min = mine.getMin();
        Vector3i max = mine.getMax();
        String sector = mine.getSector();

        System.out.println("[Voidcraft] Regenerating mine '" + mine.getName() + "' sector=" + sector);

        int placed = 0;

        try {
            for (int x = min.x; x <= max.x; x++) {
                for (int y = min.y; y <= max.y; y++) {
                    for (int z = min.z; z <= max.z; z++) {
                        String oreId = MineConfig.getRandomOre(sector);
                        try {
                            world.setBlock(x, y, z, oreId, 0);
                            placed++;
                        } catch (Exception e) {
                            // Block ID not found - skip
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Mine regen error: " + e.getMessage());
        }

        System.out.println("[Voidcraft] Mine '" + mine.getName() + "' regenerated: " + placed + " blocks");

        return placed;
    }

    /**
     * Send warning chat messages to all players with countdown
     */
    @SuppressWarnings("deprecation")
    public void warnPlayersWithCountdown(Mine mine, World world, int seconds) {
        if (world == null || mine == null) return;

        try {
            for (Player player : world.getPlayers()) {
                if (player == null) continue;
                try {
                    // Send big warning chat messages (no UI to avoid blocking player)
                    player.sendMessage(Message.raw("").color("#FF0000"));
                    player.sendMessage(Message.raw("========================================").color("#FF0000"));
                    player.sendMessage(Message.raw("  ⚠ WARNING! MINE REGENERATION! ⚠").color("#FFFF00"));
                    player.sendMessage(Message.raw("  Mine: " + mine.getName().toUpperCase()).color("#FFD700"));
                    player.sendMessage(Message.raw("  Leave the mine within " + seconds + " seconds!").color("#FFFFFF"));
                    player.sendMessage(Message.raw("========================================").color("#FF0000"));
                    player.sendMessage(Message.raw("").color("#FF0000"));
                } catch (Exception e) {
                    System.out.println("[Voidcraft] Could not warn player: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error warning players: " + e.getMessage());
        }
    }

    /**
     * Regenerate mine multiple times to fill all holes
     */
    public void regenerateMineMultiple(Mine mine, World world, int times) {
        if (world == null || mine == null) return;

        System.out.println("[Voidcraft] Regenerating mine " + mine.getName() + " (" + times + " times)...");

        // Warn players 15 seconds before
        warnPlayersWithCountdown(mine, world, 15);

        // Wait 15 seconds for players to escape
        try { Thread.sleep(15000); } catch (InterruptedException ie) {}

        for (int i = 1; i <= times; i++) {
            int blocks = regenerateMine(mine, world);
            System.out.println("[Voidcraft] Pass " + i + "/" + times + ": placed " + blocks + " blocks");
            try { Thread.sleep(100); } catch (InterruptedException ie) {}
        }

        mine.markRegenerated();
        save();
        System.out.println("[Voidcraft] Done regenerating " + mine.getName());
    }

    /**
     * Check and regenerate mines that need it
     */
    public void checkAndRegenerateMines(World world) {
        for (Mine mine : mines.values()) {
            if (mine.needsRegen()) {
                System.out.println("[Voidcraft] Auto-regenerating mine: " + mine.getName());
                int blocks = regenerateMine(mine, world);
                System.out.println("[Voidcraft] Regenerated " + blocks + " blocks in " + mine.getName());
            }
        }
    }

    /**
     * Get mines for a sector
     */
    public List<Mine> getMinesForSector(String sector) {
        List<Mine> result = new ArrayList<>();
        for (Mine mine : mines.values()) {
            if (mine.getSector().equalsIgnoreCase(sector)) {
                result.add(mine);
            }
        }
        return result;
    }

    // JSON data classes
    private static class MineData {
        List<MineJsonData> mines;
    }

    private static class MineJsonData {
        String name;
        String sector;
        int x1, y1, z1;
        int x2, y2, z2;
        int regenInterval;
        long lastRegen;
    }
}
