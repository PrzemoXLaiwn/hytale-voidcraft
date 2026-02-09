package pl.jailbreak.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.jailbreak.player.PlayerData;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonFileDatabase implements Database {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String dataFolder;

    public JsonFileDatabase(String dataFolder) { this.dataFolder = dataFolder; }
    public void connect() { new File(dataFolder, "players").mkdirs(); }
    public void disconnect() {}

    public PlayerData loadPlayer(String uuid) {
        File file = new File(dataFolder + "/players", uuid + ".json");
        if (!file.exists()) return null;
        try (Reader r = new FileReader(file)) { return gson.fromJson(r, PlayerData.class); }
        catch (Exception e) { return null; }
    }

    public void savePlayer(PlayerData player) {
        File file = new File(dataFolder + "/players", player.getUuid() + ".json");
        try (Writer w = new FileWriter(file)) { gson.toJson(player, w); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public List<PlayerData> loadAllPlayers() {
        List<PlayerData> players = new ArrayList<>();
        File playersDir = new File(dataFolder, "players");
        if (!playersDir.exists()) return players;

        File[] files = playersDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return players;

        for (File file : files) {
            try (Reader r = new FileReader(file)) {
                PlayerData data = gson.fromJson(r, PlayerData.class);
                if (data != null) players.add(data);
            } catch (Exception e) { /* skip corrupted files */ }
        }
        return players;
    }
}
