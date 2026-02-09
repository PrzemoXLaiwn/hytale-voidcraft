package pl.jailbreak.statue;

import com.hypixel.hytale.math.Vector3d;
import com.hypixel.hytale.math.Vector3f;

import java.util.Objects;

/**
 * Model reprezentujący pojedynczą statuetkę gracza na spawnie.
 * Przechowuje informacje o pozycji w rankingu, danych gracza i lokacji statuetki.
 */
public class StatueData {
    private int rank;              // Pozycja w rankingu (1-4)
    private String playerUuid;     // UUID gracza
    private String playerName;     // Nick gracza
    private long balance;          // Balance gracza
    private Vector3d position;     // Pozycja statuetki w świecie
    private Vector3f rotation;     // Rotacja statuetki (yaw, pitch, roll)

    /**
     * Konstruktor pełny
     */
    public StatueData(int rank, String playerUuid, String playerName, long balance,
                      Vector3d position, Vector3f rotation) {
        this.rank = rank;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.balance = balance;
        this.position = position;
        this.rotation = rotation;
    }

    /**
     * Konstruktor bez pozycji (do późniejszego ustawienia)
     */
    public StatueData(int rank, String playerUuid, String playerName, long balance) {
        this(rank, playerUuid, playerName, balance, null, null);
    }

    // Gettery
    public int getRank() {
        return rank;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getBalance() {
        return balance;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    // Settery
    public void setPosition(Vector3d position) {
        this.position = position;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    /**
     * Porównuje dwie statuetki - równe jeśli ten sam gracz i balance
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatueData that = (StatueData) o;
        return balance == that.balance &&
               rank == that.rank &&
               Objects.equals(playerUuid, that.playerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, playerUuid, balance);
    }

    @Override
    public String toString() {
        return "StatueData{" +
               "rank=" + rank +
               ", playerName='" + playerName + '\'' +
               ", balance=" + balance +
               ", position=" + position +
               '}';
    }
}
