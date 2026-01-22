package xyz.realplussmp.bounty.listeners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import xyz.realplussmp.bounty.bounty.BountyManager;
import xyz.realplussmp.bounty.util.MessageUtil;

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
        if (bounty > 0) {
            economy.depositPlayer(killer, bounty);
            bountyManager.removeBounty(victim.getUniqueId());
            killer.sendMessage(MessageUtil.get("claimed", Map.of("amount", String.valueOf(bounty))));
            Bukkit.broadcast(MessageUtil.get("global.claimed-global", Map.of("player", killer.getName(), "amount", String.valueOf(bounty))));
        }
    }
}