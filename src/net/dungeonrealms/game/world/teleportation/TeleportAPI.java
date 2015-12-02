package net.dungeonrealms.game.world.teleportation;

import net.dungeonrealms.API;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.handlers.TutorialIslandHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.achievements.Achievements;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Kieran on 9/19/2015.
 */
public class TeleportAPI {

    /**
     * Checks if the player can use their hearthstone
     *
     * @param uuid
     * @return boolean
     * @since 1.0
     */
    public static boolean canUseHearthstone(UUID uuid) {
        if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.containsKey(uuid)) {
            if (API.getGamePlayer(Bukkit.getPlayer(uuid)).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                if (Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(uuid) <= 0 && Bukkit.getPlayer(uuid).getWorld().getName().equalsIgnoreCase(Bukkit.getWorlds().get(0).getName()) && (!TutorialIslandHandler.getInstance().onTutorialIsland(uuid))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds a cooldown to the players hearthstone
     *
     * @param uuid
     * @since 1.0
     */
    public static void addPlayerHearthstoneCD(UUID uuid, int cooldown) {
        Teleportation.PLAYER_TELEPORT_COOLDOWNS.put(uuid, cooldown);
    }

    /**
     * Adds the player to the currently teleporting list
     * Used for checking if the player is moving/in combat etc
     *
     * @param uuid
     * @since 1.0
     */
    public static void addPlayerCurrentlyTeleporting(UUID uuid, Location location) {
        Teleportation.PLAYERS_TELEPORTING.put(uuid, location);
    }

    /**
     * Checks if the player is in the currently teleporting list
     * Used for checking if the player is moving/in combat etc
     *
     * @param uuid
     * @return boolean
     * @since 1.0
     */
    public static boolean isPlayerCurrentlyTeleporting(UUID uuid) {
        return Teleportation.PLAYERS_TELEPORTING.containsKey(uuid);
    }

    /**
     * Removes the player to the currently teleporting list
     * Used for checking if the player is moving/in combat etc
     *
     * @param uuid
     * @@return boolean
     * @since 1.0
     */
    public static void removePlayerCurrentlyTeleporting(UUID uuid) {
        if (Teleportation.PLAYERS_TELEPORTING.containsKey(uuid)) {
            Teleportation.PLAYERS_TELEPORTING.remove(uuid);
        }
    }

    /**
     * Gets the players cooldown on hearthstone usage
     *
     * @param uuid
     * @return int
     * @since 1.0
     */
    public static int getPlayerHearthstoneCD(UUID uuid) {
        return Teleportation.PLAYER_TELEPORT_COOLDOWNS.get(uuid);
    }

    /**
     * Checks if the item is a teleportation book
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isTeleportBook(ItemStack itemStack) {
        if (itemStack.getType() != Material.BOOK) {
            return false;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return !(tag == null || nmsItem == null) && tag.getString("type").equalsIgnoreCase("teleport");
    }

    /**
     * Checks if the item is a hearthstone
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isHearthstone(ItemStack itemStack) {
        if (itemStack.getType() != Material.QUARTZ) {
            return false;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return !(tag == null || nmsItem == null) && tag.getString("type").equalsIgnoreCase("important") && tag.getString("usage").equalsIgnoreCase("hearthstone");
    }

    /**
     * Gets the location of a players hearthstone from Mongo
     *
     * @param uuid
     * @return String
     * @since 1.0
     */
    public static String getLocationFromDatabase(UUID uuid) {
        if (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid) != null) {
            return DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString();
        } else {
            return "Cyrennica";
        }
    }

    /**
     * Gets the location of a teleport from a given string
     *
     * @param location
     * @return Location
     * @since 1.0
     */
    public static Location getLocationFromString(String location) {
        switch (location.toLowerCase()) {
            case "starter": {
                return Teleportation.Tutorial;
            }
            case "cyrennica": {
                return Teleportation.Cyrennica;
            }
            case "harrison_field": {
                return Teleportation.Harrison_Field;
            }
            case "dark_oak": {
                return Teleportation.Dark_Oak_Tavern;
            }
            case "trollsbane": {
                return Teleportation.Trollsbane_tavern;
            }
            case "tripoli": {
                return Teleportation.Tripoli;
            }
            case "gloomy_hollows": {
                return Teleportation.Gloomy_Hollows;
            }
            case "crestguard": {
                return Teleportation.Crestguard_Keep;
            }
            case "deadpeaks": {
                return Teleportation.Deadpeaks_Mountain_Camp;
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Gets the display name of a teleport location
     *
     * @param location
     * @return Location
     * @since 1.0
     */
    public static String getDisplayNameOfLocation(String location) {
        switch (location.toLowerCase()) {
            case "starter": {
                return "Tutorial Island";
            }
            case "cyrennica": {
                return "City of Cyrennica";
            }
            case "harrison_field": {
                return "Harrison Field";
            }
            case "dark_oak": {
                return "Dark Oak Tavern";
            }
            case "trollsbane": {
                return "Trollsbane Tavern";
            }
            case "tripoli": {
                return "Tripoli";
            }
            case "gloomy_hollows": {
                return "Gloomy Hollows";
            }
            case "crestguard": {
                return "Crestguard Keep";
            }
            case "deadpeaks": {
                return "DeadPeaks Mountain Camp" + ChatColor.RED + " WARNING: CHAOTIC ZONE";
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Returns a random string "location"
     *
     * @return String
     * @since 1.0
     */
    public static String getRandomTeleportString() {
        switch (new Random().nextInt(8)) {
            case 0: {
                return "Cyrennica";
            }
            case 1: {
                return "Harrison_Field";
            }
            case 2: {
                return "Dark_Oak";
            }
            case 3: {
                return "Trollsbane";
            }
            case 4: {
                return "Tripoli";
            }
            case 5: {
                return "Gloomy_Hollows";
            }
            case 6: {
                return "Crestguard";
            }
            case 7: {
                return "Deadpeaks";
            }
            default: {
                return "Cyrennica";
            }
        }
    }

    public static boolean canSetHearthstoneLocation(Player player, String hearthstoneLocation) {
        switch (hearthstoneLocation.toLowerCase()) {
            case "starter":
                return false;
            case "cyrennica":
                return true;
            case "harrison_field":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.VILLAGE_SAFE);
            case "dark_oak":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.DARK_OAK_WILD2);
            case "trollsbane":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFRONT_OF_TAVERN);
            case "tripoli":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.SAVANNAH_SAFEZONE);
            case "gloomy_hollows":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.SWAMP1);
            case "crestguard":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.CREST_GUARD);
            case "deadpeaks":
                return Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.DEAD_PEAKS);
            default:
                return false;
        }
    }
}