package net.dungeonrealms.game.player.trade;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Chase on Nov 16, 2015
 */
public class TradeManager {

    public static ArrayList<Trade> trades = new ArrayList<>();

    /**
     * sender, receiver
     *
     * @param p1
     * @param p2
     */
    public static void openTrade(UUID p1, UUID p2) {
        Player sender = Bukkit.getPlayer(p1);
        Player requested = Bukkit.getPlayer(p2);
        if (sender == null || requested == null) {
            return;
        }
    }

    public static Player getTarget(Player trader) {
        ArrayList<Entity> list = new ArrayList<>();
        trader.getNearbyEntities(2.0D, 2.0D, 2.0D).stream().filter(e -> e instanceof Player && !e.hasMetadata("NPC") && canTrade(e.getUniqueId())).forEach(list::add);
        if (list.size() == 0)
            return null;
        return (Player) list.get(0);
    }

    public static boolean canTrade(UUID uniqueId) {
        Player p = Bukkit.getPlayer(uniqueId);
        if (p == null) {
            return false;
        }

        if (CombatLog.isInCombat(p)) {
            return false;
        }

        //TODO: Check if the player has an inventory open.

        if (getTrade(uniqueId) != null) {
            return false;
        }
        return true;
    }

    public static boolean canTradeItem(ItemStack stack) {
        return true;
    }

    public static void startTrade(Player p1, Player p2) {
        trades.add(new Trade(p1, p2));
    }

    public static Trade getTrade(UUID uuid) {
        for (Trade trade : trades) {
            if (trade.p1.getUniqueId().toString().equalsIgnoreCase(uuid.toString())
                    || trade.p2.getUniqueId().toString().equalsIgnoreCase(uuid.toString()))
                return trade;
        }
        return null;
    }

}
