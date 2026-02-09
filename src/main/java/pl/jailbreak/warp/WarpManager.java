package pl.jailbreak.warp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Manages warp points (teleport locations)
 */
public class WarpManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, WarpPoint> warps = new LinkedHashMap<>();
    private File configFile;

    public void init(File dataFolder) {
        configFile = new File(dataFolder, "warps.json");
        load();

        // Create default warps if empty
        if (warps.isEmpty()) {
            createDefaultWarps();
            save();
        }

        System.out.println("[Voidcraft] WarpManager loaded " + warps.size() + " warps");
    }

    private void createDefaultWarps() {
        // Default spawn warp - coordinates from server
        WarpPoint spawn = new WarpPoint("spawn", "Spawn", 133.619, 96.500, 136.421);
        spawn.setDescription("Return to spawn point");
        spawn.setColor("#00FF00");
        spawn.setIcon("home");
        spawn.setSlot(0);
        warps.put("spawn", spawn);

        // Mine Sector A warp
        WarpPoint mineA = new WarpPoint("minea", "Mine A", -140.35, 107.00, 2330.64);
        mineA.setDescription("Sector A mining area");
        mineA.setColor("#FFD700");
        mineA.setIcon("pickaxe");
        mineA.setSlot(1);
        warps.put("minea", mineA);

        // Generic mine warp (fallback)
        WarpPoint mine = new WarpPoint("mine", "Mine", -140.35, 107.00, 2330.64);
        mine.setDescription("Go to the mining area");
        mine.setColor("#FFD700");
        mine.setIcon("pickaxe");
        mine.setSlot(2);
        warps.put("mine", mine);
    }

    private void load() {
        if (!configFile.exists()) return;

        try (Reader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, WarpPoint>>(){}.getType();
            Map<String, WarpPoint> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                warps.clear();
                warps.putAll(loaded);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error loading warps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(warps, writer);
        } catch (IOException e) {
            System.out.println("[Voidcraft] Error saving warps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public WarpPoint getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public Collection<WarpPoint> getAllWarps() {
        return warps.values();
    }

    public List<WarpPoint> getEnabledWarps() {
        List<WarpPoint> enabled = new ArrayList<>();
        for (WarpPoint warp : warps.values()) {
            if (warp.isEnabled()) {
                enabled.add(warp);
            }
        }
        // Sort by slot
        enabled.sort(Comparator.comparingInt(WarpPoint::getSlot));
        return enabled;
    }

    public void addWarp(WarpPoint warp) {
        warps.put(warp.getName().toLowerCase(), warp);
        save();
    }

    public boolean removeWarp(String name) {
        WarpPoint removed = warps.remove(name.toLowerCase());
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    /**
     * Teleport player to a warp point
     */
    public boolean teleportToWarp(Player player, String warpName) {
        WarpPoint warp = getWarp(warpName);
        if (warp == null) {
            player.sendMessage(Message.raw("[Voidcraft] Warp '" + warpName + "' not found!").color("#FF0000"));
            return false;
        }

        if (!warp.isEnabled()) {
            player.sendMessage(Message.raw("[Voidcraft] This warp is currently disabled!").color("#FF0000"));
            return false;
        }

        // Check permission if set
        if (warp.getPermission() != null && !warp.getPermission().isEmpty()) {
            if (!player.hasPermission(warp.getPermission())) {
                player.sendMessage(Message.raw("[Voidcraft] You don't have permission to use this warp!").color("#FF0000"));
                return false;
            }
        }

        try {
            World world = player.getWorld();
            if (world == null) {
                player.sendMessage(Message.raw("[Voidcraft] Cannot teleport - world not loaded!").color("#FF0000"));
                return false;
            }

            world.execute(() -> {
                try {
                    Ref<EntityStore> ref = player.getReference();
                    if (ref == null) {
                        player.sendMessage(Message.raw("[Voidcraft] Teleport failed!").color("#FF0000"));
                        return;
                    }

                    Store<EntityStore> store = ref.getStore();
                    Vector3d position = new Vector3d(warp.getX(), warp.getY(), warp.getZ());
                    Teleport teleport = Teleport.createForPlayer(world, position, new Vector3f(0, 0, 0));
                    store.addComponent(ref, Teleport.getComponentType(), teleport);

                    player.sendMessage(Message.raw("[Voidcraft] Teleported to " + warp.getDisplayName() + "!").color(warp.getColor()));
                } catch (Exception e) {
                    player.sendMessage(Message.raw("[Voidcraft] Teleport error: " + e.getMessage()).color("#FF0000"));
                    e.printStackTrace();
                }
            });

            return true;
        } catch (Exception e) {
            player.sendMessage(Message.raw("[Voidcraft] Teleport error: " + e.getMessage()).color("#FF0000"));
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create warp at specified coordinates
     */
    public void createWarp(String name, String displayName, double x, double y, double z) {
        WarpPoint warp = new WarpPoint(name.toLowerCase(), displayName, x, y, z);
        warp.setSlot(warps.size());
        addWarp(warp);
        System.out.println("[Voidcraft] Warp '" + displayName + "' created at " +
            String.format("%.1f, %.1f, %.1f", x, y, z));
    }

    public int getWarpCount() {
        return warps.size();
    }
}
