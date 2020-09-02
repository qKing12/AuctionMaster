package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.API.Events.AuctionCreateEvent;
import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionBIN;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionClassic;
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

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.*;

public class CreateAuctionConfirmMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private double fee;

    public CreateAuctionConfirmMenu(Player player, double fee){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.fee = fee;
            inventory = Bukkit.createInventory(player, Main.configLoad.createAuctionConfirmMenuSize, utilsAPI.chat(player, Main.configLoad.createAuctionConfirmMenuName));

            if (Main.configLoad.useBackgoundGlass)
                for (int i = 0; i < Main.configLoad.createAuctionConfirmMenuSize; i++)
                    inventory.setItem(i, Main.configLoad.backgroundGlass.clone());

            ArrayList<String> lore = new ArrayList<>();
            for (String line : Main.configLoad.confirmItemLore)
                lore.add(utilsAPI.chat(player, line.replace("%cost%", Main.numberFormatHelper.formatNumber(fee))));
            inventory.setItem(Main.menusCfg.getInt("create-auction-confirm-menu.confirm-item-slot"), itemConstructor.getItem(Main.configLoad.confirmItemMaterial, utilsAPI.chat(player, Main.configLoad.confirmItemName.replace("%cost%", Main.numberFormatHelper.formatNumber(fee))), lore));

            lore = new ArrayList<>();
            for (String line : Main.configLoad.cancelItemLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("create-auction-confirm-menu.cancel-item-slot"), itemConstructor.getItem(Main.configLoad.cancelItemMaterial, utilsAPI.chat(player, Main.configLoad.cancelItemName), lore));

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
                    if (e.getSlot() == Main.menusCfg.getInt("create-auction-confirm-menu.cancel-item-slot")) {
                        utils.playSound(player, "auction-cancel");
                        new CreateAuctionMainMenu(player);
                    }
                    else if (e.getSlot() == Main.menusCfg.getInt("create-auction-confirm-menu.confirm-item-slot")){
                        utils.playSound(player, "auction-confirm");
                        String uuid = player.getUniqueId().toString();
                        double startingBid;
                        if(Main.auctionsHandler.startingBid.containsKey(player.getUniqueId().toString()))
                            startingBid=Main.auctionsHandler.startingBid.get(uuid);
                        else
                            startingBid=Main.configLoad.defaultStartingBid;
                        long duration;
                        if(Main.auctionsHandler.startingDuration.containsKey(uuid))
                            duration=Main.auctionsHandler.startingDuration.get(uuid);
                        else
                            duration=Main.configLoad.defaultDuration;
                        Main.economy.removeMoney(player, fee);
                        Auction auction;
                        if(Main.auctionsHandler.buyItNowSelected!=null && Main.auctionsHandler.buyItNowSelected.contains(player.getUniqueId().toString()))
                            Main.auctionsHandler.createAuction(auction=new AuctionBIN(player, startingBid, duration, Main.auctionsHandler.previewItems.get(uuid)));
                        else
                            Main.auctionsHandler.createAuction(auction=new AuctionClassic(player, startingBid, duration, Main.auctionsHandler.previewItems.get(uuid)));
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new AuctionCreateEvent(player, auction)));
                        player.sendMessage(utilsAPI.chat(player, Main.auctionsManagerCfg.getString("auction-created-message")));
                        Main.auctionsHandler.startingBid.remove(uuid);
                        Main.auctionsHandler.startingDuration.remove(uuid);
                        Main.auctionsHandler.previewItems.remove(uuid);
                        Main.auctionsDatabase.removePreviewItem(uuid);
                        player.closeInventory();
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
