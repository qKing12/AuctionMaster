package me.qKing12.AuctionMaster.Menus;

import me.qKing12.AuctionMaster.InputGUIs.StartingBidGUI.StartingBidGUI;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.AuctionMaster.*;

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
        if(AuctionMaster.plugin.getConfig().getBoolean("use-auction-limit")){
            for (int start = 28; start >= 0; start--)
                if (player.hasPermission("auctionmaster.limit.auctions." + start))
                    return start;
        }
        return 28;
    }

    private ItemStack getCreateAuctionItemYes(String displayName){
        ItemStack toReturn = AuctionMaster.configLoad.createAuctionConfirmYesMaterial.clone();
        ItemMeta meta = toReturn.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(player, AuctionMaster.configLoad.createAuctionConfirmYesName));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : AuctionMaster.configLoad.createAuctionConfirmYesLore)
            lore.add(utilsAPI.chat(player, line
                    .replace("%item-name%", displayName)
                    .replace("%duration%", startingDuration)
                    .replace("%starting-bid%", AuctionMaster.numberFormatHelper.formatNumber(startingBid))
                    .replace("%fee%", AuctionMaster.numberFormatHelper.formatNumber(startingBidFee+startingFeeTime))
            ));
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    private void generateCreateAuctionItemNo(){
        createAuctionItemNo = AuctionMaster.configLoad.createAuctionConfirmNoMaterial.clone();
        ItemMeta meta = createAuctionItemNo.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(player, AuctionMaster.configLoad.createAuctionConfirmNoName));
        ArrayList<String> lore = new ArrayList<>();
        for(String line : AuctionMaster.configLoad.createAuctionConfirmNoLore)
            lore.add(utilsAPI.chat(player, line));
        meta.setLore(lore);
        createAuctionItemNo.setItemMeta(meta);
    }

    private ItemStack transformToPreview(ItemStack toTransform){
        String name =utils.getDisplayName(toTransform);
        ItemStack toReturn=toTransform.clone();
        ItemMeta meta = toReturn.getItemMeta();
        meta.setDisplayName(utilsAPI.chat(player, AuctionMaster.auctionsManagerCfg.getString("preview-selected-item-name")));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(name);
        lore.add(" ");
        if(meta.getLore()!=null) {
            for (String line : meta.getLore())
                lore.add(utilsAPI.chat(player, line));
            lore.add(" ");
        }
        lore.add(utilsAPI.chat(player, AuctionMaster.auctionsManagerCfg.getString("preview-selected-item-take-back")));
        meta.setLore(lore);
        toReturn.setItemMeta(meta);
        return toReturn;
    }

    private void setupStartingBidItem(){
        ArrayList<String> lore = new ArrayList<>();
        if(buyItNow){
            if(auctionsHandler.buyItNowSelected!=null) {
                for (String line : AuctionMaster.configLoad.switchToAuctionLore)
                    lore.add(utilsAPI.chat(player, line));
                inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.switch-type-slot"), itemConstructor.getItem(AuctionMaster.configLoad.switchToAuctionMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.switchToAuctionName), lore));
            }

            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.editBINPriceLore)
                lore.add(utilsAPI.chat(player, line
                        .replace("%price%", AuctionMaster.numberFormatHelper.formatNumber(startingBid))
                        .replace("%fee%", AuctionMaster.numberFormatHelper.formatNumber(startingBidFee))
                ));
            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.starting-bid-slot"), itemConstructor.getItem(AuctionMaster.configLoad.editBINPriceMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.editBINPriceName.replace("%price%", AuctionMaster.numberFormatHelper.formatNumber(startingBid)).replace("%fee%", AuctionMaster.numberFormatHelper.formatNumber(startingBidFee))), lore));
        }
        else {
            if(auctionsHandler.buyItNowSelected!=null) {
                for (String line : AuctionMaster.configLoad.switchToBinLore)
                    lore.add(utilsAPI.chat(player, line));
                inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.switch-type-slot"), itemConstructor.getItem(AuctionMaster.configLoad.switchToBinMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.switchToBinName), lore));
            }

            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.startingBidItemLore)
                lore.add(utilsAPI.chat(player, line
                        .replace("%starting-bid%", AuctionMaster.numberFormatHelper.formatNumber(startingBid))
                        .replace("%starting-fee%", AuctionMaster.numberFormatHelper.formatNumber(startingBidFee))
                ));
            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.starting-bid-slot"), itemConstructor.getItem(AuctionMaster.configLoad.startingBidItemMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.startingBidItemName.replace("%starting-bid%", AuctionMaster.numberFormatHelper.formatNumber(startingBid)).replace("%starting-fee%", AuctionMaster.numberFormatHelper.formatNumber(startingBidFee))), lore));
        }
    }

    boolean buyItNow;

    public CreateAuctionMainMenu(Player player){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.player = player;
            inventory = Bukkit.createInventory(player, AuctionMaster.configLoad.createAuctionMenuSize, utilsAPI.chat(player, AuctionMaster.configLoad.createAuctionMenuName));

            generateCreateAuctionItemNo();
            if (auctionsHandler.buyItNowSelected != null && auctionsHandler.buyItNowSelected.contains(player.getUniqueId().toString()))
                buyItNow = true;
            else
                buyItNow = false;

            if (AuctionMaster.configLoad.useBackgoundGlass)
                for (int i = 0; i < AuctionMaster.configLoad.createAuctionMenuSize; i++)
                    inventory.setItem(i, AuctionMaster.configLoad.backgroundGlass.clone());

            ArrayList<String> lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.createAuctionPreviewLoreNoItem)
                lore.add(utilsAPI.chat(player, line));
            previewItem = itemConstructor.getItem(AuctionMaster.configLoad.createAuctionPreviewMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.createAuctionPreviewNameNoItem), lore);
            if (AuctionMaster.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())) {
                inventory.setItem(previewSlot = AuctionMaster.menusCfg.getInt("create-auction-menu.preview-item-slot"), transformToPreview(AuctionMaster.auctionsHandler.previewItems.get(player.getUniqueId().toString())));
            } else {
                inventory.setItem(previewSlot = AuctionMaster.menusCfg.getInt("create-auction-menu.preview-item-slot"), previewItem);
            }

            if (AuctionMaster.auctionsHandler.startingBid.containsKey(player.getUniqueId().toString()))
                startingBid = AuctionMaster.auctionsHandler.startingBid.get(player.getUniqueId().toString());
            else
                startingBid = AuctionMaster.configLoad.defaultStartingBid;
            startingBidFee = startingBid * (buyItNow ? configLoad.startingBidBINFee : AuctionMaster.configLoad.startingBidFee) / 100;
            if (!AuctionMaster.numberFormatHelper.useDecimals) {
                startingBid = Math.floor(startingBid);
                startingBidFee = Math.floor(startingBidFee);
            }

            setupStartingBidItem();

            long startingTime;
            if (AuctionMaster.auctionsHandler.startingDuration.containsKey(player.getUniqueId().toString()))
                startingTime = AuctionMaster.auctionsHandler.startingDuration.get(player.getUniqueId().toString());
            else
                startingTime = AuctionMaster.configLoad.defaultDuration;
            startingDuration = utils.fromMiliseconds((int) startingTime);
            String startingDuration = AuctionMaster.numberFormatHelper.useDecimals ? AuctionMaster.numberFormatHelper.formatNumber(startingFeeTime = AuctionMaster.configLoad.durationFeeCalculator((int) (startingTime / 3600000))) : AuctionMaster.numberFormatHelper.formatNumber(startingFeeTime = Math.floor(AuctionMaster.configLoad.durationFeeCalculator((int) (startingTime / 3600000))));
            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.durationItemLore)
                lore.add(utilsAPI.chat(player, line
                        .replace("%auction-time%", this.startingDuration)
                        .replace("%auction-fee%", startingDuration)
                ));
            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.duration-slot"), itemConstructor.getItem(AuctionMaster.configLoad.durationItemMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.durationItemName.replace("%auction-time%", this.startingDuration).replace("%auction-fee%", startingDuration)), lore));

            if (AuctionMaster.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString()))
                inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), getCreateAuctionItemYes(utils.getDisplayName(AuctionMaster.auctionsHandler.previewItems.get(player.getUniqueId().toString()))));
            else
                inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), createAuctionItemNo);

            lore = new ArrayList<>();
            for (String line : AuctionMaster.configLoad.goBackLore)
                lore.add(utilsAPI.chat(player, line));
            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.go-back-slot"), itemConstructor.getItem(AuctionMaster.configLoad.goBackMaterial, utilsAPI.chat(player, AuctionMaster.configLoad.goBackName), lore));

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().registerEvents(listener, AuctionMaster.plugin);
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
                    if(AuctionMaster.configLoad.isBlacklisted(e.getCurrentItem())){
                        player.sendMessage(utilsAPI.chat(player, AuctionMaster.plugin.getConfig().getString("blacklist-item-message")));
                        return;
                    }
                    utils.playSound(player, "inventory-item-click");
                    ItemStack saveCurrentItem=e.getCurrentItem().clone();
                    ItemStack toSet=transformToPreview(e.getCurrentItem());
                    if(AuctionMaster.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())){
                        player.getInventory().setItem(e.getSlot(), AuctionMaster.auctionsHandler.previewItems.get(player.getUniqueId().toString()));
                    }
                    else
                        player.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                    AuctionMaster.auctionsHandler.previewItems.put(player.getUniqueId().toString(), saveCurrentItem);
                    inventory.setItem(previewSlot, toSet);
                    inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), getCreateAuctionItemYes(utils.getDisplayName(saveCurrentItem)));
                }
                else{
                    if(e.getSlot()== AuctionMaster.menusCfg.getInt("create-auction-menu.duration-slot")) {
                        utils.playSound(player, "duration-item-click");
                        new DurationSelectMenu(player);
                    }
                    else if(e.getSlot()==previewSlot){
                        if(AuctionMaster.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString()) && player.getInventory().firstEmpty()!=-1){
                            player.getInventory().addItem(AuctionMaster.auctionsHandler.previewItems.get(player.getUniqueId().toString()));
                            AuctionMaster.auctionsHandler.previewItems.remove(player.getUniqueId().toString());
                            inventory.setItem(previewSlot, previewItem.clone());
                            inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.create-auction-button-slot"), createAuctionItemNo);
                        }
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("create-auction-menu.switch-type-slot")){
                        if(auctionsHandler.buyItNowSelected!=null){
                            if(buyItNow) {
                                auctionsHandler.buyItNowSelected.remove(player.getUniqueId().toString());
                                buyItNow=false;
                            }
                            else {
                                auctionsHandler.buyItNowSelected.add(player.getUniqueId().toString());
                                buyItNow=true;
                            }
                            startingBidFee=startingBid*(buyItNow? configLoad.startingBidBINFee: AuctionMaster.configLoad.startingBidFee)/100;
                            if(!AuctionMaster.numberFormatHelper.useDecimals){
                                startingBid=Math.floor(startingBid);
                                startingBidFee=Math.floor(startingBidFee);
                            }
                            setupStartingBidItem();
                            if(auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())){
                                inventory.setItem(AuctionMaster.menusCfg.getInt("create-auction-menu.create-auction-button-slot"),getCreateAuctionItemYes(utils.getDisplayName(auctionsHandler.previewItems.get(player.getUniqueId().toString()))));
                            }
                        }
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("create-auction-menu.starting-bid-slot")){
                        utils.playSound(player, "starting-bid-item-click");
                        StartingBidGUI.selectStartingBid.openGUI(player);
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("create-auction-menu.create-auction-button-slot")){
                        if(AuctionMaster.auctionsHandler.previewItems.containsKey(player.getUniqueId().toString())){
                            if(AuctionMaster.auctionsHandler.ownAuctions.getOrDefault(player.getUniqueId().toString(), new ArrayList<>()).size()<getMaximumAuctions()) {
                                if (AuctionMaster.economy.hasMoney(player, startingBidFee + startingFeeTime)) {
                                    utils.playSound(player, "create-auction-item-click");
                                    new CreateAuctionConfirmMenu(player, startingBidFee + startingFeeTime);
                                } else {
                                    player.sendMessage(utilsAPI.chat(player, AuctionMaster.auctionsManagerCfg.getString("not-enough-money-auction")));
                                }
                            }
                            else
                                player.sendMessage(utilsAPI.chat(player, AuctionMaster.plugin.getConfig().getString("auction-limit-reached-message")));
                        }
                    }
                    else if(e.getSlot()== AuctionMaster.menusCfg.getInt("create-auction-menu.go-back-slot")){
                        utils.playSound(player, "go-back-click");
                        if(AuctionMaster.auctionsHandler.ownAuctions.containsKey(player.getUniqueId().toString())){
                                new ManageOwnAuctionsMenu(player);
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
                if(AuctionMaster.auctionsHandler.previewItems.containsKey(e.getPlayer().getUniqueId().toString()))
                    AuctionMaster.auctionsDatabase.registerPreviewItem(player.getUniqueId().toString(), utils.itemToBase64(AuctionMaster.auctionsHandler.previewItems.get(e.getPlayer().getUniqueId().toString())));
                HandlerList.unregisterAll(this);
                inventory = null;
                player = null;
            }
        }
    }

}
