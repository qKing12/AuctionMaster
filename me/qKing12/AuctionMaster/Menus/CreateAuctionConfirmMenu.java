package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.API.Events.AuctionCreateEvent;
import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionBIN;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionClassic;
import me.qKing12.AuctionMaster.AuctionMaster;
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

import static me.qKing12.AuctionMaster.AuctionMaster.*;

public class CreateAuctionConfirmMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private double fee;

    public CreateAuctionConfirmMenu(Player player, double fee){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.fee = fee;
            inventory = Bukkit.createInventory(player, AuctionMaster.configLoad.createAuctionConfirmMenuSize, utilsAPI.chat(player, AuctionMaster.configLoad.createAuctionConfirmMenuName));

            if (AuctionMaster.configLoad.useBackgoundGlass)
                for (int i = 0; i < AuctionMaster.configLoad.createAuctionConfirmMenuSize; i++)
                    inventory.setItem(i, AuctionMaster.configLoad.backgroundGlass.clone());

            ArrayList<String> lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.confirmItemLore)
                lore.add(utilsAPI.chat(player, line.replace("%cost%", AuctionMaster.numberFormatHelper.formatNumber(fee))));
            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-confirm-menu.confirm-item-slot"), itemConstructor.getItem(AuctionMaster.configLoad.confirmItemMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.confirmItemName.replace("%cost%", AuctionMaster.numberFormatHelper.formatNumber(fee))), lore));

            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.cancelItemLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-confirm-menu.cancel-item-slot"), itemConstructor.getItem(AuctionMaster.configLoad.cancelItemMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.cancelItemName), lore));

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().registerEvents(listener, AuctionMaster.plugin);
                player.openInventory(inventory);
            });
        });
    }

    public class ClickListen implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent e){
            if(e.getInventory().equals(inventory)){
                e.setCancelled(true);
                if(e.getCurrentItem()==null || e.getCurrentItem().getType().equals(Material.AIR)) {
                    return;
                }
                if(e.getClickedInventory().equals(inventory)) {
                    if (e.getSlot() == AuctionMaster.menusCfg.getInt("create-auction-confirm-menu.cancel-item-slot")) {
                        utils.playSound(player, "auction-cancel");
                        new CreateAuctionMainMenu(player);
                    }
                    else if (e.getSlot() == AuctionMaster.menusCfg.getInt("create-auction-confirm-menu.confirm-item-slot")){
                        utils.playSound(player, "auction-confirm");
                        String uuid = player.getUniqueId().toString();
                        double startingBid;
                        if(AuctionMaster.auctionsHandler.startingBid.containsKey(player.getUniqueId().toString()))
                            startingBid= AuctionMaster.auctionsHandler.startingBid.get(uuid);
                        else
                            startingBid= AuctionMaster.configLoad.defaultStartingBid;
                        long duration;
                        if(AuctionMaster.auctionsHandler.startingDuration.containsKey(uuid))
                            duration= AuctionMaster.auctionsHandler.startingDuration.get(uuid);
                        else
                            duration= AuctionMaster.configLoad.defaultDuration;
                        AuctionMaster.economy.removeMoney(player, fee);
                        Auction auction;
                        if(auctionsHandler.buyItNowSelected != null && (configLoad.onlyBuyItNow || ((AuctionMaster.configLoad.defaultBuyItNow && !auctionsHandler.buyItNowSelected.contains(player.getUniqueId().toString())) || (!configLoad.defaultBuyItNow && auctionsHandler.buyItNowSelected.contains(player.getUniqueId().toString())))))
                            AuctionMaster.auctionsHandler.createAuction(auction=new AuctionBIN(player, startingBid, duration, AuctionMaster.auctionsHandler.previewItems.get(uuid)));
                        else
                            AuctionMaster.auctionsHandler.createAuction(auction=new AuctionClassic(player, startingBid, duration, AuctionMaster.auctionsHandler.previewItems.get(uuid)));
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new AuctionCreateEvent(player, auction)));
                        player.sendMessage(utilsAPI.chat(player, AuctionMaster.auctionsManagerCfg.getString("auction-created-message")));
                        AuctionMaster.auctionsHandler.startingBid.remove(uuid);
                        AuctionMaster.auctionsHandler.startingDuration.remove(uuid);
                        AuctionMaster.auctionsHandler.previewItems.remove(uuid);
                        AuctionMaster.auctionsDatabase.removePreviewItem(uuid);
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
