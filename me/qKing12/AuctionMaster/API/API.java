package me.qKing12.AuctionMaster.API;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.Categories.Category;
import me.qKing12.AuctionMaster.Main;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class API {

    public static Auction getAuction(String id){
        return Main.auctionsHandler.auctions.getOrDefault(id, null);
    }

    public static Category getCategory(String category){
        return Main.auctionsHandler.getCategory(category);
    }

    public static ArrayList<Auction> getBiddedAuctions(String playerUUID){
        return Main.auctionsHandler.bidAuctions.getOrDefault(playerUUID, new ArrayList<>());
    }

    public static ArrayList<Auction> getOwnAuctions(String playerUUID){
        return Main.auctionsHandler.ownAuctions.getOrDefault(playerUUID, new ArrayList<>());
    }

    public static double getDeliveryCoins(String playerUUID){
        return Main.deliveries.getCoins(playerUUID);
    }

    public static ArrayList<ItemStack> getDeliveryItems(String playerUUID){
        return Main.deliveries.getDeliveryItems(playerUUID);
    }

    public static void sendDeliveryCoins(String playerUUID, double coins){
        Main.deliveries.addCoins(playerUUID, coins);
    }

    public static void sendDeliveryItem(String playerUUID, ItemStack item){
        Main.deliveries.addItem(playerUUID, item);
    }

    public static void setDeliveryCoins(String playerUUID, double coins){
        Main.deliveries.setCoins(playerUUID, coins);
    }

    public static void setDeliveryItems(String playerUUID, ArrayList<ItemStack> items){
        Main.deliveries.setItems(playerUUID, items);
    }

    public static void removeDeliveries(String playerUUID){
        Main.deliveries.removeDelivery(playerUUID);
    }

    public static void setDeliveryItemsAndCoins(String playerUUID, ArrayList<ItemStack> items, double coins){
        Main.deliveries.setCoinsAndItems(playerUUID, items, coins);
    }
}
