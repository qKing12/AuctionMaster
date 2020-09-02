package me.qKing12.AuctionMaster.AuctionObjects;

import me.qKing12.AuctionMaster.API.Events.BINPurchaseEvent;
import me.qKing12.AuctionMaster.API.Events.SellerClaimEndedAuctionEvent;
import me.qKing12.AuctionMaster.API.Events.SellerClaimExpiredAuctionEvent;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Utils.utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZonedDateTime;
import java.util.*;

public class AuctionBIN implements Auction{
    String id;
    private double coins;
    private long endingDate;
    private String sellerDisplayName;
    private String sellerName;
    private String sellerUUID;
    private ItemStack item;
    private String displayName;
    private Bids bids;

    @Override
    public boolean isBIN() {
        return true;
    }


    public boolean checkForDeletion(){
        return false;
    }

    public boolean forceEnd() {
        if (isEnded())
            return false;
        endingDate = ZonedDateTime.now().toInstant().toEpochMilli();
        HashMap<String, String> toChange = new HashMap<>();
        toChange.put("ending", String.valueOf(endingDate));
        Main.auctionsDatabase.updateAuctionField(id, toChange);
        return true;
    }

    public void addMinutesToAuction(int minutes){
        endingDate+=minutes*60000;
        HashMap<String, String> toChange = new HashMap<>();
        toChange.put("ending", String.valueOf(endingDate));
        Main.auctionsDatabase.updateAuctionField(id, toChange);
    }

    public void setEndingDate(long date){
        endingDate=date;
        HashMap<String, String> toChange = new HashMap<>();
        toChange.put("ending", String.valueOf(endingDate));
        Main.auctionsDatabase.updateAuctionField(id, toChange);
    }

    public void adminRemoveAuction(boolean withDeliveries){
        if(withDeliveries) {
            if (bids.getNumberOfBids() == 0 || !isEnded())
                Main.deliveries.addItem(sellerUUID, item);
            else
                Main.deliveries.addCoins(sellerUUID, coins);
        }

        Main.auctionsDatabase.removeFromOwnAuctions(sellerUUID, id);
        Main.auctionsHandler.ownAuctions.get(sellerUUID).remove(this);
        if(Main.auctionsHandler.ownAuctions.get(sellerUUID).isEmpty()){
            Main.auctionsHandler.ownAuctions.remove(sellerUUID);
        }

        Main.auctionsDatabase.deleteAuction(id);
        Main.auctionsHandler.removeAuctionFromBrowse(this);
        Main.auctionsHandler.auctions.remove(id);
        bids=null;
        sellerDisplayName=null;
        sellerName=null;
        sellerUUID=null;
        item=null;
        displayName=null;
    }

    public AuctionBIN(String id, double coins, long endingDate, String sellerDisplayName, String sellerName, String sellerUUID, String item, String displayName, String bids, boolean sellerClaimed){
        this.item=utils.itemFromBase64(item);
        if(this.item==null)
            return;
        this.id=id;
        this.coins=coins;
        this.endingDate=endingDate;
        this.sellerDisplayName=sellerDisplayName;
        this.sellerName=sellerName;
        this.sellerUUID=sellerUUID;
        this.displayName=displayName;
        this.bids=new Bids(bids.substring(3), id);
    }

    public AuctionBIN(Player seller, double startingBid, long duration, ItemStack item){
        id=UUID.randomUUID().toString();
        coins=startingBid;
        sellerUUID=seller.getUniqueId().toString();
        sellerName=seller.getName();
        sellerDisplayName=seller.getDisplayName();
        endingDate= ZonedDateTime.now().toInstant().toEpochMilli()+duration;
        this.item=item;
        displayName=utils.getDisplayName(item);
        bids = new Bids(id);
        utils.injectToLog("[BIN Created] "+seller.getName()+" created an buy it now auction for "+displayName+" with ID="+id);
        utils.injectToLog("[Advanced Base64 Item] ^"+seller.getName()+": "+utils.itemToBase64(item).replace("\r\n", "%nll%").replace("\n", "%nll%"));
    }

    public void sellerClaim(Player player){
            if(bids.getNumberOfBids()==0) {
                Bukkit.getScheduler().runTask(Main.plugin, () -> Bukkit.getPluginManager().callEvent(new SellerClaimExpiredAuctionEvent(player, this, item)));
                player.getInventory().addItem(item);
                utils.playSound(player, "claim-item");
            }
            else {
                Bukkit.getScheduler().runTask(Main.plugin, () -> Bukkit.getPluginManager().callEvent(new SellerClaimEndedAuctionEvent(player, this, coins)));
                Main.economy.addMoney(player, coins);
                utils.playSound(player, "claim-money");
            }
            String uuid = player.getUniqueId().toString();
            Main.auctionsHandler.ownAuctions.get(uuid).remove(this);
            if(Main.auctionsHandler.ownAuctions.get(uuid).isEmpty()){
                Main.auctionsHandler.ownAuctions.remove(uuid);
            }
            Main.auctionsDatabase.removeFromOwnAuctions(uuid, id);
        Main.auctionsDatabase.deleteAuction(id);
        Main.auctionsHandler.auctions.remove(id);
    }

    public void claimBid(Player player){

    }

    public void topBidClaim(Player player){

    }

    public void normalBidClaim(Player player){

    }

    public boolean placeBid(Player player, double amount, int cacheBids){
        Bukkit.getScheduler().runTask(Main.plugin, () -> Bukkit.getPluginManager().callEvent(new BINPurchaseEvent(player, this, amount)));
        bids.placeBids(player, amount);
        endingDate=ZonedDateTime.now().toInstant().toEpochMilli()-1000;
        this.cacheBids=1;

        utils.injectToLog("[Bid Place] "+player.getName()+" placed a bid of "+amount+"on buy it now auction with ID="+id);

        HashMap<String, String> fields = new HashMap<>();
        fields.put("coins", String.valueOf(coins));
        fields.put("ending", String.valueOf(endingDate));
        fields.put("bids", "'BIN"+bids.getBidsAsString()+"'");
        Main.auctionsDatabase.updateAuctionField(id, fields);

        Player seller = Bukkit.getPlayer(UUID.fromString(sellerUUID));
        if(seller!=null){
            seller.sendMessage(utils.chat(Main.buyItNowCfg.getString("bought-seller-notification")).replace("%bidder-display-name%", player.getDisplayName()).replace("%bidder-name%", player.getName()).replace("%price%", Main.numberFormatHelper.formatNumber(coins)).replace("%item%", displayName));
        }

        player.sendMessage(utils.chat(Main.buyItNowCfg.getString("bought-item-message").replace("%item%", displayName).replace("%seller-display-name%", sellerDisplayName).replace("%seller-name%", sellerName).replace("%coins%", Main.numberFormatHelper.formatNumber(coins))));

        return true;
    }

    public Bids.Bid getLastBid(String uuid){
        return null;
    }

    public boolean isEnded(){
        return endingDate<=ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public ItemStack getBidHistory(){
        return null;
    }

    public ItemStack generateEndedDisplay(){
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(displayName);
        ArrayList<String> lore = new ArrayList<>();
        if(meta.getLore()!=null)
            lore.addAll(meta.getLore());
        durationLine=lore.size();
        if(bids.getNumberOfBids()==0){
            for(String line : Main.buyItNowCfg.getStringList("buy-it-now-lore.expired")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%price%", Main.numberFormatHelper.formatNumber(coins))
                ));
            }
        }
        else{
            for(String line : Main.buyItNowCfg.getStringList("buy-it-now-lore.bought")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%price%", Main.numberFormatHelper.formatNumber(coins))
                        .replace("%buyer-display-name%", bids.getTopBid())
                ));
            }
        }
        meta.setLore(lore);
        display.setItemMeta(meta);
        endedDisplay=display;
        return display;
    }

    public ItemStack generateDisplay(){
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(displayName);
        ArrayList<String> lore = new ArrayList<>();
        if(meta.getLore()!=null)
            lore.addAll(meta.getLore());
        durationLine=lore.size();
        if(bids.getNumberOfBids()==0){
            int index=0;
            for(String line : Main.buyItNowCfg.getStringList("buy-it-now-lore.on-going")) {
                lore.add(utils.chat(line
                    .replace("%display-name-seller%", sellerDisplayName)
                    .replace("%starting-bid%", Main.numberFormatHelper.formatNumber(coins))
                    .replace("%duration%", utils.fromMilisecondsAuction(endingDate-ZonedDateTime.now().toInstant().toEpochMilli()))
                ));
                if(line.contains("%duration%")){
                    durationLine+=index;
                    durationLineString=utils.chat(line);
                }
                index++;
            }
        }
        this.lore=lore;
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ArrayList<String> lore;
    private String durationLineString;
    private int durationLine;
    private int cacheBids=0;
    private ItemStack endedDisplay;
    public ItemStack getUpdatedDisplay(){
        if(endedDisplay!=null){
            return endedDisplay;
        }
        else if(ZonedDateTime.now().toInstant().toEpochMilli()>=endingDate){
            return generateEndedDisplay();
        }
        else if(cacheBids!=bids.getNumberOfBids() || this.lore==null)
            return generateDisplay();
        else {
            ItemStack updated = item.clone();
            ItemMeta meta = updated.getItemMeta();
            meta.setDisplayName(displayName);
            ArrayList<String> lore = (ArrayList<String>) this.lore.clone();
            lore.set(durationLine, durationLineString.replace("%duration%", utils.fromMilisecondsAuction(endingDate - ZonedDateTime.now().toInstant().toEpochMilli())));
            meta.setLore(lore);
            updated.setItemMeta(meta);
            return updated;
        }
    }

    public String getSellerUUID(){
        return sellerUUID;
    }

    public String getSellerDisplayName() {
        return sellerDisplayName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getId() {
        return id;
    }

    public Bids getBids(){
        return bids;
    }

    public long getEndingDate() {
        return endingDate;
    }

    public ItemStack getItemStack() {
        return item.clone();
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getCoins() {
        return coins;
    }
}
