package xyz.realplussmp.bounty.listeners;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import xyz.realplussmp.bounty.bounty.BountyManager;
import xyz.realplussmp.bounty.util.MessageUtil;
import xyz.realplussmp.bounty.util.NumberUtil;

import java.util.Map;

public class KillListener implements Listener {

    private final BountyManager bountyManager;
    private final Economy economy;

    public KillListener(BountyManager bountyManager, Economy economy) {
        this.bountyManager = bountyManager;
        this.economy = economy;
    }

    @EventHandler
    public void onKill(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        double bounty = bountyManager.getBounty(victim.getUniqueId());
        if (Double.isNaN(bounty) || bounty <= 0) return;

        EconomyResponse res = economy.depositPlayer(killer, bounty);

        if (!res.transactionSuccess()) {
            killer.sendMessage(Component.text("§cFailed to deposit bounty: " + res.errorMessage));
            return;
        }

        bountyManager.removeBounty(victim.getUniqueId());

        killer.sendMessage(MessageUtil.get("claimed", Map.of("amount", NumberUtil.format(bounty))));
        Bukkit.broadcast(MessageUtil.get("global.claimed-global", Map.of("player", killer.getName(), "amount", NumberUtil.format(bounty))));
    }
}