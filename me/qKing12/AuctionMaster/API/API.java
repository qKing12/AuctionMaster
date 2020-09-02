package me.qKing12.AuctionMaster.API;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.Categories.Category;
import me.qKing12.AuctionMaster.AuctionMaster;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class API {

    public static Auction getAuction(String id){
        return AuctionMaster.auctionsHandler.auctions.getOrDefault(id, null);
    }

    public static Category getCategory(String category){
        return AuctionMaster.auctionsHandler.getCategory(category);
    }

    public static ArrayList<Auction> getBiddedAuctions(String playerUUID){
        return AuctionMaster.auctionsHandler.bidAuctions.getOrDefault(playerUUID, new ArrayList<>());
    }

    public static ArrayList<Auction> getOwnAuctions(String playerUUID){
        return AuctionMaster.auctionsHandler.ownAuctions.getOrDefault(playerUUID, new ArrayList<>());
    }

    public static double getDeliveryCoins(String playerUUID){
        return AuctionMaster.deliveries.getCoins(playerUUID);
    }

    public static ArrayList<ItemStack> getDeliveryItems(String playerUUID){
        return AuctionMaster.deliveries.getDeliveryItems(playerUUID);
    }

    public static void sendDeliveryCoins(String playerUUID, double coins){
        AuctionMaster.deliveries.addCoins(playerUUID, coins);
    }

    public static void sendDeliveryItem(String playerUUID, ItemStack item){
        AuctionMaster.deliveries.addItem(playerUUID, item);
    }

    public static void setDeliveryCoins(String playerUUID, double coins){
        AuctionMaster.deliveries.setCoins(playerUUID, coins);
    }

    public static void setDeliveryItems(String playerUUID, ArrayList<ItemStack> items){
        AuctionMaster.deliveries.setItems(playerUUID, items);
    }

    public static void removeDeliveries(String playerUUID){
        AuctionMaster.deliveries.removeDelivery(playerUUID);
    }

    public static void setDeliveryItemsAndCoins(String playerUUID, ArrayList<ItemStack> items, double coins){
        AuctionMaster.deliveries.setCoinsAndItems(playerUUID, items, coins);
    }
}
