package pl.jailbreak.systems;

import pl.jailbreak.config.EconomyConfig;

import java.util.Random;
import java.util.TimerTask;

public class EventManager extends TimerTask {

    private static EventManager instance;

    private static final Random RANDOM = new Random();
    private static final int EVENT_CHANCE_PERCENT = 15; // 15% chance each check
    private static final long EVENT_DURATION_MS = 10 * 60 * 1000; // 10 minutes
    private static final long CHECK_INTERVAL_MS = 30 * 60 * 1000; // check every 30 min

    private String activeEvent = null;
    private double activeMultiplier = 1.0;
    private long eventEndTime = 0;

    private static final String[] EVENT_NAMES = {
        "DOUBLE MONEY",
        "TRIPLE ORE VALUE",
        "GOLD RUSH",
        "MINING FRENZY"
    };
    private static final double[] EVENT_MULTIPLIERS = {
        2.0,
        3.0,
        2.5,
        2.0
    };

    public EventManager() {
        instance = this;
    }

    @Override
    public void run() {
        // Check if current event expired
        if (activeEvent != null && System.currentTimeMillis() >= eventEndTime) {
            endEvent();
        }

        // Maybe start new event
        if (activeEvent == null && RANDOM.nextInt(100) < EVENT_CHANCE_PERCENT) {
            startRandomEvent();
        }
    }

    private void startRandomEvent() {
        int idx = RANDOM.nextInt(EVENT_NAMES.length);
        activeEvent = EVENT_NAMES[idx];
        activeMultiplier = EVENT_MULTIPLIERS[idx];
        eventEndTime = System.currentTimeMillis() + EVENT_DURATION_MS;

        EconomyConfig.setGlobalMultiplier(activeMultiplier);

        System.out.println("[Voidcraft] EVENT STARTED: " + activeEvent + " (x" + activeMultiplier + ") for 10 minutes!");
    }

    private void endEvent() {
        System.out.println("[Voidcraft] EVENT ENDED: " + activeEvent);
        activeEvent = null;
        activeMultiplier = 1.0;
        eventEndTime = 0;

        EconomyConfig.setGlobalMultiplier(1.0);
    }

    public static boolean isEventActive() {
        return instance != null && instance.activeEvent != null;
    }

    public static String getActiveEventName() {
        return instance != null ? instance.activeEvent : null;
    }

    public static double getActiveMultiplier() {
        return instance != null ? instance.activeMultiplier : 1.0;
    }

    public static long getTimeRemainingMs() {
        if (instance == null || instance.activeEvent == null) return 0;
        return Math.max(0, instance.eventEndTime - System.currentTimeMillis());
    }

    public static String getTimeRemainingFormatted() {
        long ms = getTimeRemainingMs();
        if (ms <= 0) return "0:00";
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    public static long getCheckIntervalMs() {
        return CHECK_INTERVAL_MS;
    }
}
