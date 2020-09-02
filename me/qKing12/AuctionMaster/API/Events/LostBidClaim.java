package me.qKing12.AuctionMaster.API.Events;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LostBidClaim extends Event{
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Auction auction;
    private double bidAmount;

    public LostBidClaim(Player player, Auction auction, double bidAmount) {
        this.player = player;
        this.auction=auction;
        this.bidAmount=bidAmount;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Auction getAuction() {
        return this.auction;
    }

    public double getBidAmount() {
        return this.bidAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
