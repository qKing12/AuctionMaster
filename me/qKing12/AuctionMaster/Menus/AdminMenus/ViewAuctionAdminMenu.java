package me.qKing12.AuctionMaster.Menus.AdminMenus;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.Bids;
import me.qKing12.AuctionMaster.InputGUIs.BidSelectGUI.BidSelectGUI;
import me.qKing12.AuctionMaster.InputGUIs.EditDurationGUI.EditDurationGUI;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.*;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.*;

public class ViewAuctionAdminMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private Auction auction;
    private String goBackTo;

    public ViewAuctionAdminMenu(Player player, Auction auction, String goBackTo){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.auction = auction;
            this.goBackTo = goBackTo;
            inventory = Bukkit.createInventory(player, Main.configLoad.viewAuctionMenuSize, utilsAPI.chat(player, Main.configLoad.viewAuctionMenuName));

            if (Main.configLoad.useBackgoundGlass)
                for (int i = 0; i < Main.configLoad.viewAuctionMenuSize; i++)
                    inventory.setItem(i, Main.configLoad.backgroundGlass.clone());

            inventory.setItem(13, auction.getUpdatedDisplay());

            inventory.setItem(28, Main.configLoad.adminDeleteWithDelivery.clone());
            inventory.setItem(29, Main.configLoad.adminDeleteWithoutDelivery.clone());
            inventory.setItem(31, Main.configLoad.adminCopyAuction);
            inventory.setItem(33, Main.configLoad.adminEditDurationAuction);
            inventory.setItem(34, Main.configLoad.adminForceEndAuction.clone());


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
                    if(e.getSlot()==49){
                        if(goBackTo.equals("ended-menu"))
                            new EndedAuctionsMenu(player, 0);
                        else
                            new ViewAuctionMenu(player, auction, goBackTo, 0);
                    }
                    else if(e.getSlot()==28){
                        if(Main.deliveries!=null) {
                            if (e.getCurrentItem().getAmount() == 2)
                                e.getCurrentItem().setAmount(1);
                            else {
                                auction.adminRemoveAuction(true);
                                player.closeInventory();
                            }
                        }
                        else
                            player.sendMessage(utils.chat("&cDeliveries are disabled."));
                    }
                    else if(e.getSlot()==29){
                        if(e.getCurrentItem().getAmount()==2)
                            e.getCurrentItem().setAmount(1);
                        else{
                            auction.adminRemoveAuction(false);
                            player.closeInventory();
                        }
                    }
                    else if(e.getSlot()==31){
                        player.getInventory().addItem(auction.getItemStack());
                    }
                    else if(e.getSlot()==33){
                        EditDurationGUI.editDuration.openGUI(player, auction, goBackTo, e.getClick().equals(ClickType.RIGHT));
                    }
                    else if(e.getSlot()==34){
                        if(e.getCurrentItem().getAmount()==2){
                            e.getCurrentItem().setAmount(1);
                        }
                        else {
                            if (auction.forceEnd()) {
                                utils.injectToLog("[Admin Force End] Auction with ID="+auction.getId()+" was forcefully terminated by "+player.getName());
                                player.sendMessage(utils.chat("&aAuction was forcefully terminated!"));
                            }
                            else
                                player.sendMessage(utils.chat("&cAuction is already ended!"));
                            player.closeInventory();
                        }
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