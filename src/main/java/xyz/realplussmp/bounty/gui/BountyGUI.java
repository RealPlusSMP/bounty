package xyz.realplussmp.bounty.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.realplussmp.bounty.bounty.BountyManager;
import xyz.realplussmp.bounty.util.MessageUtil;

import java.util.*;

public class BountyGUI {

    private final BountyManager bountyManager;
    private final FileConfiguration config;

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private static final int PAGE_SIZE = 45;

    public BountyGUI(BountyManager bountyManager, FileConfiguration config) {
        this.bountyManager = bountyManager;
        this.config = config;
    }

    public void open(Player player) {
        int page = playerPages.getOrDefault(player.getUniqueId(), 0);

        Inventory inv = Bukkit.createInventory(null, 54, MessageUtil.get("gui.title", Map.of("currentpage", String.valueOf(page + 1))));

        List<Map.Entry<UUID, Double>> entries = new ArrayList<>(bountyManager.getAllBounties().entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, entries.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            Map.Entry<UUID, Double> entry = entries.get(i);

            var offline = Bukkit.getOfflinePlayer(entry.getKey());
            String name = offline.getName();
            if (name == null) name = "Unknown";

            double amount = entry.getValue();
            inv.setItem(slot++, createBountyItem(name, amount));
        }

        addStaticItems(inv, page, entries.size());
        player.openInventory(inv);
    }

    private Set<Integer> getReservedSlots() {
        Set<Integer> set = new HashSet<>();
        set.add(config.getInt("gui.info-item.slot"));
        set.add(config.getInt("gui.search-item.slot"));
        return set;
    }

    private ItemStack createBountyItem(String playerName, double amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();

        meta.displayName(MessageUtil.get("gui.player-head.item-name",
                Map.of("player", playerName)));
        meta.lore(MessageUtil.getList("gui.player-head.lore",
                Map.of("amount", String.format("%.2f", amount))));

        head.setItemMeta(meta);
        return head;
    }

    public void handleClick(InventoryClickEvent e) {
        if (!e.getView().title().contains(Component.text("ʙᴏᴜɴᴛɪᴇꜱ"))) return;

        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        UUID uuid = player.getUniqueId();

        if (slot == config.getInt("gui.info-item.slot")) {
            open(player);
            return;
        }

        if (slot == config.getInt("gui.search-item.slot")) {
            player.sendMessage(Component.text("Search coming soon!"));
            return;
        }

        if (slot == config.getInt("gui.next-page-item.slot")) {
            playerPages.put(uuid, playerPages.getOrDefault(uuid, 0) + 1);
            open(player);
            return;
        }

        if (slot == config.getInt("gui.back-item.slot")) {
            int current = playerPages.getOrDefault(uuid, 0);
            if (current > 0) {
                playerPages.put(uuid, current - 1);
                open(player);
            }
        }
    }

    private void addStaticItems(Inventory inv, int page, int totalEntries) {
        int maxPage = (int) Math.ceil(totalEntries / (double) PAGE_SIZE) - 1;

        // Info item
        Material infoMat = Material.valueOf(config.getString("gui.info-item.item"));
        int infoSlot = config.getInt("gui.info-item.slot");
        ItemStack info = new ItemStack(infoMat);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(MessageUtil.get("gui.info-item.item-name"));
        infoMeta.lore(MessageUtil.getList("gui.info-item.lore"));
        info.setItemMeta(infoMeta);
        inv.setItem(infoSlot, info);

        // Search item
        Material searchMat = Material.valueOf(config.getString("gui.search-item.item"));
        int searchSlot = config.getInt("gui.search-item.slot");
        ItemStack search = new ItemStack(searchMat);
        ItemMeta searchMeta = search.getItemMeta();
        searchMeta.displayName(MessageUtil.get("gui.search-item.item-name"));
        searchMeta.lore(MessageUtil.getList("gui.search-item.lore"));
        search.setItemMeta(searchMeta);
        inv.setItem(searchSlot, search);

        // Next page
        if (page < maxPage) {
            Material nextMat = Material.valueOf(config.getString("gui.next-page-item.item"));
            int nextSlot = config.getInt("gui.next-page-item.slot");

            ItemStack next = new ItemStack(nextMat);
            ItemMeta meta = next.getItemMeta();
            meta.displayName(MessageUtil.get("gui.next-page-item.item-name"));
            meta.lore(MessageUtil.getList("gui.next-page-item.lore",
                    Map.of("currentpage", String.valueOf(page + 1))));
            next.setItemMeta(meta);
            inv.setItem(nextSlot, next);
        }

        // Back page
        if (page > 0) {
            Material backMat = Material.valueOf(config.getString("gui.back-item.item"));
            int backSlot = config.getInt("gui.back-item.slot");

            ItemStack back = new ItemStack(backMat);
            ItemMeta meta = back.getItemMeta();
            meta.displayName(MessageUtil.get("gui.back-item.item-name"));
            meta.lore(MessageUtil.getList("gui.back-item.lore"));
            back.setItemMeta(meta);
            inv.setItem(backSlot, back);
        }
    }
}
