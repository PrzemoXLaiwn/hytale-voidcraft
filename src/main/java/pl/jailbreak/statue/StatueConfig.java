package pl.jailbreak.statue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.math.Vector3d;
import com.hypixel.hytale.math.Vector3f;

import java.io.*;
import java.util.*;

/**
 * Konfiguracja systemu statuelek - pozycje, rotacje, interval aktualizacji.
 * Dane przechowywane w statue_config.json
 */
public class StatueConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private File dataFile;
    private ConfigData config;

    /**
     * Inicjalizacja - wczytaj lub utwórz domyślną konfigurację
     */
    public void init(File dataFolder) {
        this.dataFile = new File(dataFolder, "statue_config.json");
        load();
        System.out.println("[Voidcraft] StatueConfig loaded: " + config.statues.size() + " statue positions");
    }

    /**
     * Wczytaj konfigurację z pliku
     */
    private void load() {
        if (!dataFile.exists()) {
            createDefault();
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            config = GSON.fromJson(reader, ConfigData.class);
            if (config == null || config.statues == null) {
                System.out.println("[StatueConfig] Invalid config file, creating default");
                createDefault();
            }
        } catch (Exception e) {
            System.out.println("[StatueConfig] Error loading config: " + e.getMessage());
            createDefault();
        }
    }

    /**
     * Zapisz konfigurację do pliku
     */
    public void save() {
        try {
            dataFile.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(dataFile)) {
                GSON.toJson(config, writer);
            }
            System.out.println("[StatueConfig] Configuration saved");
        } catch (Exception e) {
            System.out.println("[StatueConfig] Error saving config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tworzy domyślną konfigurację z pozycjami wokół spawnu
     */
    private void createDefault() {
        config = new ConfigData();
        config.updateIntervalMinutes = 10;
        config.npcModelKey = "Player";
        config.statues = new ArrayList<>();

        // Pozycje wokół spawnu (133.619, 96.500, 136.421)
        // #1 - Północ (patrzą do środka)
        config.statues.add(new StatueLocation(1, 134.5, 97.0, 141.5, 180f, 0f, 0f));

        // #2 - Zachód
        config.statues.add(new StatueLocation(2, 129.5, 97.0, 136.5, 90f, 0f, 0f));

        // #3 - Wschód
        config.statues.add(new StatueLocation(3, 138.5, 97.0, 136.5, 270f, 0f, 0f));

        // #4 - Południe
        config.statues.add(new StatueLocation(4, 134.5, 97.0, 131.5, 0f, 0f, 0f));

        save();
    }

    /**
     * Pobierz pozycję dla danego ranku
     */
    public Vector3d getPosition(int rank) {
        for (StatueLocation loc : config.statues) {
            if (loc.rank == rank) {
                return new Vector3d(loc.x, loc.y, loc.z);
            }
        }
        // Fallback - spawn position
        return new Vector3d(133.619, 97.0, 136.421);
    }

    /**
     * Pobierz rotację dla danego ranku
     */
    public Vector3f getRotation(int rank) {
        for (StatueLocation loc : config.statues) {
            if (loc.rank == rank) {
                return new Vector3f(loc.yaw, loc.pitch, loc.roll);
            }
        }
        // Fallback - no rotation
        return new Vector3f(0, 0, 0);
    }

    /**
     * Ustaw pozycję dla danego ranku
     */
    public void setPosition(int rank, double x, double y, double z, float yaw, float pitch, float roll) {
        // Znajdź istniejącą lokację
        for (StatueLocation loc : config.statues) {
            if (loc.rank == rank) {
                loc.x = x;
                loc.y = y;
                loc.z = z;
                loc.yaw = yaw;
                loc.pitch = pitch;
                loc.roll = roll;
                save();
                return;
            }
        }

        // Dodaj nową lokację jeśli nie istnieje
        config.statues.add(new StatueLocation(rank, x, y, z, yaw, pitch, roll));
        save();
    }

    /**
     * Pobierz interval aktualizacji w minutach
     */
    public int getUpdateIntervalMinutes() {
        return config.updateIntervalMinutes;
    }

    /**
     * Ustaw interval aktualizacji w minutach
     */
    public void setUpdateIntervalMinutes(int minutes) {
        config.updateIntervalMinutes = minutes;
        save();
    }

    /**
     * Pobierz klucz modelu NPC
     */
    public String getNpcModelKey() {
        return config.npcModelKey;
    }

    /**
     * Ustaw klucz modelu NPC
     */
    public void setNpcModelKey(String modelKey) {
        config.npcModelKey = modelKey;
        save();
    }

    /**
     * Pobierz wszystkie lokacje statuelek
     */
    public List<StatueLocation> getAllLocations() {
        return new ArrayList<>(config.statues);
    }

    // === Klasy wewnętrzne dla struktury JSON ===

    /**
     * Główna klasa konfiguracji
     */
    private static class ConfigData {
        int updateIntervalMinutes;
        String npcModelKey;
        List<StatueLocation> statues;
    }

    /**
     * Lokacja pojedynczej statuetki
     */
    public static class StatueLocation {
        int rank;
        double x;
        double y;
        double z;
        float yaw;
        float pitch;
        float roll;

        StatueLocation(int rank, double x, double y, double z, float yaw, float pitch, float roll) {
            this.rank = rank;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        @Override
        public String toString() {
            return String.format("Rank #%d: (%.1f, %.1f, %.1f) yaw=%.0f°",
                rank, x, y, z, yaw);
        }
    }
}
