package xyz.realplussmp.bounty.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.realplussmp.bounty.bounty.BountyManager;
import xyz.realplussmp.bounty.bounty.BountySortType;
import xyz.realplussmp.bounty.util.MessageUtil;
import xyz.realplussmp.bounty.util.NumberUtil;

import java.util.*;

public class BountyGUI {

    private final BountyManager bountyManager;
    private final FileConfiguration config;

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, BountySortType> playerSort = new HashMap<>();
    private static final int PAGE_SIZE = 45;

    public BountyGUI(BountyManager bountyManager, FileConfiguration config) {
        this.bountyManager = bountyManager;
        this.config = config;
    }

    public void open(Player player) {
        int page = playerPages.getOrDefault(player.getUniqueId(), 0);

        Inventory inv = Bukkit.createInventory(null, 54, MessageUtil.get("gui.title", Map.of("currentpage", String.valueOf(page + 1))));

        List<Map.Entry<UUID, Double>> entries;
        BountySortType sort = playerSort.getOrDefault(player.getUniqueId(), BountySortType.AMOUNT);

        if (sort == BountySortType.RECENT) {
            entries = bountyManager.getAllBountiesSortedByRecent();
        } else {
            entries = new ArrayList<>(bountyManager.getAllBounties().entrySet());
        }

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, entries.size());

        Set<Integer> reserved = getReservedSlots();
        int slot = 0;

        for (int i = start; i < end; i++) {
            while (reserved.contains(slot)) slot++;

            Map.Entry<UUID, Double> entry = entries.get(i);
            var offline = Bukkit.getOfflinePlayer(entry.getKey());
            String name = offline.getName() != null ? offline.getName() : "Unknown";

            inv.setItem(slot++, createBountyItem(name, entry.getValue()));
        }

        addStaticItems(player, inv, page, entries.size());
        player.openInventory(inv);
    }

    private Set<Integer> getReservedSlots() {
        Set<Integer> set = new HashSet<>();
        set.add(config.getInt("gui.info-item.slot"));
        set.add(config.getInt("gui.search-item.slot"));
        set.add(config.getInt("gui.sort-item.slot"));
        set.add(config.getInt("gui.next-page-item.slot"));
        set.add(config.getInt("gui.back-item.slot"));
        return set;
    }

    private ItemStack createBountyItem(String playerName, double amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            meta = skullMeta;
        }

        meta.displayName(MessageUtil.get("gui.player-head.item-name",
                Map.of("player", playerName)));
        meta.lore(MessageUtil.getList("gui.player-head.lore",
                Map.of("amount", NumberUtil.format(amount))));

        head.setItemMeta(meta);
        return head;
    }

    public void handleClick(InventoryClickEvent e) {
        String opened = PlainTextComponentSerializer.plainText().serialize(e.getView().title());

        String base = config.getString("gui.title");
        if (base == null) return;

        String basePlain = base.split("%currentpage%")[0];

        if (!opened.startsWith(basePlain)) return;

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
            int current = playerPages.getOrDefault(uuid, 0);
            int size = playerSort.getOrDefault(uuid, BountySortType.AMOUNT) == BountySortType.RECENT
                    ? bountyManager.getAllBountiesSortedByRecent().size()
                    : bountyManager.getAllBounties().size();

            int maxPage = (int) Math.ceil(size / (double) PAGE_SIZE) - 1;

            if (current < maxPage) {
                playerPages.put(uuid, current + 1);
                open(player);
            }
            return;
        }

        if (slot == config.getInt("gui.back-item.slot")) {
            int current = playerPages.getOrDefault(uuid, 0);
            if (current > 0) {
                playerPages.put(uuid, current - 1);
                open(player);
            }
        }

        if (slot == config.getInt("gui.sort-item.slot")) {
            BountySortType current = playerSort.getOrDefault(uuid, BountySortType.AMOUNT);
            playerSort.put(uuid, current == BountySortType.AMOUNT ? BountySortType.RECENT : BountySortType.AMOUNT);
            open(player);
            return;
        }
    }

    private void addStaticItems(Player player, Inventory inv, int page, int totalEntries) {
        int maxPage = (int) Math.ceil(totalEntries / (double) PAGE_SIZE) - 1;

        addItem(inv, "gui.info-item");
        addItem(inv, "gui.search-item");

        if (page < maxPage) {
            addItem(inv, "gui.next-page-item", Map.of("currentpage", String.valueOf(page + 1)));
        }

        if (page > 0) {
            addItem(inv, "gui.back-item");
        }

        BountySortType sortType = playerSort.getOrDefault(player.getUniqueId(), BountySortType.AMOUNT);
        addItem(inv, "gui.sort-item", Map.of("sorting", sortType.getDisplay()));
    }

    private void addItem(Inventory inv, String path, Map<String, String> placeholders) {
        String matName = config.getString(path + ".item");
        if (matName == null) return;

        Material mat;
        try {
            mat = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        int slot = config.getInt(path + ".slot");
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtil.get(path + ".item-name", placeholders));
        meta.lore(MessageUtil.getList(path + ".lore", placeholders));

        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private void addItem(Inventory inv, String path) {
        addItem(inv, path, Map.of());
    }
}
