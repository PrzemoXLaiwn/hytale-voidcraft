package pl.jailbreak.utils;

/**
 * Narzędzia do formatowania wiadomości.
 * Kolory, prefix, formatowanie tekstu.
 */
public class MessageUtil {

    // Prefix pluginu - pojawia się przed każdą wiadomością
    public static final String PREFIX = "&8[&6Jailbreak&8] &7";

    // Kolory (kody Minecraft/Hytale)
    public static final String COLOR_PRIMARY = "&6";    // Złoty - główny kolor
    public static final String COLOR_SECONDARY = "&7";  // Szary - tekst
    public static final String COLOR_SUCCESS = "&a";    // Zielony - sukces
    public static final String COLOR_ERROR = "&c";      // Czerwony - błąd
    public static final String COLOR_WARNING = "&e";    // Żółty - ostrzeżenie
    public static final String COLOR_INFO = "&b";       // Aqua - info
    public static final String COLOR_MONEY = "&2";      // Ciemny zielony - kasa
    public static final String COLOR_PRESTIGE = "&d";   // Różowy - prestige

    /**
     * Dodaje prefix do wiadomości
     */
    public static String prefix(String message) {
        return colorize(PREFIX + message);
    }

    /**
     * Wiadomość sukcesu (zielona)
     */
    public static String success(String message) {
        return prefix(COLOR_SUCCESS + message);
    }

    /**
     * Wiadomość błędu (czerwona)
     */
    public static String error(String message) {
        return prefix(COLOR_ERROR + message);
    }

    /**
     * Wiadomość ostrzeżenia (żółta)
     */
    public static String warning(String message) {
        return prefix(COLOR_WARNING + message);
    }

    /**
     * Wiadomość info (aqua)
     */
    public static String info(String message) {
        return prefix(COLOR_INFO + message);
    }

    /**
     * Zamienia kody kolorów (&a, &b, &c) na prawdziwe kolory
     * W Hytale może być inny system - dostosujemy później
     */
    public static String colorize(String message) {
        if (message == null) return "";

        // Standardowy system kolorów Minecraft (§)
        // Hytale może używać innego - sprawdzimy
        return message.replace("&", "§");
    }

    /**
     * Usuwa kody kolorów z tekstu
     */
    public static String stripColors(String message) {
        if (message == null) return "";

        return message.replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * Formatuje czas (sekundy -> "2h 30m 15s")
     */
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("s");
        }

        return sb.toString().trim();
    }

    /**
     * Formatuje gwiazdki prestige (3 -> "⭐⭐⭐")
     */
    public static String formatPrestige(int prestige) {
        if (prestige <= 0) {
            return COLOR_SECONDARY + "Brak";
        }

        if (prestige <= 10) {
            return COLOR_PRESTIGE + "⭐".repeat(prestige);
        } else {
            // Dla 10+ pokazuj liczbę
            return COLOR_PRESTIGE + "⭐x" + prestige;
        }
    }

    /**
     * Formatuje sektor (dodaje kolor zależnie od tieru)
     */
    public static String formatSector(String sector) {
        if (sector == null || sector.isEmpty()) {
            return COLOR_SECONDARY + "?";
        }

        char s = sector.charAt(0);

        // Tier 1 (A-F) - biały
        if (s >= 'A' && s <= 'F') {
            return "&f" + sector;
        }
        // Tier 2 (G-L) - zielony
        else if (s >= 'G' && s <= 'L') {
            return "&a" + sector;
        }
        // Tier 3 (M-R) - niebieski
        else if (s >= 'M' && s <= 'R') {
            return "&b" + sector;
        }
        // Tier 4 (S-V) - fioletowy
        else if (s >= 'S' && s <= 'V') {
            return "&d" + sector;
        }
        // Tier 5 (W-Z) - złoty
        else {
            return "&6" + sector;
        }
    }

    /**
     * Tworzy pasek postępu [████████░░] 80%
     */
    public static String progressBar(double percent, int length) {
        int filled = (int) Math.round(percent / 100.0 * length);
        int empty = length - filled;

        return "&a" + "█".repeat(Math.max(0, filled)) +
                "&7" + "░".repeat(Math.max(0, empty)) +
                " &f" + Math.round(percent) + "%";
    }
}