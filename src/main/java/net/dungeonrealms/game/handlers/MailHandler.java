package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Nick on 10/14/2015.
 */
public class MailHandler {

    static MailHandler instance = null;

    public static MailHandler getInstance() {
        if (instance == null) {
            instance = new MailHandler();
        }
        return instance;
    }

    /**
     * Will give the player the item they have clicked on.
     *
     * @param item   serialized mail item.
     * @param player who to give item to?
     * @since 1.0
     */
    public void giveItemToPlayer(ItemStack item, Player player) {
        if (isMailItem(item)) {
            player.closeInventory();
            String serializedItem = CraftItemStack.asNMSCopy(item).getTag().getString("item");
            String from = serializedItem.split(",")[0];
            long unix = Long.valueOf(serializedItem.split(",")[1]);
            String rawItem = serializedItem.split(",")[2];

            ItemStack actualItem = ItemSerialization.itemStackFromBase64(rawItem);


            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.MAILBOX, from + "," + String.valueOf(unix) + "," + rawItem, true);
            player.getInventory().addItem(actualItem);
            sendMailMessage(player, ChatColor.GREEN + "You opened mail from " + ChatColor.AQUA + from + ChatColor.GREEN + "!");
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1f, 63f);
        }
    }

    /**
     * Checks of the item that is specified is a mail item containing NBT.
     *
     * @param item specify if the item is of the serialized item type.
     * @return boolean
     * @since 1.0
     */
    private boolean isMailItem(ItemStack item) {
        return !(item == null || item.getType() == null || item.getType().equals(Material.AIR)) && CraftItemStack.asNMSCopy(item).hasTag() && CraftItemStack.asNMSCopy(item).getTag().hasKey("item");
    }

    /**
     * Will apply the hidden data to an item.
     *
     * @param itemStack              item applying hidden date to..
     * @param base64SerializedString The encrypted string.
     * @return
     * @since 1.0
     */
    public ItemStack setItemAsMail(ItemStack itemStack, String base64SerializedString) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("mail"));
        tag.set("item", new NBTTagString(base64SerializedString));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * @param player    From e.g. (xFinityPro PLAYER OBJECT)
     * @param to        To e.g. (Proxying)
     * @param itemStack The package!
     * @since 1.0
     */
    public void sendMail(Player player, String to, ItemStack itemStack) {

        UUID fromUUID = player.getUniqueId();

        //TODO
        UUID toUUID = UUID.randomUUID();

        String serializedItem = ItemSerialization.itemStackToBase64(itemStack);

        String mailIdentification = player.getName() + "," + (System.currentTimeMillis() / 1000L) + "," + serializedItem;

        if (API.isOnline(toUUID)) {
            DatabaseAPI.getInstance().update(toUUID, EnumOperators.$PUSH, EnumData.MAILBOX, mailIdentification, true);
            sendMailMessage(Bukkit.getPlayer(toUUID), ChatColor.GREEN + "You have received a present from " + ChatColor.GOLD + player.getName());
        } else {
            DatabaseAPI.getInstance().update(toUUID, EnumOperators.$PUSH, EnumData.MAILBOX, mailIdentification, false);
        }

        sendMailMessage(player, ChatColor.GREEN + "You have sent " + ChatColor.GOLD + to + ChatColor.GREEN + " a present!");
    }

    /**
     * @param player  who to send message to?
     * @param message string message.
     * @since 1.0
     */
    public void sendMailMessage(Player player, String message) {
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "NORTH POLE" + ChatColor.WHITE + "]" + " " + message);
    }

}