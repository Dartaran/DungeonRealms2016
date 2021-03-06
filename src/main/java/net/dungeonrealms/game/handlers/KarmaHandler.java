package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kieran on 10/7/2015.
 */
public class KarmaHandler implements GenericMechanic {

    private static KarmaHandler instance = null;

    public static KarmaHandler getInstance() {
        if (instance == null) {
            instance = new KarmaHandler();
        }
        return instance;
    }

    public static HashMap<Player, EnumPlayerAlignments> PLAYER_ALIGNMENTS = new HashMap<>();
    public static ConcurrentHashMap<Player, Integer> PLAYER_ALIGNMENT_TIMES = new ConcurrentHashMap<>();
    public static HashMap<Player, EnumPlayerAlignments> PLAYER_LOCATIONS = new HashMap<>();
    public static List<Location> CHAOTIC_RESPAWNS = new ArrayList<>();

    public enum EnumPlayerAlignments {
        LAWFUL(0, "lawful", ChatColor.WHITE, "-30% Durability Arm/Wep on Death"),
        NEUTRAL(1, "neutral", ChatColor.YELLOW, "25%/50% Arm/Wep LOST on Death"),
        CHAOTIC(2, "chaotic", ChatColor.RED, "Inventory LOST on Death"),
        NONE(3, "none", ChatColor.GRAY, "-30% Durability Arm/Wep on Death");

        private int id;
        private String name;
        private ChatColor alignmentColor;
        public String description;

        EnumPlayerAlignments(int id, String name, ChatColor alignmentColor, String description) {
            this.id = id;
            this.name = name;
            this.alignmentColor = alignmentColor;
            this.description = description;
        }

        public static EnumPlayerAlignments getByName(String rawName) {
            for (EnumPlayerAlignments playerAlignments : values()) {
                if (playerAlignments.name.equalsIgnoreCase(rawName)) {
                    return playerAlignments;
                }
            }
            return null;
        }

        public ChatColor getAlignmentColor() {
            return alignmentColor;
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -382, 68, 867));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -350, 67, 883));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -330, 65, 898));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -419, 61, 830));

        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::updateAllPlayerAlignments, 100L, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Updates all player alignments
     * from Chaotic->Neutral or Neutral->Lawful
     * if they are not in combat and in the
     * main world
     *
     * @since 1.0
     */
    private void updateAllPlayerAlignments() {
        List<Player> toRemove = new ArrayList<>();
        for (Map.Entry<Player, EnumPlayerAlignments> alignment : PLAYER_ALIGNMENTS.entrySet()) {
            Player player = alignment.getKey();
            EnumPlayerAlignments currentAlignment = alignment.getValue();
            if (!(PLAYER_ALIGNMENT_TIMES.containsKey(player))) {
                continue;
            }
            if (!(player.getWorld().getName().equalsIgnoreCase(DungeonRealms.getInstance().getServer().getWorlds().get(0).getName()))) {
                continue;
            }

            int timeLeft = PLAYER_ALIGNMENT_TIMES.get(player);
            timeLeft--;

            if (timeLeft <= 0) {
                try {
                    if (currentAlignment.equals(EnumPlayerAlignments.CHAOTIC)) {
                        setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name, false);
                        PLAYER_ALIGNMENT_TIMES.put(player, 120);
                    } else if (currentAlignment.equals(EnumPlayerAlignments.NEUTRAL)) {
                        setPlayerAlignment(player, EnumPlayerAlignments.LAWFUL.name, false);
                        toRemove.add(player);
                    }
                } catch (NullPointerException npe) {
                }
            } else if (timeLeft > 0) {
                PLAYER_ALIGNMENT_TIMES.put(player, timeLeft);
            }
        }
        toRemove.forEach(PLAYER_ALIGNMENT_TIMES::remove);
    }

    /**
     * Handles players logging in,
     * sets their alignment based on
     * their mongo document.
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        if (PLAYER_ALIGNMENT_TIMES.containsKey(player)) {
            PLAYER_ALIGNMENT_TIMES.remove(player);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> setPlayerAlignment(player, getAlignmentOnLogin(player.getUniqueId()), true), 20L);
    }

    /**
     * Handles players logging out,
     * updates mongo document with
     * their alignment.
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        int alignmentTime = 0;
        if (PLAYER_ALIGNMENT_TIMES.containsKey(player)) {
            alignmentTime = PLAYER_ALIGNMENT_TIMES.get(player);
            PLAYER_ALIGNMENT_TIMES.remove(player);
        }
        if (alignmentTime > 0) {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, alignmentTime, false);
        } else {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, 0, false);
        }
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, getPlayerRawAlignment(player), false);
    }

    /**
     * Returns the players current alignment
     * as a string.
     *
     * @param player
     * @return String
     * @since 1.0
     */
    public String getPlayerRawAlignment(Player player) {
        if (PLAYER_ALIGNMENTS.containsKey(player)) {
            return PLAYER_ALIGNMENTS.get(player).name;
        }
        return "lawful"; //Should never happen, but safety checks.
    }

    /**
     * Returns the seconds passed since last login
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public int getSecondsPassed(Player player) {
        long currentTime = System.currentTimeMillis() / 1000L;
        long endTime = Long.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, player.getUniqueId())));
        return (int) (currentTime - endTime);
    }

    /**
     * Sets the alignment of a specific player
     * adds them to hashmap with cooldown
     * if applicable and sends them a message
     * detailing what that alignment causes.
     *
     * @param player
     * @param alignmentRawName
     * @since 1.0
     */
    public void setPlayerAlignment(Player player, String alignmentRawName, boolean login) {
        if (API.getGamePlayer(player) == null) {
            return;
        }
        EnumPlayerAlignments alignmentTo = EnumPlayerAlignments.getByName(alignmentRawName);
        EnumPlayerAlignments alignmentPlayer = API.getGamePlayer(player).getPlayerAlignment();
        int alignmentTime = 0;
        if (login) {
            alignmentTime = (int) DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT_TIME, player.getUniqueId());
        }
        if (alignmentTo == null || alignmentTo.equals(EnumPlayerAlignments.NONE)) {
            alignmentTo = EnumPlayerAlignments.LAWFUL;
        }
        switch (alignmentTo) {
            case LAWFUL:
                // Don't show alignment on player login.
                if (!login) {
                    player.sendMessage(new String[]{
                            "",
                            ChatColor.GREEN + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "LAWFUL" + ChatColor.RESET + ChatColor.GREEN + " ALIGNMENT *",
                            ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die.",
                            ""
                    });
                }
                ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, ChatColor.WHITE, API.getGamePlayer(player).getLevel());
                /*if (Instance.getInstance().getPlayerRealm(player) != null && Instance.getInstance().getPlayerRealm(player).isRealmPortalOpen()) {
                    Instance.getInstance().getPlayerRealm(player).getRealmHologram().appendTextLine(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " [" + ChatColor.WHITE + playerAlignment.toUpperCase() + ChatColor.GOLD + "]");
                }*/
                PLAYER_ALIGNMENTS.put(player, alignmentTo);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, 0, false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, EnumPlayerAlignments.LAWFUL.name, true);
                break;
            case NEUTRAL:
                if (!(alignmentPlayer.equals(EnumPlayerAlignments.NEUTRAL)) && !login) {
                    player.sendMessage(new String[]{
                            "",
                            ChatColor.YELLOW + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "NEUTRAL" + ChatColor.RESET + ChatColor.YELLOW + " ALIGNMENT *",
                            ChatColor.GRAY + "While neutral, you have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equipped armor on death.",
                            ""
                    });
                }
                ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, ChatColor.YELLOW, API.getGamePlayer(player).getLevel());
                /*if (Instance.getInstance().getPlayerRealm(player) != null && Instance.getInstance().getPlayerRealm(player).isRealmPortalOpen()) {
                    Instance.getInstance().getPlayerRealm(player).getRealmHologram().appendTextLine(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " [" + ChatColor.YELLOW + playerAlignment.toUpperCase() + ChatColor.GOLD + "]");
                }*/
                if (alignmentTime == 0) {
                    alignmentTime = 120;
                }
                PLAYER_ALIGNMENT_TIMES.put(player, alignmentTime);
                PLAYER_ALIGNMENTS.put(player, alignmentTo);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, alignmentTime, false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, EnumPlayerAlignments.NEUTRAL.name, true);
                break;
            case CHAOTIC:
                if (!(alignmentPlayer.equals(EnumPlayerAlignments.CHAOTIC)) && !login) {
                    player.sendMessage(new String[]{
                            "",
                            ChatColor.RED + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "CHAOTIC" + ChatColor.RESET + ChatColor.RED + " ALIGNMENT *",
                            ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory.",
                            ""
                    });
                }
                ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, ChatColor.RED, API.getGamePlayer(player).getLevel());
                /*if (Instance.getInstance().getPlayerRealm(player) != null && Instance.getInstance().getPlayerRealm(player).isRealmPortalOpen()) {
                    Instance.getInstance().getPlayerRealm(player).getRealmHologram().appendTextLine(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " [" + ChatColor.RED + playerAlignment.toUpperCase() + ChatColor.GOLD + "]");
                }*/
                if (alignmentTime == 0) {
                    alignmentTime = 1200;
                }
                PLAYER_ALIGNMENT_TIMES.put(player, alignmentTime);
                PLAYER_ALIGNMENTS.put(player, alignmentTo);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, alignmentTime, false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, EnumPlayerAlignments.CHAOTIC.name, true);
                break;
            default:
                Utils.log.info("[KARMA] Could not set player " + player.getName() + "'s alignment! UH OH");
                break;
        }
    }

    /**
     * Returns the players current alignment
     * from Mongo Doc as a string.
     *
     * @param uuid
     * @return String
     * @since 1.0
     */
    public static String getAlignmentOnLogin(UUID uuid) {
        return String.valueOf(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid));
    }

    /**
     * Handles when the player "dies" in combat
     * Checks to see if their killer should change alignment
     * and changes it if they should.
     *
     * @param player
     * @param killer
     * @since 1.0
     */
    public void handlePlayerPsuedoDeath(Player player, Entity killer) {
        LivingEntity leKiller = null;
        switch (killer.getType()) {
            case ARROW:
            case TIPPED_ARROW:
            case SNOWBALL:
            case SMALL_FIREBALL:
            case ENDER_PEARL:
            case FIREBALL:
            case WITHER_SKULL:
                Projectile projectile = (Projectile) killer;
                if (!(projectile.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) projectile.getShooter();
                break;
            case PLAYER:
                leKiller = (LivingEntity) killer;
                break;
            default:
                break;
        }
        Player killerPlayer;
        if (API.isPlayer(leKiller)) {
            killerPlayer = (Player) leKiller;
            GamePlayer deathGP = API.getGamePlayer(player);
            if (deathGP != null) {
                deathGP.getPlayerStatistics().setDeaths(deathGP.getPlayerStatistics().getDeaths() + 1);
            }
            String alignmentPlayer = getPlayerRawAlignment(player);
            GamePlayer killerGP = API.getGamePlayer(killerPlayer);
            if (killerGP != null) {
                if (killerGP.hasNewbieProtection()) {
                    ProtectionHandler.getInstance().removePlayerProtection(killerPlayer);
                }
                killerGP.getPlayerStatistics().setPlayerKills(killerGP.getPlayerStatistics().getPlayerKills() + 1);
                if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name)) {
                    killerGP.getPlayerStatistics().setLawfulKills(killerGP.getPlayerStatistics().getLawfulKills() + 1);
                } else {
                    killerGP.getPlayerStatistics().setUnlawfulKills(killerGP.getPlayerStatistics().getUnlawfulKills() + 1);
                }
            }
            String alignmentKiller = getPlayerRawAlignment(killerPlayer);
            if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name)) {
                setPlayerAlignment(killerPlayer, EnumPlayerAlignments.CHAOTIC.name, false);
            } else if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.NEUTRAL.name) || alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.CHAOTIC.name)) {
                setPlayerAlignment(killerPlayer, alignmentKiller, false);
            }
        }
    }

    /**
     * Handles when the player attacks another player
     * Checks to see if they should change alignment
     * and changes it if they should.
     *
     * @param player
     * @since 1.0
     */
    public void handleAlignmentChanges(Player player) {
        String alignmentPlayer = getPlayerRawAlignment(player);
        if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name)) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name, false);
        } else if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.NEUTRAL.name)) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name, false);
        } else {
        }
    }


    public void tellPlayerRegionInfo(Player player) {
        if (!PLAYER_LOCATIONS.containsKey(player)) {
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.NONE);
            return;
        }
        if (API.isInSafeRegion(player.getLocation()) && !PLAYER_LOCATIONS.get(player).equals(EnumPlayerAlignments.LAWFUL)) {
            player.sendMessage(ChatColor.GREEN + "                " + ChatColor.BOLD + "*** SAFE ZONE (DMG-OFF) ***");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.LAWFUL);
            return;
        }
        if (!API.isInSafeRegion(player.getLocation()) && API.isNonPvPRegion(player.getLocation()) && !PLAYER_LOCATIONS.get(player).equals(EnumPlayerAlignments.NEUTRAL)) {
            player.sendMessage(ChatColor.YELLOW + "           " + ChatColor.BOLD + "*** WILDERNESS (MOBS-ON, PVP-OFF) ***");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.NEUTRAL);
            return;
        }
        if (!API.isInSafeRegion(player.getLocation()) && !API.isNonPvPRegion(player.getLocation()) && !PLAYER_LOCATIONS.get(player).equals(EnumPlayerAlignments.CHAOTIC)) {
            player.sendMessage(ChatColor.RED + "                " + ChatColor.BOLD + "*** CHAOTIC ZONE (PVP-ON) ***");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.CHAOTIC);
        }
    }

    public int getAlignmentTime(Player player) {
        if (!PLAYER_ALIGNMENT_TIMES.containsKey(player)) {
            return 0;
        } else {
            return PLAYER_ALIGNMENT_TIMES.get(player);
        }
    }
}
