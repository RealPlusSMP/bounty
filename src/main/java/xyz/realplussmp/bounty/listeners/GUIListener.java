package xyz.realplussmp.bounty.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.realplussmp.bounty.gui.BountyGUI;

public class GUIListener implements Listener {

    private final BountyGUI bountyGUI;

    public GUIListener(BountyGUI bountyGUI) {
        this.bountyGUI = bountyGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        bountyGUI.handleClick(e);
    }
}
