package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.InputGUIs.StartingBidGUI.StartingBidGUI;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.*;

public class CreateAuctionMainMenu {

    private Inventory inventory;
    private Player player;
    private final ClickListen listener = new ClickListen();
    private ItemStack previewItem;
    private int previewSlot;
    private ItemStack createAuctionItemNo;
    private double startingBid;
    private double startingFeeTime;
    private double startingBidFee;
    private String startingDuration;

    private int getMaximumAuctions(){
        if(Main.plugin.getConfig().getBoolean("use-auction-limit")){
            for (int start = 28; start >= 0; start--)
                if (player.hasPermission("auctionmaster.limit.auctions." + start))
                    return start;
        }
        return 28;
    }

    private ItemStack getCreateAuctionItemYes(String displayName){
        ItemStack toReturn = Main.configLoad.createAuctionConfirmYesMaterial.clone();
        ItemMeta meta = toReturn.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(player, Main.configLoad.createAuctionConfirmYesName));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : Main.configLoad.createAuctionConfirmYesLore)
            lore.add(utilsAPI.chat(player, line
                    .replace("%item-name%", displayName)
                    .replace("%duration%", startingDuration)
                    .replace("%starting-bid%", Main.numberFormatHelper.formatNumber(startingBid))
                    .replace("%fee%", Main.numberFormatHelper.formatNumber(startingBidFee+startingFeeTime))
            ));
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    private void generateCreateAuctionItemNo(){
        createAuctionItemNo = Main.configLoad.createAuctionConfirmNoMaterial.clone();
        ItemMeta meta = createAuctionItemNo.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(player, Main.configLoad.createAuctionConfirmNoName));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : Main.configLoad.createAuctionConfirmNoLore)
            lore.add(utilsAPI.chat(player, line));
        meta.setLore(lore);
        createAuctionItemNo.setItemMeta(meta);
    }

    private ItemStack transformToPreview(ItemStack toTransform){
        String name =utils.getDisplayName(toTransform);
        ItemStack toReturn=toTransform.clone();
        ItemMeta meta = toReturn.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(player, Main.auctionsManagerCfg.getString("preview-selected-item-name")));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(name);
        lore.add(" ");
        if(meta.getLore()!=null) {
            for (String line : meta.getLore())
                lore.add(utilsAPI.chat(player, line));
            lore.add(" ");
        }
        lore.add(utilsAPI.chat(player, Main.auctionsManagerCfg.getString("preview-selected-item-take-back")));
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    private void setupStartingBidItem(){
        ArrayList<String> lore = new ArrayList<>();
        if(buyItNow){
            for (String line : Main.configLoad.switchToAuctionLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.switch-type-slot"), itemConstructor.getItem(Main.configLoad.switchToAuctionMaterial, utilsAPI.chat(player, Main.configLoad.switchToAuctionName), lore));

            lore = new ArrayList<>();
            for (String line : Main.configLoad.editBINPriceLore)
                lore.add(utilsAPI.chat(player, line
                        .replace("%price%", Main.numberFormatHelper.formatNumber(startingBid))
                        .replace("%fee%", Main.numberFormatHelper.formatNumber(startingBidFee))
                ));
            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.starting-bid-slot"), itemConstructor.getItem(Main.configLoad.editBINPriceMaterial, utilsAPI.chat(player, Main.configLoad.editBINPriceName.replace("%price%", Main.numberFormatHelper.formatNumber(startingBid)).replace("%fee%", Main.numberFormatHelper.formatNumber(startingBidFee))), lore));
        }
        else {
            for (String line : Main.configLoad.switchToBinLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.switch-type-slot"), itemConstructor.getItem(Main.configLoad.switchToBinMaterial, utilsAPI.chat(player, Main.configLoad.switchToBinName), lore));

            lore = new ArrayList<>();
            for (String line : Main.configLoad.startingBidItemLore)
                lore.add(utilsAPI.chat(player, line
                        .replace("%starting-bid%", Main.numberFormatHelper.formatNumber(startingBid))
                        .replace("%starting-fee%", Main.numberFormatHelper.formatNumber(startingBidFee))
                ));
            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.starting-bid-slot"), itemConstructor.getItem(Main.configLoad.startingBidItemMaterial, utilsAPI.chat(player, Main.configLoad.startingBidItemName.replace("%starting-bid%", Main.numberFormatHelper.formatNumber(startingBid)).replace("%starting-fee%", Main.numberFormatHelper.formatNumber(startingBidFee))), lore));
        }
    }

    boolean buyItNow;

    public CreateAuctionMainMenu(Player player){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            inventory = Bukkit.createInventory(player, Main.configLoad.createAuctionMenuSize, utilsAPI.chat(player, Main.configLoad.createAuctionMenuName));

            generateCreateAuctionItemNo();
            if (auctionsHandler.buyItNowSelected != null && auctionsHandler.buyItNowSelected.contains(player.getUniqueId().toString()))
                buyItNow = true;
            else
                buyItNow = false;

            if (Main.configLoad.useBackgoundGlass)
                for (int i = 0; i < Main.configLoad.createAuctionMenuSize; i++)
                    inventory.setItem(i, Main.configLoad.backgroundGlass.clone());

            ArrayList<String> lore = new ArrayList<>();
            for (String line : Main.configLoad.createAuctionPreviewLoreNoItem)
                lore.add(utilsAPI.chat(player, line));
            previewItem = itemConstructor.getItem(Main.configLoad.createAuctionPreviewMaterial, utilsAPI.chat(player, Main.configLoad.createAuctionPreviewNameNoItem), lore);
            if (Main.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())) {
                inventory.setItem(previewSlot = Main.menusCfg.getInt("create-auction-menu.preview-item-slot"), transformToPreview(Main.auctionsHandler.previewItems.get(player.getUniqueId().toString())));
            } else {
                inventory.setItem(previewSlot = Main.menusCfg.getInt("create-auction-menu.preview-item-slot"), previewItem);
            }

            if (Main.auctionsHandler.startingBid.containsKey(player.getUniqueId().toString()))
                startingBid = Main.auctionsHandler.startingBid.get(player.getUniqueId().toString());
            else
                startingBid = Main.configLoad.defaultStartingBid;
            startingBidFee = startingBid * (buyItNow ? configLoad.startingBidBINFee : Main.configLoad.startingBidFee) / 100;
            if (!Main.numberFormatHelper.useDecimals) {
                startingBid = Math.floor(startingBid);
                startingBidFee = Math.floor(startingBidFee);
            }

            setupStartingBidItem();

            long startingTime;
            if (Main.auctionsHandler.startingDuration.containsKey(player.getUniqueId().toString()))
                startingTime = Main.auctionsHandler.startingDuration.get(player.getUniqueId().toString());
            else
                startingTime = Main.configLoad.defaultDuration;
            startingDuration = utils.fromMiliseconds((int) startingTime);
            String startingDuration = Main.numberFormatHelper.useDecimals ? Main.numberFormatHelper.formatNumber(startingFeeTime = Main.configLoad.durationFeeCalculator((int) (startingTime / 3600000))) : Main.numberFormatHelper.formatNumber(startingFeeTime = Math.floor(Main.configLoad.durationFeeCalculator((int) (startingTime / 3600000))));
            lore = new ArrayList<>();
            for (String line : Main.configLoad.durationItemLore)
                lore.add(utilsAPI.chat(player, line
                        .replace("%auction-time%", this.startingDuration)
                        .replace("%auction-fee%", startingDuration)
                ));
            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.duration-slot"), itemConstructor.getItem(Main.configLoad.durationItemMaterial, utilsAPI.chat(player, Main.configLoad.durationItemName.replace("%auction-time%", this.startingDuration).replace("%auction-fee%", startingDuration)), lore));

            if (Main.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString()))
                inventory.setItem(Main.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), getCreateAuctionItemYes(utils.getDisplayName(Main.auctionsHandler.previewItems.get(player.getUniqueId().toString()))));
            else
                inventory.setItem(Main.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), createAuctionItemNo);

            lore = new ArrayList<>();
            for (String line : Main.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.go-back-slot"), itemConstructor.getItem(Main.configLoad.goBackMaterial, utilsAPI.chat(player, Main.configLoad.goBackName), lore));

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
                if(e.getClickedInventory().equals(player.getInventory())){
                    if(Main.configLoad.isBlacklisted(e.getCurrentItem())){
                        player.sendMessage(utilsAPI.chat(player, Main.plugin.getConfig().getString("blacklist-item-message")));
                        return;
                    }
                    utils.playSound(player, "inventory-item-click");
                    ItemStack saveCurrentItem=e.getCurrentItem().clone();
                    ItemStack toSet=transformToPreview(e.getCurrentItem());
                    if(Main.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())){
                        player.getInventory().setItem(e.getSlot(), Main.auctionsHandler.previewItems.get(player.getUniqueId().toString()));
                    }
                    else
                        player.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                    Main.auctionsHandler.previewItems.put(player.getUniqueId().toString(), saveCurrentItem);
                    inventory.setItem(previewSlot, toSet);
                    inventory.setItem(Main.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), getCreateAuctionItemYes(utils.getDisplayName(saveCurrentItem)));
                }
                else{
                    if(e.getSlot()==Main.menusCfg.getInt("create-auction-menu.duration-slot")) {
                        utils.playSound(player, "duration-item-click");
                        new DurationSelectMenu(player);
                    }
                    else if(e.getSlot()==previewSlot){
                        if(Main.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString()) && player.getInventory().firstEmpty()!=-1){
                            player.getInventory().addItem(Main.auctionsHandler.previewItems.get(player.getUniqueId().toString()));
                            Main.auctionsHandler.previewItems.remove(player.getUniqueId().toString());
                            inventory.setItem(previewSlot, previewItem.clone());
                            inventory.setItem(Main.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), createAuctionItemNo);
                        }
                    }
                    else if(e.getSlot()==Main.menusCfg.getInt("create-auction-menu.switch-type-slot")){
                        if(auctionsHandler.buyItNowSelected!=null){
                            if(buyItNow) {
                                auctionsHandler.buyItNowSelected.remove(player.getUniqueId().toString());
                                buyItNow=false;
                            }
                            else {
                                auctionsHandler.buyItNowSelected.add(player.getUniqueId().toString());
                                buyItNow=true;
                            }
                            startingBidFee=startingBid*(buyItNow? configLoad.startingBidBINFee:Main.configLoad.startingBidFee)/100;
                            if(!Main.numberFormatHelper.useDecimals){
                                startingBid=Math.floor(startingBid);
                                startingBidFee=Math.floor(startingBidFee);
                            }
                            setupStartingBidItem();
                            if(auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())){
                                inventory.setItem(Main.menusCfg.getInt("create-auction-menu.create-auction-button-slot"),getCreateAuctionItemYes(utils.getDisplayName(auctionsHandler.previewItems.get(player.getUniqueId().toString()))));
                            }
                        }
                    }
                    else if(e.getSlot()==Main.menusCfg.getInt("create-auction-menu.starting-bid-slot")){
                        utils.playSound(player, "starting-bid-item-click");
                        StartingBidGUI.selectStartingBid.openGUI(player);
                    }
                    else if(e.getSlot()==Main.menusCfg.getInt("create-auction-menu.create-auction-button-slot")){
                        if(Main.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())){
                            if(Main.economy.hasMoney(player, startingBidFee+startingFeeTime)) {
                                utils.playSound(player, "create-auction-item-click");
                                new CreateAuctionConfirmMenu(player, startingBidFee + startingFeeTime);
                            }
                            else{
                                player.sendMessage(utilsAPI.chat(player, Main.auctionsManagerCfg.getString("not-enough-money-auction")));
                            }
                        }
                    }
                    else if(e.getSlot()==Main.menusCfg.getInt("create-auction-menu.go-back-slot")){
                        utils.playSound(player, "go-back-click");
                        if(Main.auctionsHandler.ownAuctions.containsKey(player.getUniqueId().toString())){
                            if(Main.auctionsHandler.ownAuctions.get(player.getUniqueId().toString()).size()<getMaximumAuctions())
                                new ManageOwnAuctionsMenu(player);
                            else
                                player.sendMessage(utilsAPI.chat(player, Main.plugin.getConfig().getString("auction-limit-reached-message")));
                        }
                        else
                            new MainAuctionMenu(player);
                    }
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e){
            if(inventory.equals(e.getInventory())) {
                if(Main.auctionsHandler.previewItems.containsKey(e.getPlayer().getUniqueId().toString()))
                    Main.auctionsDatabase.registerPreviewItem(player.getUniqueId().toString(), utils.itemToBase64(Main.auctionsHandler.previewItems.get(e.getPlayer().getUniqueId().toString())));
                HandlerList.unregisterAll(this);
                inventory = null;
                player = null;
            }
        }
    }

}
