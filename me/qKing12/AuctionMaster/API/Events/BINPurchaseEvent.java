package me.qKing12.AuctionMaster.API.Events;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BINPurchaseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Auction auction;
    private double pricePaid;

    public BINPurchaseEvent(Player player, Auction auction, double pricePaid) {
        this.player = player;
        this.auction=auction;
        this.pricePaid=pricePaid;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Auction getAuction() {
        return this.auction;
    }

    public double getPricePaid() {
        return this.pricePaid;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
