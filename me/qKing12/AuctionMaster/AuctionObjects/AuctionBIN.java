package me.qKing12.AuctionMaster.AuctionObjects;

import me.qKing12.AuctionMaster.API.Events.BINPurchaseEvent;
import me.qKing12.AuctionMaster.API.Events.SellerClaimEndedAuctionEvent;
import me.qKing12.AuctionMaster.API.Events.SellerClaimExpiredAuctionEvent;
import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        AuctionMaster.auctionsDatabase.updateAuctionField(id, toChange);
        return true;
    }

    public void addMinutesToAuction(int minutes){
        endingDate+=minutes*60000;
        HashMap<String, String> toChange = new HashMap<>();
        toChange.put("ending", String.valueOf(endingDate));
        AuctionMaster.auctionsDatabase.updateAuctionField(id, toChange);
    }

    public void setEndingDate(long date){
        endingDate=date;
        HashMap<String, String> toChange = new HashMap<>();
        toChange.put("ending", String.valueOf(endingDate));
        AuctionMaster.auctionsDatabase.updateAuctionField(id, toChange);
    }

    public void adminRemoveAuction(boolean withDeliveries){
        if(withDeliveries) {
            if (bids.getNumberOfBids() == 0 || !isEnded())
                AuctionMaster.deliveries.addItem(sellerUUID, item);
            else
                AuctionMaster.deliveries.addCoins(sellerUUID, coins);
        }

        AuctionMaster.auctionsDatabase.removeFromOwnAuctions(sellerUUID, id);
        try {
            AuctionMaster.auctionsHandler.ownAuctions.get(sellerUUID).remove(this);
            if (AuctionMaster.auctionsHandler.ownAuctions.get(sellerUUID).isEmpty()) {
                AuctionMaster.auctionsHandler.ownAuctions.remove(sellerUUID);
            }
        }catch(Exception x){
            AuctionMaster.plugin.getLogger().info("Error while trying to delete from seller with id "+sellerUUID+" his BIN Auction");
        }

        AuctionMaster.auctionsDatabase.deleteAuction(id);
        AuctionMaster.auctionsHandler.removeAuctionFromBrowse(this);
        AuctionMaster.auctionsHandler.auctions.remove(id);
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
                Bukkit.getScheduler().runTask(AuctionMaster.plugin, () -> Bukkit.getPluginManager().callEvent(new SellerClaimExpiredAuctionEvent(player, this, item)));
                player.getInventory().addItem(item);
                utils.playSound(player, "claim-item");
            }
            else {
                Bukkit.getScheduler().runTask(AuctionMaster.plugin, () -> Bukkit.getPluginManager().callEvent(new SellerClaimEndedAuctionEvent(player, this, coins)));
                AuctionMaster.economy.addMoney(player, coins);
                utils.playSound(player, "claim-money");
            }
            String uuid = player.getUniqueId().toString();
            AuctionMaster.auctionsHandler.ownAuctions.get(uuid).remove(this);
            if(AuctionMaster.auctionsHandler.ownAuctions.get(uuid).isEmpty()){
                AuctionMaster.auctionsHandler.ownAuctions.remove(uuid);
            }
            AuctionMaster.auctionsDatabase.removeFromOwnAuctions(uuid, id);
        AuctionMaster.auctionsDatabase.deleteAuction(id);
        AuctionMaster.auctionsHandler.auctions.remove(id);
    }

    public void claimBid(Player player){

    }

    public void topBidClaim(Player player){

    }

    public void normalBidClaim(Player player){

    }

    public boolean placeBid(Player player, double amount, int cacheBids){
        if(isEnded() || bids==null)
            return false;
        Bukkit.getScheduler().runTask(AuctionMaster.plugin, () -> Bukkit.getPluginManager().callEvent(new BINPurchaseEvent(player, this, amount)));
        bids.placeBids(player, amount);
        endingDate=ZonedDateTime.now().toInstant().toEpochMilli()-1000;
        this.cacheBids=1;

        utils.injectToLog("[Bid Place] "+player.getName()+" placed a bid of "+amount+"on buy it now auction with ID="+id);

        HashMap<String, String> fields = new HashMap<>();
        fields.put("coins", String.valueOf(coins));
        fields.put("ending", String.valueOf(endingDate));
        fields.put("bids", "'BIN"+bids.getBidsAsString()+"'");
        AuctionMaster.auctionsDatabase.updateAuctionField(id, fields);

        Player seller = Bukkit.getPlayer(UUID.fromString(sellerUUID));
        if(seller!=null){
            seller.sendMessage(utils.chat(AuctionMaster.buyItNowCfg.getString("bought-seller-notification")).replace("%bidder-display-name%", player.getDisplayName()).replace("%bidder-name%", player.getName()).replace("%price%", AuctionMaster.numberFormatHelper.formatNumber(coins)).replace("%item%", displayName));
        }

        player.sendMessage(utils.chat(AuctionMaster.buyItNowCfg.getString("bought-item-message").replace("%item%", displayName).replace("%seller-display-name%", sellerDisplayName).replace("%seller-name%", sellerName).replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(coins))));

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
            for(String line : AuctionMaster.buyItNowCfg.getStringList("buy-it-now-lore.expired")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%price%", AuctionMaster.numberFormatHelper.formatNumber(coins))
                ));
            }
        }
        else{
            for(String line : AuctionMaster.buyItNowCfg.getStringList("buy-it-now-lore.bought")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%price%", AuctionMaster.numberFormatHelper.formatNumber(coins))
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
            for(String line : AuctionMaster.buyItNowCfg.getStringList("buy-it-now-lore.on-going")) {
                lore.add(utils.chat(line
                    .replace("%display-name-seller%", sellerDisplayName)
                    .replace("%starting-bid%", AuctionMaster.numberFormatHelper.formatNumber(coins))
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
            return endedDisplay.clone();
        }
        else if(ZonedDateTime.now().toInstant().toEpochMilli()>=endingDate){
            return generateEndedDisplay().clone();
        }
        else if(cacheBids!=bids.getNumberOfBids() || this.lore==null)
            return generateDisplay().clone();
        else {
            try {
                ItemStack updated = item.clone();
                ItemMeta meta = updated.getItemMeta();
                meta.setDisplayName(displayName);
                ArrayList<String> lore = (ArrayList<String>) this.lore.clone();
                lore.set(durationLine, durationLineString.replace("%duration%", utils.fromMilisecondsAuction(endingDate - ZonedDateTime.now().toInstant().toEpochMilli())));
                meta.setLore(lore);
                updated.setItemMeta(meta);
                return updated;
            }catch(Exception x){
                return generateDisplay().clone();
            }
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
