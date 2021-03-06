package net.dungeonrealms;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.listeners.ProxyChannelListener;
import net.dungeonrealms.game.network.bungeecord.serverpinger.PingResponse;
import net.dungeonrealms.game.network.bungeecord.serverpinger.ServerAddress;
import net.dungeonrealms.game.network.bungeecord.serverpinger.ServerPinger;
import net.dungeonrealms.game.network.bungeecord.serverpinger.response.BungeePingResponse;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin implements Listener {

    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;
    private static DungeonRealmsProxy instance;
    private final String[] DR_SHARDS = new String[]{"us1", "us2", "us3", "sub1"}; // @note: don't include us0

    //private Map<String, Long> restartingServers = new HashMap<>();

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("DungeonRealmsProxy onEnable() ... STARTING UP");
        getLogger().info("DungeonRealms Starting [MONGODB] Connection...");
        mongoClientURI = new MongoClientURI("mongodb://dungeonuser:mwH47e552qxWPwxL@ds025224-a0.mlab.com:25224,ds025224-a1.mlab.com:25224/dungeonrealms?replicaSet=rs-ds025224");
        mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase("dungeonrealms");

        getLogger().info("[GUILDS] Pull guilds from database...");
        guilds = database.getCollection("guilds");
        GuildDatabase.setGuilds(guilds);

        getLogger().info("DungeonRealms [MONGODB] has connected successfully!");
        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
        this.getProxy().getPluginManager().registerListener(this, this);
    }

//    public String getRank(UUID uuid) {
//
//        database.getCollection("player_data").
//    }

    public List<ServerInfo> getOptimalShards() {
        List<ServerInfo> server = new ArrayList<>();

        for (String shardName : DR_SHARDS)
            // We want to only put them on a US as they may fail the criteria for another shard.
            // They are free to join another shard once connected.
            if (shardName.startsWith("us") && !shardName.equalsIgnoreCase("us0"))
                server.add(getProxy().getServerInfo(shardName));

        //Arrays.
        Collections.sort(server, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());
        //Collections.reverse(server);

        return server;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if ((event.getPlayer().getServer() == null) || event.getTarget().getName().equals("Lobby")) {
            Iterator<ServerInfo> optimalShardFinder = getOptimalShards().iterator();
            event.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Finding an available shard for you...");

            while (optimalShardFinder.hasNext()) {

                ServerInfo target = optimalShardFinder.next();

                try {
                    PingResponse data = new BungeePingResponse(ServerPinger.fetchData(new ServerAddress(target.getAddress().getHostName(), target.getAddress().getPort()), 700));
                    if (!data.isOnline() || data.getMotd().equals("offline")) {

                        if (!optimalShardFinder.hasNext()) {
                            event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                            return;
                        }

                        continue;
                    }
                } catch (Exception e) {

                    if (!optimalShardFinder.hasNext()) {
                        event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                        return;
                    }

                    continue;
                }

                if (target.canAccess(event.getPlayer()) && !(event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().equals(target))) {
                    try {
                        event.setTarget(target);

                    } catch (Exception e) {
                        if (!optimalShardFinder.hasNext())
                            event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                    }

                    break;
                } else {
                    if (!optimalShardFinder.hasNext()) {
                        event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                        return;
                    }
                }
            }
        }
    }

    public void sendMessageToGuild(String guildName, String message, String... filters) {
        loop:
        for (UUID uuid : GuildDatabaseAPI.get().getAllOfGuild(guildName)) {
            ProxiedPlayer player = getProxy().getPlayer(uuid);

            if (player != null) {
                for (String s : filters)
                    if (player.getName().equalsIgnoreCase(s))
                        continue loop;
                player.sendMessage(message);
            }
        }
    }

    public void relayPacket(String channel, byte[] data) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values())
            server.sendData(channel, data);
    }
}
