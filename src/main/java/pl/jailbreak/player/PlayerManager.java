package pl.jailbreak.player;

import pl.jailbreak.database.Database;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private final Database database;
    private final Map<String, PlayerData> playerCache = new ConcurrentHashMap<>();

    public PlayerManager(Database database) { this.database = database; }

    public PlayerData loadOrCreate(String uuid, String name) {
        PlayerData data = playerCache.get(uuid);
        if (data != null) return data;
        data = database.loadPlayer(uuid);
        if (data == null) { data = new PlayerData(uuid, name); database.savePlayer(data); }
        playerCache.put(uuid, data);
        return data;
    }

    public PlayerData getPlayer(String uuid) { return playerCache.get(uuid); }
    public PlayerData getPlayerByName(String name) {
        for (PlayerData d : playerCache.values()) if (d.getName().equalsIgnoreCase(name)) return d;
        return null;
    }
    public void savePlayer(String uuid) {
        PlayerData d = playerCache.get(uuid);
        if (d != null) database.savePlayer(d);
    }
    public void saveAllPlayers() { for (PlayerData d : playerCache.values()) database.savePlayer(d); }

    public void removePlayer(String uuid) {
        PlayerData d = playerCache.remove(uuid);
        if (d != null) database.savePlayer(d);
    }

    public java.util.Collection<PlayerData> getAllPlayers() { return playerCache.values(); }

    public java.util.List<PlayerData> getTopBalances(int limit) {
        return playerCache.values().stream()
            .sorted((a, b) -> Long.compare(b.getBalance(), a.getBalance()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }
}