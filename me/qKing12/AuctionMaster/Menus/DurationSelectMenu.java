package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.InputGUIs.DurationSelectGUI.SelectDurationGUI;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.*;

public class DurationSelectMenu {

    private static int getMaximumDuration(Player p){
        for (int start = 48; start >= 1; start--)
            if (p.hasPermission("auctionmaster.limit.duration." + start))
                return start;
        return -1;
    }

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private int maximum_hours;

    public DurationSelectMenu(Player player){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            inventory = Bukkit.createInventory(player, Main.configLoad.mainMenuSize, utilsAPI.chat(player, Main.configLoad.mainMenuName));

            if (Main.plugin.getConfig().getBoolean("use-duration-limit"))
                maximum_hours = getMaximumDuration(player);
            else maximum_hours = -1;

            long startingDuration;
            if (Main.auctionsHandler.startingDuration.containsKey(player.getUniqueId().toString()))
                startingDuration = Main.auctionsHandler.startingDuration.get(player.getUniqueId().toString());
            else
                startingDuration = Main.configLoad.defaultDuration;


            if (Main.configLoad.useBackgoundGlass)
                for (int i = 0; i < Main.configLoad.mainMenuSize; i++)
                    inventory.setItem(i, Main.configLoad.backgroundGlass.clone());

            boolean custom = true;
            double hoursToCompare;

            hoursToCompare = Main.menusCfg.getDouble("duration-select-menu.first-item.hours");
            if (maximum_hours == -1 || hoursToCompare >= maximum_hours) {
                if (hoursToCompare < 1)
                    hoursToCompare = hoursToCompare * 6000000;
                else
                    hoursToCompare = hoursToCompare * 3600000;
                ItemStack toSet = Main.configLoad.firstTimeItem.clone();
                if (hoursToCompare == startingDuration) {
                    custom = false;
                    toSet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                }
                inventory.setItem(Main.menusCfg.getInt("duration-select-menu.first-item.slot"), toSet);
            }

            hoursToCompare = Main.menusCfg.getDouble("duration-select-menu.second-item.hours");
            if (maximum_hours == -1 || hoursToCompare >= maximum_hours) {
                if (hoursToCompare < 1)
                    hoursToCompare = hoursToCompare * 6000000;
                else
                    hoursToCompare = hoursToCompare * 3600000;
                ItemStack toSet = Main.configLoad.secondTimeItem.clone();
                if (hoursToCompare == startingDuration) {
                    custom = false;
                    toSet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                }
                inventory.setItem(Main.menusCfg.getInt("duration-select-menu.second-item.slot"), toSet);
            }

            hoursToCompare = Main.menusCfg.getDouble("duration-select-menu.third-item.hours");
            if (maximum_hours == -1 || hoursToCompare >= maximum_hours) {
                if (hoursToCompare < 1)
                    hoursToCompare = hoursToCompare * 6000000;
                else
                    hoursToCompare = hoursToCompare * 3600000;
                ItemStack toSet = Main.configLoad.thirdTimeItem.clone();
                if (hoursToCompare == startingDuration) {
                    custom = false;
                    toSet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                }
                inventory.setItem(Main.menusCfg.getInt("duration-select-menu.third-item.slot"), toSet);
            }

            hoursToCompare = Main.menusCfg.getDouble("duration-select-menu.forth-item.hours");
            if (maximum_hours == -1 || hoursToCompare >= maximum_hours) {
                if (hoursToCompare < 1)
                    hoursToCompare = hoursToCompare * 6000000;
                else
                    hoursToCompare = hoursToCompare * 3600000;
                ItemStack toSet = Main.configLoad.forthTimeItem.clone();
                if (hoursToCompare == startingDuration) {
                    custom = false;
                    toSet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                }
                inventory.setItem(Main.menusCfg.getInt("duration-select-menu.forth-item.slot"), toSet);
            }

            hoursToCompare = Main.menusCfg.getDouble("duration-select-menu.fifth-item.hours");
            if (maximum_hours == -1 || hoursToCompare >= maximum_hours) {
                if (hoursToCompare < 1)
                    hoursToCompare = hoursToCompare * 6000000;
                else
                    hoursToCompare = hoursToCompare * 3600000;
                ItemStack toSet = Main.configLoad.fifthTimeItem.clone();
                if (hoursToCompare == startingDuration) {
                    custom = false;
                    toSet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                }
                inventory.setItem(Main.menusCfg.getInt("duration-select-menu.fifth-item.slot"), toSet);
            }

            ItemStack toSet = Main.configLoad.customTimeItem.clone();
            if (custom)
                toSet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            inventory.setItem(Main.menusCfg.getInt("duration-select-menu.custom-time-item-slot"), toSet);

            ArrayList<String> lore = new ArrayList<>();
            for (String line : Main.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("duration-select-menu.go-back-slot"), itemConstructor.getItem(Main.configLoad.goBackMaterial, utilsAPI.chat(player, Main.configLoad.goBackName), lore));

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
                    if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.go-back-slot")) {
                        new CreateAuctionMainMenu(player);
                        utils.playSound(player, "go-back-click");
                    } else if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.first-item.slot")) {
                        utils.playSound(player, "1-hour");
                        double duration = Main.menusCfg.getDouble("duration-select-menu.first-item.hours");
                        if (duration < 1)
                            duration = duration * 6000000;
                        else
                            duration = duration * 3600000;
                        Main.auctionsHandler.startingDuration.put(player.getUniqueId().toString(), (int) duration);
                        new CreateAuctionMainMenu(player);
                    } else if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.second-item.slot")) {
                        utils.playSound(player, "6-hours");
                        double duration = Main.menusCfg.getDouble("duration-select-menu.second-item.hours");
                        if (duration < 1)
                            duration = duration * 6000000;
                        else
                            duration = duration * 3600000;
                        Main.auctionsHandler.startingDuration.put(player.getUniqueId().toString(), (int) duration);
                        new CreateAuctionMainMenu(player);
                    } else if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.third-item.slot")) {
                        utils.playSound(player, "12-hours");
                        double duration = Main.menusCfg.getDouble("duration-select-menu.third-item.hours");
                        if (duration < 1)
                            duration = duration * 6000000;
                        else
                            duration = duration * 3600000;
                        Main.auctionsHandler.startingDuration.put(player.getUniqueId().toString(), (int) duration);
                        new CreateAuctionMainMenu(player);
                    } else if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.forth-item.slot")) {
                        utils.playSound(player, "24-hours");
                        double duration = Main.menusCfg.getDouble("duration-select-menu.forth-item.hours");
                        if (duration < 1)
                            duration = duration * 6000000;
                        else
                            duration = duration * 3600000;
                        Main.auctionsHandler.startingDuration.put(player.getUniqueId().toString(), (int) duration);
                        new CreateAuctionMainMenu(player);
                    } else if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.fifth-item.slot")) {
                        utils.playSound(player, "2-days");
                        double duration = Main.menusCfg.getDouble("duration-select-menu.fifth-item.hours");
                        if (duration < 1)
                            duration = duration * 6000000;
                        else
                            duration = duration * 3600000;
                        Main.auctionsHandler.startingDuration.put(player.getUniqueId().toString(), (int) duration);
                        new CreateAuctionMainMenu(player);
                    } else if (e.getSlot() == Main.menusCfg.getInt("duration-select-menu.custom-time-item-slot")) {
                        utils.playSound(player, "custom-duration-click");
                        SelectDurationGUI.selectDuration.openGUI(player, maximum_hours, e.getClick().equals(ClickType.RIGHT));
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