package me.qKing12.AuctionMaster.AuctionObjects;

import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class SortingObject {
    private HashMap<Player, Integer> sortCache = new HashMap<>();
    private HashMap<Player, Integer> sortBINCache = new HashMap<>();

    private ItemStack sortItem;
    private ItemStack sortItemBIN;

    private String highestBid;
    private String lowestBid;
    private String endingSoon;
    private String mostBids;
    private String clickToSwitch;

    private String allAuctions;
    private String binOnly;
    private String auctionsOnly;
    private String clickToSwitchBIN;

    public SortingObject(){
        sortItem= AuctionMaster.itemConstructor.getItem(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item"), utils.chat(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item-name")), null);
        sortItemBIN= AuctionMaster.itemConstructor.getItem(AuctionMaster.buyItNowCfg.getString("sort-item.material"), utils.chat(AuctionMaster.buyItNowCfg.getString("sort-item.name")),null);

        highestBid=utils.chat(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item-sorting.highest-bid"));
        lowestBid=utils.chat(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item-sorting.lowest-bid"));
        endingSoon=utils.chat(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item-sorting.ending-soon"));
        mostBids=utils.chat(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item-sorting.most-bids"));
        clickToSwitch=utils.chat(AuctionMaster.auctionsManagerCfg.getString("sort-auction-item-sorting.click-to-switch"));

        allAuctions=utils.chat(AuctionMaster.buyItNowCfg.getString("sort-item.show-all"));
        binOnly=utils.chat(AuctionMaster.buyItNowCfg.getString("sort-item.bin-only"));
        auctionsOnly=utils.chat(AuctionMaster.buyItNowCfg.getString("sort-item.auctions-only"));
        clickToSwitchBIN=utils.chat(AuctionMaster.buyItNowCfg.getString("sort-item.click-to-switch"));

    }

    public ItemStack getSortItem(Player p){
        int sort = sortCache.getOrDefault(p, 0);
        ItemStack toReturn = sortItem.clone();
        ItemMeta meta =toReturn.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add((sort==0?utils.chat("&b► "):utils.chat("&7"))+highestBid);
        lore.add((sort==1?utils.chat("&b► "):utils.chat("&7"))+lowestBid);
        lore.add((sort==2?utils.chat("&b► "):utils.chat("&7"))+endingSoon);
        lore.add((sort==3?utils.chat("&b► "):utils.chat("&7"))+mostBids);
        lore.add("");
        lore.add(clickToSwitch);
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    public ItemStack getSortItemBIN(Player p){
        int sort = sortBINCache.getOrDefault(p, 0);
        ItemStack toReturn = sortItemBIN.clone();
        ItemMeta meta =toReturn.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add((sort==0?utils.chat("&3► "):utils.chat("&7"))+allAuctions);
        lore.add((sort==1?utils.chat("&3► "):utils.chat("&7"))+auctionsOnly);
        lore.add((sort==2?utils.chat("&3► "):utils.chat("&7"))+binOnly);
        lore.add("");
        lore.add(clickToSwitchBIN);
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    public void changeSort(Player p){
        int sort=sortCache.getOrDefault(p, 0);
        if(sort==3)
            sortCache.put(p, 0);
        else
            sortCache.put(p, sort+1);
    }

    public void changeSortBIN(Player p){
        int sort=sortBINCache.getOrDefault(p, 0);
        if(sort==2)
            sortBINCache.put(p, 0);
        else
            sortBINCache.put(p, sort+1);
    }

    public int getSortIndex(Player p){
        return sortCache.getOrDefault(p, 0);
    }

    public int getSortIndexBIN(Player p){
        return sortBINCache.getOrDefault(p, 0);
    }

}
