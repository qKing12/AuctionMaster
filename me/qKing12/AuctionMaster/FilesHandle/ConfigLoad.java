package me.qKing12.AuctionMaster.FilesHandle;

import com.mysql.fabric.xmlrpc.base.Array;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

import static me.qKing12.AuctionMaster.Main.adminCfg;
import static me.qKing12.AuctionMaster.Main.buyItNowCfg;

public class ConfigLoad {

    private ArrayList<ItemStack> blacklistIds;
    private ArrayList<String> blacklistNames;
    private ArrayList<ArrayList<String>> blacklistLore;
    public boolean isBlacklisted(ItemStack item){
        String itemName=utils.getDisplayName(item);
        if(blacklistNames.contains(itemName))
            return true;

        ArrayList<String> toCheck = (ArrayList<String>)item.getItemMeta().getLore();
        if(toCheck!=null)
            for(ArrayList<String> lore : blacklistLore){
                if(lore.containsAll(toCheck))
                    return true;
            }

        if(Main.upperVersion){
            for(ItemStack item2 : blacklistIds)
                if(item.getType().equals(item2.getType()))
                    return true;
        }
        else{
            for(ItemStack item2 : blacklistIds)
                if(item.getType().equals(item2.getType()) && item.getData()==item2.getData())
                    return true;
        }

        return false;
    }

    public ItemStack skullItem;

    public boolean useBackgoundGlass;
    public ItemStack backgroundGlass;

    public String mainMenuName;
    public int mainMenuSize;

    public String closeMenuName;
    public ArrayList<String> closeMenuLore;
    public ItemStack closeMenuMaterial;

    public String goBackName;
    public ArrayList<String> goBackLore;
    public ItemStack goBackMaterial;

    public String nextPageName;
    public ArrayList<String> nextPageLore;
    public ItemStack nextPageMaterial;

    public String previousPageName;
    public ArrayList<String> previousPageLore;
    public ItemStack previousPageMaterial;

    public String browsingMenuItemName;
    public ArrayList<String> browsingMenuItemLore;
    public ItemStack browsingMenuItemMaterial;

    public String viewBidsMenuItemName;
    public ArrayList<String> viewBidsMenuItemLoreWithBids;
    public ArrayList<String> viewBidsMenuItemLoreWithoutBids;
    public ItemStack viewBidsMenuItemMaterial;

    public String manageAuctionsItemName;
    public ArrayList<String> manageAuctionsItemLoreWithAuctions;
    public ArrayList<String> manageAuctionsItemLoreWithoutAuctions;
    public ItemStack manageAuctionsItemMaterial;

    public String deliveryMenuName;

    public String deliveryItemYesName;
    public String deliveryItemNoName;
    public ArrayList<String> deliveryItemYesLore;
    public ArrayList<String> deliveryItemNoLore;

    public String mainMenuDeliveryName;
    public ItemStack mainMenuDeliveryItem;
    public ArrayList<String> mainMenuDeliveryLore;

    public String createAuctionMenuName;
    public int createAuctionMenuSize;

    public String createAuctionConfirmMenuName;
    public int createAuctionConfirmMenuSize;

    public String createAuctionPreviewNameNoItem;
    public String createAuctionPreviewNameYesItem;
    public ArrayList<String> createAuctionPreviewLoreNoItem;
    public String createAuctionPreviewItemSelectedLoreAdd;
    public ItemStack createAuctionPreviewMaterial;

    public String createAuctionConfirmYesName;
    public ArrayList<String> createAuctionConfirmYesLore;
    public ItemStack createAuctionConfirmYesMaterial;

    public String createAuctionConfirmNoName;
    public ArrayList<String> createAuctionConfirmNoLore;
    public ItemStack createAuctionConfirmNoMaterial;

    public String startingBidItemName;
    public ArrayList<String> startingBidItemLore;
    public ItemStack startingBidItemMaterial;

    public String durationItemName;
    public ArrayList<String> durationItemLore;
    public ItemStack durationItemMaterial;

    public String confirmItemName;
    public ItemStack confirmItemMaterial;
    public ArrayList<String> confirmItemLore;

    public String cancelItemName;
    public ItemStack cancelItemMaterial;
    public ArrayList<String> cancelItemLore;

    public String switchToBinName;
    public ArrayList<String> switchToBinLore;
    public ItemStack switchToBinMaterial;

    public String switchToAuctionName;
    public ArrayList<String> switchToAuctionLore;
    public ItemStack switchToAuctionMaterial;

    public String editBINPriceName;
    public ArrayList<String> editBINPriceLore;
    public ItemStack editBINPriceMaterial;

    private ScriptEngine engine = new ScriptEngineManager(null).getEngineByExtension("js");
    private String formula;
    public Double durationFeeCalculator(int hours){
        if(hours==0)
            return minutesFee;
        try {
            return (Double)engine.eval(formula.replace("x", Integer.toString(hours)));
        }catch(Exception x){
            try {
                return (Double)engine.eval(formula.replace("x", Integer.toString(hours)));
            } catch (ScriptException e) {
                e.printStackTrace();
                return 0d;
            }
        }
    }

    public double minutesFee;
    public double startingBidFee;
    public double startingBidBINFee;

    public ItemStack firstTimeItem;
    public ItemStack secondTimeItem;
    public ItemStack thirdTimeItem;
    public ItemStack forthTimeItem;
    public ItemStack fifthTimeItem;
    public ItemStack customTimeItem;

    public String second;
    public String seconds;
    public String minute;
    public String minutes;
    public String hour;
    public String hours;
    public String day;
    public String days;
    public String short_second;
    public String short_minute;
    public String short_hour;
    public String short_day;

    public long defaultDuration;
    public double defaultStartingBid;

    public ItemStack collectAllMaterial;
    public String collectAllName;
    public ArrayList<String> collectAllLoreOwnAuctions;
    public ArrayList<String> collectAllLoreBids;

    public String searchItemName;
    public ArrayList<String> searchItemLore;
    public ItemStack searchItemMaterial;

    public String manageOwnAuctionsMenuName;

    public String browsingMenuName;

    public String viewAuctionMenuName;
    public int viewAuctionMenuSize;

    public ItemStack bidHistoryDefaultItem;
    public ArrayList<String> bidHistoryItemLoreStructure;

    public ItemStack editBidMaterial;
    public String editBidName;
    public ArrayList<String> editBidLore;

    public ItemStack cantAffordSubmitBidMaterial;
    public String cantAffordSubmitBidName;
    public ArrayList<String> cantAffordSubmitBidLore;

    public ItemStack cantAffordSubmitBuyMaterial;
    public ArrayList<String> cantAffordSubmitBuyLore;

    public ItemStack submitBidMaterial;
    public String submitBidName;
    public ArrayList<String> submitBidLoreNoPreviousBids;
    public ArrayList<String> submitBidLoreWithPreviousBids;

    public ItemStack submitBuyMaterial;
    public String submitBuyName;
    public ArrayList<String> submitBuyLore;

    public ItemStack collectAuctionMaterial;
    public String collectAuctionName;
    public ArrayList<String> collectAuctionCoins;
    public ArrayList<String> collectAuctionItem;

    public ArrayList<String> outbidMessage;

    public String viewPlayerAuctionsMenuName;

    public ItemStack adminNpcCreate;
    public ItemStack adminDebugNames;
    public ItemStack adminManageEndedAuctions;
    public ItemStack adminDelivery;
    public ItemStack adminHelp;
    public ItemStack adminDeliverySendDelivery;
    public ItemStack adminDeliverySetDelivery;
    public ItemStack adminDeliveryRemoveDelivery;
    public ItemStack adminDeliveryCopyDelivery;
    public ItemStack adminDeliveryConfirmDelivery;
    public ItemStack adminDeliveryCancelDelivery;
    public ItemStack adminDeleteWithDelivery;
    public ItemStack adminDeleteWithoutDelivery;
    public ItemStack adminCopyAuction;
    public ItemStack adminForceEndAuction;
    public ItemStack adminEditDurationAuction;
    public void loadAdminItems(){
        adminNpcCreate = new ItemStack(Material.CHEST, 1);
        ItemMeta meta = adminNpcCreate.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("npccreate-item-name")));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("npccreate-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminNpcCreate.setItemMeta(meta);

        adminDebugNames = new ItemStack(Material.FEATHER, 1);
        meta = adminDebugNames.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("npc-debug-item-name")));
        lore=new ArrayList<>();
        for(String line: adminCfg.getStringList("npc-debug-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDebugNames.setItemMeta(meta);

        adminManageEndedAuctions = new ItemStack(Material.REDSTONE_BLOCK, 1);
        meta = adminManageEndedAuctions.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("manage-ended-auctions-item-name")));
        lore=new ArrayList<>();
        for(String line: adminCfg.getStringList("manage-ended-auctions-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminManageEndedAuctions.setItemMeta(meta);

        adminDelivery = new ItemStack(Material.ENDER_CHEST, 1);
        meta = adminDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delivery-item-name")));
        lore=new ArrayList<>();
        for(String line: adminCfg.getStringList("delivery-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDelivery.setItemMeta(meta);

        adminHelp = new ItemStack(Material.PAPER, 1);
        meta = adminHelp.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("help-item-name")));
        lore=new ArrayList<>();
        for(String line: adminCfg.getStringList("help-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminHelp.setItemMeta(meta);

        adminDeliverySendDelivery = new ItemStack(Material.HOPPER, 1);
        meta = adminDeliverySendDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delivery-send-item-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("delivery-send-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeliverySendDelivery.setItemMeta(meta);

        adminDeliverySetDelivery = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        meta = adminDeliverySetDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delivery-set-item-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("delivery-set-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeliverySetDelivery.setItemMeta(meta);

        adminDeliveryRemoveDelivery = new ItemStack(Material.REDSTONE, 2);
        meta = adminDeliveryRemoveDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delivery-remove-item-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("delivery-remove-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeliveryRemoveDelivery.setItemMeta(meta);

        adminDeliveryCopyDelivery = new ItemStack(Material.DRAGON_EGG, 1);
        meta = adminDeliveryCopyDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delivery-item-clone-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("delivery-item-clone-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeliveryCopyDelivery.setItemMeta(meta);

        adminDeliveryConfirmDelivery = confirmItemMaterial.clone();
        meta = adminDeliveryConfirmDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("confirm-action-item-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("confirm-action-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeliveryConfirmDelivery.setItemMeta(meta);

        adminDeliveryCancelDelivery = cancelItemMaterial.clone();
        meta = adminDeliveryCancelDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("cancel-action-item-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("cancel-action-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeliveryCancelDelivery.setItemMeta(meta);

        adminDeleteWithDelivery = new ItemStack(Material.CHEST, 2);
        meta = adminDeleteWithDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delete-auction-item.with-delivery-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("delete-auction-item.with-delivery-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeleteWithDelivery.setItemMeta(meta);

        adminDeleteWithoutDelivery = new ItemStack(Material.ENDER_CHEST, 2);
        meta = adminDeleteWithoutDelivery.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("delete-auction-item.without-delivery-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("delete-auction-item.without-delivery-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminDeleteWithoutDelivery.setItemMeta(meta);

        adminCopyAuction = new ItemStack(Material.DRAGON_EGG, 1);
        meta = adminCopyAuction.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("get-item-copy-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("get-item-copy-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminCopyAuction.setItemMeta(meta);

        adminForceEndAuction = new ItemStack(Material.COMPASS, 2);
        meta = adminForceEndAuction.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("force-end-auction-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("force-end-auction-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminForceEndAuction.setItemMeta(meta);

        adminEditDurationAuction = new ItemStack(customTimeItem.getType(), 1);
        meta = adminEditDurationAuction.getItemMeta();
        meta.setDisplayName(utils.chat(adminCfg.getString("edit-duration-item-name")));
        lore = new ArrayList<>();
        for(String line : adminCfg.getStringList("edit-duration-item-lore"))
            lore.add(utils.chat(line));
        meta.setLore(lore);
        adminEditDurationAuction.setItemMeta(meta);
    }

    public boolean endOwnAuction;

    public ConfigLoad(){

        if(Main.upperVersion) {
            skullItem = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
        }
        else {
            skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        }

        endOwnAuction=Main.plugin.getConfig().getBoolean("use-end-own-auction");

        backgroundGlass=Main.itemConstructor.getItem("160:"+Main.plugin.getConfig().getInt("background-color"),  " ", null);

        mainMenuName=Main.plugin.getConfig().getString("starting-menu-name");
        mainMenuSize=Main.menusCfg.getInt("main-menu.size");

        useBackgoundGlass=Main.plugin.getConfig().getBoolean("use-background-glass");

        goBackName=Main.plugin.getConfig().getString("go-back-item-name");
        goBackLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("go-back-item-lore");
        goBackMaterial=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("go-back-item"));

        previousPageName=Main.plugin.getConfig().getString("previous-page-item-name");
        previousPageLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("previous-page-item-lore");
        previousPageMaterial=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("previous-page-item"));

        nextPageName=Main.plugin.getConfig().getString("next-page-item-name");
        nextPageLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("next-page-item-lore");
        nextPageMaterial=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("next-page-item"));

        closeMenuName=Main.plugin.getConfig().getString("close-menu-item-name");
        closeMenuLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("close-menu-item-lore");
        closeMenuMaterial=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("close-menu-item"));

        browsingMenuItemName=Main.plugin.getConfig().getString("browsing-menu-item-name");
        browsingMenuItemLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("browsing-menu-item-lore");
        browsingMenuItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("browsing-menu-item"));

        viewBidsMenuItemName=Main.plugin.getConfig().getString("view-bids-menu-item-name");
        viewBidsMenuItemLoreWithBids=(ArrayList<String>)Main.plugin.getConfig().getStringList("view-bids-menu-item-lore.with-bids");
        viewBidsMenuItemLoreWithoutBids=(ArrayList<String>)Main.plugin.getConfig().getStringList("view-bids-menu-item-lore.without-bids");
        viewBidsMenuItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("view-bids-menu-item"));

        manageAuctionsItemName=Main.auctionsManagerCfg.getString("manage-menu-item-name");
        manageAuctionsItemLoreWithAuctions=(ArrayList<String>)Main.auctionsManagerCfg.getStringList("manage-menu-item-lore.manage-with-own-auctions");
        manageAuctionsItemLoreWithoutAuctions=(ArrayList<String>)Main.auctionsManagerCfg.getStringList("manage-menu-item-lore.manage-no-own-auctions");
        manageAuctionsItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("manage-menu-item"));

        deliveryMenuName=Main.plugin.getConfig().getString("delivery-menu-name");

        deliveryItemNoName=Main.plugin.getConfig().getString("delivery-item.no-delivery-name");
        deliveryItemYesName=Main.plugin.getConfig().getString("delivery-item.yes-delivery-name");
        deliveryItemNoLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("delivery-item.no-delivery-lore");
        deliveryItemYesLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("delivery-item.yes-delivery-lore");

        mainMenuDeliveryName=Main.plugin.getConfig().getString("delivery-menu-item-name");
        mainMenuDeliveryLore=(ArrayList<String>)Main.plugin.getConfig().getStringList("delivery-menu-item-lore");
        mainMenuDeliveryItem=Main.itemConstructor.getItemFromMaterial(Main.plugin.getConfig().getString("delivery-menu-item"));

        createAuctionMenuName = Main.auctionsManagerCfg.getString("create-menu-name");
        createAuctionMenuSize = Main.menusCfg.getInt("create-auction-menu.size");

        createAuctionConfirmMenuName = Main.auctionsManagerCfg.getString("auction-confirm-menu-name");
        createAuctionConfirmMenuSize = Main.menusCfg.getInt("create-auction-confirm-menu.size");

        createAuctionPreviewNameNoItem = Main.auctionsManagerCfg.getString("preview-no-item-selected-name");
        createAuctionPreviewLoreNoItem = (ArrayList<String>)Main.auctionsManagerCfg.getStringList("preview-no-item-selected-lore");
        createAuctionPreviewItemSelectedLoreAdd = Main.auctionsManagerCfg.getString("preview-selected-item-take-back");
        createAuctionPreviewNameYesItem = Main.auctionsManagerCfg.getString("preview-selected-item-name");
        createAuctionPreviewMaterial = Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("preview-no-item-selected"));

        createAuctionConfirmNoName = Main.auctionsManagerCfg.getString("create-auction-item.no-item-selected-name");
        createAuctionConfirmNoLore = (ArrayList<String>)Main.auctionsManagerCfg.getStringList("create-auction-item.no-item-selected-lore");
        createAuctionConfirmNoMaterial = Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("create-auction-item.no-item-selected-material"));

        createAuctionConfirmYesName = Main.auctionsManagerCfg.getString("create-auction-item.item-selected-name");
        createAuctionConfirmYesLore = (ArrayList<String>)Main.auctionsManagerCfg.getStringList("create-auction-item.item-selected-lore");
        createAuctionConfirmYesMaterial = Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("create-auction-item.item-selected-material"));

        startingBidItemName=Main.auctionsManagerCfg.getString("starting-bid-item-name");
        startingBidItemLore=(ArrayList<String>) Main.auctionsManagerCfg.getStringList("starting-bid-item-lore");
        startingBidItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("starting-bid-item"));

        durationItemName=Main.auctionsManagerCfg.getString("duration-item-name");
        durationItemLore=(ArrayList<String>) Main.auctionsManagerCfg.getStringList("duration-item-lore");
        durationItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("duration-item"));

        confirmItemName=Main.auctionsManagerCfg.getString("auction-confirm-item-name");
        confirmItemLore=(ArrayList<String>) Main.auctionsManagerCfg.getStringList("auction-confirm-item-lore");
        confirmItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("auction-confirm-item-material"));

        cancelItemName=Main.auctionsManagerCfg.getString("auction-cancel-item-name");
        cancelItemLore=(ArrayList<String>) Main.auctionsManagerCfg.getStringList("auction-cancel-item-lore");
        cancelItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("auction-cancel-item-material"));

        second = Main.auctionsManagerCfg.getString("second");
        seconds = Main.auctionsManagerCfg.getString("seconds");
        minute = Main.auctionsManagerCfg.getString("minute");
        minutes = Main.auctionsManagerCfg.getString("minutes");
        hour = Main.auctionsManagerCfg.getString("hour");
        hours = Main.auctionsManagerCfg.getString("hours");
        day = Main.auctionsManagerCfg.getString("day");
        days = Main.auctionsManagerCfg.getString("days");
        short_second = Main.auctionsManagerCfg.getString("short_second");
        short_minute = Main.auctionsManagerCfg.getString("short_minute");
        short_hour = Main.auctionsManagerCfg.getString("short_hour");
        short_day = Main.auctionsManagerCfg.getString("short_day");
        formula = Main.auctionsManagerCfg.getString("extra-fee-formula");

        defaultDuration= utils.toMiliseconds(Main.auctionsManagerCfg.getString("default-starting-duration"));
        defaultStartingBid=Double.parseDouble(Main.auctionsManagerCfg.getString("default-starting-bid"));

        minutesFee=Double.parseDouble(Main.auctionsManagerCfg.getString("extra-fee-minutes"));
        startingBidFee=Double.parseDouble(Main.auctionsManagerCfg.getString("starting-bid-fee-procent"));
        startingBidBINFee=buyItNowCfg.getDouble("buy-it-now-fee");

        double time = Main.menusCfg.getDouble("duration-select-menu.first-item.hours");
        ArrayList<String> lore = new ArrayList<>();
        if(time<1){
            double fractional = time - Math.floor(time);
            fractional*=100;
            int minutes = (int) fractional;
            double firstTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(minutesFee) : minutesFee;
            if(firstTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(firstTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            firstTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.first-item.material")), utils.chat("&a")+minutes+" "+(minutes==1?minute:this.minutes), lore);
        }
        else{
            int hours=(int)time;
            double firstTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(durationFeeCalculator(hours)) : durationFeeCalculator(hours);
            if(firstTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(firstTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            firstTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.first-item.material")), utils.chat("&a")+hours+" "+(hours==1?hour:this.hours), lore);
        }

        time = Main.menusCfg.getDouble("duration-select-menu.second-item.hours");
        lore = new ArrayList<>();
        if(time<1){
            double fractional = time - Math.floor(time);
            fractional*=100;
            int minutes = (int) fractional;
            double secondTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(minutesFee) : minutesFee;
            if(secondTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(secondTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            secondTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.second-item.material")), utils.chat("&a")+minutes+" "+(minutes==1?minute:this.minutes), lore);
        }
        else{
            int hours=(int)time;
            double secondTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(durationFeeCalculator(hours)) : durationFeeCalculator(hours);
            if(secondTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(secondTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            secondTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.second-item.material")), utils.chat("&a")+hours+" "+(hours==1?hour:this.hours), lore);
        }

        time = Main.menusCfg.getDouble("duration-select-menu.third-item.hours");
        lore = new ArrayList<>();
        if(time<1){
            double fractional = time - Math.floor(time);
            fractional*=100;
            int minutes = (int) fractional;
            double thirdTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(minutesFee) : minutesFee;
            if(thirdTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(thirdTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            thirdTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.third-item.material")), utils.chat("&a")+minutes+" "+(minutes==1?minute:this.minutes), lore);
        }
        else{
            int hours=(int)time;
            double thirdTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(durationFeeCalculator(hours)) : durationFeeCalculator(hours);
            if(thirdTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(thirdTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            thirdTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.third-item.material")), utils.chat("&a")+hours+" "+(hours==1?hour:this.hours), lore);
        }

        time = Main.menusCfg.getDouble("duration-select-menu.forth-item.hours");
        lore = new ArrayList<>();
        if(time<1){
            double fractional = time - Math.floor(time);
            fractional*=100;
            int minutes = (int) fractional;
            double forthTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(minutesFee) : minutesFee;
            if(forthTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(forthTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            forthTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.forth-item.material")), utils.chat("&a")+minutes+" "+(minutes==1?minute:this.minutes), lore);
        }
        else{
            int hours=(int)time;
            double forthTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(durationFeeCalculator(hours)) : durationFeeCalculator(hours);
            if(forthTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(forthTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            forthTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.forth-item.material")), utils.chat("&a")+hours+" "+(hours==1?hour:this.hours), lore);
        }

        time = Main.menusCfg.getDouble("duration-select-menu.fifth-item.hours");
        lore = new ArrayList<>();
        if(time<1){
            double fractional = time - Math.floor(time);
            fractional*=100;
            int minutes = (int) fractional;
            double fifthTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(minutesFee) : minutesFee;
            if(fifthTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(fifthTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            fifthTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.fifth-item.material")), utils.chat("&a")+minutes+" "+(minutes==1?minute:this.minutes), lore);
        }
        else{
            int hours=(int)time;
            double fifthTimeFee = Main.numberFormatHelper.useDecimals? Math.floor(durationFeeCalculator(hours)) : durationFeeCalculator(hours);
            if(fifthTimeFee!=0)
                lore.add(utils.chat("&7")+Main.auctionsManagerCfg.getString("extra-fee-message")+utils.chat("&6")+Main.numberFormatHelper.formatNumber(fifthTimeFee));
            lore.add("");
            lore.add(utils.chat(Main.auctionsManagerCfg.getString("duration-selection-lore")));
            fifthTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.fifth-item.material")), utils.chat("&a")+hours+" "+(hours==1?hour:this.hours), lore);
        }

        lore=new ArrayList<>();
        for(String line : Main.auctionsManagerCfg.getStringList("duration-select-item-lore")){
            lore.add(utils.chat(line));
        }
        customTimeItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.menusCfg.getString("duration-select-menu.custom-time-item-material")), utils.chat(Main.auctionsManagerCfg.getString("duration-select-item-name")), lore);

        collectAllMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("collect-all-item"));
        collectAllName=Main.auctionsManagerCfg.getString("collect-all-item-name");
        collectAllLoreOwnAuctions=(ArrayList<String>)Main.auctionsManagerCfg.getStringList("collect-all-item-lore.own-auctions");
        collectAllLoreBids=(ArrayList<String>)Main.auctionsManagerCfg.getStringList("collect-all-item-lore.own-bids");

        manageOwnAuctionsMenuName=Main.auctionsManagerCfg.getString("manage-own-auctions-menu-name");
        browsingMenuName=Main.plugin.getConfig().getString("browsing-menu-name");

        searchItemName=Main.auctionsManagerCfg.getString("search-auction-item-name");
        searchItemLore=(ArrayList<String>)Main.auctionsManagerCfg.getStringList("search-auction-item-lore");
        searchItemMaterial=Main.itemConstructor.getItemFromMaterial(Main.auctionsManagerCfg.getString("search-auction-item"));

        viewAuctionMenuName=Main.bidsRelatedCfg.getString("bids-menu-name");
        viewAuctionMenuSize=Main.menusCfg.getInt("view-auction-menu.size");

        editBidMaterial=Main.itemConstructor.getItemFromMaterial(Main.bidsRelatedCfg.getString("edit-bid-item"));
        editBidName=Main.bidsRelatedCfg.getString("edit-bid-item-name");
        editBidLore=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("edit-bid-item-lore");

        cantAffordSubmitBidMaterial=Main.itemConstructor.getItemFromMaterial(Main.bidsRelatedCfg.getString("submit-bid-cannot-afford-item"));
        cantAffordSubmitBidName=Main.bidsRelatedCfg.getString("submit-bid-cannot-afford-item-name");
        cantAffordSubmitBidLore=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("submit-bid-cannot-afford-item-lore");

        submitBidMaterial=Main.itemConstructor.getItemFromMaterial(Main.bidsRelatedCfg.getString("submit-bid-item"));
        submitBidName=Main.bidsRelatedCfg.getString("submit-bid-item-name");
        submitBidLoreNoPreviousBids=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("submit-bid-item-lore.no-previous-bids");
        submitBidLoreWithPreviousBids=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("submit-bid-item-lore.with-previous-bids");

        collectAuctionMaterial=Main.itemConstructor.getItemFromMaterial(Main.bidsRelatedCfg.getString("collect-bid-item"));
        collectAuctionName=Main.bidsRelatedCfg.getString("collect-bid-item-name");
        collectAuctionCoins=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("collect-bid-item-lore.collect-coins");
        collectAuctionItem=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("collect-bid-item-lore.collect-item");

        lore=new ArrayList<>();
        for(String line : Main.bidsRelatedCfg.getStringList("bid-history-item-lore.no-bids"))
            lore.add(utils.chat(line));
        bidHistoryDefaultItem=Main.itemConstructor.getItem(Main.itemConstructor.getItemFromMaterial(Main.bidsRelatedCfg.getString("bid-history-item")), utils.chat(Main.bidsRelatedCfg.getString("bid-history-item-name")), lore);
        bidHistoryItemLoreStructure=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("bid-history-item-lore.bid-structure");

        outbidMessage=(ArrayList<String>)Main.bidsRelatedCfg.getStringList("outbid-message");

        viewPlayerAuctionsMenuName=Main.plugin.getConfig().getString("player-auction-menu-name");

        blacklistIds=new ArrayList<>();
        for(String item : Main.plugin.getConfig().getStringList("blacklist-item-id")){
            blacklistIds.add(Main.itemConstructor.getItemFromMaterial(item));
        }

        blacklistNames=new ArrayList<>();
        for(String name : Main.plugin.getConfig().getStringList("blacklist-item-name"))
            blacklistNames.add(utils.chat(name));

        blacklistLore=new ArrayList<>();
        for(String key : Main.plugin.getConfig().getConfigurationSection("blacklist-item-lore").getKeys(false)) {
            ArrayList<String> loreToBlacklist = new ArrayList<>();
            for (String line : Main.plugin.getConfig().getStringList("blacklist-item-lore." + key))
                loreToBlacklist.add(line);
            blacklistLore.add(loreToBlacklist);
        }

        loadAdminItems();

        cantAffordSubmitBuyLore=(ArrayList<String>)Main.buyItNowCfg.getStringList("view-auction-buy-now-button.cannot-afford.lore");
        cantAffordSubmitBuyMaterial=Main.itemConstructor.getItemFromMaterial(Main.buyItNowCfg.getString("view-auction-buy-now-button.cannot-afford.material"));

        submitBuyName=Main.buyItNowCfg.getString("view-auction-buy-now-button.name");
        submitBuyMaterial=Main.itemConstructor.getItemFromMaterial(Main.buyItNowCfg.getString("view-auction-buy-now-button.click-to-buy.material"));
        submitBuyLore=(ArrayList<String>)Main.buyItNowCfg.getStringList("view-auction-buy-now-button.click-to-buy.lore");

        switchToAuctionName=Main.buyItNowCfg.getString("switch-button.switch-to-auction.name");
        switchToAuctionLore=(ArrayList<String>)Main.buyItNowCfg.getStringList("switch-button.switch-to-auction.lore");
        switchToAuctionMaterial=Main.itemConstructor.getItemFromMaterial(Main.buyItNowCfg.getString("switch-button.switch-to-auction.material"));

        switchToBinName=Main.buyItNowCfg.getString("switch-button.switch-to-bin.name");
        switchToBinLore=(ArrayList<String>)Main.buyItNowCfg.getStringList("switch-button.switch-to-bin.lore");
        switchToBinMaterial=Main.itemConstructor.getItemFromMaterial(Main.buyItNowCfg.getString("switch-button.switch-to-bin.material"));

        editBINPriceName=Main.buyItNowCfg.getString("buy-it-now-select-price.name");
        editBINPriceLore=(ArrayList<String>)Main.buyItNowCfg.getStringList("buy-it-now-select-price.lore");
        editBINPriceMaterial=Main.itemConstructor.getItemFromMaterial(Main.buyItNowCfg.getString("buy-it-now-select-price.item-id"));
    }
}
