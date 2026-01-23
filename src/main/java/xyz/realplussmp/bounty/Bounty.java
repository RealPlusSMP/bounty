package xyz.realplussmp.bounty;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.realplussmp.bounty.bounty.BountyManager;
import xyz.realplussmp.bounty.commands.BountyCommand;
import xyz.realplussmp.bounty.database.MySQL;
import xyz.realplussmp.bounty.gui.BountyGUI;
import xyz.realplussmp.bounty.listeners.GUIListener;
import xyz.realplussmp.bounty.listeners.KillListener;
import xyz.realplussmp.bounty.util.ConfigUtil;
import xyz.realplussmp.bounty.util.MessageUtil;

import java.io.File;

public final class Bounty extends JavaPlugin {

    private static Bounty instance;
    private Economy economy;
    private MySQL database;
    private BountyManager bountyManager;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", true);
        reloadMessages();
        MessageUtil.init(getMessages());

        if (!setupEconomy()) {
            getLogger().severe("Vault not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        database = new MySQL(this);
        database.connect();

        bountyManager = new BountyManager(database);
        ConfigUtil configUtil = new ConfigUtil(getConfig());
        BountyGUI bountyGUI = new BountyGUI(bountyManager, getMessages());

        getCommand("bounty").setExecutor(new BountyCommand(bountyManager, economy, configUtil, bountyGUI));

        getServer().getPluginManager().registerEvents(new KillListener(bountyManager, economy), this);
        getServer().getPluginManager().registerEvents(new GUIListener(bountyGUI), this);
    }

    public void reloadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }

    public static Bounty getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
