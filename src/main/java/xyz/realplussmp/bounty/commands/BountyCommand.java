package xyz.realplussmp.bounty.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import xyz.realplussmp.bounty.bounty.BountyManager;
import xyz.realplussmp.bounty.gui.BountyGUI;
import xyz.realplussmp.bounty.util.ConfigUtil;
import xyz.realplussmp.bounty.util.MessageUtil;

import java.util.Map;

public class BountyCommand implements CommandExecutor {

    private final BountyManager bountyManager;
    private final Economy economy;
    private final ConfigUtil configUtil;
    private final BountyGUI bountyGUI;

    public BountyCommand(BountyManager bountyManager, Economy economy, ConfigUtil configUtil, BountyGUI bountyGUI) {
        this.bountyManager = bountyManager;
        this.economy = economy;
        this.configUtil = configUtil;
        this.bountyGUI = bountyGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            bountyGUI.open(player);
            return true;
        }

        if (args.length != 3 || !args[0].equalsIgnoreCase("add")) {
            player.sendMessage(MessageUtil.get("errors.invalid-cmd"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            player.sendMessage(MessageUtil.get("errors.player-not-found"));
            return true;
        }

        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtil.get("errors.invalid-amount"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(MessageUtil.get("errors.negative-amount"));
            return true;
        }

        double min = configUtil.getDouble("bounty.min", 10);
        double max = configUtil.getDouble("bounty.max", 100000000);

        if (amount < min) {
            player.sendMessage(MessageUtil.get("errors.bounty-too-low", Map.of("min", String.format("%.2f", min))));
            return true;
        }

        if (amount > max) {
            player.sendMessage(MessageUtil.get("errors.bounty-too-high", Map.of("max", String.format("%.2f", max))));
            return true;
        }

        if (target == player) {
            player.sendMessage(MessageUtil.get("errors.self-bounty"));
            return true;
        }

        if (!economy.has(player, amount)) {
            player.sendMessage(MessageUtil.get("errors.not-enough-money"));
            return true;
        }


        economy.withdrawPlayer(player, amount);
        bountyManager.setBounty(target.getUniqueId(),
                bountyManager.getBounty(target.getUniqueId()) + amount);

        player.sendMessage(MessageUtil.get("bounty-placed", Map.of("amount", String.format("%.2f", amount), "player", target.getName())));
        Bukkit.broadcast(MessageUtil.get("global.bounty-placed", Map.of("amount", String.format("%.2f", amount), "player", target.getName()))
        );
        return true;
    }
}