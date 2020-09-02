package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.Main;
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

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.*;

public class MainAuctionMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();

    public MainAuctionMenu(Player player){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            inventory = Bukkit.createInventory(player, Main.configLoad.mainMenuSize, utilsAPI.chat(player, Main.configLoad.mainMenuName));

            if (Main.configLoad.useBackgoundGlass)
                for (int i = 0; i < Main.configLoad.mainMenuSize; i++)
                    inventory.setItem(i, Main.configLoad.backgroundGlass.clone());

            ArrayList<String> lore = new ArrayList<>();
            for (String line : Main.configLoad.closeMenuLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("main-menu.close-menu-slot"), itemConstructor.getItem(Main.configLoad.closeMenuMaterial, utilsAPI.chat(player, Main.configLoad.closeMenuName), lore));

            lore = new ArrayList<>();
            for (String line : Main.configLoad.browsingMenuItemLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("main-menu.browsing-menu-slot"), itemConstructor.getItem(Main.configLoad.browsingMenuItemMaterial, utilsAPI.chat(player, Main.configLoad.browsingMenuItemName), lore));

            lore = new ArrayList<>();
            if (Main.auctionsHandler.bidAuctions.containsKey(player.getUniqueId().toString())) {
                for (String line : Main.configLoad.viewBidsMenuItemLoreWithBids)
                    lore.add(utilsAPI.chat(player, line
                            .replace("%total-top-bid%", String.valueOf(Main.auctionsHandler.topBidsCount(player.getUniqueId().toString())))
                            .replace("%total-bids%", String.valueOf(Main.auctionsHandler.totalBidsOnOtherAuctions(player.getUniqueId().toString())))
                    ));
            } else {
                for (String line : Main.configLoad.viewBidsMenuItemLoreWithoutBids)
                    lore.add(utilsAPI.chat(player, line));
            }
            inventory.setItem(Main.menusCfg.getInt("main-menu.view-bids-menu-slot"), itemConstructor.getItem(Main.configLoad.viewBidsMenuItemMaterial, utilsAPI.chat(player, Main.configLoad.viewBidsMenuItemName), lore));


            lore = new ArrayList<>();
            if (Main.auctionsHandler.ownAuctions.containsKey(player.getUniqueId().toString())) {
                for (String line : Main.configLoad.manageAuctionsItemLoreWithAuctions)
                    lore.add(utilsAPI.chat(player, line
                            .replace("%auctions%", String.valueOf(Main.auctionsHandler.ownAuctions.get(player.getUniqueId().toString()).size()))
                            .replace("%bids%", String.valueOf(Main.auctionsHandler.totalBidsOnOwnAuctions(player.getUniqueId().toString())))
                            .replace("%coins%", Main.numberFormatHelper.formatNumber(Main.auctionsHandler.totalCoinsOnOwnAuctions(player.getUniqueId().toString())))
                    ));
            } else {
                for (String line : Main.configLoad.manageAuctionsItemLoreWithoutAuctions)
                    lore.add(utilsAPI.chat(player, line));
            }
            inventory.setItem(Main.menusCfg.getInt("main-menu.manage-auctions-menu-slot"), itemConstructor.getItem(Main.configLoad.manageAuctionsItemMaterial, utilsAPI.chat(player, Main.configLoad.manageAuctionsItemName), lore));

            if (Main.deliveries != null) {
                lore = new ArrayList<>();
                for (String line : Main.configLoad.mainMenuDeliveryLore)
                    lore.add(utilsAPI.chat(player, line));
                inventory.setItem(Main.menusCfg.getInt("main-menu.delivery-menu-slot"), itemConstructor.getItem(Main.configLoad.mainMenuDeliveryItem, utilsAPI.chat(player, Main.configLoad.mainMenuDeliveryName), lore));
            }

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
                    if (e.getSlot() == Main.menusCfg.getInt("main-menu.manage-auctions-menu-slot")) {
                        utils.playSound(player, "own-auctions-click");
                        if (!Main.auctionsHandler.ownAuctions.containsKey(player.getUniqueId().toString()))
                            new CreateAuctionMainMenu(player);
                        else
                            new ManageOwnAuctionsMenu(player);
                    }
                    else if(e.getSlot() == Main.menusCfg.getInt("main-menu.browsing-menu-slot")){
                        utils.playSound(player, "auction-browser-click");
                        new BrowsingAuctionsMenu(player, Main.menusCfg.getString("browsing-menu.default-category"), 0, null);
                    }
                    else if(e.getSlot() == Main.menusCfg.getInt("main-menu.view-bids-menu-slot")){
                        if(Main.auctionsHandler.bidAuctions.containsKey(player.getUniqueId().toString())) {
                            utils.playSound(player, "view-bids-has-click");
                            new ManageOwnBidsMenu(player);
                        }
                        else{
                            utils.playSound(player, "view-bids-no-has-click");
                        }
                    }
                    else if(Main.deliveries!=null && e.getSlot()==Main.menusCfg.getInt("main-menu.delivery-menu-slot")){
                        utils.playSound(player, "delivery-menu-click");
                        new DeliveryPlayerMenu(player, true);
                    }
                    else if(e.getSlot()==Main.menusCfg.getInt("main-menu.close-menu-slot")){
                        player.closeInventory();
                        utils.playSound(player, "close-menu-click");
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
