package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.Categories.*;
import me.qKing12.AuctionMaster.InputGUIs.SearchGUI.SearchGUI;
import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static me.qKing12.AuctionMaster.AuctionMaster.*;

public class BrowsingAuctionsMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private Category category;
    private String categoryString;
    private int page;
    private String search;

    private HashMap<Integer, Auction> auctions = new HashMap<>();

    private BukkitTask keepUpdated;

    private boolean searchVerify(String displayName){
        return ChatColor.stripColor(displayName.toLowerCase()).contains(search);
    }

    private void keepUpdated(){
        keepUpdated=Bukkit.getScheduler().runTaskTimerAsynchronously(AuctionMaster.plugin, () -> {
            Iterator<Map.Entry<Integer, Auction>> auction = auctions.entrySet().iterator();
            while(auction.hasNext()){
                Map.Entry<Integer, Auction> entry=auction.next();
                inventory.setItem(entry.getKey(), entry.getValue().getUpdatedDisplay());
            }
        }, 20, 20);
    }

    private void setupPreviousPage(){
        ArrayList<String> lore = new ArrayList<>();
        for(String line : AuctionMaster.configLoad.previousPageLore)
            lore.add(utilsAPI.chat(player, line.replace("%page-number%", String.valueOf(page))));

        inventory.setItem(46, itemConstructor.getItem(AuctionMaster.configLoad.previousPageMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.previousPageName.replace("%page-number%", String.valueOf(page))), lore));
    }

    private void setupNextPage(){
        ArrayList<String> lore = new ArrayList<>();
        for(String line : AuctionMaster.configLoad.nextPageLore)
            lore.add(utilsAPI.chat(player, line.replace("%page-number%", String.valueOf(page))));

        inventory.setItem(53, itemConstructor.getItem(AuctionMaster.configLoad.nextPageMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.nextPageName.replace("%page-number%", String.valueOf(page))), lore));
    }


    private interface CheckForSort{
        boolean checkForBINSort(Auction auction);
    }

    private CheckForSort checkForSort;

    private void loadAuctions(boolean binUpdate){
        auctions.clear();

        ArrayList<Auction> auctions=this.category.getAuctions(player);
        Iterator<Auction> auctionIterator=auctions.iterator();

        int toSkip=page*24;

        int currentSlot = 11;
        if(search==null) {
            while (auctionIterator.hasNext() && currentSlot < 44) {
                Auction auction = auctionIterator.next();
                if(auction.isEnded())
                    category.removeFromCategory(auction);
                else if(checkForSort.checkForBINSort(auction)){
                    if(toSkip!=0){
                        toSkip--;
                        continue;
                    }
                    this.auctions.put(currentSlot, auction);
                    inventory.setItem(currentSlot, auction.getUpdatedDisplay());
                    if (currentSlot % 9 == 7)
                        currentSlot += 4;
                    else
                        currentSlot++;
                }
            }
        }
        else{
            while (auctionIterator.hasNext() && currentSlot < 44) {
                Auction auction = auctionIterator.next();
                if(auction.isEnded())
                    category.removeFromCategory(auction);
                else if(checkForSort.checkForBINSort(auction)){
                    if (searchVerify(auction.getDisplayName())) {
                        if(toSkip!=0){
                            toSkip--;
                            continue;
                        }
                        this.auctions.put(currentSlot, auction);
                        inventory.setItem(currentSlot, auction.getUpdatedDisplay());
                        if (currentSlot % 9 == 7)
                            currentSlot += 4;
                        else
                            currentSlot++;
                    }
                }
            }
        }

        if(binUpdate){
            do{
                inventory.setItem(currentSlot, new ItemStack(Material.AIR));
            }while(currentSlot<44 && inventory.getItem(currentSlot)!=null);
        }

        if(page!=0)
            setupPreviousPage();
        else
            inventory.setItem(46, category.getBackgroundGlass());
        if(currentSlot>44)
            setupNextPage();
        else
            inventory.setItem(53, category.getBackgroundGlass());
    }

    private void loadCategories(){
        if(AuctionMaster.auctionsHandler.weapons!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.weapons.getSlot(), category instanceof Weapons ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.weapons.getDisplay());
        if(AuctionMaster.auctionsHandler.armor!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.armor.getSlot(), category instanceof Armor ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.armor.getDisplay());
        if(AuctionMaster.auctionsHandler.tools!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.tools.getSlot(), category instanceof Tools ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.tools.getDisplay());
        if(AuctionMaster.auctionsHandler.consumables!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.consumables.getSlot(), category instanceof Consumables ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.consumables.getDisplay());
        if(AuctionMaster.auctionsHandler.blocks!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.blocks.getSlot(), category instanceof Blocks ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.blocks.getDisplay());
        if(AuctionMaster.auctionsHandler.others!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.others.getSlot(), category instanceof Others ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.others.getDisplay());
        if(AuctionMaster.auctionsHandler.global!=null)
            inventory.setItem(AuctionMaster.auctionsHandler.global.getSlot(), category instanceof Global ? category.getDisplayCategoryItemSelected() : AuctionMaster.auctionsHandler.global.getDisplay());
    }

    public BrowsingAuctionsMenu(Player player, String category, int page, String search){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.page = page;
            this.categoryString = category;
            this.sortBIN = auctionsHandler.sortingObject.getSortIndexBIN(player);
            if (sortBIN == 0)
                checkForSort = (auction) -> true;
            else if (sortBIN == 1)
                checkForSort = (auction) -> !auction.isBIN();
            else
                checkForSort = (auction) -> auction.isBIN();
            if (search != null) {
                this.search = search;
                this.search = search.toLowerCase();
            }
            inventory = Bukkit.createInventory(player, 54, utilsAPI.chat(player, AuctionMaster.configLoad.browsingMenuName));
            this.category = AuctionMaster.auctionsHandler.getCategory(category);

            ItemStack backgroundGlass = this.category.getBackgroundGlass();

            for (int i = 2; i < 8; i++) {
                inventory.setItem(i, backgroundGlass.clone());
                inventory.setItem(45 + i, backgroundGlass.clone());
            }
            for (int i = 0; i < 54; i += 9) {
                inventory.setItem(i, backgroundGlass.clone());
                inventory.setItem(i + 1, backgroundGlass.clone());
                inventory.setItem(i + 8, backgroundGlass.clone());
            }

            loadAuctions(false);

            if (auctionsHandler.buyItNowSelected != null)
                inventory.setItem(51, AuctionMaster.auctionsHandler.sortingObject.getSortItemBIN(player));
            inventory.setItem(52, AuctionMaster.auctionsHandler.sortingObject.getSortItem(player));

            loadCategories();

            ArrayList<String> lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.searchItemLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(48, itemConstructor.getItem(AuctionMaster.configLoad.searchItemMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.searchItemName), lore));

            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(49, itemConstructor.getItem(AuctionMaster.configLoad.goBackMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.goBackName), lore));

            Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().registerEvents(listener, AuctionMaster.plugin);
                        player.openInventory(inventory);
                    });
            keepUpdated();
        });
    }

    private int sortBIN;
    public class ClickListen implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getClickedInventory().equals(inventory)) {
                    if(e.getSlot()==48){
                        utils.playSound(player, "search-item-click");
                        SearchGUI.searchFor.openGUI(player, categoryString);
                    }
                    else if(e.getSlot()==46){
                        if(!e.getCurrentItem().equals(category.getBackgroundGlass())){
                            utils.playSound(player, "previous-page-click");
                            new BrowsingAuctionsMenu(player, categoryString, page-1, search);
                        }
                    }
                    else if(e.getSlot()==53){
                        if(!e.getCurrentItem().equals(category.getBackgroundGlass())){
                            utils.playSound(player, "next-page-click");
                            new BrowsingAuctionsMenu(player, categoryString, page+1, search);
                        }
                    }
                    else if(e.getSlot()==49){
                        utils.playSound(player, "go-back-click");
                        new MainAuctionMenu(player);
                    }
                    else if(e.getSlot()==51){
                        if(auctionsHandler.buyItNowSelected==null)
                            return;
                        utils.playSound(player, "sort-item-click");
                        AuctionMaster.auctionsHandler.sortingObject.changeSortBIN(player);
                        page=0;
                        if(sortBIN==2)
                            sortBIN=0;
                        else
                            sortBIN++;
                        if(sortBIN==0)
                            checkForSort=(auction) -> true;
                        else if(sortBIN==1)
                            checkForSort=(auction) -> !auction.isBIN();
                        else
                            checkForSort=(auction) -> auction.isBIN();
                        loadAuctions(true);
                        inventory.setItem(51, AuctionMaster.auctionsHandler.sortingObject.getSortItemBIN(player));
                    }
                    else if(e.getSlot()==52){
                        utils.playSound(player, "sort-item-click");
                        AuctionMaster.auctionsHandler.sortingObject.changeSort(player);
                        loadAuctions(false);
                        inventory.setItem(52, AuctionMaster.auctionsHandler.sortingObject.getSortItem(player));
                    }
                    else if(AuctionMaster.auctionsHandler.weapons!=null && e.getSlot()== AuctionMaster.auctionsHandler.weapons.getSlot()){
                        if(!(category instanceof Weapons)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "weapons", 0, null);
                        }
                    }
                    else if(AuctionMaster.auctionsHandler.armor!=null && e.getSlot()== AuctionMaster.auctionsHandler.armor.getSlot()){
                        if(!(category instanceof Armor)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "armor", 0, null);
                        }
                    }
                    else if(AuctionMaster.auctionsHandler.tools!=null && e.getSlot()== AuctionMaster.auctionsHandler.tools.getSlot()){
                        if(!(category instanceof Tools)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "tools", 0, null);
                        }
                    }
                    else if(AuctionMaster.auctionsHandler.consumables!=null && e.getSlot()== AuctionMaster.auctionsHandler.consumables.getSlot()){
                        if(!(category instanceof Consumables)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "consumables", 0, null);
                        }
                    }
                    else if(AuctionMaster.auctionsHandler.blocks!=null && e.getSlot()== AuctionMaster.auctionsHandler.blocks.getSlot()){
                        if(!(category instanceof Blocks)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "blocks", 0, null);
                        }
                    }
                    else if(AuctionMaster.auctionsHandler.others!=null && e.getSlot()== AuctionMaster.auctionsHandler.others.getSlot()){
                        if(!(category instanceof Others)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "others", 0, null);
                        }
                    }
                    else if(AuctionMaster.auctionsHandler.global!=null && e.getSlot()== AuctionMaster.auctionsHandler.global.getSlot()){
                        if(!(category instanceof Global)){
                            utils.playSound(player, "category-click");
                            new BrowsingAuctionsMenu(player, "global", 0, null);
                        }
                    }
                    else if(auctions.containsKey(e.getSlot())){
                        new ViewAuctionMenu(player, auctions.get(e.getSlot()), "browsing_"+categoryString, 0);
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory())) {
                HandlerList.unregisterAll(this);
                keepUpdated.cancel();
                inventory = null;
                player = null;
            }
        }
    }

}
