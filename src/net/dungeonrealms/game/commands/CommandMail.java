package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/15/2015.
 */
public class CommandMail extends BasicCommand {

    public CommandMail(String command, String usage, String description) {
        super(command, usage, description);
    }
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (args.length > 0) {
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
                return false;
            }
            if (args[0].equals("send")) {
                MailHandler.getInstance().sendMail(player, args[1], player.getItemInHand());

            }
            return true;
        }

        PlayerMenus.openMailInventory(player);

        return true;

    }
}