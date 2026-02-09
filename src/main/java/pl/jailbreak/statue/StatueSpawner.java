package pl.jailbreak.statue;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.data.Pair;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.npc.NPCPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Odpowiedzialny za spawning i despawning statuelek NPCs w świecie.
 * Używa NPCPlugin API do tworzenia entity.
 */
public class StatueSpawner {
    // Mapa aktywnych statuelek: rank -> entity reference
    private final Map<Integer, Ref<EntityStore>> activeStatues = new ConcurrentHashMap<>();

    /**
     * Spawuje statuetkę NPC dla danego gracza na określonej pozycji.
     * Jeśli statuetka już istnieje na tym ranku, despawnuje starą przed spawnem nowej.
     *
     * @param world Świat gdzie spawić statuetkę
     * @param store EntityStore
     * @param statue Dane statuetki (rank, gracz, pozycja)
     * @param modelKey Klucz modelu NPC (np. "Player")
     * @return Ref do utworzonej entity lub null jeśli błąd
     */
    public Ref<EntityStore> spawnStatue(World world, Store<EntityStore> store,
                                         StatueData statue, String modelKey) {
        if (world == null || store == null || statue == null) {
            System.out.println("[StatueSpawner] Cannot spawn statue: null parameter");
            return null;
        }

        final Ref<EntityStore>[] result = new Ref[1];

        world.execute(() -> {
            try {
                // Despawnuj starą statuetkę jeśli istnieje
                despawnStatue(statue.getRank(), store);

                System.out.println("[StatueSpawner] Spawning statue #" + statue.getRank() +
                    " for player " + statue.getPlayerName() + " at " + statue.getPosition());

                // Spawnuj NPC używając NPCPlugin
                Pair<Ref<EntityStore>, INonPlayerCharacter> spawnResult =
                    NPCPlugin.get().spawnNPC(
                        store,
                        modelKey != null ? modelKey : "Player",
                        null,  // config group (optional)
                        statue.getPosition(),
                        statue.getRotation()
                    );

                if (spawnResult != null) {
                    Ref<EntityStore> npcRef = spawnResult.first();
                    INonPlayerCharacter npc = spawnResult.second();

                    if (npcRef != null) {
                        // TODO: KRYTYCZNE - Ustawienie skinu gracza
                        // Metody do zbadania podczas testowania:
                        // 1. Sprawdź INonPlayerCharacter interface dla metod setSkin/setPlayerModel
                        // 2. Sprawdź NPCEntity component dla pól skin-related
                        // 3. Możliwe metody:
                        //    - npc.setPlayerModel(true);
                        //    - npc.setSkinUsername(statue.getPlayerName());
                        //    - npc.setSkin(statue.getPlayerUuid());

                        // TODO: KRYTYCZNE - Ustawienie nazwy wyświetlanej
                        // Metody do zbadania:
                        // 1. Sprawdź czy NPC ma display name component
                        // 2. Możliwe rozwiązania:
                        //    - setupNameHologram(npcRef, store, statue);
                        //    - npc.setDisplayName(formatStatueName(statue));

                        // Zapisz referencję do aktywnej statuetki
                        activeStatues.put(statue.getRank(), npcRef);
                        result[0] = npcRef;

                        System.out.println("[StatueSpawner] Successfully spawned statue #" +
                            statue.getRank() + " for " + statue.getPlayerName());
                    } else {
                        System.out.println("[StatueSpawner] NPCPlugin returned null reference!");
                    }
                } else {
                    System.out.println("[StatueSpawner] NPCPlugin.spawnNPC() returned null!");
                }

            } catch (Exception e) {
                System.out.println("[StatueSpawner] Error spawning statue #" +
                    statue.getRank() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        return result[0];
    }

    /**
     * Usuwa statuetkę z danego ranku.
     *
     * @param rank Pozycja w rankingu (1-4)
     * @param store EntityStore
     */
    public void despawnStatue(int rank, Store<EntityStore> store) {
        Ref<EntityStore> ref = activeStatues.remove(rank);
        if (ref != null && store != null) {
            try {
                // Usuń entity ze store
                // Uwaga: Może wymagać world.execute() w kontekście świata
                store.removeEntity(ref);

                System.out.println("[StatueSpawner] Despawned statue #" + rank);
            } catch (Exception e) {
                System.out.println("[StatueSpawner] Error despawning statue #" +
                    rank + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Despawnuje wszystkie statuetki.
     * Używane przy shutdown pluginu.
     *
     * @param store EntityStore
     */
    public void despawnAll(Store<EntityStore> store) {
        System.out.println("[StatueSpawner] Despawning all statues...");
        for (int rank = 1; rank <= 4; rank++) {
            despawnStatue(rank, store);
        }
    }

    /**
     * Sprawdza czy statuetka o danym ranku jest aktywna.
     *
     * @param rank Pozycja w rankingu (1-4)
     * @return true jeśli statuetka istnieje
     */
    public boolean isStatueActive(int rank) {
        return activeStatues.containsKey(rank);
    }

    /**
     * Pobiera referencję do statuetki.
     *
     * @param rank Pozycja w rankingu (1-4)
     * @return Ref do entity lub null jeśli nie istnieje
     */
    public Ref<EntityStore> getStatueRef(int rank) {
        return activeStatues.get(rank);
    }

    /**
     * Pobiera liczbę aktywnych statuelek.
     *
     * @return Liczba spawnionych statuelek
     */
    public int getActiveStatueCount() {
        return activeStatues.size();
    }

    // === Metody pomocnicze (TODO - do implementacji podczas testowania) ===

    /**
     * Ustawia hologram z nazwą gracza nad statuetką.
     * TODO: Implementować podczas testowania
     *
     * Opcje:
     * A) Użyć Hylograms plugin API jeśli zainstalowany
     * B) Stworzyć custom text entity z TransformComponent
     * C) Ustawić display name na NPC component
     *
     * Format tekstu:
     * Linia 1: "#1" (w złotym kolorze dla rank 1)
     * Linia 2: "PlayerName" (w białym)
     * Linia 3: "$1,234,567" (w zielonym)
     */
    private void setupNameHologram(Ref<EntityStore> npcRef, Store<EntityStore> store,
                                    StatueData statue) {
        // TODO: Implementacja
        /*
        String line1 = formatRank(statue.getRank());
        String line2 = statue.getPlayerName();
        String line3 = formatBalance(statue.getBalance());

        // Pozycja hologramu - 2 bloki nad NPC
        Vector3d hologramPos = statue.getPosition().add(0, 2.0, 0);

        // Sprawdź czy Hylograms dostępny:
        // if (HylogramsAPI.isAvailable()) {
        //     HylogramsAPI.createHologram(hologramPos, Arrays.asList(line1, line2, line3));
        // }
        */
    }

    /**
     * Formatuje rank z kolorami.
     */
    private String formatRank(int rank) {
        switch (rank) {
            case 1: return "#FFD700#1";  // Złoty
            case 2: return "#C0C0C0#2";  // Srebrny
            case 3: return "#CD7F32#3";  // Brązowy
            default: return "#FFFFFF#" + rank;  // Biały
        }
    }

    /**
     * Formatuje balance.
     */
    private String formatBalance(long balance) {
        if (balance >= 1_000_000) {
            return String.format("$%.1fM", balance / 1_000_000.0);
        } else if (balance >= 1_000) {
            return String.format("$%.1fK", balance / 1_000.0);
        }
        return "$" + balance;
    }

    /**
     * Formatuje pełną nazwę statuetki.
     */
    private String formatStatueName(StatueData statue) {
        return formatRank(statue.getRank()) + " " +
               statue.getPlayerName() + " " +
               formatBalance(statue.getBalance());
    }
}
