package pl.jailbreak.playershop;

import java.util.*;

/**
 * Represents a player-owned shop
 */
public class PlayerShop {
    private String ownerUuid;
    private String ownerName;
    private String shopName;
    private String description;
    private long createdAt;
    private long totalSales;
    private int totalTransactions;
    private boolean open;
    private List<PlayerShopItem> items;

    public PlayerShop() {
        this.items = new ArrayList<>();
        this.open = true;
        this.createdAt = System.currentTimeMillis();
    }

    public PlayerShop(String ownerUuid, String ownerName, String shopName) {
        this();
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.shopName = shopName;
        this.description = ownerName + "'s Shop";
    }

    // Getters and setters
    public String getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(String ownerUuid) { this.ownerUuid = ownerUuid; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getTotalSales() { return totalSales; }
    public void setTotalSales(long totalSales) { this.totalSales = totalSales; }
    public void addSale(long amount) { this.totalSales += amount; this.totalTransactions++; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public List<PlayerShopItem> getItems() { return items; }
    public void setItems(List<PlayerShopItem> items) { this.items = items; }

    public void addItem(PlayerShopItem item) {
        item.setSlot(items.size());
        items.add(item);
    }

    public PlayerShopItem getItem(int slot) {
        for (PlayerShopItem item : items) {
            if (item.getSlot() == slot) return item;
        }
        return null;
    }

    public boolean removeItem(int slot) {
        return items.removeIf(item -> item.getSlot() == slot);
    }

    public int getItemCount() {
        return items.size();
    }
}
