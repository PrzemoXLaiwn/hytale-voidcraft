package pl.jailbreak.database;

import pl.jailbreak.player.PlayerData;

public interface Database {
    void connect();
    void disconnect();
    PlayerData loadPlayer(String uuid);
    void savePlayer(PlayerData player);
}