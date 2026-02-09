package pl.jailbreak.statue;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Główny manager systemu statuelek TOP 4 graczy.
 * Odpowiedzialny za:
 * - Inicjalizację systemu
 * - Odświeżanie rankingu i aktualizację statuelek
 * - Timer automatycznej aktualizacji
 * - Koordynację między spawnerem a danymi graczy
 */
public class StatueManager {
    private final PlayerManager playerManager;
    private final StatueSpawner spawner;
    private final StatueConfig config;

    private Timer updateTimer;
    private StatueUpdateTask updateTask;

    // Cache aktualnych statuelek: rank -> StatueData
    private final Map<Integer, StatueData> currentStatues = new ConcurrentHashMap<>();

    // Referencje do world i store (cachowane po inicjalizacji)
    private World world;
    private Store<EntityStore> store;

    /**
     * Konstruktor
     *
     * @param playerManager Manager graczy do pobierania TOP balances
     */
    public StatueManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.spawner = new StatueSpawner();
        this.config = new StatueConfig();
    }

    /**
     * Inicjalizacja managera przy starcie serwera.
     * UWAGA: Wymaga dostępu do World, więc powinna być wywołana gdy world jest dostępny!
     *
     * @param dataFolder Folder danych pluginu
     * @param world Świat gdzie spawić statuetki
     * @param store EntityStore
     */
    public void init(File dataFolder, World world, Store<EntityStore> store) {
        if (world == null || store == null) {
            System.out.println("[StatueManager] Cannot initialize: world or store is null!");
            return;
        }

        this.world = world;
        this.store = store;

        // Załaduj konfigurację
        config.init(dataFolder);

        System.out.println("[Voidcraft] StatueManager initializing...");

        // Pierwsze utworzenie statuelek
        refreshStatues();

        // Uruchom timer aktualizacji
        scheduleUpdates();

        System.out.println("[Voidcraft] StatueManager initialized with " +
            currentStatues.size() + " statues");
    }

    /**
     * Odświeża statuetki - główna logika systemu.
     * Pobiera TOP 4 graczy i aktualizuje statuetki jeśli ranking się zmienił.
     */
    public void refreshStatues() {
        if (world == null || store == null) {
            System.out.println("[StatueManager] Cannot refresh: not initialized!");
            return;
        }

        try {
            // Pobierz TOP 4 graczy
            List<PlayerData> topPlayers = playerManager.getTopBalances(4);

            if (topPlayers.isEmpty()) {
                System.out.println("[StatueManager] No players to display");
                // Despawn wszystkich jeśli nie ma graczy
                for (int rank = 1; rank <= 4; rank++) {
                    if (currentStatues.containsKey(rank)) {
                        spawner.despawnStatue(rank, store);
                        currentStatues.remove(rank);
                    }
                }
                return;
            }

            System.out.println("[StatueManager] Refreshing statues with " +
                topPlayers.size() + " players");

            // Dla każdego gracza w TOP (rank 1-4)
            for (int i = 0; i < topPlayers.size() && i < 4; i++) {
                int rank = i + 1;
                PlayerData player = topPlayers.get(i);

                // Sprawdź czy statuetka wymaga aktualizacji
                StatueData oldStatue = currentStatues.get(rank);

                boolean needsUpdate = oldStatue == null ||
                    !oldStatue.getPlayerUuid().equals(player.getUuid()) ||
                    oldStatue.getBalance() != player.getBalance();

                if (needsUpdate) {
                    // Stwórz nową StatueData
                    Vector3d position = config.getPosition(rank);
                    Vector3f rotation = config.getRotation(rank);

                    StatueData newStatue = new StatueData(
                        rank,
                        player.getUuid(),
                        player.getName(),
                        player.getBalance(),
                        position,
                        rotation
                    );

                    // Spawn/respawn statuetki
                    String modelKey = config.getNpcModelKey();
                    Ref<EntityStore> ref = spawner.spawnStatue(world, store, newStatue, modelKey);

                    if (ref != null) {
                        currentStatues.put(rank, newStatue);
                        System.out.println("[StatueManager] Updated statue #" + rank +
                            " to " + player.getName() + " ($" + formatBalance(player.getBalance()) + ")");
                    } else {
                        System.out.println("[StatueManager] Failed to spawn statue #" + rank);
                    }
                } else {
                    System.out.println("[StatueManager] Statue #" + rank +
                        " unchanged (" + player.getName() + ")");
                }
            }

            // Jeśli mniej niż 4 graczy, usuń dodatkowe statuetki
            for (int rank = topPlayers.size() + 1; rank <= 4; rank++) {
                if (currentStatues.containsKey(rank)) {
                    System.out.println("[StatueManager] Removing statue #" + rank +
                        " (no player for this rank)");
                    spawner.despawnStatue(rank, store);
                    currentStatues.remove(rank);
                }
            }

            System.out.println("[StatueManager] Refresh completed. Active statues: " +
                spawner.getActiveStatueCount());

        } catch (Exception e) {
            System.out.println("[StatueManager] Error during refresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Uruchamia timer do automatycznej aktualizacji statuelek.
     */
    private void scheduleUpdates() {
        int intervalMinutes = config.getUpdateIntervalMinutes();
        long intervalMillis = intervalMinutes * 60 * 1000L;

        updateTask = new StatueUpdateTask(this);
        updateTimer = new Timer("Voidcraft-StatueUpdate", true);

        // Pierwsze uruchomienie po intervalu, potem regularnie
        updateTimer.scheduleAtFixedRate(updateTask, intervalMillis, intervalMillis);

        System.out.println("[StatueManager] Update timer scheduled every " +
            intervalMinutes + " minutes");
    }

    /**
     * Zatrzymuje timer i cleanup.
     * Wywoływane przy shutdown pluginu.
     */
    public void shutdown() {
        System.out.println("[StatueManager] Shutting down...");

        // Zatrzymaj timer
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }

        // Opcjonalnie: despawn wszystkich statuelek przy wyłączeniu
        // Zakomentowane, żeby statuetki zostały w świecie
        // if (store != null) {
        //     spawner.despawnAll(store);
        // }

        currentStatues.clear();
        System.out.println("[StatueManager] Shutdown complete");
    }

    /**
     * Manualnie triggeruje refresh statuelek.
     * Używane przez komendę admin.
     */
    public void manualRefresh() {
        System.out.println("[StatueManager] Manual refresh triggered");
        refreshStatues();
    }

    /**
     * Pobiera aktualne dane statuelek (read-only copy).
     *
     * @return Mapa rank -> StatueData
     */
    public Map<Integer, StatueData> getCurrentStatues() {
        return new HashMap<>(currentStatues);
    }

    /**
     * Pobiera dane konkretnej statuetki.
     *
     * @param rank Pozycja w rankingu (1-4)
     * @return StatueData lub null jeśli nie istnieje
     */
    public StatueData getStatue(int rank) {
        return currentStatues.get(rank);
    }

    /**
     * Sprawdza czy system jest zainicjalizowany.
     *
     * @return true jeśli world i store są dostępne
     */
    public boolean isInitialized() {
        return world != null && store != null;
    }

    /**
     * Pobiera konfigurację (dla komend admin).
     *
     * @return StatueConfig
     */
    public StatueConfig getConfig() {
        return config;
    }

    /**
     * Pobiera spawner (dla komend admin).
     *
     * @return StatueSpawner
     */
    public StatueSpawner getSpawner() {
        return spawner;
    }

    /**
     * Pobiera world (dla komend admin).
     *
     * @return World lub null jeśli nie zainicjalizowane
     */
    public World getWorld() {
        return world;
    }

    /**
     * Pobiera store (dla komend admin).
     *
     * @return Store<EntityStore> lub null jeśli nie zainicjalizowane
     */
    public Store<EntityStore> getStore() {
        return store;
    }

    // === Metody pomocnicze ===

    /**
     * Formatuje balance do czytelnej formy.
     */
    private String formatBalance(long balance) {
        if (balance >= 1_000_000) {
            return String.format("%.1fM", balance / 1_000_000.0);
        } else if (balance >= 1_000) {
            return String.format("%.1fK", balance / 1_000.0);
        }
        return String.valueOf(balance);
    }
}
