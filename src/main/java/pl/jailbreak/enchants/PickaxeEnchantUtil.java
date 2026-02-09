package pl.jailbreak.enchants;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import org.bson.BsonDocument;
import org.bson.BsonInt32;

/**
 * Utility for reading/writing enchant data from ItemStack BsonDocument metadata.
 * Enchants are stored per-pickaxe, not per-player account.
 */
public class PickaxeEnchantUtil {

    private static final String ENCHANT_KEY = "voidcraft_enchants";

    /**
     * Check if an ItemStack is a pickaxe
     */
    public static boolean isPickaxe(ItemStack item) {
        if (item == null || !item.isValid()) return false;
        String id = item.getItemId();
        return id != null && id.startsWith("Tool_Pickaxe_");
    }

    /**
     * Get the pickaxe the player is currently holding.
     * Uses getItemInHand() which handles both hotbar and tools container.
     * Falls back to scanning all tools slots to find any pickaxe.
     * Returns null if not holding a pickaxe.
     */
    public static ItemStack getHeldPickaxe(Player player) {
        try {
            Inventory inv = player.getInventory();
            if (inv == null) return null;

            // Primary: getItemInHand() auto-handles hotbar vs tools
            try {
                ItemStack held = inv.getItemInHand();
                if (held != null && isPickaxe(held)) return held;
            } catch (Exception ignored) {}

            // Fallback 1: check active tool item directly
            try {
                ItemStack held = inv.getActiveToolItem();
                if (held != null && isPickaxe(held)) return held;
            } catch (Exception ignored) {}

            // Fallback 2: check active hotbar item
            try {
                ItemStack held = inv.getActiveHotbarItem();
                if (held != null && isPickaxe(held)) return held;
            } catch (Exception ignored) {}

            // Fallback 3: scan ALL tools slots for any pickaxe
            try {
                ItemContainer tools = inv.getTools();
                if (tools != null) {
                    short capacity = tools.getCapacity();
                    for (short slot = 0; slot < capacity; slot++) {
                        ItemStack item = tools.getItemStack(slot);
                        if (item != null && isPickaxe(item)) return item;
                    }
                }
            } catch (Exception ignored) {}

            // Fallback 4: scan hotbar for any pickaxe
            try {
                ItemContainer hotbar = inv.getHotbar();
                if (hotbar != null) {
                    short capacity = hotbar.getCapacity();
                    for (short slot = 0; slot < capacity; slot++) {
                        ItemStack item = hotbar.getItemStack(slot);
                        if (item != null && isPickaxe(item)) return item;
                    }
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.out.println("[Voidcraft] Error getting held pickaxe: " + e.getMessage());
        }
        return null;
    }

    /**
     * Read enchants from a pickaxe's BsonDocument metadata.
     * Returns a new PlayerEnchants with level 0 for all if no metadata found.
     */
    public static PlayerEnchants readEnchants(ItemStack pickaxe) {
        PlayerEnchants enchants = new PlayerEnchants();
        if (pickaxe == null || !pickaxe.isValid()) return enchants;

        try {
            BsonDocument metadata = pickaxe.getMetadata();
            if (metadata == null || !metadata.containsKey(ENCHANT_KEY)) return enchants;

            BsonDocument enchantDoc = metadata.getDocument(ENCHANT_KEY);
            if (enchantDoc == null) return enchants;

            for (EnchantType type : EnchantType.values()) {
                String key = type.name();
                if (enchantDoc.containsKey(key)) {
                    int level = enchantDoc.getInt32(key).getValue();
                    enchants.setLevel(type, level);
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error reading pickaxe enchants: " + e.getMessage());
        }

        return enchants;
    }

    /**
     * Write enchants to a pickaxe's BsonDocument metadata.
     * Returns a NEW ItemStack with updated metadata (ItemStack is immutable).
     */
    public static ItemStack writeEnchants(ItemStack pickaxe, PlayerEnchants enchants) {
        if (pickaxe == null || !pickaxe.isValid()) return pickaxe;

        try {
            BsonDocument enchantDoc = new BsonDocument();
            for (EnchantType type : EnchantType.values()) {
                int level = enchants.getLevel(type);
                if (level > 0) {
                    enchantDoc.put(type.name(), new BsonInt32(level));
                }
            }

            return pickaxe.withMetadata(ENCHANT_KEY, enchantDoc);
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error writing pickaxe enchants: " + e.getMessage());
            return pickaxe;
        }
    }

    /**
     * Replace the pickaxe in the player's currently active slot (tools or hotbar).
     * Checks usingToolsItem() to determine the correct container.
     */
    public static void replaceHeldPickaxe(Player player, ItemStack newPickaxe) {
        try {
            Inventory inv = player.getInventory();
            if (inv == null) return;

            if (inv.usingToolsItem()) {
                // Pickaxe is in the tools container
                byte slot = inv.getActiveToolsSlot();
                ItemContainer tools = inv.getTools();
                if (tools != null) {
                    tools.setItemStackForSlot(slot, newPickaxe);
                }
            } else {
                // Pickaxe is in the hotbar
                byte slot = inv.getActiveHotbarSlot();
                ItemContainer hotbar = inv.getHotbar();
                if (hotbar != null) {
                    hotbar.setItemStackForSlot(slot, newPickaxe);
                }
            }
        } catch (Exception e) {
            System.out.println("[Voidcraft] Error replacing held pickaxe: " + e.getMessage());
        }
    }

    /**
     * Get a display string of enchants on a pickaxe (for lore/chat display).
     */
    public static String getEnchantLore(PlayerEnchants enchants) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (EnchantType type : EnchantType.values()) {
            int level = enchants.getLevel(type);
            if (level > 0) {
                if (!first) sb.append(", ");
                sb.append(type.getDisplayWithLevel(level));
                first = false;
            }
        }
        return sb.length() > 0 ? sb.toString() : "None";
    }

    /**
     * Get the pickaxe tier display name from item ID.
     * E.g. "Tool_Pickaxe_Iron" -> "Iron"
     */
    public static String getPickaxeTierName(ItemStack pickaxe) {
        if (pickaxe == null || !isPickaxe(pickaxe)) return "None";
        String id = pickaxe.getItemId();
        return id.replace("Tool_Pickaxe_", "");
    }
}
