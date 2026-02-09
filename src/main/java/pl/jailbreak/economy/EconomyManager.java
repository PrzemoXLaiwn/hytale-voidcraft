package pl.jailbreak.economy;

import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

public class EconomyManager {
    private final PlayerManager playerManager;
    public EconomyManager(PlayerManager playerManager) { this.playerManager = playerManager; }
    public long getBalance(String uuid) {
        PlayerData p = playerManager.getPlayer(uuid);
        return p != null ? p.getBalance() : 0;
    }
    public boolean withdraw(String uuid, long amount) {
        PlayerData p = playerManager.getPlayer(uuid);
        return p != null && p.removeBalance(amount);
    }
    public void deposit(String uuid, long amount) {
        PlayerData p = playerManager.getPlayer(uuid);
        if (p != null) p.addBalance(amount);
    }
}