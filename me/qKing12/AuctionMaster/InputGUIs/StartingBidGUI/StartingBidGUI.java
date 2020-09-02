package me.qKing12.AuctionMaster.InputGUIs.StartingBidGUI;

import me.qKing12.AuctionMaster.InputGUIs.AnvilGUI;
import me.qKing12.AuctionMaster.InputGUIs.ChatListener;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.CreateAuctionMainMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.utilsAPI;

public class StartingBidGUI {
    private ItemStack paper;

    public interface StartingBid{
        void openGUI(Player p);
    }

    public static StartingBid selectStartingBid;

    public StartingBidGUI(){
        if(Main.plugin.getConfig().getBoolean("use-chat-instead-sign")){
            selectStartingBid=this::chatTrigger;
        }
        else if(Main.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !Main.hasProtocolLib){
            paper = new ItemStack(Material.PAPER);
            ArrayList<String> lore=new ArrayList<>();
            for(String line : Main.auctionsManagerCfg.getStringList("starting-bid-sign-message"))
                lore.add(utils.chat(line));
            paper=Main.itemConstructor.getItem(paper, " ", lore);
            selectStartingBid=this::anvilTrigger;
        }
        else{
            selectStartingBid=this::signTrigger;
        }
    }

    private void signTrigger(Player p){
        new StartingBidSignGUI(p);
    }

    private void anvilTrigger(Player p){
        new AnvilGUI(p, paper, (reply) ->{
            try{
                double timeInput = Main.numberFormatHelper.useDecimals? Double.parseDouble(reply):Math.floor(Double.parseDouble(reply));
                if(timeInput<1){
                    p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("starting-bid-sign-deny")));
                }
                else
                    Main.auctionsHandler.startingBid.put(p.getUniqueId().toString(), timeInput);
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("starting-bid-sign-deny")));
            }

            new CreateAuctionMainMenu(p);
            return null;
        });
    }

    private void chatTrigger(Player p){
        for(String line : Main.auctionsManagerCfg.getStringList("starting-bid-sign-message"))
            p.sendMessage(utilsAPI.chat(p, line));
        new ChatListener(p, (reply) -> {
            try {
                double timeInput = Main.numberFormatHelper.useDecimals ? Double.parseDouble(reply) : Math.floor(Double.parseDouble(reply));
                if (timeInput < 1) {
                    p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("starting-bid-sign-deny")));
                } else
                    Main.auctionsHandler.startingBid.put(p.getUniqueId().toString(), timeInput);
            } catch (Exception x) {
                p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("starting-bid-sign-deny")));
            }

            new CreateAuctionMainMenu(p);
        });
    }
}
