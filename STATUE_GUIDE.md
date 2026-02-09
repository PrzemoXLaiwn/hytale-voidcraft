# 🗽 System Statuelek TOP 4 Graczy - Instrukcja Użytkowania

System automatycznie wyświetla na spawnie 4 statuetki reprezentujące graczy z największym balansem na serwerze.

---

## 📋 Spis treści
1. [Jak działa system](#jak-działa-system)
2. [Pierwsze uruchomienie](#pierwsze-uruchomienie)
3. [Komendy](#komendy)
4. [Konfiguracja](#konfiguracja)
5. [Troubleshooting](#troubleshooting)
6. [Dalszy rozwój](#dalszy-rozwój)

---

## 🎯 Jak działa system

### Automatyczne działanie
- **Inicjalizacja**: System uruchamia się automatycznie gdy pierwszy gracz dołączy do serwera
- **Aktualizacja**: Co 10 minut (domyślnie) sprawdzane są balanse graczy i statuetki są aktualizowane
- **Lokalizacja**: Statuetki spawią się wokół spawnu w 4 kierunkach (N, E, S, W)

### Ranking
- System używa `PlayerManager.getTopBalances(4)` do pobrania TOP 4 graczy
- Ranking jest automatycznie sortowany po `balance` (od największego)
- Gdy gracz zmieni pozycję w rankingu, jego statuetka jest respawnowana na nowej pozycji

---

## 🚀 Pierwsze uruchomienie

### Krok 1: Build projektu
```bash
./gradlew build
# lub
gradlew.bat build
```

### Krok 2: Uruchom serwer Hytale
```bash
java -jar hytale-server.jar
```

### Krok 3: Dołącz jako pierwszy gracz
- System automatycznie zainicjalizuje się przy pierwszym graczu
- W konsoli serwera zobaczysz:
  ```
  [Voidcraft] First player joined - initializing StatueManager...
  [Voidcraft] StatueManager initialized with X statues
  [StatueManager] Updated statue #1 to PlayerName ($1,234,567)
  ```

### Krok 4: Sprawdź statuetki
- Teleportuj się na spawn: `/warp spawn`
- Zobaczysz 4 statuetki wokół spawnu
- Użyj `/statue list` aby zobaczyć aktualny ranking

---

## 🎮 Komendy

### `/statue list`
**Opis**: Wyświetla aktualny TOP 4 graczy z balansami
**Przykład**:
```
=== TOP 4 BALANCE STATUES ===

⭐ #1 - PlayerOne - $5.2M
◆ #2 - PlayerTwo - $3.8M
▲ #3 - PlayerThree - $2.1M
• #4 - PlayerFour - $1.5M

Active statues: 4/4
```

### `/statue refresh`
**Opis**: Manualnie odświeża statuetki (dla adminów)
**Kiedy użyć**:
- Po dużej zmianie balansów
- Gdy statuetki nie są zsynchronizowane
- Do testowania systemu

**Przykład**:
```
Refreshing statues...
Statues refreshed successfully!
```

### `/statue info`
**Opis**: Pokazuje status systemu
**Przykład**:
```
=== STATUE SYSTEM INFO ===

Status: Active
Active statues: 4/4
Update interval: 10 minutes
NPC Model: Player
```

### `/statue setpos <rank>`
**Opis**: Ustawia pozycję statuetki na lokację gracza (TODO - wymaga dopracowania)
**Parametry**: `rank` - numer statuetki (1-4)
**Uwaga**: Obecnie w implementacji - wymaga pobrania pozycji gracza z API

---

## ⚙️ Konfiguracja

### Plik konfiguracyjny
**Lokalizacja**: `plugins/Voidcraft/statue_config.json`

**Domyślna konfiguracja**:
```json
{
  "updateIntervalMinutes": 10,
  "npcModelKey": "Player",
  "statues": [
    {
      "rank": 1,
      "x": 134.5,
      "y": 97.0,
      "z": 141.5,
      "yaw": 180.0,
      "pitch": 0.0,
      "roll": 0.0
    },
    {
      "rank": 2,
      "x": 129.5,
      "y": 97.0,
      "z": 136.5,
      "yaw": 90.0,
      "pitch": 0.0,
      "roll": 0.0
    },
    {
      "rank": 3,
      "x": 138.5,
      "y": 97.0,
      "z": 136.5,
      "yaw": 270.0,
      "pitch": 0.0,
      "roll": 0.0
    },
    {
      "rank": 4,
      "x": 134.5,
      "y": 97.0,
      "z": 131.5,
      "yaw": 0.0,
      "pitch": 0.0,
      "roll": 0.0
    }
  ]
}
```

### Parametry konfiguracji

| Parametr | Typ | Opis | Domyślna wartość |
|----------|-----|------|------------------|
| `updateIntervalMinutes` | int | Częstotliwość aktualizacji (w minutach) | 10 |
| `npcModelKey` | string | Klucz modelu NPC z Hytale API | "Player" |
| `statues[].rank` | int | Numer pozycji w rankingu (1-4) | - |
| `statues[].x` | double | Pozycja X w świecie | - |
| `statues[].y` | double | Pozycja Y w świecie | - |
| `statues[].z` | double | Pozycja Z w świecie | - |
| `statues[].yaw` | float | Rotacja pozioma (0-360°) | - |
| `statues[].pitch` | float | Rotacja pionowa | 0.0 |
| `statues[].roll` | float | Rotacja boczna | 0.0 |

### Zmiana pozycji statuelek

#### Metoda 1: Edycja pliku JSON (Zalecana)
1. Zatrzymaj serwer
2. Edytuj `plugins/Voidcraft/statue_config.json`
3. Zmień współrzędne `x`, `y`, `z` i rotacje `yaw`, `pitch`, `roll`
4. Zapisz plik
5. Uruchom serwer

#### Metoda 2: Komenda (W przyszłości)
```
/statue setpos 1
```
(Obecnie w implementacji)

### Zmiana częstotliwości aktualizacji

**Edytuj plik JSON**:
```json
{
  "updateIntervalMinutes": 5  // Zmień na 5 minut
}
```

**Restart serwera** - zmiany wejdą w życie po restarcie

---

## 🔧 Troubleshooting

### Problem 1: Statuetki nie spawią się

**Objawy**: Brak statuelek na spawnie po dołączeniu gracza

**Rozwiązanie**:
1. Sprawdź logi serwera:
   ```
   [StatueManager] Cannot initialize: world or store is null!
   ```
2. Sprawdź czy `PlayerListener` jest zarejestrowany
3. Sprawdź czy `NPCPlugin` jest dostępny w Hytale API

**Debug**:
```java
// W konsoli serwera szukaj:
[Voidcraft] StatueManager created!
[Voidcraft] First player joined - initializing StatueManager...
[StatueManager] Updated statue #1 to PlayerName ($...)
```

### Problem 2: Statuetki nie mają skinów graczy

**Objawy**: NPCs spawią się ale mają domyślny skin

**Przyczyna**: API dla ustawienia skinu gracza wymaga badania

**Rozwiązanie** (TODO podczas testowania):
1. Otwórz [StatueSpawner.java:60-75](src/main/java/pl/jailbreak/statue/StatueSpawner.java)
2. Sprawdź dostępne metody na `INonPlayerCharacter`:
   ```java
   // Możliwe metody do przetestowania:
   npc.setPlayerModel(true);
   npc.setSkinUsername(statue.getPlayerName());
   npc.setSkinUUID(statue.getPlayerUuid());
   ```
3. Sprawdź dokumentację HyCitizens plugin jako przykład

### Problem 3: Brak nazw nad statuetkami

**Objawy**: Statuetki bez nazw/ranków

**Przyczyna**: Hologramy/display names wymagają dodatkowej implementacji

**Rozwiązanie** (TODO podczas testowania):
1. **Opcja A**: Zainstaluj Hylograms plugin i użyj jego API
2. **Opcja B**: Stwórz custom text entity w [StatueSpawner.java:150+](src/main/java/pl/jailbreak/statue/StatueSpawner.java)
3. **Opcja C**: Użyj display name na NPC component

### Problem 4: Statuetki nie aktualizują się

**Objawy**: Ranking graczy się zmienił ale statuetki pozostają stare

**Rozwiązanie**:
1. Sprawdź logi czy timer działa:
   ```
   [StatueUpdateTask] Running scheduled statue update...
   ```
2. Manualnie odśwież: `/statue refresh`
3. Sprawdź czy `updateIntervalMinutes` jest ustawiony poprawnie
4. Sprawdź czy `Timer` nie został anulowany

### Problem 5: Błąd kompilacji

**Objawy**: Błędy podczas `./gradlew build`

**Typowe błędy**:
- Missing imports: Dodaj brakujące importy z `com.hypixel.hytale.*`
- NPCPlugin not found: Sprawdź czy Hytale Server API zawiera `com.hypixel.hytale.server.npc.NPCPlugin`
- Syntax errors: Sprawdź zgodność z wersją Java (Java 11+)

**Rozwiązanie**:
```bash
./gradlew clean build --refresh-dependencies
```

---

## 🚧 Dalszy rozwój (TODO)

### Wysokie priorytety

#### 1. Skiny graczy na NPCs
**Status**: ⚠️ Wymaga testowania
**Lokalizacja**: [StatueSpawner.java:60-75](src/main/java/pl/jailbreak/statue/StatueSpawner.java)

**Co zrobić**:
```java
// W metodzie spawnStatue(), po utworzeniu NPC:
if (result != null) {
    INonPlayerCharacter npc = result.second();

    // TODO: Przetestuj te metody:
    try {
        npc.setPlayerModel(true);
        npc.setSkinUsername(statue.getPlayerName());
        // lub
        npc.setSkin(statue.getPlayerUuid());
    } catch (NoSuchMethodError e) {
        // Metoda nie istnieje - sprawdź INonPlayerCharacter API
    }
}
```

#### 2. Hologramy/nazwy nad statuetkami
**Status**: ⚠️ Wymaga testowania
**Lokalizacja**: [StatueSpawner.java:150-175](src/main/java/pl/jailbreak/statue/StatueSpawner.java)

**Opcje**:
- **Hylograms API**: Jeśli plugin zainstalowany
- **Custom text entity**: Stwórz entity z ModelComponent tekstowym
- **NPC display name**: Jeśli NPCEntity ma pole displayName

**Przykład (Hylograms)**:
```java
private void setupNameHologram(Ref<EntityStore> npcRef,
                                Store<EntityStore> store,
                                StatueData statue) {
    Vector3d hologramPos = statue.getPosition().add(0, 2.5, 0);

    List<String> lines = Arrays.asList(
        formatRank(statue.getRank()),        // "#1" w złotym
        statue.getPlayerName(),              // "PlayerName"
        formatBalance(statue.getBalance())   // "$1,234,567"
    );

    // Jeśli Hylograms dostępny:
    // HylogramsAPI.createHologram(hologramPos, lines);
}
```

#### 3. Komenda `/statue setpos`
**Status**: ⚠️ Wymaga implementacji
**Lokalizacja**: [StatueCommand.java:150+](src/main/java/pl/jailbreak/commands/StatueCommand.java)

**Co zrobić**:
```java
private CompletableFuture<Void> executeSetPos(CommandContext ctx, String[] args) {
    Player player = ctx.getPlayer();

    // TODO: Sprawdź jak uzyskać pozycję gracza w Hytale API:
    // Możliwe metody:
    // - player.getPosition()
    // - player.getTransform().getPosition()
    // - player.getLocation()

    Vector3d position = player.getTransform().getPosition(); // ???
    Vector3f rotation = player.getTransform().getRotation(); // ???

    config.setPosition(rank, position.x, position.y, position.z,
                      rotation.x, rotation.y, rotation.z);

    return completed();
}
```

### Średnie priorytety

#### 4. Natychmiastowa aktualizacja po dużej zmianie balance
**Lokalizacja**: Plan w [PlayerData.java](src/main/java/pl/jailbreak/player/PlayerData.java)

**Idea**: Hook w `addBalance()` / `removeBalance()` który triggeruje refresh jeśli gracz może być w TOP 4

#### 5. Persistent NPCs po restarcie
**Problem**: NPCs mogą znikać po restarcie serwera

**Rozwiązanie**: Dodać component oznaczający entity jako persistent lub re-spawn w world load event

#### 6. Chunk loading
**Problem**: Statuetki znikają jeśli chunk nie jest loaded

**Rozwiązanie**: Upewnić się że chunk spawnu jest zawsze loaded (force load chunk)

### Niskie priorytety

#### 7. Animacje/efekty
- Cząsteczki wokół statuelek (#1 = złote, #2 = srebrne, etc.)
- Światło/glow effect
- Animacja respawnu

#### 8. Interakcja
- Kliknięcie na statuetkę pokazuje szczegóły gracza
- Custom page z statystykami gracza

#### 9. Więcej statuelek
- TOP 10 zamiast TOP 4
- Dodatkowe rankingi (blocks mined, prestige, etc.)

---

## 📁 Struktura plików

```
src/main/java/pl/jailbreak/
├── statue/
│   ├── StatueData.java          # Model danych statuetki
│   ├── StatueConfig.java        # Manager konfiguracji
│   ├── StatueSpawner.java       # Spawner NPCs
│   ├── StatueManager.java       # Główny manager
│   └── StatueUpdateTask.java   # Timer task
├── commands/
│   └── StatueCommand.java       # Komendy admin
├── JailbreakPlugin.java         # Integracja systemu
└── listeners/
    └── PlayerListener.java      # Hook inicjalizacji

plugins/Voidcraft/
└── statue_config.json           # Konfiguracja (auto-generated)
```

---

## 🤝 Pomoc

### Logi debugowania

**Włącz szczegółowe logi** - dodaj w [StatueManager.java](src/main/java/pl/jailbreak/statue/StatueManager.java):
```java
System.out.println("[StatueManager DEBUG] World: " + world);
System.out.println("[StatueManager DEBUG] Store: " + store);
System.out.println("[StatueManager DEBUG] TOP 4: " + topPlayers);
```

### Testowanie manualnie

1. **Zmień balanse graczy**:
   ```
   /setbal PlayerOne 5000000
   /setbal PlayerTwo 4000000
   /setbal PlayerThree 3000000
   /setbal PlayerFour 2000000
   ```

2. **Odśwież statuetki**:
   ```
   /statue refresh
   ```

3. **Sprawdź ranking**:
   ```
   /statue list
   ```

### Dokumentacja API

- **Hytale Modding Docs**: https://hytalemodding.dev/en/docs/guides/plugin/spawning-npcs
- **NPCPlugin API**: Sprawdź `com.hypixel.hytale.server.npc.NPCPlugin`
- **HyCitizens Example**: https://www.curseforge.com/hytale/mods/hycitizens (przykład NPCs ze skinami)

---

## 📝 Changelog

### v1.0.0 (2026-02-09)
- ✅ Podstawowy system statuelek TOP 4
- ✅ Automatyczna aktualizacja co 10 minut
- ✅ Komendy admin (/statue refresh, list, info)
- ✅ Konfiguracja JSON z pozycjami
- ⚠️ TODO: Skiny graczy (wymaga testowania API)
- ⚠️ TODO: Hologramy/nazwy (wymaga testowania API)

---

**Autor**: Voidcraft Development Team
**Wersja**: 1.0.0
**Data**: 2026-02-09
