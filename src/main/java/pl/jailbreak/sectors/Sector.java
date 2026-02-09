package pl.jailbreak.sectors;

public class Sector {
    public final String id;
    public final String name;
    public final long price;
    public final String ores;
    public final int x, y, z;
    public final String nextSectorId;
    
    public Sector(String id, String name, long price, String ores, int x, int y, int z) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ores = ores;
        this.x = x;
        this.y = y;
        this.z = z;
        char next = (char)(id.charAt(0) + 1);
        this.nextSectorId = next <= 'J' ? String.valueOf(next) : null;
    }
    
    public boolean isFree() { return price == 0; }
    public boolean hasNext() { return nextSectorId != null; }
}