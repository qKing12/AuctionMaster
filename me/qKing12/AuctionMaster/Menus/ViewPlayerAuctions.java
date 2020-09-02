package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.Main;
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

import java.util.*;

import static me.qKing12.AuctionMaster.Main.*;

public class ViewPlayerAuctions {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private HashMap<Integer, Auction> auctions = new HashMap<>();
    private BukkitTask keepUpdated;
    private String uuid;

    private void keepUpdated(){
        keepUpdated=Bukkit.getScheduler().runTaskTimerAsynchronously(Main.plugin, () -> {
            Iterator<Map.Entry<Integer, Auction>> auction = auctions.entrySet().iterator();
            while(auction.hasNext()){
                Map.Entry<Integer, Auction> entry=auction.next();
                inventory.setItem(entry.getKey(), entry.getValue().getUpdatedDisplay());
            }
        }, 20, 20);
    }

    public ViewPlayerAuctions(Player player, String uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;

            if(Main.auctionsHandler.ownAuctions.get(uuid)==null){
                player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("no-auctions-message")));
                return;
            }
            ArrayList<Auction> auctions = new ArrayList<>();

            for(Auction auction : auctionsHandler.ownAuctions.get(uuid)){
                if(!auction.isEnded())
                    auctions.add(auction);
            }

            if(auctions.isEmpty()){
                player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("no-auctions-message")));
                return;
            }

            this.uuid = uuid;

            int size = 2;
            size += auctions.size() / 9;
            if (auctions.size() % 9 > 0)
                size += 1;
            size *= 9;

            inventory = Bukkit.createInventory(player, size, utilsAPI.chat(player, Main.configLoad.viewPlayerAuctionsMenuName.replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName())));

            int relativeSlot = size - 9;
            for (int i = 1; i < 8; i++) {
                inventory.setItem(i, Main.configLoad.backgroundGlass.clone());
                inventory.setItem(relativeSlot + i, Main.configLoad.backgroundGlass.clone());
            }
            for (int i = 0; i < size; i += 9) {
                inventory.setItem(i, Main.configLoad.backgroundGlass.clone());
                inventory.setItem(i + 8, Main.configLoad.backgroundGlass.clone());
            }

            int slot = 10;
            for (Auction auction : auctions) {
                inventory.setItem(slot, auction.getUpdatedDisplay());
                this.auctions.put(slot, auction);
                if (slot % 9 == 7)
                    slot += 3;
                else
                    slot++;
            }
            keepUpdated();

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
                    if(auctions.containsKey(e.getSlot())){
                        new ViewAuctionMenu(player, auctions.get(e.getSlot()), uuid, 0);
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