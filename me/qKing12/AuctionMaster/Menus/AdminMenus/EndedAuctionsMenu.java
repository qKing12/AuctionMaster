package me.qKing12.AuctionMaster.Menus.AdminMenus;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.Categories.*;
import me.qKing12.AuctionMaster.InputGUIs.SearchGUI.SearchGUI;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.BrowsingAuctionsMenu;
import me.qKing12.AuctionMaster.Menus.MainAuctionMenu;
import me.qKing12.AuctionMaster.Menus.ViewAuctionMenu;
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

import static me.qKing12.AuctionMaster.Main.*;

public class EndedAuctionsMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private int page;

    private HashMap<Integer, Auction> auctions = new HashMap<>();


    private void setupPreviousPage(){
        ArrayList<String> lore = new ArrayList<>();
        for(String line : Main.configLoad.previousPageLore)
            lore.add(utilsAPI.chat(player, line.replace("%page-number%", String.valueOf(page))));

        inventory.setItem(46, itemConstructor.getItem(Main.configLoad.previousPageMaterial, utilsAPI.chat(player, Main.configLoad.previousPageName.replace("%page-number%", String.valueOf(page))), lore));
    }

    private void setupNextPage(){
        ArrayList<String> lore = new ArrayList<>();
        for(String line : Main.configLoad.nextPageLore)
            lore.add(utilsAPI.chat(player, line.replace("%page-number%", String.valueOf(page))));

        inventory.setItem(53, itemConstructor.getItem(Main.configLoad.nextPageMaterial, utilsAPI.chat(player, Main.configLoad.nextPageName.replace("%page-number%", String.valueOf(page))), lore));
    }

    private void loadAuctions() {
        auctions.clear();

        Iterator<Auction> auctionIterator = Main.auctionsHandler.auctions.values().iterator();

        int toSkip = page * 28;

        int currentSlot = 10;
        while (auctionIterator.hasNext() && currentSlot < 44) {
            Auction auction = auctionIterator.next();
            if (auction.isEnded()){
                if (toSkip != 0) {
                    toSkip--;
                    continue;
                }
                this.auctions.put(currentSlot, auction);
                inventory.setItem(currentSlot, auction.getUpdatedDisplay());
                if (currentSlot % 9 == 7)
                    currentSlot += 3;
                else
                    currentSlot++;
            }
        }

        if (page != 0)
            setupPreviousPage();
        if (currentSlot > 44)
            setupNextPage();
    }

    public EndedAuctionsMenu(Player player, int page){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.page = page;
            inventory = Bukkit.createInventory(player, 54, utilsAPI.chat(player, Main.configLoad.browsingMenuName));

            ItemStack backgroundGlass = Main.configLoad.backgroundGlass;

            for (int i = 1; i < 8; i++) {
                inventory.setItem(i, backgroundGlass.clone());
                inventory.setItem(45 + i, backgroundGlass.clone());
            }
            for (int i = 0; i < 54; i += 9) {
                inventory.setItem(i, backgroundGlass.clone());
                inventory.setItem(i + 8, backgroundGlass.clone());
            }

            loadAuctions();

            ArrayList<String> lore = new ArrayList<>();
            for (String line : Main.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(49, itemConstructor.getItem(Main.configLoad.goBackMaterial, utilsAPI.chat(player, Main.configLoad.goBackName), lore));

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().registerEvents(listener, Main.plugin);
                player.openInventory(inventory);
            });
        });
    }

    public class ClickListen implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR))
                return;
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getClickedInventory().equals(inventory)) {
                    if(e.getSlot()==49)
                        new MainAdminMenu(player);
                    else if(e.getSlot()==46){
                        if(!e.getCurrentItem().equals(Main.configLoad.backgroundGlass)){
                            new EndedAuctionsMenu(player, page-1);
                        }
                    }
                    else if(e.getSlot()==53){
                        if(!e.getCurrentItem().equals(Main.configLoad.backgroundGlass)){
                            new EndedAuctionsMenu(player, page+1);
                        }
                    }
                    else if(auctions.containsKey(e.getSlot())){
                        new ViewAuctionAdminMenu(player, auctions.get(e.getSlot()), "ended-menu");
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory())) {
                HandlerList.unregisterAll(this);
                inventory = null;
                player = null;
            }
        }
    }

}
