package pl.jailbreak.mine;

import com.hypixel.hytale.math.vector.Vector3i;

/**
 * Static storage for mine positions during creation
 */
public class MinePositions {

    private static Vector3i pos1 = null;
    private static Vector3i pos2 = null;

    public static Vector3i getPos1() {
        return pos1;
    }

    public static void setPos1(Vector3i pos) {
        pos1 = pos;
    }

    public static Vector3i getPos2() {
        return pos2;
    }

    public static void setPos2(Vector3i pos) {
        pos2 = pos;
    }

    public static void clear() {
        pos1 = null;
        pos2 = null;
    }

    public static boolean bothSet() {
        return pos1 != null && pos2 != null;
    }
}
