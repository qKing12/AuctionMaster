package me.qKing12.AuctionMaster.API.Events;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SellerClaimEndedAuctionEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Auction auction;
    private double toClaim;

    public SellerClaimEndedAuctionEvent(Player player, Auction auction, double toClaim) {
        this.player = player;
        this.auction=auction;
        this.toClaim=toClaim;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Auction getAuction() {
        return this.auction;
    }

    public double getToClaim() {
        return this.toClaim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
