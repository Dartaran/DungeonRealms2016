package net.dungeonrealms.listeners;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.teleportation.Teleportation;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {


    /**
     * Handles player clicking with a teleportation item
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseTeleportItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (event.getPlayer().getItemInHand().getType() == Material.QUARTZ) {
            if (event.getPlayer().hasMetadata("Hearthstone") && !(CombatLog.isInCombat(event.getPlayer().getUniqueId()))) {
                Teleportation.teleportPlayer(event.getPlayer().getUniqueId());
            } else {
                if (event.getPlayer().hasMetadata("Hearthstone")) {
                    event.getPlayer().removeMetadata("Hearthstone", DungeonRealms.getInstance());
                }
                event.getPlayer().sendMessage("Your Hearthstone is not ready yet, or you are currently in combat! (" + Teleportation.PLAYER_TELEPORTS.get(event.getPlayer().getUniqueId()) + ")");
            }
        }
    }
}
