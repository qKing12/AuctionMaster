package me.qKing12.AuctionMaster.AuctionObjects.Categories;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static me.qKing12.AuctionMaster.Main.othersCfg;

public class Others implements Category{

    ArrayList<Auction> orderedAuctionsMoney = new ArrayList<>();
    ArrayList<Auction> orderedAuctionsTime = new ArrayList<>();
    ArrayList<Auction> orderedAuctionsBids = new ArrayList<>();

    private ItemStack backgroundGlass;
    private int slot;
    private ItemStack displayCategoryItem;
    private ItemStack displayCategoryItemSelected;

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public ItemStack getDisplay() {
        return displayCategoryItem;
    }

    @Override
    public ItemStack getDisplayCategoryItemSelected(){
        return displayCategoryItemSelected;
    }

    @Override
    public ItemStack getBackgroundGlass() {
        return backgroundGlass;
    }

    @Override
    public ArrayList<Auction> getAuctionsBids() {
        return orderedAuctionsBids;
    }

    @Override
    public ArrayList<Auction> getAuctionsCoins() {
        return orderedAuctionsMoney;
    }

    @Override
    public ArrayList<Auction> getAuctionsTime() {
        return orderedAuctionsTime;
    }

    private ArrayList<ItemStack> priorityIds = new ArrayList<>();

    public ArrayList<Auction> getAuctions(Player p){
        int sortIndex = Main.auctionsHandler.sortingObject.getSortIndex(p);
        if(sortIndex==0){
            ArrayList<Auction> auctions = (ArrayList<Auction>)orderedAuctionsMoney.clone();
            Collections.reverse(auctions);
            return auctions;
        }
        else if(sortIndex==1){
            return orderedAuctionsMoney;
        }
        else if(sortIndex==2){
            return orderedAuctionsTime;
        }
        else if(sortIndex==3){
            return orderedAuctionsBids;
        }
        return null;
    }

    public Others(){
        for (String line : othersCfg.getStringList("custom-item-ids")) {
            priorityIds.add(Main.itemConstructor.getItemFromMaterial(line));
        }
        backgroundGlass=Main.itemConstructor.getItemFromMaterial("160:"+Main.plugin.getConfig().getString("others-menu-color"));
        ItemMeta meta = backgroundGlass.getItemMeta();
        meta.setDisplayName(" ");
        backgroundGlass.setItemMeta(meta);

        displayCategoryItem=Main.itemConstructor.getItemFromMaterial(othersCfg.getString("others-menu-item"));
        meta = displayCategoryItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(utils.chat(othersCfg.getString("others-menu-name")));
        ArrayList<String> lore = new ArrayList<>();
        for (String line : othersCfg.getStringList("others-menu-lore"))
            lore.add(utils.chat(line));
        lore.add(" ");
        lore.add(utils.chat(Main.plugin.getConfig().getString("category-no-browsing")));
        meta.setLore(lore);
        displayCategoryItem.setItemMeta(meta);

        lore.set(lore.size()-1, utils.chat(Main.plugin.getConfig().getString("category-browsing")));
        meta.setLore(lore);
        displayCategoryItemSelected=displayCategoryItem.clone();
        displayCategoryItemSelected.setItemMeta(meta);
        displayCategoryItemSelected.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        slot=Main.menusCfg.getInt("browsing-menu.others-slot");
        slot--;
        slot*=9;
    }

    public void sort(){
        orderedAuctionsMoney.sort(Comparator.comparing(Auction::getCoins));
        orderedAuctionsBids.sort(Comparator.comparing(Auction -> Auction.getBids().getNumberOfBids()));
        orderedAuctionsTime.sort(Comparator.comparing(Auction::getEndingDate));
        Collections.reverse(orderedAuctionsBids);
    }

    public ArrayList<Auction> getOrderedAuctionsBids() {
        return orderedAuctionsBids;
    }

    public ArrayList<Auction> getOrderedAuctionsMoney() {
        return orderedAuctionsMoney;
    }

    public ArrayList<Auction> getOrderedAuctionsTime() {
        return orderedAuctionsTime;
    }

    public boolean checkPriorityName(Auction auction){
        String name=auction.getDisplayName().replace("§", "&");
        return othersCfg.getStringList("custom-item-names").contains(name);
    }

    public boolean checkPriorityItem(ItemStack item){
        if(Main.upperVersion){
            for(ItemStack itemToCheck : priorityIds)
                if(itemToCheck.getType().equals(item.getType()))
                    return true;
        }
        else{
            for(ItemStack itemToCheck : priorityIds){
                if(itemToCheck.getType().equals(item.getType()) && itemToCheck.getData().equals(item.getData()))
                    return true;
            }
        }
        return false;
    }

    public boolean addToCategory(Auction auction) {
        String priority = Main.auctionsHandler.checkPriority(auction);
        if (priority.equals("others"))
            return true;
        else if (!priority.equals("")) {
            return false;
        }

        if(!orderedAuctionsBids.contains(auction)) {
            orderedAuctionsBids.add(auction);
            orderedAuctionsMoney.add(auction);
            orderedAuctionsTime.add(auction);
            sort();
        }
        return true;
    }

    public boolean removeFromCategory(Auction auction){
        if(orderedAuctionsBids.contains(auction)) {
            orderedAuctionsBids.remove(auction);
            orderedAuctionsMoney.remove(auction);
            orderedAuctionsTime.remove(auction);
            return true;
        }
        return false;
    }
}
