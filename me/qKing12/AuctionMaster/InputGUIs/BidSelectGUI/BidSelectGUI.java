package me.qKing12.AuctionMaster.InputGUIs.BidSelectGUI;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.InputGUIs.AnvilGUI;
import me.qKing12.AuctionMaster.InputGUIs.ChatListener;
import me.qKing12.AuctionMaster.InputGUIs.StartingBidGUI.StartingBidSignGUI;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.CreateAuctionMainMenu;
import me.qKing12.AuctionMaster.Menus.ViewAuctionMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.hasProtocolLib;
import static me.qKing12.AuctionMaster.Main.utilsAPI;

public class BidSelectGUI {
    private ItemStack paper;

    public interface UpdatedBid{
        void openGUI(Player p, Auction auction, String goBackTo, double minimumBid);
    }

    public static UpdatedBid selectUpdateBid;

    public BidSelectGUI(){
        if(Main.plugin.getConfig().getBoolean("use-chat-instead-sign")){
            selectUpdateBid=this::chatTrigger;
        }
        else if(Main.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !hasProtocolLib){
            paper = new ItemStack(Material.PAPER);
            ArrayList<String> lore=new ArrayList<>();
            for(String line : Main.auctionsManagerCfg.getStringList("starting-bid-sign-message"))
                lore.add(utils.chat(line));
            paper=Main.itemConstructor.getItem(paper, " ", lore);
            selectUpdateBid=this::anvilTrigger;
        }
        else{
            selectUpdateBid=this::signTrigger;
        }
    }

    private void signTrigger(Player p, Auction auction, String goBackTo, double minimumBid){
        new BidSelectSignGUI(p, auction, goBackTo, minimumBid);
    }

    private void anvilTrigger(Player p, Auction auction, String goBackTo, double minimumBid){
        new AnvilGUI(p, paper, (reply) ->{
            try{
                double bidSelect = Main.numberFormatHelper.useDecimals? Double.parseDouble(reply):Math.floor(Double.parseDouble(reply));
                if(bidSelect>=minimumBid)
                    new ViewAuctionMenu(p, auction, goBackTo, bidSelect);
                else
                    new ViewAuctionMenu(p, auction, goBackTo, 0);
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("edit-bid-deny-message")));
                new ViewAuctionMenu(p, auction, goBackTo, 0);
            }

            return null;
        });
    }

    private void chatTrigger(Player p, Auction auction, String goBackTo, double minimumBid){
        for(String line : Main.auctionsManagerCfg.getStringList("starting-bid-sign-message"))
            p.sendMessage(utilsAPI.chat(p, line));
        new ChatListener(p, (reply) -> {
            try{
                double bidSelect = Main.numberFormatHelper.useDecimals? Double.parseDouble(reply):Math.floor(Double.parseDouble(reply));
                if(bidSelect>=minimumBid)
                    new ViewAuctionMenu(p, auction, goBackTo, bidSelect);
                else
                    new ViewAuctionMenu(p, auction, goBackTo, 0);
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("edit-bid-deny-message")));
                new ViewAuctionMenu(p, auction, goBackTo, 0);
            }

        });
    }
}
