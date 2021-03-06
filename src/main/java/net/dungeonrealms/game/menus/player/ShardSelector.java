package net.dungeonrealms.game.menus.player;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.menus.AbstractMenu;
import net.dungeonrealms.game.miscellaneous.Cooldown;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.game.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.ui.GUIButtonClickEvent;
import net.dungeonrealms.game.ui.VolatileGUI;
import net.dungeonrealms.game.ui.item.GUIButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/18/2016
 */
public class ShardSelector extends AbstractMenu implements VolatileGUI {

    public static Map<String, Map<String, GUIButton>> CACHED_PING_SHARD_BUTTONS = new HashMap<>();
    private final String playerHostName;

    public ShardSelector(Player player) {
        super("DungeonRealms Shards", AbstractMenu.round(getFilteredServers().size()), player.getUniqueId());
        setDestroyOnExit(true);

        this.playerHostName = player.getAddress().getHostName();

        // DISPLAY AVAILABLE SHARDS //
        for (Entry<String, BungeeServerInfo> e : getFilteredServers().entrySet()) {
            String bungeeName = e.getKey();
            String shardID = DungeonRealms.getInstance().DR_SHARDS.get(bungeeName).getShardID();
            BungeeServerInfo info = e.getValue();

            // Do not show YT / CS shards unless they've got the appropriate permission to see them.
            if ((shardID.contains("YT") && !Rank.isYouTuber(player)) || (shardID.contains("CS") && !Rank.isSupport(player)) || (shardID.equalsIgnoreCase("US-0") && !Rank.isGM(player)))
                continue;

            GUIButton button = new GUIButton(shardID.equalsIgnoreCase("US-0") ? Material.DIAMOND : Material.END_CRYSTAL) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player player = event.getWhoClicked();
                    player.closeInventory();

                    if (CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.RED + "You cannot transfer shards while in combat.");
                        return;
                    }

                    if (Cooldown.hasCooldown(player.getUniqueId()))
                        return;

                    Cooldown.addCooldown(player.getUniqueId(), 1000L);

                    if (shardID.contains("SUB") && !Rank.isSubscriber(player)) {
                        player.sendMessage(new String[]{
                                ChatColor.RED + "This is a " + ChatColor.BOLD + ChatColor.UNDERLINE + "SUBSCRIBER ONLY" + ChatColor.RED + " shard!",
                                ChatColor.RED + "You can subscribe at: " + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/shop"
                        });
                        return;
                    } else if ((shardID.contains("YT") && !Rank.isYouTuber(player)) || (shardID.contains("CS") && !Rank.isSupport(player))) {
                        player.sendMessage(ChatColor.RED + "You are " + ChatColor.BOLD + ChatColor.UNDERLINE + "NOT" + ChatColor.RED + " authorized to connect to this shard.");
                        return;
                    }

                    BountifulAPI.sendTitle(player, 1, 60, 1, ChatColor.YELLOW + "Loading Shard - " + ChatColor.BOLD + shardID + ChatColor.YELLOW + " ...", ChatColor.GRAY.toString() + "Do not disconnect");

                    player.sendMessage(ChatColor.GRAY + "Retrieving relevant server information...");
                    player.sendMessage(" ");
                    player.sendMessage("                     " + ChatColor.YELLOW + "Loading Shard - " + ChatColor.BOLD + shardID + ChatColor.YELLOW + " ...");
                    player.sendMessage(ChatColor.GRAY + "Your current game session has been paused while you are transferred.");

                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LAST_SHARD_TRANSFER, System.currentTimeMillis(), true);
                    API.handleLogout(player.getUniqueId());
                    DungeonRealms.getInstance().getLoggingOut().add(player.getName());
                    DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 5); //Prevents dungeon entry for 5 seconds.

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                            () -> {
                                NetworkAPI.getInstance().sendToServer(player.getName(), bungeeName);
                                DungeonRealms.getInstance().getLoggingOut().remove(player.getName());
                            }, 10);
                }
            };

            List<String> lore = new ArrayList<>();

            if (!getServerType(shardID).equals(""))
                lore.add(ChatColor.RED.toString() + ChatColor.ITALIC + getServerType(shardID));


            final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

            lore.add(ChatColor.GREEN + "This shard is online!");
            lore.add(ChatColor.WHITE + "Click here to load your");
            lore.add(ChatColor.WHITE + "character onto this shard.");
            lore.add(" ");
            lore.add(ChatColor.GRAY + "Online: " + info.getOnlinePlayers() + "/" + info.getMaxPlayers());

            button.setDisplayName((!shardID.equalsIgnoreCase("US-0") ? ChatColor.YELLOW : ChatColor.AQUA) + "" + ChatColor.BOLD + shardID);
            button.setLore(lore);

//            button.setSlot(slot);
//            button.setGui(this);
//
//            if (!CACHED_PING_SHARD_BUTTONS.containsKey(playerHostName)) {
//                Map<String, GUIButton> map = new HashMap<>();
//                map.put(bungeeName, button);
//
//                CACHED_PING_SHARD_BUTTONS.put(playerHostName, map);
//            } else {
//                Map<String, GUIButton> map = CACHED_PING_SHARD_BUTTONS.get(playerHostName);
//                map.put(bungeeName, button);
//
//                CACHED_PING_SHARD_BUTTONS.put(playerHostName, map);
//            }
//
//            NetworkAPI.getInstance().sendNetworkMessage("DungeonRealms", "Ping", playerHostName);

            set(slot, button);
        }
    }

    private int getNormalServers() {
        int count = 0;

        for (String bungeeName : getFilteredServers().keySet()) {
            String shardID = DungeonRealms.getInstance().DR_SHARDS.get(bungeeName).getShardID();
            if (getServerType(shardID).equals(""))
                count++;
        }

        return count;
    }

    private static Map<String, BungeeServerInfo> getFilteredServers() {
        Map<String, BungeeServerInfo> filteredServers = new HashMap<>();

        for (Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet()) {
            String bungeeName = e.getKey();
            if (!DungeonRealms.getInstance().DR_SHARDS.containsKey(bungeeName)) continue;

            String shardID = DungeonRealms.getInstance().DR_SHARDS.get(bungeeName).getShardID();
            BungeeServerInfo info = e.getValue();


            if (!info.isOnline() || shardID.equals(DungeonRealms.getInstance().shardid) || info.getOnlinePlayers() >= info.getMaxPlayers() || info.getMotd1().equals("offline"))
                continue;


            filteredServers.put(bungeeName, info);
        }

        return filteredServers;
    }

    public String getServerType(String shardID) {
        if (shardID.contains("SUB")) return "Subscribers Only";
        if (shardID.contains("YT")) return "YouTubers Only";
        if (shardID.contains("BR")) return "Brazilian Shard";
        if (shardID.contains("RP")) return "Role-playing Shard";
        if (shardID.contains("CS")) return "Support Agents Only";
        return "";
    }

    @Override
    public void open(Player player) {
        if (getSize() == 0) {
            player.sendMessage(ChatColor.RED + "Unable to find an available shard for you.");
            return;
        }

        long lastShardTransfer = (long) DatabaseAPI.getInstance().getData(EnumData.LAST_SHARD_TRANSFER, player.getUniqueId());

        if (lastShardTransfer != 0 && !Rank.isGM(player)) {
            if (API.isInSafeRegion(player.getLocation()) && (System.currentTimeMillis() - lastShardTransfer) < 30000) {
                player.sendMessage(ChatColor.RED + "You must wait 30 seconds before you can transfer between shards.");
                return;
            } else if (!API.isInSafeRegion(player.getLocation()) && (System.currentTimeMillis() - lastShardTransfer) < 300000) {
                player.sendMessage(ChatColor.RED + "You must wait 5 minutes before you can transfer between shards.");
                return;
            }
        }

        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "You cannot transfer shards while in combat.");
            return;
        }


        player.openInventory(inventory);
    }

    @Override
    public void onDestroy(Event event) {
        CACHED_PING_SHARD_BUTTONS.remove(playerHostName);
    }

}
