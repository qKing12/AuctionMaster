package me.qKing12.AuctionMaster.AuctionObjects;

import me.qKing12.AuctionMaster.API.Events.LostBidClaim;
import me.qKing12.AuctionMaster.API.Events.SellerClaimEndedAuctionEvent;
import me.qKing12.AuctionMaster.API.Events.SellerClaimExpiredAuctionEvent;
import me.qKing12.AuctionMaster.API.Events.TopBidClaim;
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

public class AuctionClassic implements Auction{
    String id;
    private double coins;
    private long endingDate;
    private String sellerDisplayName;
    private String sellerName;
    private String sellerUUID;
    private ItemStack item;
    private String displayName;
    private Bids bids;

    public boolean isBIN(){
        return false;
    }

    private boolean sellerClaimed=false;

    public boolean checkForDeletion(){
        if(sellerClaimed && bids.allBidsClaimed()){
            Main.auctionsDatabase.deleteAuction(id);
            Main.auctionsHandler.auctions.remove(id);
            return true;
        }
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
        if(withDeliveries && !sellerClaimed) {
            if (bids.getNumberOfBids() == 0 || !isEnded())
                Main.deliveries.addItem(sellerUUID, item);
            else
                Main.deliveries.addCoins(sellerUUID, coins);
        }
        HashMap<String, Double> cache = new HashMap<>();
        for(Bids.Bid bid : bids.getBidList()){
            if(!bid.isClaimed() && !cache.containsKey(bid.getBidderUUID())) {
                Main.auctionsHandler.bidAuctions.get(bid.getBidderUUID()).remove(this);
                if (Main.auctionsHandler.bidAuctions.get(bid.getBidderUUID()).isEmpty())
                    Main.auctionsHandler.bidAuctions.remove(bid.getBidderUUID());
            }
            cache.put(bid.getBidderUUID(), bid.getCoins());
        }
        for(String uuid : cache.keySet()){
            Main.auctionsDatabase.removeFromOwnBids(uuid, id);
        }
        if(withDeliveries){
            if(bids.getNumberOfBids()!=0 && isEnded())
                cache.put(bids.getTopBidUUID(), -1d);
            for(Map.Entry<String, Double> entry : cache.entrySet()){
                if(entry.getValue()==-1)
                    Main.deliveries.addItem(entry.getKey(), item);
                Main.deliveries.addCoins(entry.getKey(), entry.getValue());
            }
        }

        if(!sellerClaimed) {
            Main.auctionsDatabase.removeFromOwnAuctions(sellerUUID, id);
            Main.auctionsHandler.ownAuctions.get(sellerUUID).remove(this);
            if (Main.auctionsHandler.ownAuctions.get(sellerUUID).isEmpty()) {
                Main.auctionsHandler.ownAuctions.remove(sellerUUID);
            }
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

    public AuctionClassic(String id, double coins, long endingDate, String sellerDisplayName, String sellerName, String sellerUUID, String item, String displayName, String bids, boolean sellerClaimed){
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
        this.sellerClaimed=sellerClaimed;
        this.bids=new Bids(bids, id);
    }

    public AuctionClassic(Player seller, double startingBid, long duration, ItemStack item){
        id=UUID.randomUUID().toString();
        coins=startingBid;
        sellerUUID=seller.getUniqueId().toString();
        sellerName=seller.getName();
        sellerDisplayName=seller.getDisplayName();
        endingDate= ZonedDateTime.now().toInstant().toEpochMilli()+duration;
        this.item=item;
        displayName=utils.getDisplayName(item);
        bids = new Bids(id);
        utils.injectToLog("[Auction Created] "+seller.getName()+" created an classic auction for "+displayName+" with ID="+id);
        utils.injectToLog("[Advanced Base64 Item] ^"+seller.getName()+": "+utils.itemToBase64(item).replace("\r\n", "%nll%").replace("\n", "%nll%"));
    }

    public void sellerClaim(Player player){
        if(!sellerClaimed){
            sellerClaimed=true;
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
            if(!checkForDeletion()){
                HashMap<String, String> toChange = new HashMap<>();
                toChange.put("sellerClaimed", "1");
                Main.auctionsDatabase.updateAuctionField(id, toChange);
            }
        }
    }

    public void claimBid(Player player){
        if(bids.getTopBidUUID().equalsIgnoreCase(player.getUniqueId().toString())) {
            topBidClaim(player);
            utils.playSound(player, "claim-item");
        }
        else {
            utils.playSound(player, "claim-money");
            normalBidClaim(player);
        }
    }

    public void topBidClaim(Player player){
        if(!bids.getBidList().get(bids.getBidList().size()-1).isClaimed()) {
            bids.claimBid(player);
            Bukkit.getScheduler().runTask(Main.plugin, () -> Bukkit.getPluginManager().callEvent(new TopBidClaim(player, this, item)));
            player.getInventory().addItem(item);
            String uuid = player.getUniqueId().toString();
            Main.auctionsDatabase.removeFromOwnBids(uuid, id);
            Main.auctionsHandler.bidAuctions.get(uuid).remove(this);
            if(Main.auctionsHandler.bidAuctions.get(uuid).isEmpty()){
                Main.auctionsHandler.bidAuctions.remove(uuid);
            }
            if(!checkForDeletion()){
                HashMap<String, String> toChange = new HashMap<>();
                toChange.put("bids", "'"+bids.getBidsAsString()+"'");
                Main.auctionsDatabase.updateAuctionField(id, toChange);
            }
        }
    }

    public void normalBidClaim(Player player){
        double coins = bids.claimBid(player);
        Bukkit.getScheduler().runTask(Main.plugin, () -> Bukkit.getPluginManager().callEvent(new LostBidClaim(player, this, coins)));
        Main.economy.addMoney(player, coins);
        Main.auctionsDatabase.removeFromOwnBids(player.getUniqueId().toString(), id);
        String uuid = player.getUniqueId().toString();
        Main.auctionsHandler.bidAuctions.get(uuid).remove(this);
        if(Main.auctionsHandler.bidAuctions.get(uuid).isEmpty()){
            Main.auctionsHandler.bidAuctions.remove(uuid);
        }
        if(!checkForDeletion()){
            HashMap<String, String> toChange = new HashMap<>();
            toChange.put("bids", "'"+bids.getBidsAsString()+"'");
            Main.auctionsDatabase.updateAuctionField(id, toChange);
        }
    }

    public boolean placeBid(Player player, double amount, int cacheBids){
        if(cacheBids!=bids.getNumberOfBids()) {
            return false;
        }

        utils.injectToLog("[Bid Place] "+player.getName()+" placed a bid of "+amount+"on classic auction with ID="+id);

        endingDate+=Main.plugin.getConfig().getInt("add-time-to-auction")*1000;
        coins=amount;
        if(Main.auctionsHandler.bidAuctions.containsKey(player.getUniqueId().toString())){
            if(getLastBid(player.getUniqueId().toString())==null) {
                Main.auctionsHandler.bidAuctions.get(player.getUniqueId().toString()).add(this);
                Main.auctionsDatabase.addToOwnBids(player.getUniqueId().toString(), id);
            }
        }
        else{
            ArrayList<Auction> auctions = new ArrayList<>();
            auctions.add(this);
            Main.auctionsHandler.bidAuctions.put(player.getUniqueId().toString(), auctions);
            Main.auctionsDatabase.addToOwnBids(player.getUniqueId().toString(), id);
        }
        bids.placeBids(player, amount);

        HashMap<String, String> fields = new HashMap<>();
        fields.put("coins", String.valueOf(coins));
        fields.put("ending", String.valueOf(endingDate));
        fields.put("bids", "'"+bids.getBidsAsString()+"'");
        Main.auctionsDatabase.updateAuctionField(id, fields);

        Player seller = Bukkit.getPlayer(UUID.fromString(sellerUUID));
        if(seller!=null){
            seller.sendMessage(utils.chat(Main.bidsRelatedCfg.getString("bid-message")).replace("%bidder%", player.getDisplayName()).replace("%bid-amount%", Main.numberFormatHelper.formatNumber(amount)).replace("%bid-item%", displayName));
        }

        ArrayList<Player> outbidPlayers = new ArrayList<>();
        for(Bids.Bid bid : bids.getBidList()){
            Player bidder = Bukkit.getPlayer(UUID.fromString(bid.getBidderUUID()));
            if(bidder!=null && !outbidPlayers.contains(bidder) && !bidder.equals(player))
                outbidPlayers.add(bidder);
        }
        String outbidMsg = "";
        for (String msj : Main.configLoad.outbidMessage)
            outbidMsg=outbidMsg+utils.chat(msj).replace("%outbid-player-display-name%", player.getDisplayName()).replace("%top-bid%", Main.numberFormatHelper.formatNumber(coins)).replace("%bid-item%", displayName)+"\n";
        TextComponent clickMess = new TextComponent();
        clickMess.setText(outbidMsg.substring(0, outbidMsg.length()-1));
        clickMess.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ahview "+id));
        for(Player p : outbidPlayers){
            utils.playSound(p, "outbid");
            p.spigot().sendMessage(clickMess);
        }

        return true;
    }

    public Bids.Bid getLastBid(String uuid){
        ListIterator<Bids.Bid> bidIterator = bids.getBidList().listIterator(bids.getBidList().size());
        while(bidIterator.hasPrevious()){
            Bids.Bid bid = bidIterator.previous();
            if(bid.getBidderUUID().equals(uuid))
                return bid;
        }
        return null;
    }

    public boolean isEnded(){
        return endingDate<=ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public ItemStack getBidHistory(){
        ItemStack toReturn=Main.configLoad.bidHistoryDefaultItem.clone();

        int numberOfBids=bids.getNumberOfBids();
        if(numberOfBids!=0){
            ArrayList<String> lore = new ArrayList<>();
            int toSkip=0;
            if(numberOfBids>7)
                toSkip=numberOfBids-7;
            Iterator<Bids.Bid> bidsIterator = bids.getBidList().iterator();
            for(int i=0;i<toSkip;i++)
                bidsIterator.next();

            while(bidsIterator.hasNext()){
                Bids.Bid bid = bidsIterator.next();
                for(String line : Main.configLoad.bidHistoryItemLoreStructure){
                    lore.add(utils.chat(line
                        .replace("%bid-amount%", Main.numberFormatHelper.formatNumber(bid.getCoins()))
                        .replace("%bidder-display-name%", bid.getBidderDisplayName())
                        .replace("%when-bidded%", utils.fromMiliseconds((int)(ZonedDateTime.now().toInstant().toEpochMilli()-bid.getBidDate())))
                    ));
                }
            }
            ItemMeta meta = toReturn.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            meta.setLore(lore);
            toReturn.setItemMeta(meta);
        }

        return toReturn;
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
            for(String line : Main.auctionsManagerCfg.getStringList("auction-item-lore.auction-item-expired")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%starting-bid%", Main.numberFormatHelper.formatNumber(coins))
                ));
            }
        }
        else{
            for(String line : Main.auctionsManagerCfg.getStringList("auction-item-lore.auction-item-ended")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%starting-bid%", Main.numberFormatHelper.formatNumber(coins))
                        .replace("%top-bid%", Main.numberFormatHelper.formatNumber(bids.getTopBidCoins()))
                        .replace("%top-bid-player%", bids.getTopBid())
                        .replace("%bids%", String.valueOf(bids.getNumberOfBids()))
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
            for(String line : Main.auctionsManagerCfg.getStringList("auction-item-lore.auction-item-no-bid")) {
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
        else{
            int index=0;
            for(String line : Main.auctionsManagerCfg.getStringList("auction-item-lore.auction-item-with-bids")) {
                lore.add(utils.chat(line
                        .replace("%display-name-seller%", sellerDisplayName)
                        .replace("%starting-bid%", Main.numberFormatHelper.formatNumber(coins))
                        .replace("%duration%", utils.fromMilisecondsAuction(endingDate-ZonedDateTime.now().toInstant().toEpochMilli()))
                        .replace("%top-bid%", Main.numberFormatHelper.formatNumber(bids.getTopBidCoins()))
                        .replace("%top-bid-player%", bids.getTopBid())
                        .replace("%bids%", String.valueOf(bids.getNumberOfBids()))
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
        this.cacheBids=bids.getNumberOfBids();
        return display;
    }

    private ArrayList<String> lore;
    private String durationLineString;
    private int durationLine;
    private int cacheBids;
    private ItemStack endedDisplay;
    public ItemStack getUpdatedDisplay(){
        if(endedDisplay!=null){
            return endedDisplay.clone();
        }
        else if(ZonedDateTime.now().toInstant().toEpochMilli()>=endingDate){
            return generateEndedDisplay().clone();
        }
        else if(cacheBids!=bids.getNumberOfBids() || this.lore==null) {
            return generateDisplay().clone();
        }
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
