package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.handlers.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Chase on Nov 11, 2015
 */
public class CommandModeration extends BasicCommand {
    public CommandModeration(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!sender.isOp()) return false;

        switch (args[0]) {
            case "tp":
                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    sender.teleport(player.getLocation());
                    sender.sendMessage("Teleported to " + player.getName());
                } else
                    sender.sendMessage(ChatColor.RED + playerName + " not online");
                break;
            case "invsee":
                playerName = args[1];
                player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    sender.openInventory(player.getInventory());
                }
                break;
            case "armorsee":
                playerName = args[1];
                player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    Inventory inv = Bukkit.createInventory(null, 9, player.getName() + " Armor");
                    for (ItemStack stack : player.getInventory().getArmorContents()) {
                        inv.addItem(stack);
                    }
                    inv.setItem(8, player.getItemInHand());
                    sender.openInventory(inv);
                }
                break;
            case "hide":
                if (sender.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    sender.removePotionEffect(PotionEffectType.INVISIBILITY);
                    ScoreboardHandler.getInstance().matchMainScoreboard(sender);
                    sender.sendMessage(ChatColor.GREEN + " You have been unhidden");
                    sender.setCustomNameVisible(true);
                } else {
                    sender.setCustomNameVisible(false);
                    sender.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                    sender.sendMessage(ChatColor.GREEN + " You have been hidden");
                }
                break;
        }
        return false;
    }
}