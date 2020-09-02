package me.qKing12.AuctionMaster.API.Events;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class SellerClaimExpiredAuctionEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Auction auction;
    private ItemStack item;

    public SellerClaimExpiredAuctionEvent(Player player, Auction auction, ItemStack item) {
        this.player = player;
        this.auction=auction;
        this.item=item;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Auction getAuction() {
        return this.auction;
    }

    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
