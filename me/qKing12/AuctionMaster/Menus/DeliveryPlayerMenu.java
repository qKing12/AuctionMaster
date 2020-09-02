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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.*;

public class DeliveryPlayerMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private boolean fromMainMenu;
    private ArrayList<ItemStack> deliveryItems;
    private double deliveryCoins;

    public DeliveryPlayerMenu(Player player, boolean fromMainMenu){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            this.fromMainMenu = fromMainMenu;
            this.deliveryCoins = Main.deliveries.getCoins(player.getUniqueId().toString());
            this.deliveryItems = Main.deliveries.getDeliveryItems(player.getUniqueId().toString());
            inventory = Bukkit.createInventory(player, InventoryType.DISPENSER, utilsAPI.chat(player, Main.configLoad.deliveryMenuName));

            if (Main.configLoad.useBackgoundGlass)
                for (int i = 0; i < 9; i++)
                    inventory.setItem(i, Main.configLoad.backgroundGlass.clone());

            ArrayList<String> lore = new ArrayList<>();
            if (deliveryItems.isEmpty() && deliveryCoins == 0) {
                for (String line : Main.configLoad.deliveryItemNoLore)
                    lore.add(utilsAPI.chat(player, line));
                inventory.setItem(4, itemConstructor.getItem(Main.configLoad.mainMenuDeliveryItem, utilsAPI.chat(player, Main.configLoad.deliveryItemNoName), lore));
            } else {
                for (String line : Main.configLoad.deliveryItemYesLore)
                    lore.add(utilsAPI.chat(player, line
                            .replace("%coins%", Main.numberFormatHelper.formatNumber(deliveryCoins))
                            .replace("%items-count%", String.valueOf(deliveryItems.size()))
                    ));
                inventory.setItem(4, itemConstructor.getItem(Main.configLoad.mainMenuDeliveryItem, utilsAPI.chat(player, Main.configLoad.deliveryItemYesName), lore));
            }

            if (fromMainMenu) {
                lore = new ArrayList<>();
                for (String line : Main.configLoad.goBackLore)
                    lore.add(utilsAPI.chat(player, line));
                inventory.setItem(7, itemConstructor.getItem(Main.configLoad.goBackMaterial, utilsAPI.chat(player, Main.configLoad.goBackName), lore));
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
                    if (e.getSlot() == 7) {
                        if (fromMainMenu)
                            new MainAuctionMenu(player);
                    }
                    else if(e.getSlot()==4){
                        if(deliveryItems.isEmpty() && deliveryCoins==0){
                            utils.playSound(player, "ah-no-delivery");
                            player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("no-delivery-to-claim")));
                            return;
                        }
                        try {
                            economy.addMoney(player, deliveryCoins);
                            ArrayList<ItemStack> itemStacks = new ArrayList<>();
                            for (ItemStack itemStack : deliveryItems) {
                                if (player.getInventory().firstEmpty() == -1) {
                                    itemStacks.add(itemStack);
                                } else
                                    player.getInventory().addItem(itemStack);
                            }
                            if (!itemStacks.isEmpty()) {
                                player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("not-enough-space-delivery")));
                                deliveries.setCoinsAndItems(player.getUniqueId().toString(), itemStacks, 0);
                            } else
                                deliveries.removeDelivery(player.getUniqueId().toString());
                            utils.playSound(player, "ah-delivery-claimed");
                            player.sendMessage(utilsAPI.chat(player, plugin.getConfig().getString("delivery-claimed-message")));
                        }catch(Exception x){
                            x.printStackTrace();
                            deliveries.removeDelivery(player.getUniqueId().toString());
                        }
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
