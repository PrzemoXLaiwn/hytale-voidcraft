package pl.jailbreak.statue;

import java.util.TimerTask;

/**
 * Timer task do periodycznej aktualizacji rankingu statuelek.
 * Uruchamiany co X minut (konfigurowane w statue_config.json).
 */
public class StatueUpdateTask extends TimerTask {
    private final StatueManager manager;

    /**
     * Konstruktor
     *
     * @param manager StatueManager do wywoływania refresh
     */
    public StatueUpdateTask(StatueManager manager) {
        this.manager = manager;
    }

    /**
     * Wykonywane okresowo przez Timer.
     * Wywołuje refresh statuelek i loguje wynik.
     */
    @Override
    public void run() {
        try {
            System.out.println("[StatueUpdateTask] Running scheduled statue update...");
            long startTime = System.currentTimeMillis();

            manager.refreshStatues();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[StatueUpdateTask] Update completed in " + duration + "ms");

        } catch (Exception e) {
            System.out.println("[StatueUpdateTask] Error during scheduled update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
