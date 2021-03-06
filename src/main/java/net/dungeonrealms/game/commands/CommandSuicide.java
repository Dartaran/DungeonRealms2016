package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSuicide extends BasicCommand {

	public CommandSuicide(String command, String usage, String description, List<String> aliases) {
		super(command, usage, description, aliases);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p;
		if(sender instanceof Player) {
			p = (Player) sender;
		}
		else {
			return true;
		}

		p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.GRAY + "This " +
				"command will KILL you, you will LOSE everything you are carrying. If you are sure, type '" +
				ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.GRAY + "', if not, type '" + ChatColor
				.RED.toString() + "cancel" + ChatColor.RED + "'.");

		Chat.listenForMessage(p, event -> {
			if (event.getMessage().equalsIgnoreCase("y")) {
				HealthHandler.getInstance().handlePlayerDeath(p, null);
			}
		}, player -> player.sendMessage(ChatColor.YELLOW + "suicide - " + ChatColor.BOLD + "CANCELLED"));
		return true;
	}
	
}
