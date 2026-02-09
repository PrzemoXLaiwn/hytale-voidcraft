package pl.jailbreak.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;
import pl.jailbreak.util.StarterKit;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.hud.ScoreboardManager;
import pl.jailbreak.mine.MineRegenTask;
import pl.jailbreak.achievements.AchievementCheckTask;
import pl.jailbreak.statue.StatueManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerListener {

    private final PlayerManager playerManager;
    private static boolean statuesInitialized = false;

    public PlayerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        System.out.println("[Voidcraft] PlayerListener created!");
    }

    public void register(EventRegistry eventRegistry) {
        eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
        eventRegistry.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        System.out.println("[Voidcraft] Events registered!");
    }

    @SuppressWarnings("deprecation")
    private void onPlayerConnect(PlayerConnectEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUuid().toString();

        String name;
        try {
            name = player.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = uuid.substring(0, 8);
            }
        } catch (Exception e) {
            name = uuid.substring(0, 8);
        }

        PlayerData data = playerManager.loadOrCreate(uuid, name);

        if (data != null && !name.equals(data.getName())) {
            data.setName(name);
            System.out.println("[Voidcraft] Updated player name: " + name);
        }

        if (data != null) {
            sendWelcomeMessage(player, name);

            if (data.isFirstJoin()) {
                StarterKit.giveStarterKit(player);
                data.setFirstJoin(false);
                playerManager.savePlayer(uuid);
            }
        }

        try {
            if (player.getWorld() != null) {
                MineRegenTask regenTask = JailbreakPlugin.getMineRegenTask();
                if (regenTask != null) {
                    regenTask.setWorld(player.getWorld());
                }

                AchievementCheckTask achievementTask = JailbreakPlugin.getAchievementCheckTask();
                if (achievementTask != null) {
                    achievementTask.setWorld(player.getWorld());
                }

                var spawnHealTask = JailbreakPlugin.getSpawnProtectionHealTask();
                if (spawnHealTask != null) {
                    spawnHealTask.setWorld(player.getWorld());
                }

                System.out.println("[Voidcraft] World set for background tasks");
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error setting world: " + e.getMessage());
        }

        // Force Adventure mode for non-OP players
        try {
            if (player.getGameMode() != GameMode.Adventure) {
                boolean isOp = false;
                try { isOp = player.hasPermission("voidcraft.admin.bypass"); } catch (Exception ignored) {}
                if (!isOp) {
                    var world = player.getWorld();
                    if (world != null) {
                        world.execute(() -> {
                            try {
                                Ref<EntityStore> ref = player.getReference();
                                if (ref != null) {
                                    Store<EntityStore> st = ref.getStore();
                                    Player.setGameMode(ref, GameMode.Adventure, st);
                                    System.out.println("[Voidcraft] Set " + player.getDisplayName() + " to Adventure mode");
                                }
                            } catch (Exception e) {
                                System.out.println("[Voidcraft] GameMode set error: " + e.getMessage());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] GameMode check error: " + e.getMessage());
        }

        // Set up Scoreboard HUD
        try {
            ScoreboardManager scoreboardManager = JailbreakPlugin.getScoreboardManager();
            if (scoreboardManager != null && data != null) {
                scoreboardManager.setupHud(player, data);
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] HUD setup error: " + e.getMessage());
        }

        // Initialize statue system on first player join (one-time)
        if (!statuesInitialized) {
            try {
                StatueManager statueManager = JailbreakPlugin.getStatueManager();
                if (statueManager != null && player.getWorld() != null) {
                    World world = player.getWorld();
                    Store<EntityStore> store = world.getEntityStore().getStore();

                    System.out.println("[Voidcraft] First player joined - initializing StatueManager...");
                    statueManager.init(JailbreakPlugin.getDataFolder(), world, store);

                    statuesInitialized = true;
                    System.out.println("[Voidcraft] StatueManager initialized successfully!");
                }
            } catch (Exception e) {
                System.out.println("[Voidcraft] StatueManager initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef ref = event.getPlayerRef();
        String uuid = ref.getUuid().toString();
        playerManager.savePlayer(uuid);

        // Remove HUD
        try {
            ScoreboardManager scoreboardManager = JailbreakPlugin.getScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.removeHud(ref.getUuid());
            }
        } catch (Exception e) {
            // Silently ignore
        }

        // Clean up spawn protection invulnerability tracking
        try {
            var spawnHealTask = JailbreakPlugin.getSpawnProtectionHealTask();
            if (spawnHealTask != null) {
                spawnHealTask.removePlayer(ref.getUuid());
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }

    private void sendWelcomeMessage(Player player, String playerName) {
        player.sendMessage(Message.raw(" "));
        player.sendMessage(Message.raw("========================================").color("#9400D3"));
        player.sendMessage(Message.raw("       WELCOME TO VOIDCRAFT").color("#9400D3").bold(true));
        player.sendMessage(Message.raw("========================================").color("#9400D3"));
        player.sendMessage(Message.raw(" "));
        player.sendMessage(Message.raw("  Hello, " + playerName + "!").color("#FFFFFF"));
        player.sendMessage(Message.raw(" "));
        player.sendMessage(Message.raw("  Commands:").color("#FFD700"));
        player.sendMessage(Message.raw("    /bal - Check balance").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /sell - Sell ores").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /shop - Open shop").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /warp - Teleport menu").color("#AAAAAA"));
        player.sendMessage(Message.raw("    /daily - Daily reward").color("#AAAAAA"));
        player.sendMessage(Message.raw(" "));
        player.sendMessage(Message.raw("  Have fun mining!").color("#00FF00"));
        player.sendMessage(Message.raw("========================================").color("#9400D3"));
        player.sendMessage(Message.raw(" "));
        
        // Vote reminder
        player.sendMessage(Message.raw("Heads Up! You have not voted today! Use /vote for rewards!").color("#FF6600"));
        player.sendMessage(Message.raw(" "));
        player.sendMessage(Message.raw("------------------ Our Vote Link ------------------").color("#FFD700"));
        player.sendMessage(Message.raw("Vote at: hyghest.com/server/voidcraft").color("#00BFFF"));
        player.sendMessage(Message.raw("Reward: $500 per vote!").color("#00FF00"));
        player.sendMessage(Message.raw("------------------ Our Vote Link ------------------").color("#FFD700"));
    }
}
