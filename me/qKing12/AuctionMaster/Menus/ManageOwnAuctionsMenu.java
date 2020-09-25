package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static me.qKing12.AuctionMaster.AuctionMaster.*;

public class ManageOwnAuctionsMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private HashMap<Integer, Auction> auctions = new HashMap<>();
    private BukkitTask keepUpdated;
    private int createMenuSlot;
    private int goBackSlot;
    private int collectAllSlot=-1;

    private ArrayList<Auction> toCollectAll=new ArrayList<>();

    private void collectAll() {
        player.sendMessage(utilsAPI.chat(player, AuctionMaster.auctionsManagerCfg.getString("collect-all-message")));
        for (Auction auction : toCollectAll) {
            if (player.getInventory().firstEmpty() != -1) {
                auction.sellerClaim(player);
            } else {
                player.sendMessage(utilsAPI.chat(player, AuctionMaster.auctionsManagerCfg.getString("not-enough-inventory-space")));
                break;
            }
        }
        player.closeInventory();
    }

    private void keepUpdated(){
        keepUpdated=Bukkit.getScheduler().runTaskTimerAsynchronously(AuctionMaster.plugin, () -> {
            Iterator<Map.Entry<Integer, Auction>> auction = auctions.entrySet().iterator();
            while(auction.hasNext()){
                Map.Entry<Integer, Auction> entry=auction.next();
                try {
                    inventory.setItem(entry.getKey(), entry.getValue().getUpdatedDisplay());
                }catch(NullPointerException x){
                    if(inventory!=null)
                        x.printStackTrace();
                }
            }
        }, 20, 20);
    }

    public ManageOwnAuctionsMenu(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;

            ArrayList<Auction> auctions = AuctionMaster.auctionsHandler.ownAuctions.get(player.getUniqueId().toString());

            int size = 2;
            size += auctions.size() / 7;
            if (auctions.size() % 7 > 0)
                size += 1;
            size *= 9;

            inventory = Bukkit.createInventory(player, size, utilsAPI.chat(player, AuctionMaster.configLoad.manageOwnAuctionsMenuName));

            int relativeSlot = size - 9;
            for (int i = 1; i < 8; i++) {
                inventory.setItem(i, AuctionMaster.configLoad.backgroundGlass.clone());
                inventory.setItem(relativeSlot + i, AuctionMaster.configLoad.backgroundGlass.clone());
            }
            for (int i = 0; i < size; i += 9) {
                inventory.setItem(i, AuctionMaster.configLoad.backgroundGlass.clone());
                inventory.setItem(i + 8, AuctionMaster.configLoad.backgroundGlass.clone());
            }

            int slot = 10;
            for (Auction auction : auctions) {
                inventory.setItem(slot, auction.getUpdatedDisplay());
                if (auction.isEnded())
                    toCollectAll.add(auction);
                this.auctions.put(slot, auction);
                if (slot % 9 == 7)
                    slot += 3;
                else
                    slot++;
            }
            keepUpdated();


            ArrayList<String> lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.manageAuctionsItemLoreWithoutAuctions) {
                lore.add(utilsAPI.chat(player, line));
            }
            inventory.setItem(createMenuSlot = size - 2, itemConstructor.getItem(AuctionMaster.configLoad.manageAuctionsItemMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.manageAuctionsItemName), lore));

            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(goBackSlot = size - 5, itemConstructor.getItem(AuctionMaster.configLoad.goBackMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.goBackName), lore));

            if (toCollectAll.size() > 1) {
                double coinsToCollect = 0;
                int contor = 0;
                for (Auction auction : toCollectAll) {
                    if (auction.getBids().getNumberOfBids() == 0)
                        contor++;
                    else
                        coinsToCollect += auction.getBids().getTopBidCoins();
                }
                collectAllSlot = size - 8;
                lore = new ArrayList<>();
                for (String line : AuctionMaster.configLoad.collectAllLoreOwnAuctions)
                    lore.add(utilsAPI.chat(player, line
                            .replace("%auctions%", String.valueOf(toCollectAll.size()))
                            .replace("%coins%", AuctionMaster.numberFormatHelper.formatNumber(coinsToCollect))
                            .replace("%items%", String.valueOf(contor))
                    ));
                inventory.setItem(size - 8, itemConstructor.getItem(AuctionMaster.configLoad.collectAllMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.collectAllName), lore));
            }

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
                    if (e.getSlot() == createMenuSlot) {
                        new CreateAuctionMainMenu(player);
                    }
                    else if(e.getSlot()==collectAllSlot){
                        utils.playSound(player, "claim-all-click");
                        collectAll();
                    }
                    else if(e.getSlot() == goBackSlot) {
                        utils.playSound(player, "go-back-click");
                        new MainAuctionMenu(player);
                    }
                    else if(auctions.containsKey(e.getSlot())){
                        new ViewAuctionMenu(player, auctions.get(e.getSlot()), "ownAuction", 0);
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
