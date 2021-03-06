package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.dungeonrealms.game.player.rank.Rank;

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

        if (!Rank.isGM(sender)) return false;

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
                    for (int i = 0; i < 4; i++) {
                    	ItemStack stack = player.getInventory().getArmorContents()[i];
                        inv.addItem(stack);
                    }
                    inv.setItem(8, player.getEquipment().getItemInMainHand());
                    sender.openInventory(inv);
                }
                break;
            case "hide":
                sender.sendMessage(ChatColor.YELLOW + "Please use " + ChatColor.BOLD + ChatColor.UNDERLINE + "/gm" + ChatColor.YELLOW + ".");
                // @todo: remove this later on.
                break;
            case "banksee":
            	playerName = args[1];
            	if(Bukkit.getPlayer(playerName) != null){
            		Storage storage = BankMechanics.getInstance().getStorage(Bukkit.getPlayer(playerName).getUniqueId());
            		sender.openInventory(storage.inv);
            	}
            	break;
        }
        return false;
    }
}
