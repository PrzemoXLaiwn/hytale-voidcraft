package pl.jailbreak.hud;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import pl.jailbreak.enchants.PickaxeEnchantUtil;
import pl.jailbreak.enchants.PlayerEnchants;
import pl.jailbreak.player.PlayerData;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

/**
 * Persistent HUD overlay showing player stats (balance, sector, prestige, pickaxe, blocks mined).
 */
public class ScoreboardHud extends CustomUIHud {

    public ScoreboardHud(PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Scoreboard.ui");
    }

    /**
     * Refresh the HUD with current player data.
     */
    public void refresh(Player player, PlayerData data) {
        if (data == null) return;

        try {
            UICommandBuilder update = new UICommandBuilder();

            update.set("#HudBalance.TextSpans", Message.raw("Balance: $" + formatNumber(data.getBalance())));
            update.set("#HudSector.TextSpans", Message.raw("Sector: " + data.getCurrentSector()));
            update.set("#HudPrestige.TextSpans", Message.raw("Prestige: " + data.getPrestige()));
            update.set("#HudBlocksMined.TextSpans", Message.raw("Blocks: " + formatNumber(data.getBlocksMined())));

            // Get pickaxe info from held item
            String pickaxeName = "None";
            String enchantLore = "None";
            if (player != null) {
                ItemStack pickaxe = PickaxeEnchantUtil.getHeldPickaxe(player);
                if (pickaxe != null) {
                    pickaxeName = PickaxeEnchantUtil.getPickaxeTierName(pickaxe);
                    PlayerEnchants enchants = PickaxeEnchantUtil.readEnchants(pickaxe);
                    enchantLore = PickaxeEnchantUtil.getEnchantLore(enchants);
                }
            }

            update.set("#HudPickaxe.TextSpans", Message.raw("Pickaxe: " + pickaxeName));
            update.set("#HudEnchants.TextSpans", Message.raw("Enchants: " + enchantLore));

            update(false, update);
        } catch (Exception e) {
            System.out.println("[Voidcraft] HUD refresh error: " + e.getMessage());
        }
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
}
