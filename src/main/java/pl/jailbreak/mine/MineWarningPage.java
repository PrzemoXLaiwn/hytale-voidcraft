package pl.jailbreak.mine;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * UI page that shows mine regeneration warning on screen
 */
public class MineWarningPage extends InteractiveCustomUIPage<MineWarningPage.WarningEventData> {

    private final String mineName;
    private final int countdown;

    public static class WarningEventData {
        public static final BuilderCodec<WarningEventData> CODEC = BuilderCodec.builder(WarningEventData.class, WarningEventData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                (data, value) -> data.action = value, data -> data.action).add()
            .build();

        public String action = "";
    }

    public MineWarningPage(PlayerRef playerRef, String mineName, int countdown) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, WarningEventData.CODEC);
        this.mineName = mineName;
        this.countdown = countdown;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder,
                      UIEventBuilder uiEventBuilder, Store<EntityStore> store) {

        // Load warning UI
        uiCommandBuilder.append("MineWarning.ui");

        // Set texts in English
        uiCommandBuilder.set("#WarningText.TextSpans",
            Message.raw("Mine is regenerating in " + countdown + " seconds!"));
        uiCommandBuilder.set("#MineNameText.TextSpans",
            Message.raw("MINE: " + mineName.toUpperCase()).color("#FFD700"));
        uiCommandBuilder.set("#CountdownText.TextSpans",
            Message.raw("LEAVE NOW! " + countdown + "s").color("#FF0000"));
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, WarningEventData eventData) {
        // No events to handle - this is just a display
    }
}
