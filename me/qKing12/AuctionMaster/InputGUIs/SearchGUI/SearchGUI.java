package me.qKing12.AuctionMaster.InputGUIs.SearchGUI;

import me.qKing12.AuctionMaster.InputGUIs.AnvilGUI;
import me.qKing12.AuctionMaster.InputGUIs.ChatListener;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.BrowsingAuctionsMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SearchGUI {
    private ItemStack paper;

    public interface SearchFor{
        void openGUI(Player p, String category);
    }

    public static SearchFor searchFor;

    public SearchGUI(){
        if(Main.plugin.getConfig().getBoolean("use-chat-instead-sign")){
            searchFor=this::chatTrigger;
        }
        else if(Main.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !Main.hasProtocolLib){
            paper = new ItemStack(Material.PAPER);
            ArrayList<String> lore=new ArrayList<>();
            for(String line : Main.auctionsManagerCfg.getStringList("search-sign-message"))
                lore.add(utils.chat(line));
            paper=Main.itemConstructor.getItem(paper, " ", lore);
            searchFor=this::anvilTrigger;
        }
        else{
            searchFor=this::signTrigger;
        }
    }

    private void signTrigger(Player p, String category){
        new SearchSignGUI(p, category);
    }

    private void anvilTrigger(Player p, String category){
        new AnvilGUI(p, paper.clone(), (reply) ->{
            new BrowsingAuctionsMenu(p, category, 0, reply.equals("")?null:reply);
            return null;
        });
    }

    private void chatTrigger(Player p, String category){
        for(String line : Main.auctionsManagerCfg.getStringList("search-sign-message"))
            p.sendMessage(utils.chat(line));
        new ChatListener(p, (reply) -> {
            new BrowsingAuctionsMenu(p, category, 0, reply.equals("")?null:reply);
        });
    }

}
