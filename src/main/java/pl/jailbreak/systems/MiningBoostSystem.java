package pl.jailbreak.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import pl.jailbreak.enchants.EnchantType;
import pl.jailbreak.enchants.PickaxeEnchantUtil;
import pl.jailbreak.enchants.PlayerEnchants;
import pl.jailbreak.config.EconomyConfig;
import pl.jailbreak.crates.CrateManager;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;
import pl.jailbreak.protection.SpawnProtection;
import pl.jailbreak.JailbreakPlugin;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ECS System that handles mining enchants:
 * - Efficiency: Breaks extra adjacent blocks
 * - Auto-Sell: Instantly sells mined ores
 * - Multi-Drop: Chance for bonus ore drop
 */
public class MiningBoostSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final PlayerManager playerManager;
    private final Map<UUID, Long> lastBreakTime = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 200;
    private static final Random RANDOM = new Random();
    // Flag to prevent recursive triggering
    private static final ThreadLocal<Boolean> IS_BOOSTING = ThreadLocal.withInitial(() -> false);

    public MiningBoostSystem(PlayerManager playerManager) {
        super(BreakBlockEvent.class);
        this.playerManager = playerManager;
        System.out.println("[Voidcraft] MiningBoostSystem created!");
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int entityId, ArchetypeChunk<EntityStore> chunk,
                      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
                      BreakBlockEvent event) {

        // Prevent recursive calls
        if (IS_BOOSTING.get()) return;

        try {
            // Get player reference and component
            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityId);
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) return;

            UUID playerUuid = player.getUuid();
            String uuid = playerUuid.toString();

            // Cooldown check
            long now = System.currentTimeMillis();
            Long lastBreak = lastBreakTime.get(playerUuid);
            if (lastBreak != null && (now - lastBreak) < COOLDOWN_MS) {
                return;
            }
            lastBreakTime.put(playerUuid, now);

            // Get player data
            PlayerData data = playerManager.getPlayer(uuid);
            if (data == null) return;

            // Get block info
            BlockType blockType = event.getBlockType();
            if (blockType == null) return;

            String blockId = blockType.getId();
            if (blockId == null) return;
            // Check spawn protection (skip for operators)
            Vector3i targetBlock = event.getTargetBlock();
            if (targetBlock != null) {
                SpawnProtection protection = JailbreakPlugin.getSpawnProtection();
                if (protection != null && protection.isProtected(targetBlock)) {
                    // Allow operators to bypass protection
                    boolean canBypass = false;
                    try {
                        canBypass = player.hasPermission("voidcraft.admin.bypass");
                    } catch (Exception ignored) {}

                    if (!canBypass) {
                        player.sendMessage(Message.raw("This area is protected!").color("#FF0000"));
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            // Check if it's an ore/mine block
            boolean isOre = isOreBlock(blockId);

            // ALWAYS count blocks mined (for achievements) - even non-ore blocks
            data.addBlocksMined(1);

            // If not an ore, stop here (no enchants apply)
            if (!isOre) return;

            // === CRATE KEY DROP ===
            if (RANDOM.nextDouble() < CrateManager.KEY_DROP_CHANCE) {
                String keyType = CrateManager.rollKeyDrop();
                data.addKey(keyType, 1);
                String keyColor = CrateManager.getColor(keyType);
                player.sendMessage(Message.raw("  KEY FOUND! " + CrateManager.getDisplayName(keyType) + " Key!").color(keyColor));
            }

            // Get enchants from held pickaxe (per-pickaxe enchants)
            ItemStack heldPickaxe = PickaxeEnchantUtil.getHeldPickaxe(player);
            PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(heldPickaxe);
            World world = player.getWorld();

            // Track total ores mined (for auto-sell/multi-drop)
            int totalOresMined = 1; // The block player is mining

            // === EFFICIENCY ENCHANT ===
            int efficiencyLevel = enchants.getLevel(EnchantType.EFFICIENCY);
            int extraBlocksBroken = 0;

            if (efficiencyLevel >= 1 && world != null && targetBlock != null) {
                int extraBlocks = efficiencyLevel; // level 1 = +1, level 2 = +2, etc.
                IS_BOOSTING.set(true);
                try {
                    extraBlocksBroken = breakAdjacentBlocks(world, targetBlock, blockId, extraBlocks, player);
                    totalOresMined += extraBlocksBroken;
                    if (extraBlocksBroken > 0) {
                        player.sendMessage(Message.raw("Efficiency +" + extraBlocksBroken + " blocks!").color("#00FFFF"));
                    }
                } finally {
                    IS_BOOSTING.set(false);
                }
            }

            // === MULTI-DROP ENCHANT ===
            // Chance for bonus virtual drops (adds to auto-sell value or just shows message)
            double multiDropChance = enchants.getMultiDropChance();
            int bonusDrops = 0;
            if (multiDropChance > 0) {
                // Check for each ore mined
                for (int i = 0; i < totalOresMined; i++) {
                    if (RANDOM.nextDouble() < multiDropChance) {
                        bonusDrops++;
                    }
                }
                if (bonusDrops > 0) {
                    player.sendMessage(Message.raw("Multi-Drop +" + bonusDrops + "!").color("#9966CC"));
                }
            }

            // === COUNT EXTRA BLOCKS MINED from efficiency (for achievements) ===
            // Note: The base block (1) was already counted above
            int extraBlocksForStats = extraBlocksBroken + bonusDrops;
            if (extraBlocksForStats > 0) {
                data.addBlocksMined(extraBlocksForStats);
            }

            // === AUTO-SELL ENCHANT ===
            if (enchants.hasAutoSell()) {
                String playerSector = data.getCurrentSector();
                int price = EconomyConfig.getOrePrice(blockId);

                if (price > 0 && EconomyConfig.canSellOre(blockId, playerSector)) {
                    // Calculate total to sell (original + efficiency blocks + bonus drops)
                    int totalToSell = totalOresMined + bonusDrops;

                    // Apply fortune bonus
                    double fortuneMultiplier = enchants.getFortuneMultiplier();
                    long baseValue = (long) price * totalToSell;
                    long totalValue = (long) (baseValue * fortuneMultiplier);

                    // Apply prestige bonus
                    int prestigeBonus = data.getPrestige() * EconomyConfig.getPrestigeBonusPercent();
                    totalValue += (totalValue * prestigeBonus) / 100;

                    // Apply global multiplier
                    totalValue = (long) (totalValue * EconomyConfig.getGlobalMultiplier());

                    // Add money
                    data.addBalance(totalValue);

                    // Cancel normal drop by not adding to inventory
                    // (the block drop is handled separately by the game)

                    player.sendMessage(Message.raw("Auto-Sell: +" + totalValue + " coins").color("#00FF00"));

                    // Remove the ores from inventory that would have been added
                    // Note: This happens after the block break, so we need to remove them
                    try {
                        Inventory inventory = player.getInventory();
                        if (inventory != null) {
                            // Remove the mined ores from inventory (game adds them automatically)
                            removeItemsFromInventory(inventory, blockId, totalOresMined);
                        }
                    } catch (Exception e) {
                        // Silently ignore - may not have items yet
                    }
                }
            } else {
                // No auto-sell - player gets items in inventory
                // For efficiency extra blocks, give items to player!
                if (extraBlocksBroken > 0) {
                    try {
                        Inventory inventory = player.getInventory();
                        if (inventory != null) {
                            // Add extra items to inventory (one for each extra block broken)
                            for (int i = 0; i < extraBlocksBroken; i++) {
                                inventory.getHotbar().addItemStack(new ItemStack(blockId, 1));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[Voidcraft] Error adding efficiency items: " + e.getMessage());
                    }
                }

                // Multi-drop bonus items
                if (bonusDrops > 0) {
                    try {
                        Inventory inventory = player.getInventory();
                        if (inventory != null) {
                            for (int i = 0; i < bonusDrops; i++) {
                                inventory.getHotbar().addItemStack(new ItemStack(blockId, 1));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[Voidcraft] Error adding multi-drop items: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[Voidcraft] MiningBoost error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove items from player inventory (used for auto-sell)
     * Note: Since we can't modify stack quantity, we remove entire stacks
     * and track how much was removed
     */
    private void removeItemsFromInventory(Inventory inventory, String itemId, int amount) {
        try {
            int remaining = amount;

            // Check hotbar first
            var hotbar = inventory.getHotbar();
            if (hotbar != null) {
                short capacity = hotbar.getCapacity();
                for (short slot = 0; slot < capacity && remaining > 0; slot++) {
                    ItemStack stack = hotbar.getItemStack(slot);
                    if (stack != null && stack.isValid() && itemId.equals(stack.getItemId())) {
                        int qty = stack.getQuantity();
                        // Remove entire slot
                        hotbar.removeItemStackFromSlot(slot);
                        remaining -= qty;
                    }
                }
            }

            // Check storage
            var storage = inventory.getStorage();
            if (storage != null && remaining > 0) {
                short capacity = storage.getCapacity();
                for (short slot = 0; slot < capacity && remaining > 0; slot++) {
                    ItemStack stack = storage.getItemStack(slot);
                    if (stack != null && stack.isValid() && itemId.equals(stack.getItemId())) {
                        int qty = stack.getQuantity();
                        // Remove entire slot
                        storage.removeItemStackFromSlot(slot);
                        remaining -= qty;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors - items may not exist yet
        }
    }

    private boolean isOreBlock(String blockId) {
        if (blockId == null) return false;
        String lower = blockId.toLowerCase();
        // Include all mine blocks: ores, stone, cobble, rocks
        return lower.contains("ore") ||
               lower.contains("coal") ||
               lower.contains("iron") ||
               lower.contains("gold") ||
               lower.contains("diamond") ||
               lower.contains("emerald") ||
               lower.contains("copper") ||
               lower.contains("lapis") ||
               lower.contains("redstone") ||
               lower.contains("quartz") ||
               lower.contains("stone") ||
               lower.contains("cobble") ||
               lower.contains("rock") ||
               lower.contains("thorium") ||
               lower.contains("silver") ||
               lower.contains("cobalt") ||
               lower.contains("adamantite") ||
               lower.contains("ruby") ||
               lower.contains("sapphire") ||
               lower.contains("topaz") ||
               lower.contains("magma");
    }

    /**
     * Remove adjacent ore blocks WITHOUT dropping items.
     * Uses setBlock to air instead of breakBlock to avoid natural drops.
     * The items will be given separately (same type as the block player mined).
     */
    private int breakAdjacentBlocks(World world, Vector3i center, String targetBlockId, int maxBlocks, Player player) {
        int broken = 0;

        // Check spawn protection for adjacent blocks
        SpawnProtection protection = JailbreakPlugin.getSpawnProtection();

        // 6 directions - adjacent blocks
        int[][] offsets = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
        };

        for (int[] offset : offsets) {
            if (broken >= maxBlocks) break;

            try {
                int x = center.x + offset[0];
                int y = center.y + offset[1];
                int z = center.z + offset[2];

                // Skip if in protected area
                if (protection != null && protection.isProtected(x, y, z)) {
                    continue;
                }

                BlockType adjacentType = world.getBlockType(x, y, z);
                if (adjacentType != null) {
                    String adjacentId = adjacentType.getId();
                    // Remove any ore/mine block (not just identical ones)
                    if (adjacentId != null && isOreBlock(adjacentId)) {
                        // Use breakBlock to destroy
                        try {
                            world.breakBlock(x, y, z, 0);
                            broken++;
                        } catch (Exception ex) {
                            // Silently ignore
                        }
                    }
                }
            } catch (Exception e) {
                // Skip this block
            }
        }

        return broken;
    }
}
