package net.dungeonrealms.game.commands;

import java.util.List;

import net.dungeonrealms.game.mastery.GamePlayer;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.world.items.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.items.Item.WeaponAttributeType;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandCheck extends BasicCommand {

	public CommandCheck(String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;

		if (!player.isOp()) {
			return true;
		}

		if (player.getInventory().getItemInMainHand() == null) {
			player.sendMessage(ChatColor.RED + "There is nothing in your hand.");
			return true;
		}

		ItemStack inHand = player.getInventory().getItemInMainHand();

		NBTTagCompound tag = CraftItemStack.asNMSCopy(inHand).getTag();

		if (args.length == 1) {
			if(args[0].equalsIgnoreCase("nbt")){
				List<String> modifiers = API.getModifiers(inHand);

				if (API.isWeapon(inHand)) {
					WeaponAttributeType attributeType;
					for (String mod : modifiers) {
						attributeType = WeaponAttributeType.getByNBTName(mod);
						if (attributeType.isRange()) { // ranged value
							sender.sendMessage(attributeType.getName() + ": " + tag.getInt(mod + "Min") + " - "
									+ tag.getInt(mod + "Max"));
						}
						else { // static value
							sender.sendMessage(attributeType.getName() + ": " + tag.getInt(mod));
						}
					}
				}
				else if (API.isArmor(inHand)) {
					ArmorAttributeType attributeType;
					for (String mod : modifiers) {
						attributeType = ArmorAttributeType.getByNBTName(mod);
						if (attributeType.isRange()) { // ranged value
							sender.sendMessage(attributeType.getName() + ": " + tag.getInt(mod + "Min") + " - "
									+ tag.getInt(mod + "Max"));
						}
						else { // static value
							sender.sendMessage(attributeType.getName() + ": " + tag.getInt(mod));
						}
					}
				}
				else {
					player.sendMessage("Listing All NBT...");
					// get all the nbt tags of the item
					tag.c().stream().forEach(key -> player.sendMessage(key + ": " + tag.get(key).toString()));
				}
			}
		}
		else if (args.length == 2) { // check player attributes
			Player attributePlayer = Bukkit.getPlayer(args[1]);
			if (args[0].equalsIgnoreCase("attributes") && attributePlayer != null) {
				GamePlayer gp = API.getGamePlayer(attributePlayer);
				gp.getAttributes().entrySet().stream().forEach(entry -> {
					player.sendMessage(entry.getKey() + ": " + entry.getValue()[0] + " - " + entry.getValue()[1]);
				});
				return true;
			}
		}
		else {
			player.sendMessage(ChatColor.RED + "EpochIdentifier: " + tag.getString("u"));
			return true;
		}
		return false;
	}
}