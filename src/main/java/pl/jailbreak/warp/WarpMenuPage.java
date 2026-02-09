package pl.jailbreak.warp;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import pl.jailbreak.JailbreakPlugin;
import pl.jailbreak.player.PlayerData;
import pl.jailbreak.player.PlayerManager;
import pl.jailbreak.sectors.Sector;
import pl.jailbreak.sectors.SectorManager;

public class WarpMenuPage extends InteractiveCustomUIPage<WarpMenuPage.WarpEventData> {

    private Player player;

    public static class WarpEventData {
        public static final BuilderCodec<WarpEventData> CODEC = BuilderCodec.builder(WarpEventData.class, WarpEventData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                (data, value) -> data.action = value, data -> data.action).add()
            .build();

        public String action = "";
    }

    public WarpMenuPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, WarpEventData.CODEC);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder,
                      UIEventBuilder uiEventBuilder, Store<EntityStore> store) {

        System.out.println("[WarpMenuPage] Building UI...");

        uiCommandBuilder.append("WarpMenu.ui");

        // Main menu buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SpawnButton", EventData.of("Action", "spawn"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MinesButton", EventData.of("Action", "mines"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "close"), false);

        // Sector buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineA", EventData.of("Action", "A"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineB", EventData.of("Action", "B"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineC", EventData.of("Action", "C"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineD", EventData.of("Action", "D"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineE", EventData.of("Action", "E"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineF", EventData.of("Action", "F"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineG", EventData.of("Action", "G"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineH", EventData.of("Action", "H"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineI", EventData.of("Action", "I"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MineJ", EventData.of("Action", "J"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of("Action", "back"), false);

        System.out.println("[WarpMenuPage] UI built");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, WarpEventData data) {
        System.out.println("[WarpMenuPage] Event: action=" + data.action);

        if (data.action == null || data.action.isEmpty()) {
            sendUpdate();
            return;
        }

        // Close
        if ("close".equals(data.action)) {
            close();
            return;
        }

        // Spawn teleport
        if ("spawn".equals(data.action)) {
            close();
            if (player != null) {
                WarpManager warpManager = JailbreakPlugin.getWarpManager();
                warpManager.teleportToWarp(player, "spawn");
            }
            return;
        }

        // Show mines panel
        if ("mines".equals(data.action)) {
            UICommandBuilder update = new UICommandBuilder();
            update.set("#MainMenu.Visible", false);
            update.set("#MinesPanel.Visible", true);
            update.set("#PageTitle.TextSpans", Message.raw("MINES").color("#FFD700"));
            update.set("#PageSubtitle.TextSpans", Message.raw("Select a sector to teleport").color("#888888"));
            sendUpdate(update, false);
            return;
        }

        // Back to main menu
        if ("back".equals(data.action)) {
            UICommandBuilder update = new UICommandBuilder();
            update.set("#MinesPanel.Visible", false);
            update.set("#MainMenu.Visible", true);
            update.set("#PageTitle.TextSpans", Message.raw("VOIDCRAFT").color("#9400D3"));
            update.set("#PageSubtitle.TextSpans", Message.raw("Teleport Menu").color("#666688"));
            sendUpdate(update, false);
            return;
        }

        // Sector buttons A-J
        if (data.action.length() == 1 && data.action.charAt(0) >= 'A' && data.action.charAt(0) <= 'J') {
            String sector = data.action;
            System.out.println("[WarpMenuPage] Sector " + sector + " clicked");

            // Check if sector has coordinates set
            if (!SectorManager.hasCoords(sector)) {
                if (player != null) {
                    player.sendMessage(Message.raw("[Voidcraft] Sector " + sector + " is not available yet!").color("#FF6666"));
                }
                sendUpdate();
                return;
            }

            // Sector A is always free
            if (!"A".equals(sector) && !hasAccess(sector)) {
                if (player != null) {
                    player.sendMessage(Message.raw("[Voidcraft] You don't have access to Sector " + sector + "!").color("#FF6666"));
                    player.sendMessage(Message.raw("[Voidcraft] Use /sectorbuy to unlock it.").color("#AAAAAA"));
                }
                sendUpdate();
                return;
            }

            close();
            teleportToSector(sector);
            return;
        }

        sendUpdate();
    }

    private boolean hasAccess(String sector) {
        if (player == null) return false;

        PlayerManager pm = JailbreakPlugin.getPlayerManager();
        if (pm == null) return false;

        PlayerData pd = pm.getPlayer(player.getUuid().toString());
        if (pd == null) return false;

        String unlocked = pd.getCurrentSector();
        if (unlocked == null || unlocked.isEmpty()) return false;

        return sector.charAt(0) <= unlocked.toUpperCase().charAt(0);
    }

    private void teleportToSector(String sector) {
        if (player == null) return;

        Sector sectorData = SectorManager.get(sector);
        if (sectorData == null) {
            player.sendMessage(Message.raw("[Voidcraft] Sector " + sector + " not found!").color("#FF6666"));
            return;
        }

        try {
            World world = player.getWorld();
            if (world == null) return;

            world.execute(() -> {
                try {
                    Ref<EntityStore> pRef = player.getReference();
                    if (pRef == null) return;

                    Store<EntityStore> pStore = pRef.getStore();
                    Teleport teleport = Teleport.createForPlayer(
                        world,
                        new Vector3d(sectorData.x, sectorData.y, sectorData.z),
                        new Vector3f(0, 0, 0)
                    );
                    pStore.addComponent(pRef, Teleport.getComponentType(), teleport);

                    player.sendMessage(Message.raw("[Voidcraft] Teleported to " + sectorData.name + "!").color("#FFD700"));
                } catch (Exception e) {
                    player.sendMessage(Message.raw("[Voidcraft] Teleport failed!").color("#FF0000"));
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            player.sendMessage(Message.raw("[Voidcraft] Teleport error!").color("#FF0000"));
            e.printStackTrace();
        }
    }
}
