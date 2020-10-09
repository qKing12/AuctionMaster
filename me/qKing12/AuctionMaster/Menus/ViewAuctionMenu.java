package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.API.Events.PlaceBidEvent;
import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.Bids;
import me.qKing12.AuctionMaster.InputGUIs.BidSelectGUI.BidSelectGUI;
import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Menus.AdminMenus.ViewAuctionAdminMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.AuctionMaster.*;

public class ViewAuctionMenu {

    private Inventory inventory;
    private Player player;
    private String goBackTo;
    private BukkitTask keepUpdated;
    private Auction auction;
    private boolean ownAuction;
    private double bidAmount;
    private boolean hasCoins;
    private double amountToSkip=0;

    private int cacheBids;

    //case 1 - seller auction ended
    //case 2 - seller auction expired
    //case 3 - top bid auction ended
    //case 4 - just normal bidding
    //case 5 - lost bid coins claim
    private int clickCase;

    public int getMaximumBids(){
        if(AuctionMaster.plugin.getConfig().getBoolean("use-bid-limit")) {
            for (int start = 28; start >= 0; start--)
                if (player.hasPermission("auctionmaster.limit.bids." + start))
                    return start;
        }
        return 28;
    }

    private void goBack(){
        if(goBackTo.equals("ownAuction")){
            if(AuctionMaster.auctionsHandler.ownAuctions.containsKey(player.getUniqueId().toString()))
                new ManageOwnAuctionsMenu(player);
            else
                player.closeInventory();
        }
        else if(goBackTo.equals("ownBids")){
            if(AuctionMaster.auctionsHandler.bidAuctions.containsKey(player.getUniqueId().toString()))
                new ManageOwnBidsMenu(player);
            else
                player.closeInventory();
        }
        else if(goBackTo.equals("Close")){
            player.closeInventory();
        }
        else if(goBackTo.startsWith("browsing_")){
            new BrowsingAuctionsMenu(player, goBackTo.replace("browsing_", ""), 0, null);
        }
        else{
            try {
                new ViewPlayerAuctions(player, goBackTo);
            }catch(Exception x){
                player.closeInventory();
            }
        }
    }

    private void keepUpdated(){
        final int slot= AuctionMaster.menusCfg.getInt("view-auction-menu.auction-display-slot");
        if(!auction.isEnded()) {
            keepUpdated = Bukkit.getScheduler().runTaskTimerAsynchronously(AuctionMaster.plugin, () -> {
                    ItemStack getDisplay = auction.getUpdatedDisplay().clone();
                    ItemMeta meta = getDisplay.getItemMeta();
                    meta.setLore(meta.getLore().subList(0, meta.getLore().size() - 2));
                    getDisplay.setItemMeta(meta);
                    inventory.setItem(slot, getDisplay);
            }, 0, 20);
        }
        else
            inventory.setItem(slot, auction.getUpdatedDisplay());
    }

    private double calculateBidAmount(){
        double bidAmount=auction.getCoins();

        double bidJump = AuctionMaster.bidsRelatedCfg.getDouble("bid-jump");
        if(auction.getBids().getNumberOfBids()!=0 && bidAmount<bidJump)
            bidAmount=bidJump;
        else if(auction.getBids().getNumberOfBids()!=0){
            String toAddString = AuctionMaster.bidsRelatedCfg.getString("bid-step");
            double toAddValue;
            if (toAddString.contains("%")) {
                double percent = Double.parseDouble(toAddString.replace("%", ""));
                toAddValue = bidAmount * percent / 100;
                if (!AuctionMaster.numberFormatHelper.useDecimals)
                    toAddValue = Math.floor(toAddValue);
            } else {
                toAddValue = Double.parseDouble(toAddString);
            }

            bidAmount += toAddValue;
        }

        return bidAmount;
    }

    private boolean submitBidSetup(){
        ItemStack toSubmitSlot;
        ArrayList<String> lore = new ArrayList<>();
        Bids.Bid bid = auction.getLastBid(player.getUniqueId().toString());
        if(auction.isEnded()){
            if(!ownAuction && bid==null){
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(utilsAPI.chat(player, AuctionMaster.bidsRelatedCfg.getString("too-late-to-open-now"))));
                return false;
            }
            else{
                if(ownAuction){
                    if(auction.getBids().getNumberOfBids()==0){
                        for(String line : AuctionMaster.configLoad.collectAuctionItem)
                            lore.add(utilsAPI.chat(player, line));
                        clickCase=2;
                    }
                    else {
                        for (String line : AuctionMaster.configLoad.collectAuctionCoins)
                            lore.add(utilsAPI.chat(player, line.replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(auction.getCoins()))));
                        clickCase=1;
                    }
                }
                else{
                    if(player.getUniqueId().toString().equals(auction.getBids().getTopBidUUID())){
                        for(String line : AuctionMaster.configLoad.collectAuctionItem)
                            lore.add(utilsAPI.chat(player, line));
                        clickCase=3;
                    }
                    else{
                        for (String line : AuctionMaster.configLoad.collectAuctionCoins)
                            lore.add(utilsAPI.chat(player, line.replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(bid.getCoins()))));
                        clickCase=5;
                    }
                }
                toSubmitSlot= itemConstructor.getItem(AuctionMaster.configLoad.collectAuctionMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.collectAuctionName), lore);
            }
        }
        else{
            clickCase=4;
            if(bid==null){
                if(hasCoins= AuctionMaster.economy.hasMoney(player, bidAmount)) {
                    for (String line : AuctionMaster.configLoad.submitBidLoreNoPreviousBids)
                        lore.add(utilsAPI.chat(player, line
                                .replace("%bid-amount%", AuctionMaster.numberFormatHelper.formatNumber(bidAmount))
                        ));
                }
                else{
                    for(String line : AuctionMaster.configLoad.cantAffordSubmitBidLore)
                        lore.add(utilsAPI.chat(player, line
                                .replace("%bid-amount%", numberFormatHelper.formatNumber(bidAmount))
                        ));
                }
            }
            else{
                amountToSkip=bidAmount-bid.getCoins();
                if(hasCoins= AuctionMaster.economy.hasMoney(player, amountToSkip)) {
                    for (String line : AuctionMaster.configLoad.submitBidLoreWithPreviousBids)
                        lore.add(utilsAPI.chat(player, line
                                .replace("%bid-amount%", AuctionMaster.numberFormatHelper.formatNumber(bidAmount))
                                .replace("%previous-bid%", AuctionMaster.numberFormatHelper.formatNumber(bid.getCoins()))
                                .replace("%coins-to-pay%", AuctionMaster.numberFormatHelper.formatNumber(amountToSkip))
                        ));
                }
                else{
                    for(String line : AuctionMaster.configLoad.cantAffordSubmitBidLore)
                        lore.add(utilsAPI.chat(player, line.replace("%bid-amount%", AuctionMaster.numberFormatHelper.formatNumber(bidAmount))));
                }
            }
            if(ownAuction){
                lore.add("");
                lore.add(utilsAPI.chat(player, AuctionMaster.bidsRelatedCfg.getString("own-auction-message")));
            }
            if(hasCoins) {
                toSubmitSlot = itemConstructor.getItem(AuctionMaster.configLoad.submitBidMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.submitBidName), lore);
                toSubmitSlot.setAmount(2);
            }
            else{
                toSubmitSlot=itemConstructor.getItem(AuctionMaster.configLoad.cantAffordSubmitBidMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.cantAffordSubmitBidName), lore);
            }
        }
        inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.place-bid-slot"), toSubmitSlot);
        return true;
    }

    private boolean submitBuyItNowSetup(){
        ItemStack toSubmitSlot;
        ArrayList<String> lore = new ArrayList<>();
        if(auction.isEnded()){
            if(!ownAuction){
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(utilsAPI.chat(player, AuctionMaster.bidsRelatedCfg.getString("too-late-to-open-now"))));
                return false;
            }
            else {
                if (auction.getBids().getNumberOfBids() == 0) {
                    for (String line : AuctionMaster.configLoad.collectAuctionItem)
                        lore.add(utilsAPI.chat(player, line));
                    clickCase = 2;
                } else {
                    for (String line : AuctionMaster.configLoad.collectAuctionCoins)
                        lore.add(utilsAPI.chat(player, line.replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(auction.getCoins()))));
                    clickCase = 1;
                }

                toSubmitSlot = itemConstructor.getItem(AuctionMaster.configLoad.collectAuctionMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.collectAuctionName), lore);
            }
        }
        else{
            clickCase=4;
                if(hasCoins= AuctionMaster.economy.hasMoney(player, bidAmount)) {
                    for (String line : configLoad.submitBuyLore)
                        lore.add(utilsAPI.chat(player, line
                                .replace("%price%", AuctionMaster.numberFormatHelper.formatNumber(bidAmount))
                        ));
                }
                else{
                    for(String line : configLoad.cantAffordSubmitBuyLore)
                        lore.add(utilsAPI.chat(player, line
                                .replace("%price%", numberFormatHelper.formatNumber(bidAmount))
                        ));
                }
            if(ownAuction){
                lore.add("");
                lore.add(utilsAPI.chat(player, AuctionMaster.bidsRelatedCfg.getString("own-auction-message")));
            }
            if(hasCoins) {
                toSubmitSlot = itemConstructor.getItem(AuctionMaster.configLoad.submitBuyMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.submitBuyName), lore);
                toSubmitSlot.setAmount(2);
            }
            else{
                toSubmitSlot=itemConstructor.getItem(configLoad.cantAffordSubmitBuyMaterial, utilsAPI.chat(player, configLoad.submitBuyName), lore);
            }
        }
        inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.buy-it-now-slot"), toSubmitSlot);
        return true;
    }

    private boolean canEndAuction=false;
    private void generateEndOwnAuction(){
        if(AuctionMaster.configLoad.endOwnAuction && !auction.isEnded() && ownAuction){
            String permission = AuctionMaster.plugin.getConfig().getString("use-end-own-auction-permission");
            if(permission.equals("none") || player.hasPermission(permission)){
                canEndAuction=true;
                ArrayList<String> lore = new ArrayList<>();
                for(String line : AuctionMaster.plugin.getConfig().getStringList("end-own-auction-lore")){
                    lore.add(utilsAPI.chat(player, line));
                }
                inventory.setItem(menusCfg.getInt("view-auction-menu.end-own-auction-slot"), itemConstructor.getItem(itemConstructor.getItemFromMaterial(plugin.getConfig().getString("end-own-auction-item")), utilsAPI.chat(player, plugin.getConfig().getString("end-own-auction-name")), lore));
            }
        }
    }

    public ViewAuctionMenu(Player player, Auction auction, String goBackTo, double bidAmount){
        String canAuction = plugin.getConfig().getString("auction-use-permission");
        if(!canAuction.equals("none") && !player.hasPermission(canAuction)){
            player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("auction-no-permission")));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.goBackTo = goBackTo;
            this.auction = auction;
            this.cacheBids = auction.getBids().getNumberOfBids();
            inventory = Bukkit.createInventory(player, AuctionMaster.configLoad.viewAuctionMenuSize, utilsAPI.chat(player, AuctionMaster.configLoad.viewAuctionMenuName));

            if (AuctionMaster.configLoad.useBackgoundGlass)
                for (int i = 0; i < AuctionMaster.configLoad.viewAuctionMenuSize; i++)
                    inventory.setItem(i, AuctionMaster.configLoad.backgroundGlass.clone());

            if (!auction.isBIN()) {
                double calculatedBid = calculateBidAmount();
                if (bidAmount == 0)
                    this.bidAmount = calculatedBid;
                else if (bidAmount < calculatedBid)
                    this.bidAmount = calculatedBid;
                else
                    this.bidAmount = bidAmount;
                if (auction.getSellerUUID().equalsIgnoreCase(player.getUniqueId().toString())) {
                    ownAuction = true;
                } else if (!auction.isEnded() && AuctionMaster.economy.hasMoney(player, this.bidAmount)) {
                    ArrayList<String> lore = new ArrayList<>();
                    for (String line : AuctionMaster.configLoad.editBidLore)
                        lore.add(utilsAPI.chat(player, line
                                .replace("%current-bid%", AuctionMaster.numberFormatHelper.formatNumber(this.bidAmount))
                                .replace("%minimum-bid%", AuctionMaster.numberFormatHelper.formatNumber(calculatedBid))
                        ));
                    inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.bid-amount-change-slot"), itemConstructor.getItem(AuctionMaster.configLoad.editBidMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.editBidName.replace("%current-bid%", AuctionMaster.numberFormatHelper.formatNumber(this.bidAmount)).replace("%minimum-bid%", AuctionMaster.numberFormatHelper.formatNumber(calculatedBid))), lore));
                }

                if (!submitBidSetup()) {
                    inventory = null;
                    return;
                }

                generateEndOwnAuction();

                inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.bid-history-slot"), auction.getBidHistory());

                if (player.hasPermission("auctionmaster.admin")) {
                    ArrayList<String> adminLore = new ArrayList<>();
                    adminLore.add(utils.chat("&7Open this auction"));
                    adminLore.add(utils.chat("&7in admin view!"));
                    inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.admin-view-slot"), itemConstructor.getItem(Material.DRAGON_EGG, utils.chat("&cAdmin View"), adminLore));
                }

                ArrayList<String> lore = new ArrayList<>();
                for (String line : AuctionMaster.configLoad.goBackLore)
                    lore.add(utilsAPI.chat(player, line));
                inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.go-back-slot"), itemConstructor.getItem(AuctionMaster.configLoad.goBackMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.goBackName), lore));
            } else {
                this.bidAmount = auction.getCoins();
                if (auction.getSellerUUID().equalsIgnoreCase(player.getUniqueId().toString()))
                    ownAuction = true;

                if (!submitBuyItNowSetup()) {
                    inventory = null;
                    return;
                }

                generateEndOwnAuction();

            }

            keepUpdated();

            if (player.hasPermission("auctionmaster.admin")) {
                ArrayList<String> adminLore = new ArrayList<>();
                adminLore.add(utils.chat("&7Open this auction"));
                adminLore.add(utils.chat("&7in admin view!"));
                inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.admin-view-slot"), itemConstructor.getItem(Material.DRAGON_EGG, utils.chat("&cAdmin View"), adminLore));
            }

            utils.playSound(player, "auction-view-menu-open");

            ArrayList<String> lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(AuctionMaster.menusCfg.getInt("view-auction-menu.go-back-slot"), itemConstructor.getItem(AuctionMaster.configLoad.goBackMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.goBackName), lore));

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().registerEvents(auction.isBIN() ? new ClickListenBIN() : new ClickListen(), AuctionMaster.plugin);
                player.openInventory(inventory);
            });
        });
    }

    public class ClickListenBIN implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                    return;
                }
                if(e.getClickedInventory().equals(inventory)) {
                    if(e.getSlot() == AuctionMaster.menusCfg.getInt("view-auction-menu.go-back-slot")) {
                        goBack();
                        utils.playSound(player, "go-back-click");
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("view-auction-menu.admin-view-slot")){
                        if(player.hasPermission("auctionmaster.admin"))
                            new ViewAuctionAdminMenu(player, auction, goBackTo);
                    }
                    else if(e.getSlot()==menusCfg.getInt("view-auction-menu.end-own-auction-slot")){
                        if(canEndAuction){
                            double coins = plugin.getConfig().getDouble("end-own-auction-fee");
                            if(economy.removeMoney(player, coins)){
                                if(auction.forceEnd()) {
                                    utils.injectToLog("[Player Force End] Auction with ID=" + auction.getId() + " was ended by seller " + player.getName());
                                    player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("end-own-auction-message")));
                                    player.closeInventory();
                                }
                            }
                            else{
                                player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("end-own-auction-no-money-message")));
                            }
                        }
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("view-auction-menu.buy-it-now-slot")){
                        utils.playSound(player, "auction-submit-bid");
                        if(clickCase==4){
                            if(ownAuction || !hasCoins)
                                return;
                            if(e.getWhoClicked().getInventory().firstEmpty()==-1){
                                player.sendMessage(utils.chat(AuctionMaster.auctionsManagerCfg.getString("not-enough-inventory-space")));
                                return;
                            }
                            if(e.getCurrentItem().getAmount()==2){
                                e.getCurrentItem().setAmount(1);
                            }
                            else {
                                if(auction.placeBid(player, bidAmount, cacheBids)) {
                                    AuctionMaster.economy.removeMoney(player, bidAmount);
                                    player.getInventory().addItem(auction.getItemStack());
                                }
                                else{
                                    player.sendMessage(utilsAPI.chat(player, AuctionMaster.bidsRelatedCfg.getString("too-late-to-open-now")));
                                }
                                goBack();
                            }
                        }
                        else if(clickCase==2){
                            if(player.getInventory().firstEmpty()!=-1){
                                auction.sellerClaim(player);
                                goBack();
                            }
                            else
                                player.sendMessage(utils.chat(AuctionMaster.auctionsManagerCfg.getString("not-enough-inventory-space")));
                        }
                        else if(clickCase==1){
                            auction.sellerClaim(player);
                            goBack();
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory())) {
                if(keepUpdated!=null)
                    keepUpdated.cancel();
                HandlerList.unregisterAll(this);
                inventory = null;
                player = null;
            }
        }
    }

    public class ClickListen implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                    return;
                }
                if(e.getClickedInventory().equals(inventory)) {
                    if(e.getSlot() == AuctionMaster.menusCfg.getInt("view-auction-menu.bid-amount-change-slot")){
                        if(!ownAuction) {
                            utils.playSound(player, "bid-editor-click");
                            BidSelectGUI.selectUpdateBid.openGUI(player, auction, goBackTo, bidAmount);
                        }
                    }
                    else if(e.getSlot() == AuctionMaster.menusCfg.getInt("view-auction-menu.go-back-slot")) {
                        goBack();
                        utils.playSound(player, "go-back-click");
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("view-auction-menu.admin-view-slot")){
                        if(player.hasPermission("auctionmaster.admin"))
                            new ViewAuctionAdminMenu(player, auction, goBackTo);
                    }
                    else if(e.getSlot()==menusCfg.getInt("view-auction-menu.end-own-auction-slot")){
                        if(canEndAuction){
                            double coins = plugin.getConfig().getDouble("end-own-auction-fee");
                            if(economy.removeMoney(player, coins)){
                                if(auction.forceEnd()) {
                                    utils.injectToLog("[Player Force End] Auction with ID=" + auction.getId() + " was ended by seller " + player.getName());
                                    player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("end-own-auction-message")));
                                    player.closeInventory();
                                }
                            }
                            else{
                                player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("end-own-auction-no-money-message")));
                            }
                        }
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("view-auction-menu.place-bid-slot")){
                        utils.playSound(player, "auction-submit-bid");
                        if(clickCase==4){
                            if(ownAuction || !hasCoins)
                                return;
                            if(AuctionMaster.auctionsHandler.bidAuctions.getOrDefault(player.getUniqueId().toString(), new ArrayList<>()).size()>=getMaximumBids()) {
                                player.sendMessage(utilsAPI.chat(player, AuctionMaster.plugin.getConfig().getString("bid-limit-reached-message")));
                                return;
                            }
                            if(e.getCurrentItem().getAmount()!=3) {
                                if (e.getCurrentItem().getAmount() == 2) {
                                    e.getCurrentItem().setAmount(1);
                                } else {
                                    if (AuctionMaster.plugin.getConfig().getBoolean("outbid-yourself") || !player.getUniqueId().toString().equals(auction.getBids().getTopBidUUID())) {
                                        PlaceBidEvent event = new PlaceBidEvent(player, auction, amountToSkip == 0 ? bidAmount : amountToSkip);
                                        Bukkit.getPluginManager().callEvent(event);
                                        if (event.isCancelled())
                                            return;
                                        if (!auction.placeBid(player, bidAmount, cacheBids)) {
                                            new ViewAuctionMenu(player, auction, goBackTo, 0);
                                            for (String line : AuctionMaster.bidsRelatedCfg.getStringList("bid-error-message")) {
                                                player.sendMessage(utilsAPI.chat(player, line));
                                            }
                                        } else {
                                            e.getCurrentItem().setAmount(3);
                                            player.sendMessage(utils.chat(AuctionMaster.plugin.bidsRelatedCfg.getString("placed-bid-message")));
                                            AuctionMaster.economy.removeMoney(player, amountToSkip == 0 ? bidAmount : amountToSkip);
                                            goBack();
                                        }
                                    }
                                }
                            }
                        }
                        else if(clickCase==5){
                            auction.normalBidClaim(player);
                            goBack();
                        }
                        else if(clickCase==3){
                            if(player.getInventory().firstEmpty()!=-1){
                                auction.topBidClaim(player);
                                goBack();
                            }
                            else
                                player.sendMessage(utils.chat(AuctionMaster.auctionsManagerCfg.getString("not-enough-inventory-space")));
                        }
                        else if(clickCase==2){
                            if(player.getInventory().firstEmpty()!=-1){
                                auction.sellerClaim(player);
                                goBack();
                            }
                            else
                                player.sendMessage(utils.chat(AuctionMaster.auctionsManagerCfg.getString("not-enough-inventory-space")));
                        }
                        else if(clickCase==1){
                            auction.sellerClaim(player);
                            goBack();
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory())) {
                if(keepUpdated!=null) {
                    keepUpdated.cancel();
                }
                HandlerList.unregisterAll(this);
                inventory = null;
                player = null;
            }
        }
    }

}
