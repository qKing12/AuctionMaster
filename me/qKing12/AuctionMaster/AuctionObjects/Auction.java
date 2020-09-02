package me.qKing12.AuctionMaster.AuctionObjects;

import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Utils.utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZonedDateTime;
import java.util.*;

public interface Auction {
    boolean isBIN();

     boolean checkForDeletion();

     boolean forceEnd();

     void addMinutesToAuction(int minutes);

     void setEndingDate(long date);

     void adminRemoveAuction(boolean withDeliveries);

     void sellerClaim(Player player);

     void claimBid(Player player);

     void topBidClaim(Player player);

     void normalBidClaim(Player player);

     boolean placeBid(Player player, double amount, int cacheBids);

     Bids.Bid getLastBid(String uuid);

     boolean isEnded();

     ItemStack getBidHistory();

     ItemStack generateDisplay();

     ItemStack getUpdatedDisplay();

     String getSellerUUID();

     String getSellerDisplayName();

     String getSellerName();

     String getId();

     Bids getBids();

     long getEndingDate();

     ItemStack getItemStack();

     String getDisplayName();

     double getCoins();
}
