package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Nick on 9/26/2015.
 */
public class Chat {

    static Chat instance = null;

    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }

    private static final Map<Player, Consumer<? super AsyncPlayerChatEvent>> chatListeners = new ConcurrentHashMap<>();
    private static final Map<Player, Consumer<? super Player>> orElseListeners = new ConcurrentHashMap<>();

    /**
     * Listens for a player message.
     * If <i>consumer</i> is null, the player is removed from memory (used in Quit event)
     *
     * @param consumer the Consumer that should listen for the event AND NOT BE NULL
     * @param orElse   the consumer that get called when another listener listens for a message (this one gets removed) or when the player quits
     */
    public static void listenForMessage(Player player, Consumer<? super AsyncPlayerChatEvent> consumer, Consumer<? super Player> orElse) {
        if (chatListeners.remove(player) != null) {
            Consumer<? super Player> old = orElseListeners.remove(player);
            if (old != null) {
                old.accept(player);
            }
        }
        if (consumer != null) {
            chatListeners.put(player, consumer);
            if (orElse != null) orElseListeners.put(player, orElse);
        }
    }

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore", "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "queer", "nigger", "dike", "dyke", "retard", "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", "faggot", "gay", "f@g", "d1ck", "titanrift", "wynncraft", "titan rift", "titanrift", "fucked"));

    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event Chat event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {

        Consumer<? super AsyncPlayerChatEvent> messageListener = chatListeners.remove(event.getPlayer());
        if (messageListener != null) {
            messageListener.accept(event);
            orElseListeners.remove(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();

        String fixedMessage = checkForBannedWords(event.getMessage());
        event.setMessage(fixedMessage);

        if (fixedMessage.startsWith("@") && !fixedMessage.contains("@i@")) {
            String playerName = fixedMessage.replace("@", "").split(" ")[0];
            if (DungeonRealms.getInstance().getDevelopers().contains(playerName)) {
                Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.PM_DEV);
            }
            fixedMessage = fixedMessage.replace("@" + playerName, "");
            String tempFixedMessage = fixedMessage.replace("@" + playerName, "");
            Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(playerName)).limit(1).forEach(theTargetPlayer -> {
                theTargetPlayer.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "FROM: " + ChatColor.AQUA + event.getPlayer().getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + tempFixedMessage);
                event.getPlayer().sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO: " + ChatColor.AQUA + theTargetPlayer.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + tempFixedMessage);
                theTargetPlayer.playSound(theTargetPlayer.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
            });
            event.setCancelled(true);
            return;
        }

        boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, uuid);

        if (gChat) {
            if (fixedMessage.contains("@i@") && event.getPlayer().getEquipment().getItemInMainHand() != null && event.getPlayer().getEquipment().getItemInMainHand().getType() != Material.AIR) {
                final Player p = event.getPlayer();
                String aprefix = GameChat.getPreMessage(p);
                String[] split = fixedMessage.split("@i@");
                String after = "";
                String before = "";
                if (split.length > 0)
                    before = split[0];
                if (split.length > 1)
                    after = split[1];


                ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();

                List<String> hoveredChat = new ArrayList<>();
                ItemMeta meta = stack.getItemMeta();
                hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
                if (meta.hasLore())
                    hoveredChat.addAll(meta.getLore());
                final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
                normal.addText(before + "");
                normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
                normal.addText(after);

                Bukkit.getOnlinePlayers().stream().forEach(player -> normal.sendToPlayer(player));
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            final String finalFixedMessage = fixedMessage;
            Bukkit.getOnlinePlayers().stream().forEach(player -> player.sendMessage(GameChat.getPreMessage(event.getPlayer(), true, GameChat.getGlobalType(finalFixedMessage)) + finalFixedMessage));
        } else {
            if (fixedMessage.contains("@i@") && event.getPlayer().getEquipment().getItemInMainHand() != null && event.getPlayer().getEquipment().getItemInMainHand().getType() != Material.AIR) {
                final Player p = event.getPlayer();
                String aprefix = GameChat.getPreMessage(p);
                String[] split = fixedMessage.split("@i@");
                String after = "";
                String before = "";
                if (split.length > 0)
                    before = split[0];
                if (split.length > 1)
                    after = split[1];

                ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();

                List<String> hoveredChat = new ArrayList<>();
                ItemMeta meta = stack.getItemMeta();
                hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
                if (meta.hasLore())
                    hoveredChat.addAll(meta.getLore());
                final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
                normal.addText(before + "");
                normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
                normal.addText(after);

                API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(normal::sendToPlayer);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles local player chat
     *
     * @param event Chat event
     */

    public void doLocalChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        final String finalFixedMessage = event.getMessage();


        // HANDLE LOCAL CHAT
        if (API.getNearbyPlayers(event.getPlayer().getLocation(), 75).size() >= 2) {
            API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player -> player.sendMessage(GameChat.getPreMessage(event.getPlayer()) + finalFixedMessage));
        } else {
            event.getPlayer().sendMessage(GameChat.getPreMessage(event.getPlayer()) + finalFixedMessage);
            event.getPlayer().sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "No one heard you...");
        }
        event.setCancelled(true);
    }

    public String checkForBannedWords(String message) {
        String returnMessage = "";
        if (!message.contains(" ")) {
            message += " ";
        }
        for (String string : message.split(" ")) {
            for (String word : bannedWords) {
                if (string.toLowerCase().contains(word.toLowerCase())) {
                    int wordLength = word.length();
                    String replacementCharacter = "";
                    while (wordLength > 0) {
                        replacementCharacter += "*";
                        wordLength--;
                    }
                    int censorStart = string.toLowerCase().indexOf(word);
                    int censorEnd = censorStart + word.length();
                    String badWord = string.substring(censorStart, censorEnd);
                    string = string.replaceAll(badWord, replacementCharacter);
                }
            }
            returnMessage += string + " ";
        }

        if (returnMessage.endsWith(" ")) {
            returnMessage = returnMessage.substring(0, returnMessage.lastIndexOf(" "));
        }

        return returnMessage;
    }



}
