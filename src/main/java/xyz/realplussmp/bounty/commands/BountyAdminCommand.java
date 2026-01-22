package xyz.realplussmp.bounty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.realplussmp.bounty.bounty.BountyManager;

public class BountyAdminCommand implements CommandExecutor {

    private final BountyManager bountyManager;

    public BountyAdminCommand(BountyManager bountyManager) {
        this.bountyManager = bountyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 3 || !args[0].equalsIgnoreCase("put")) {
            player.sendMessage("");
            return true;
        }
        return true;
    }
}
