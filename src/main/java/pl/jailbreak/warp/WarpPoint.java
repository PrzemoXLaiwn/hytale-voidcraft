package pl.jailbreak.warp;

/**
 * Represents a warp/teleport location
 */
public class WarpPoint {
    private String name;
    private String displayName;
    private String description;
    private String icon;
    private String color;
    private double x;
    private double y;
    private double z;
    private String permission;
    private boolean enabled;
    private int slot;

    public WarpPoint() {
        this.enabled = true;
        this.color = "#FFFFFF";
        this.icon = "portal";
    }

    public WarpPoint(String name, String displayName, double x, double y, double z) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.description = "Teleport to " + displayName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
}
