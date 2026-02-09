package pl.jailbreak;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import pl.jailbreak.commands.BalanceCommand;
import pl.jailbreak.commands.StatsCommand;
import pl.jailbreak.commands.SellCommand;
import pl.jailbreak.commands.SectorCommand;

import pl.jailbreak.commands.admin.SetBalanceCommand;
import pl.jailbreak.commands.admin.SetSectorCommand;
import pl.jailbreak.commands.admin.JailbreakPricesCommand;
import pl.jailbreak.commands.admin.JailbreakSetPriceCommand;
import pl.jailbreak.commands.admin.JailbreakMultiplierCommand;
import pl.jailbreak.commands.admin.JailbreakReloadCommand;
import pl.jailbreak.commands.admin.AdminCommand;
import pl.jailbreak.commands.admin.WhatIsThisCommand;
import pl.jailbreak.commands.admin.DebugInventoryCommand;
import pl.jailbreak.commands.ShopCommand;
import pl.jailbreak.commands.BuyCommand;
import pl.jailbreak.commands.EnchantsCommand;

import pl.jailbreak.config.EconomyConfig;
import pl.jailbreak.enchants.EnchantManager;
import pl.jailbreak.shop.ShopConfig;
import pl.jailbreak.shop.ShopManager;
import pl.jailbreak.database.Database;
import pl.jailbreak.database.JsonFileDatabase;
import pl.jailbreak.economy.EconomyManager;
import pl.jailbreak.listeners.PlayerListener;
import pl.jailbreak.player.PlayerManager;
import pl.jailbreak.sectors.SectorManager;
import pl.jailbreak.systems.MiningBoostSystem;
import pl.jailbreak.systems.BlockPlaceProtectionSystem;
import pl.jailbreak.mine.MineManager;
import pl.jailbreak.mine.MineCommand;
import pl.jailbreak.mine.MinePos1Command;
import pl.jailbreak.mine.MinePos2Command;
import pl.jailbreak.mine.MineCreateCommand;
import pl.jailbreak.mine.MineRegenCommand;
import pl.jailbreak.mine.MineDeleteCommand;
import pl.jailbreak.mine.MineInfoCommand;
import pl.jailbreak.mine.MineIntervalCommand;
import pl.jailbreak.mine.MineRegenTask;
import pl.jailbreak.achievements.AchievementManager;
import pl.jailbreak.achievements.AchievementCheckTask;
import pl.jailbreak.commands.LeaderboardCommand;
import pl.jailbreak.commands.DailyCommand;
import pl.jailbreak.commands.AchievementsCommand;
import pl.jailbreak.commands.ResetAchievementsCommand;
import pl.jailbreak.commands.admin.ProtectAddCommand;
import pl.jailbreak.commands.admin.ProtectDeleteCommand;
import pl.jailbreak.commands.admin.ProtectListCommand;
import pl.jailbreak.protection.SpawnProtection;
import pl.jailbreak.warp.WarpManager;
import pl.jailbreak.commands.WarpCommand;
import pl.jailbreak.commands.HelpCommand;
import pl.jailbreak.commands.admin.WarpAddCommand;
import pl.jailbreak.commands.admin.WarpDeleteCommand;
import pl.jailbreak.commands.admin.WarpListCommand;
import pl.jailbreak.commands.admin.CmdListCommand;
import pl.jailbreak.commands.VoteCommand;
import pl.jailbreak.commands.PrestigeCommand;
import pl.jailbreak.commands.PrestigeConfirmCommand;
import pl.jailbreak.commands.PricesCommand;
import pl.jailbreak.commands.RankupCommand;
import pl.jailbreak.commands.CrateCommand;
import pl.jailbreak.commands.CrateOpenCommand;
import pl.jailbreak.commands.CrateInfoCommand;
import pl.jailbreak.commands.PayCommand;
import pl.jailbreak.commands.admin.GiveVoteRewardCommand;
import pl.jailbreak.commands.admin.VoteRewardCommand;
import pl.jailbreak.systems.EventManager;
import pl.jailbreak.systems.CrateInteractSystem;
import pl.jailbreak.systems.PickaxeInteractSystem;
import pl.jailbreak.crates.CrateLocationManager;
import pl.jailbreak.hud.ScoreboardManager;
import pl.jailbreak.commands.admin.CrateAddCommand;
import pl.jailbreak.commands.admin.CrateDeleteCommand;
import pl.jailbreak.commands.admin.CrateListCommand;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class JailbreakPlugin extends JavaPlugin {

    private static JailbreakPlugin instance;

    private Database database;
    private PlayerManager playerManager;
    private EconomyManager economyManager;
    private PlayerListener playerListener;
    private ShopManager shopManager;
    private EnchantManager enchantManager;
    private MineManager mineManager;
    private MineRegenTask mineRegenTask;
    private AchievementManager achievementManager;
    private AchievementCheckTask achievementCheckTask;
    private SpawnProtection spawnProtection;
    private WarpManager warpManager;
    private File dataFolder;
    private Timer autoSaveTimer;
    private Timer mineRegenTimer;
    private Timer achievementTimer;
    private EventManager eventManager;
    private Timer eventTimer;
    private CrateLocationManager crateLocationManager;
    private ScoreboardManager scoreboardManager;
    private Timer hudRefreshTimer;
    private pl.jailbreak.systems.SpawnProtectionHealTask spawnProtectionHealTask;
    private Timer spawnProtectionTimer;

    public JailbreakPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        System.out.println("[Voidcraft] Setup starting...");

        dataFolder = new File("plugins/Voidcraft");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        EconomyConfig.init(dataFolder);
        SectorManager.init(dataFolder);

        try {
            ShopConfig.init(dataFolder);
            System.out.println("[Voidcraft] ShopConfig loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] ShopConfig ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        database = new JsonFileDatabase(dataFolder.getPath());
        database.connect();

        playerManager = new PlayerManager(database);
        economyManager = new EconomyManager(playerManager);

        try {
            shopManager = new ShopManager(playerManager, dataFolder);
            System.out.println("[Voidcraft] ShopManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] ShopManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            enchantManager = new EnchantManager(playerManager);
            System.out.println("[Voidcraft] EnchantManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] EnchantManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Mine system
        try {
            mineManager = new MineManager();
            mineManager.init(dataFolder);
            mineRegenTask = new MineRegenTask(mineManager);
            System.out.println("[Voidcraft] MineManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] MineManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Achievement system
        try {
            achievementManager = new AchievementManager(playerManager);
            achievementCheckTask = new AchievementCheckTask(achievementManager, playerManager);
            System.out.println("[Voidcraft] AchievementManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] AchievementManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Spawn protection system
        try {
            spawnProtection = new SpawnProtection();
            spawnProtection.init(dataFolder);
            // Link MineManager so mines are excluded from protection
            if (mineManager != null) {
                spawnProtection.setMineManager(mineManager);
            }
            System.out.println("[Voidcraft] SpawnProtection loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] SpawnProtection ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Crate location system
        try {
            crateLocationManager = new CrateLocationManager();
            crateLocationManager.init(dataFolder);
            System.out.println("[Voidcraft] CrateLocationManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] CrateLocationManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Warp system
        try {
            warpManager = new WarpManager();
            warpManager.init(dataFolder);
            System.out.println("[Voidcraft] WarpManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] WarpManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        playerListener = new PlayerListener(playerManager);
        playerListener.register(getEventRegistry());

        // Register Mining Boost System for Efficiency enchant
        try {
            getEntityStoreRegistry().registerSystem(new MiningBoostSystem(playerManager));
            System.out.println("[Voidcraft] MiningBoostSystem registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] MiningBoostSystem ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Block Place Protection System
        try {
            getEntityStoreRegistry().registerSystem(new BlockPlaceProtectionSystem());
            System.out.println("[Voidcraft] BlockPlaceProtectionSystem registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] BlockPlaceProtectionSystem ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Crate Interact System (UseBlockEvent.Pre on chest blocks)
        try {
            if (crateLocationManager != null) {
                getEntityStoreRegistry().registerSystem(new CrateInteractSystem(playerManager, crateLocationManager));
                System.out.println("[Voidcraft] CrateInteractSystem registered!");
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] CrateInteractSystem ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Pickaxe Interact System (right-click pickaxe opens enchant shop)
        try {
            getEntityStoreRegistry().registerSystem(new PickaxeInteractSystem(playerManager, crateLocationManager));
            System.out.println("[Voidcraft] PickaxeInteractSystem registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] PickaxeInteractSystem ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Item Pickup Protection System (blocks F pickup in protected zones)
        try {
            getEntityStoreRegistry().registerSystem(new pl.jailbreak.systems.ItemPickupProtectionSystem());
            System.out.println("[Voidcraft] ItemPickupProtectionSystem registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] ItemPickupProtectionSystem ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Block Use Protection System (blocks F/Use interactions in protected zones)
        try {
            getEntityStoreRegistry().registerSystem(new pl.jailbreak.systems.BlockUseProtectionSystem());
            System.out.println("[Voidcraft] BlockUseProtectionSystem registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] BlockUseProtectionSystem ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Scoreboard HUD manager
        try {
            scoreboardManager = new ScoreboardManager(playerManager);
            System.out.println("[Voidcraft] ScoreboardManager loaded!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] ScoreboardManager ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Help command
        getCommandRegistry().registerCommand(new HelpCommand());

        // Basic commands
        getCommandRegistry().registerCommand(new BalanceCommand());
        getCommandRegistry().registerCommand(new StatsCommand());
        getCommandRegistry().registerCommand(new SellCommand());
        getCommandRegistry().registerCommand(new SectorCommand());
        getCommandRegistry().registerCommand(new LeaderboardCommand());
        getCommandRegistry().registerCommand(new DailyCommand());
        getCommandRegistry().registerCommand(new AchievementsCommand());
        getCommandRegistry().registerCommand(new ResetAchievementsCommand());

        // Admin commands
        getCommandRegistry().registerCommand(new SetBalanceCommand());
        getCommandRegistry().registerCommand(new SetSectorCommand());
        getCommandRegistry().registerCommand(new JailbreakPricesCommand());
        getCommandRegistry().registerCommand(new JailbreakSetPriceCommand());
        getCommandRegistry().registerCommand(new JailbreakMultiplierCommand());
        getCommandRegistry().registerCommand(new JailbreakReloadCommand());
        getCommandRegistry().registerCommand(new AdminCommand());
        getCommandRegistry().registerCommand(new WhatIsThisCommand());
        getCommandRegistry().registerCommand(new DebugInventoryCommand());
        getCommandRegistry().registerCommand(new ProtectAddCommand());
        getCommandRegistry().registerCommand(new ProtectDeleteCommand());
        getCommandRegistry().registerCommand(new ProtectListCommand());

        // Shop commands
        try {
            getCommandRegistry().registerCommand(new ShopCommand());
            getCommandRegistry().registerCommand(new BuyCommand());
            getCommandRegistry().registerCommand(new EnchantsCommand());
            System.out.println("[Voidcraft] Shop commands registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Shop commands ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Mine commands
        try {
            getCommandRegistry().registerCommand(new MineCommand(mineManager));
            getCommandRegistry().registerCommand(new MinePos1Command());
            getCommandRegistry().registerCommand(new MinePos2Command());
            getCommandRegistry().registerCommand(new MineCreateCommand(mineManager));
            getCommandRegistry().registerCommand(new MineRegenCommand(mineManager));
            getCommandRegistry().registerCommand(new MineDeleteCommand(mineManager));
            getCommandRegistry().registerCommand(new MineInfoCommand(mineManager));
            getCommandRegistry().registerCommand(new MineIntervalCommand(mineManager));
            System.out.println("[Voidcraft] Mine commands registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Mine commands ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Warp commands
        try {
            getCommandRegistry().registerCommand(new WarpCommand());
            getCommandRegistry().registerCommand(new WarpAddCommand());
            getCommandRegistry().registerCommand(new WarpDeleteCommand());
            getCommandRegistry().registerCommand(new WarpListCommand());
            System.out.println("[Voidcraft] Warp commands registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Warp commands ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Cmd list command (admin help)
        try {
            getCommandRegistry().registerCommand(new CmdListCommand());
            System.out.println("[Voidcraft] CmdList command registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] CmdList ERROR: " + e.getMessage());
        }

        // Vote commands
        try {
            getCommandRegistry().registerCommand(new VoteCommand());
            getCommandRegistry().registerCommand(new GiveVoteRewardCommand());
            getCommandRegistry().registerCommand(new VoteRewardCommand());
            System.out.println("[Voidcraft] Vote commands registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Vote commands ERROR: " + e.getMessage());
        }

        // New gameplay commands
        try {
            getCommandRegistry().registerCommand(new PrestigeCommand());
            getCommandRegistry().registerCommand(new PrestigeConfirmCommand());
            getCommandRegistry().registerCommand(new PricesCommand());
            getCommandRegistry().registerCommand(new RankupCommand());
            getCommandRegistry().registerCommand(new CrateCommand());
            getCommandRegistry().registerCommand(new CrateOpenCommand());
            getCommandRegistry().registerCommand(new CrateInfoCommand());
            getCommandRegistry().registerCommand(new CrateAddCommand());
            getCommandRegistry().registerCommand(new CrateDeleteCommand());
            getCommandRegistry().registerCommand(new CrateListCommand());
            getCommandRegistry().registerCommand(new PayCommand());
            System.out.println("[Voidcraft] Gameplay commands registered!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] Gameplay commands ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        autoSaveTimer = new Timer("Voidcraft-AutoSave", true);
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (playerManager != null) playerManager.saveAllPlayers();
            }
        }, 300000, 300000);

        // Mine regen timer - checks every 30 seconds
        mineRegenTimer = new Timer("Voidcraft-MineRegen", true);
        mineRegenTimer.scheduleAtFixedRate(mineRegenTask, 30000, 30000);

        // Achievement check timer - checks every 10 seconds
        achievementTimer = new Timer("Voidcraft-AchievementCheck", true);
        achievementTimer.scheduleAtFixedRate(achievementCheckTask, 10000, 10000);

        // Event manager - random multiplier events
        try {
            eventManager = new EventManager();
            eventTimer = new Timer("Voidcraft-Events", true);
            eventTimer.scheduleAtFixedRate(eventManager, EventManager.getCheckIntervalMs(), EventManager.getCheckIntervalMs());
            System.out.println("[Voidcraft] EventManager started (checks every 30 min)!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] EventManager ERROR: " + e.getMessage());
        }

        // HUD refresh timer - updates every 5 seconds
        hudRefreshTimer = new Timer("Voidcraft-HudRefresh", true);
        hudRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (scoreboardManager == null || playerManager == null) return;
                try {
                    for (var entry : scoreboardManager.getHuds().entrySet()) {
                        java.util.UUID uuid = entry.getKey();
                        pl.jailbreak.player.PlayerData data = playerManager.getPlayer(uuid.toString());
                        if (data != null) {
                            // Get stored Player reference so pickaxe info can be read
                            com.hypixel.hytale.server.core.entity.entities.Player player = scoreboardManager.getPlayer(uuid);
                            entry.getValue().refresh(player, data);
                        }
                    }
                } catch (Exception e) {
                    // Silently ignore refresh errors
                }
            }
        }, 5000, 5000);

        // Spawn protection invulnerability timer - checks every 1 second
        try {
            spawnProtectionHealTask = new pl.jailbreak.systems.SpawnProtectionHealTask();
            spawnProtectionTimer = new Timer("Voidcraft-SpawnProtect", true);
            spawnProtectionTimer.scheduleAtFixedRate(spawnProtectionHealTask, 2000, 1000);
            System.out.println("[Voidcraft] SpawnProtectionHealTask started!");
        } catch (Exception e) {
            System.out.println("[Voidcraft] SpawnProtectionHealTask ERROR: " + e.getMessage());
        }

        System.out.println("[Voidcraft] Setup complete! All commands should be registered.");
    }

    @Override
    protected void start() {
        System.out.println("[Voidcraft] Plugin v1.0.0 STARTED!");
    }

    @Override
    protected void shutdown() {
        if (autoSaveTimer != null) autoSaveTimer.cancel();
        if (mineRegenTimer != null) mineRegenTimer.cancel();
        if (achievementTimer != null) achievementTimer.cancel();
        if (eventTimer != null) eventTimer.cancel();
        if (hudRefreshTimer != null) hudRefreshTimer.cancel();
        if (spawnProtectionTimer != null) spawnProtectionTimer.cancel();
        if (playerManager != null) playerManager.saveAllPlayers();
        if (mineManager != null) mineManager.save();
        if (database != null) database.disconnect();
    }

    public static JailbreakPlugin getInstance() { return instance; }
    public static PlayerManager getPlayerManager() { return instance.playerManager; }
    public static EconomyManager getEconomyManager() { return instance.economyManager; }
    public static ShopManager getShopManager() { return instance.shopManager; }
    public static EnchantManager getEnchantManager() { return instance.enchantManager; }
    public static MineManager getMineManager() { return instance.mineManager; }
    public static MineRegenTask getMineRegenTask() { return instance.mineRegenTask; }
    public static AchievementManager getAchievementManager() { return instance.achievementManager; }
    public static AchievementCheckTask getAchievementCheckTask() { return instance.achievementCheckTask; }
    public static SpawnProtection getSpawnProtection() { return instance.spawnProtection; }
    public static WarpManager getWarpManager() { return instance.warpManager; }
    public static CrateLocationManager getCrateLocationManager() { return instance.crateLocationManager; }
    public static ScoreboardManager getScoreboardManager() { return instance.scoreboardManager; }
    public static pl.jailbreak.systems.SpawnProtectionHealTask getSpawnProtectionHealTask() { return instance.spawnProtectionHealTask; }
    public static File getDataFolder() { return instance.dataFolder; }
}