package me.qKing12.AuctionMaster;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.qKing12.AuctionMaster.API.API;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionsHandler;
import me.qKing12.AuctionMaster.Currency.*;
import me.qKing12.AuctionMaster.FilesHandle.AuctionsDatabase;
import me.qKing12.AuctionMaster.FilesHandle.ConfigLoad;
import me.qKing12.AuctionMaster.FilesHandle.ConfigUpdater;
import me.qKing12.AuctionMaster.FilesHandle.Deliveries;
import me.qKing12.AuctionMaster.InputGUIs.AnvilGUIHelper.*;
import me.qKing12.AuctionMaster.InputGUIs.BidSelectGUI.BidSelectGUI;
import me.qKing12.AuctionMaster.InputGUIs.DeliveryCoinsGUI.DeliveryCoinsGUI;
import me.qKing12.AuctionMaster.InputGUIs.DeliveryGUI.DeliveryGUI;
import me.qKing12.AuctionMaster.InputGUIs.DurationSelectGUI.SelectDurationGUI;
import me.qKing12.AuctionMaster.InputGUIs.EditDurationGUI.EditDurationGUI;
import me.qKing12.AuctionMaster.InputGUIs.SearchGUI.SearchGUI;
import me.qKing12.AuctionMaster.InputGUIs.StartingBidGUI.StartingBidGUI;
import me.qKing12.AuctionMaster.ItemConstructor.ItemConstructor;
import me.qKing12.AuctionMaster.ItemConstructor.ItemConstructorLegacy;
import me.qKing12.AuctionMaster.ItemConstructor.ItemConstructorNew;
import me.qKing12.AuctionMaster.PlaceholderAPISupport.PlaceholderAPISupport;
import me.qKing12.AuctionMaster.PlaceholderAPISupport.PlaceholderAPISupportNo;
import me.qKing12.AuctionMaster.PlaceholderAPISupport.PlaceholderAPISupportYes;
import me.qKing12.AuctionMaster.PlaceholderAPISupport.PlaceholderRegister;
import me.qKing12.AuctionMaster.Utils.AuctionNPCHandle;
import me.qKing12.AuctionMaster.Utils.NumberFormatHelper;
import me.qKing12.AuctionMaster.bStats.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class AuctionMaster extends JavaPlugin{

    public static AuctionMaster plugin;
    public static me.qKing12.AuctionMaster.API.API API;

    public static FileConfiguration adminCfg;
    public static FileConfiguration armorCfg;
    public static FileConfiguration auctionsManagerCfg;
    public static FileConfiguration bidsRelatedCfg;
    public static FileConfiguration blocksCfg;
    public static FileConfiguration consumablesCfg;
    public static FileConfiguration currencyCfg;
    public static FileConfiguration othersCfg;
    public static FileConfiguration soundsCfg;
    public static FileConfiguration toolsCfg;
    public static FileConfiguration weaponsCfg;
    public static FileConfiguration menusCfg;
    public static FileConfiguration buyItNowCfg;

    public static boolean upperVersion;

    public static ConfigLoad configLoad;
    public static ItemConstructor itemConstructor;
    public static PlaceholderAPISupport utilsAPI;
    public static NumberFormatHelper numberFormatHelper;
    public static AnvilGUIHelper anvilHelper;
    public static Currency economy;
    public static AuctionNPCHandle auctionNPC;

    public static AuctionsHandler auctionsHandler;
    public static Deliveries deliveries;
    public static AuctionsDatabase auctionsDatabase;

    private void currencySetup(){
        String currency = currencyCfg.getString("currency-type");
        if(currency.equalsIgnoreCase("Vault")){
            economy=new VaultImpl();
        } else if (currency.equalsIgnoreCase("CustomEconomy-Balance")) {
            economy = new CustomEconomyBalance();
        } else if (currency.equalsIgnoreCase("CustomEconomy-Tokens")) {
            economy = new CustomEconomyTokens();
        } else if (currency.equalsIgnoreCase("PlayerPoints")) {
            economy = new PlayerPointsImpl();
        } else if (currency.equalsIgnoreCase("TokenManager")) {
            economy = new TokenManagerImpl();
        } else if (currency.equalsIgnoreCase("Skript")) {
            economy = new SkriptImpl();
        }
    }

    private void setupAnvilHelper(){
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
            return;
        }
        if(version.equals("v1_8_R3")) {
            anvilHelper = new Wrapper1_8_R3();
        }
        else if(version.equals("v1_9_R1"))
            anvilHelper=new Wrapper1_9_R1();
        else if(version.equals("v1_9_R2"))
            anvilHelper=new Wrapper1_9_R2();
        else if(version.equals("v1_10_R1"))
            anvilHelper=new Wrapper1_10_R1();
        else if(version.equals("v1_11_R1"))
            anvilHelper=new Wrapper1_11_R1();
        else if(version.equals("v1_12_R1"))
            anvilHelper=new Wrapper1_12_R1();
        else if(version.equals("v1_13_R1"))
            anvilHelper=new Wrapper1_13_R1();
        else if(version.equals("v1_13_R2"))
            anvilHelper=new Wrapper1_13_R2();
        else if(version.contains("v1_14_R"))
            anvilHelper=new Wrapper1_14_R1();
        else if(version.equals("v1_15_R1"))
            anvilHelper=new Wrapper1_15_R1();
        else if(version.equals("v1_16_R1"))
            anvilHelper=new Wrapper1_16_R1();
        else if(version.equals("v1_16_R2"))
            anvilHelper=new Wrapper1_16_R2();
    }

    private void loadPlaceholderAPISupport(){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            utilsAPI = new PlaceholderAPISupportYes();
            new PlaceholderRegister().register();
        }
        else
            utilsAPI=new PlaceholderAPISupportNo();
    }

    private void setupItemConstructor(){

        if(!upperVersion) {
            itemConstructor = new ItemConstructorLegacy();
        }
        else {
            itemConstructor = new ItemConstructorNew();
        }
    }

    private void setupAuctionNPC(){
        if(this.getConfig().getBoolean("auction-npc-use"))
            if (Bukkit.getPluginManager().getPlugin("Citizens") != null) {
                auctionNPC = new AuctionNPCHandle();
                auctionNPC.debugHolos();
            }
    }

    public class DeliveryAlert implements Listener {
        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                ArrayList<ItemStack> items = deliveries.getDeliveryItems(e.getPlayer().getUniqueId().toString());
                double coins = deliveries.getCoins(e.getPlayer().getUniqueId().toString());
                if (coins != 0 || !items.isEmpty()) {
                    e.getPlayer().sendMessage(utilsAPI.chat(e.getPlayer(), getConfig().getString("delivery-alert-join-message")));
                }
            });
        }
    }

    public static boolean hasProtocolLib=false;

    @Override
    public void onEnable(){
        plugin=this;
        saveDefaultConfig();
        ConfigUpdater.generateFiles();

        MetricsLite metrics = new MetricsLite(this, 8726);

        if(this.getConfig().getDouble("version")<3.22){
            new ConfigUpdater(this);
            saveDefaultConfig();
        }

        File database = new File(AuctionMaster.plugin.getDataFolder(), "database");
        if(!database.exists()){
            database.mkdir();
        }

        numberFormatHelper=new NumberFormatHelper();

        String version = Bukkit.getVersion();
        if(version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12"))
            upperVersion=false;
        else
            upperVersion=true;

        setupItemConstructor();

        adminCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "admin_config.yml"));
        buyItNowCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "buyItNow.yml"));
        armorCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus/armor.yml"));
        auctionsManagerCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "auctions_manager.yml"));
        bidsRelatedCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "bids_related.yml"));
        menusCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus.yml"));
        blocksCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus/blocks.yml"));
        consumablesCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus/consumables.yml"));
        currencyCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "currency.yml"));
        othersCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus/others.yml"));
        soundsCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "sounds.yml"));
        toolsCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus/tools.yml"));
        weaponsCfg = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "menus/weapons.yml"));

        if(Bukkit.getPluginManager().getPlugin("HeadDatabase") != null)
            new HeadDatabaseAPI();

        configLoad=new ConfigLoad();
        loadPlaceholderAPISupport();
        auctionsHandler=new AuctionsHandler();
        if(getConfig().getBoolean("use-delivery-system")) {
            deliveries = new Deliveries();
            new DeliveryGUI();
            new DeliveryCoinsGUI();
            Bukkit.getPluginManager().registerEvents(new DeliveryAlert(), this);
        }
        auctionsDatabase=new AuctionsDatabase();
        if(AuctionMaster.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !AuctionMaster.hasProtocolLib)
            setupAnvilHelper();

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            hasProtocolLib=true;
        }

        currencySetup();
        setupAuctionNPC();

        new SelectDurationGUI();
        new StartingBidGUI();
        new SearchGUI();
        new BidSelectGUI();
        new EditDurationGUI();

        new Commands();
        API=new API();
    }

    @Override
    public void onDisable(){
        auctionsDatabase.registerPreviewItem("serverCloseDate", String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli()));
    }

}
