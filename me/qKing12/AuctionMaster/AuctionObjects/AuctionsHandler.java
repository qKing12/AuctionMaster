package me.qKing12.AuctionMaster.AuctionObjects;

import me.qKing12.AuctionMaster.AuctionObjects.Categories.*;
import me.qKing12.AuctionMaster.AuctionMaster;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

import static me.qKing12.AuctionMaster.AuctionMaster.buyItNowCfg;
import static me.qKing12.AuctionMaster.AuctionMaster.utilsAPI;

public class AuctionsHandler {

    public HashMap<String, ArrayList<Auction>> ownAuctions = new HashMap<>();
    public HashMap<String, ArrayList<Auction>> bidAuctions = new HashMap<>();
    public HashMap<String, Auction> auctions = new HashMap<>();
    public HashMap<String, ItemStack> previewItems=new HashMap<>();
    public HashMap<String, Double> startingBid=new HashMap<>();
    public HashMap<String, Integer> startingDuration=new HashMap<>();
    public ArrayList<String> buyItNowSelected;
    public SortingObject sortingObject = new SortingObject();

    public Weapons weapons;
    public Armor armor;
    public Tools tools;
    public Consumables consumables;
    public Blocks blocks;
    public Others others;

    public Global global;

    public AuctionsHandler(){
        if(AuctionMaster.plugin.getConfig().getBoolean("use-global-category")){
            global=new Global();
        }
        if(AuctionMaster.menusCfg.getInt("browsing-menu.weapons-slot")!=-1)
            weapons=new Weapons();
        if(AuctionMaster.menusCfg.getInt("browsing-menu.armor-slot")!=-1)
            armor=new Armor();
        if(AuctionMaster.menusCfg.getInt("browsing-menu.tools-slot")!=-1)
            tools=new Tools();
        if(AuctionMaster.menusCfg.getInt("browsing-menu.consumables-slot")!=-1)
            consumables=new Consumables();
        if(AuctionMaster.menusCfg.getInt("browsing-menu.blocks-slot")!=-1)
            blocks=new Blocks();
        if(AuctionMaster.menusCfg.getInt("browsing-menu.others-slot")!=-1)
            others=new Others();
        if(buyItNowCfg.getBoolean("use-buy-it-now"))
            buyItNowSelected=new ArrayList<>();
    }

    public void createAuction(Auction auction){
        if(ownAuctions.containsKey(auction.getSellerUUID())){
            ownAuctions.get(auction.getSellerUUID()).add(auction);
        }
        else{
            ArrayList<Auction> auctions = new ArrayList<>();
            auctions.add(auction);
            ownAuctions.put(auction.getSellerUUID(), auctions);
        }

        AuctionMaster.auctionsDatabase.addToOwnAuctions(auction.getSellerUUID(), auction.getId());
        AuctionMaster.auctionsDatabase.insertAuction(auction);

        addToBrowse(auction);
        auctions.put(auction.getId(), auction);

        Player p = Bukkit.getPlayerExact(auction.getSellerName());
        if(p!=null) {
            String permission = AuctionMaster.plugin.getConfig().getString("broadcast-new-auction-permission");
            if (AuctionMaster.plugin.getConfig().getBoolean("broadcast-new-auction") && (permission.equals("none") || p.hasPermission(permission))) {
                if (!AuctionMaster.plugin.getConfig().getString("broadcast-new-auction-message").equals("none")) {
                    TextComponent clickMess = new TextComponent();
                    clickMess.setText(utilsAPI.chat(p, AuctionMaster.plugin.getConfig().getString("broadcast-new-auction-message").replace("%seller-username%", p.getName()).replace("%seller-display-name%", p.getDisplayName()).replace("%item-display-name%", auction.getDisplayName()).replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(auction.getCoins()))));
                    clickMess.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ahview " + auction.getId()));
                    Bukkit.spigot().broadcast(clickMess);
                }
                for (String line : AuctionMaster.plugin.getConfig().getStringList("broadcast-commands")) {
                    Bukkit.dispatchCommand(AuctionMaster.plugin.getServer().getConsoleSender(), line.replace("%seller-username%", p.getName()).replace("%seller-display-name%", p.getDisplayName()).replace("%item-display-name%", auction.getDisplayName()).replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(auction.getCoins())));
                }
            }
        }
    }

    public int totalBidsOnOwnAuctions(String uuid){
        int contor=0;
        if(ownAuctions.containsKey(uuid)){
            for(Auction auction : ownAuctions.get(uuid)){
                contor+=auction.getBids().getNumberOfBids();
            }
        }
        return contor;
    }

    public double totalCoinsOnOwnAuctions(String uuid){
        double coins=0;
        if(ownAuctions.containsKey(uuid)){
            for(Auction auction : ownAuctions.get(uuid)){
                coins+=auction.getBids().getTopBidCoins();
            }
        }
        return coins;
    }

    public int totalBidsOnOtherAuctions(String uuid){
        int contor=0;
        if(bidAuctions.containsKey(uuid)){
            contor=bidAuctions.get(uuid).size();
        }
        return contor;
    }

    public int topBidsCount(String uuid){
        int contor=0;
        if(bidAuctions.containsKey(uuid)){
            for(Auction auction : bidAuctions.get(uuid)){
                if(auction.getBids().getTopBidUUID().equals(uuid))
                    contor++;
            }
        }
        return contor;
    }

    public String checkPriority(Auction auction){
        if(weapons!=null) {
            if (weapons.checkPriorityName(auction))
                return "weapons";
        }
        if(armor!=null) {
            if (armor.checkPriorityName(auction))
                return "armor";
        }
        if(tools!=null) {
            if (tools.checkPriorityName(auction))
                return "tools";
        }
        if(consumables!=null) {
            if (consumables.checkPriorityName(auction))
                return "consumables";
        }
        if(blocks!=null) {
            if (blocks.checkPriorityName(auction))
                return "blocks";
        }
        if(others!=null) {
            if (others.checkPriorityName(auction))
                return "others";
        }


        if(weapons!=null) {
            if (weapons.checkPriorityItem(auction.getItemStack()))
                return "weapons";
        }
        if(armor!=null) {
            if (armor.checkPriorityItem(auction.getItemStack()))
                return "armor";
        }
        if(tools!=null) {
            if (tools.checkPriorityItem(auction.getItemStack()))
                return "tools";
        }
        if(consumables!=null) {
            if (consumables.checkPriorityItem(auction.getItemStack()))
                return "consumables";
        }
        if(blocks!=null) {
            if (blocks.checkPriorityItem(auction.getItemStack()))
                return "blocks";
        }
        if(others!=null) {
            if (others.checkPriorityItem(auction.getItemStack()))
                return "others";
        }

        return "";
    }

    public Category getCategory(String fromString){
        if(fromString.equals("weapons"))
            return weapons;
        if(fromString.equals("armor"))
            return armor;
        if(fromString.equals("tools"))
            return tools;
        if(fromString.equals("consumables"))
            return consumables;
        if(fromString.equals("blocks"))
            return blocks;
        if(fromString.equals("others"))
            return others;
        if(fromString.equals("global"))
            return global;
        return null;
    }

    public boolean addToBrowse(Auction auction){
        if(global!=null)
            global.addToCategory(auction);

        if(weapons==null || !weapons.addToCategory(auction))
            if(armor==null || !armor.addToCategory(auction))
                if(tools==null || !tools.addToCategory(auction))
                    if(consumables==null || !consumables.addToCategory(auction))
                        if(blocks==null || !blocks.addToCategory(auction))
                            return (others!=null && others.addToCategory(auction)) || global!=null;


        return true;
    }

    public void removeAuctionFromBrowse(Auction auction){
        if(global!=null)
            global.removeFromCategory(auction);
        if(weapons==null || !weapons.removeFromCategory(auction))
            if(armor==null || !armor.removeFromCategory(auction))
                if(tools==null || !tools.removeFromCategory(auction))
                    if(consumables==null || !consumables.removeFromCategory(auction))
                        if(blocks==null || !blocks.removeFromCategory(auction))
                            if(others!=null)
                                others.removeFromCategory(auction);
    }

}
