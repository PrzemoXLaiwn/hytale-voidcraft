package pl.jailbreak.crates;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;

import java.util.*;

/**
 * CS:GO style crate opening GUI.
 * Shows 5 slots with items rolling through them, slowing down,
 * and landing on the final reward in the center slot.
 */
public class CrateOpenPage extends InteractiveCustomUIPage<CrateOpenPage.CrateEventData> {

    private final PlayerManager playerManager;
    private final String playerUuid;
    private final String crateType;
    private Player player;
    private boolean isRolling = false;

    public static class CrateEventData {
        public static final BuilderCodec<CrateEventData> CODEC = BuilderCodec.builder(CrateEventData.class, CrateEventData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                (data, value) -> data.action = value, data -> data.action).add()
            .build();

        public String action = "";
    }

    public CrateOpenPage(PlayerRef playerRef, PlayerManager playerManager, String playerUuid, String crateType) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, CrateEventData.CODEC);
        this.playerManager = playerManager;
        this.playerUuid = playerUuid;
        this.crateType = crateType;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder,
                      UIEventBuilder uiEventBuilder, Store<EntityStore> store) {

        uiCommandBuilder.append("CrateOpen.ui");

        String color = CrateManager.getColor(crateType);
        String displayName = CrateManager.getDisplayName(crateType);

        // Set title
        uiCommandBuilder.set("#CrateTitle.TextSpans", Message.raw(displayName).color(color));

        // Show keys remaining
        PlayerData data = playerManager.getPlayer(playerUuid);
        int keys = data != null ? data.getKeys(crateType) : 0;
        uiCommandBuilder.set("#KeysLeft.TextSpans", Message.raw("Keys: " + keys + " | Click OPEN to roll!").color("#AAAAAA"));

        // Set initial slot display with random rewards preview
        List<CrateManager.CrateReward> rewards = CrateManager.getRewards(crateType);
        Random rng = new Random();
        for (int i = 1; i <= 5; i++) {
            CrateManager.CrateReward preview = rewards.get(rng.nextInt(rewards.size()));
            String slotColor = i == 3 ? "#FFFFFF" : "#555555";
            uiCommandBuilder.set("#Slot" + i + ".TextSpans", Message.raw(preview.name).color(slotColor));
        }

        uiCommandBuilder.set("#ResultLabel.TextSpans", Message.raw("Press OPEN to start!").color("#FFFF00"));

        // Register buttons
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#OpenButton",
            EventData.of("Action", "open"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            EventData.of("Action", "close"),
            false
        );

        System.out.println("[CrateOpenPage] Built for " + displayName);
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, CrateEventData data) {
        if (data.action == null || data.action.isEmpty()) {
            sendUpdate();
            return;
        }

        if ("close".equals(data.action)) {
            close();
            return;
        }

        if ("open".equals(data.action)) {
            if (isRolling) return;
            performCrateOpen();
            return;
        }

        sendUpdate();
    }

    private void performCrateOpen() {
        PlayerData data = playerManager.getPlayer(playerUuid);
        if (data == null) {
            updateResult("Error: No player data!", "#FF0000");
            return;
        }

        int keys = data.getKeys(crateType);
        if (keys <= 0) {
            updateResult("No keys! Mine ores to find keys!", "#FF0000");
            return;
        }

        // Use key
        if (!data.useKey(crateType)) {
            updateResult("Failed to use key!", "#FF0000");
            return;
        }

        isRolling = true;

        // Roll the final reward
        CrateManager.CrateReward finalReward = CrateManager.rollReward(crateType);
        if (finalReward == null) {
            updateResult("Error rolling reward!", "#FF0000");
            isRolling = false;
            return;
        }

        // Generate rolling sequence: 15 random items, last one is the real reward
        List<CrateManager.CrateReward> rewards = CrateManager.getRewards(crateType);
        Random rng = new Random();
        List<CrateManager.CrateReward> sequence = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            sequence.add(rewards.get(rng.nextInt(rewards.size())));
        }
        // The 13th item (index 12) will land on center slot (Slot3) at the end
        sequence.set(12, finalReward);

        // Animate using timer - each step shifts all slots up
        String color = CrateManager.getColor(crateType);
        Timer animTimer = new Timer("CrateAnim-" + playerUuid, true);
        final int[] step = {0};
        final long[] delays = generateDelays(15);

        scheduleNextStep(animTimer, sequence, step, delays, finalReward, data, color);
    }

    private void scheduleNextStep(Timer timer, List<CrateManager.CrateReward> sequence,
                                   int[] step, long[] delays,
                                   CrateManager.CrateReward finalReward, PlayerData data, String color) {
        if (step[0] >= sequence.size() - 4) {
            // Animation done - show final result
            timer.cancel();
            showFinalResult(sequence, finalReward, data, color);
            return;
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    int i = step[0];
                    // Show 5 consecutive items from sequence
                    UICommandBuilder update = new UICommandBuilder();

                    for (int s = 0; s < 5; s++) {
                        int seqIdx = i + s;
                        if (seqIdx < sequence.size()) {
                            CrateManager.CrateReward item = sequence.get(seqIdx);
                            String slotColor;
                            if (s == 2) {
                                // Center slot - highlighted
                                slotColor = item.color;
                            } else {
                                slotColor = "#555555";
                            }
                            update.set("#Slot" + (s + 1) + ".TextSpans", Message.raw(item.name).color(slotColor));
                        }
                    }

                    update.set("#ResultLabel.TextSpans", Message.raw("Rolling...").color(color));
                    sendUpdate(update, false);

                    step[0]++;
                    scheduleNextStep(timer, sequence, step, delays, finalReward, data, color);
                } catch (Exception e) {
                    System.out.println("[CrateOpenPage] Animation error: " + e.getMessage());
                    timer.cancel();
                    isRolling = false;
                }
            }
        }, delays[step[0]]);
    }

    private void showFinalResult(List<CrateManager.CrateReward> sequence,
                                  CrateManager.CrateReward finalReward, PlayerData data, String color) {
        try {
            // Final display - center slot shows the reward
            UICommandBuilder update = new UICommandBuilder();

            // Show final 5 slots with reward in center
            int centerIdx = 12; // where finalReward is
            for (int s = 0; s < 5; s++) {
                int seqIdx = centerIdx - 2 + s;
                if (seqIdx >= 0 && seqIdx < sequence.size()) {
                    CrateManager.CrateReward item = sequence.get(seqIdx);
                    String slotColor;
                    if (s == 2) {
                        slotColor = finalReward.color;
                    } else {
                        slotColor = "#444444";
                    }
                    update.set("#Slot" + (s + 1) + ".TextSpans", Message.raw(item.name).color(slotColor));
                }
            }

            // Give reward
            String resultText;
            if (finalReward.moneyReward > 0) {
                data.addBalance(finalReward.moneyReward);
                resultText = "WON: " + finalReward.name + "!";
            } else {
                resultText = "WON: " + finalReward.name + "!";
            }

            if (finalReward.keyReward > 0 && finalReward.keyType != null) {
                data.addKey(finalReward.keyType, finalReward.keyReward);
            }

            // Save
            PlayerManager pm = JailbreakPlugin.getPlayerManager();
            if (pm != null) pm.savePlayer(data.getUuid());

            // Update keys remaining
            int keysLeft = data.getKeys(crateType);
            update.set("#KeysLeft.TextSpans", Message.raw("Keys: " + keysLeft + " | Balance: $" + String.format("%,d", data.getBalance())).color("#00FF00"));
            update.set("#ResultLabel.TextSpans", Message.raw(resultText).color(finalReward.color));

            // Change open button text
            if (keysLeft > 0) {
                update.set("#OpenLabel.TextSpans", Message.raw("OPEN AGAIN (" + keysLeft + " keys)").color("#FFFFFF"));
            } else {
                update.set("#OpenLabel.TextSpans", Message.raw("NO KEYS LEFT").color("#FF6666"));
            }

            sendUpdate(update, false);

            // Also send chat message
            if (player != null) {
                player.sendMessage(Message.raw("[Voidcraft] Crate reward: " + finalReward.name + "!").color(finalReward.color));
                if (finalReward.moneyReward > 0) {
                    player.sendMessage(Message.raw("[Voidcraft] +" + String.format("$%,d", finalReward.moneyReward) + " | Balance: $" + String.format("%,d", data.getBalance())).color("#00FF00"));
                }
            }

            System.out.println("[Voidcraft] " + data.getName() + " opened " + crateType + " crate -> " + finalReward.name);

        } catch (Exception e) {
            System.out.println("[CrateOpenPage] Final result error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isRolling = false;
        }
    }

    /**
     * Generate delays for each animation step.
     * Starts fast (80ms), gradually slows to 400ms - like CS:GO roulette.
     */
    private long[] generateDelays(int steps) {
        long[] delays = new long[steps];
        for (int i = 0; i < steps; i++) {
            // Exponential slowdown: 80ms -> 400ms
            double progress = (double) i / (steps - 1);
            delays[i] = (long) (80 + 320 * progress * progress);
        }
        return delays;
    }

    private void updateResult(String text, String color) {
        UICommandBuilder update = new UICommandBuilder();
        update.set("#ResultLabel.TextSpans", Message.raw(text).color(color));
        sendUpdate(update, false);
    }
}
