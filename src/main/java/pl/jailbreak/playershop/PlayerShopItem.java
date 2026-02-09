package pl.jailbreak.playershop;

/**
 * Represents an item for sale in a player's shop
 */
public class PlayerShopItem {
    private String itemId;
    private int amount;
    private long price;
    private int slot;

    public PlayerShopItem() {}

    public PlayerShopItem(String itemId, int amount, long price, int slot) {
        this.itemId = itemId;
        this.amount = amount;
        this.price = price;
        this.slot = slot;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public String getDisplayName() {
        // Extract simple name from item ID
        if (itemId == null) return "Unknown";
        String[] parts = itemId.split(":");
        if (parts.length > 1) {
            return parts[1].replace("_", " ");
        }
        return itemId.replace("_", " ");
    }
}
