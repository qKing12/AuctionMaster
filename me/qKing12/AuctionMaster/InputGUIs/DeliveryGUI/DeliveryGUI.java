package me.qKing12.AuctionMaster.InputGUIs.DeliveryGUI;

import me.qKing12.AuctionMaster.InputGUIs.AnvilGUI;
import me.qKing12.AuctionMaster.InputGUIs.ChatListener;
import me.qKing12.AuctionMaster.InputGUIs.SearchGUI.SearchSignGUI;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.AdminMenus.DeliveryAdminMenu;
import me.qKing12.AuctionMaster.Menus.BrowsingAuctionsMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class DeliveryGUI {
    private ItemStack paper;

    public interface DeliveryInstance {
        void openGUI(Player p);
    }

    public static DeliveryInstance deliveryInstance;

    public DeliveryGUI(){
        if(Main.plugin.getConfig().getBoolean("use-chat-instead-sign")){
            deliveryInstance =this::chatTrigger;
        }
        else if(Main.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !Main.hasProtocolLib){
            paper = new ItemStack(Material.PAPER);
            ArrayList<String> lore=new ArrayList<>();
            lore.add(utils.chat("&7^^^^^^^^^^^^^^^"));
            lore.add(utils.chat("&fPlease enter the player's"));
            lore.add(utils.chat("&fname whose deliveeries you"));
            lore.add(utils.chat("&fwant to manage."));
            paper=Main.itemConstructor.getItem(paper, " ", lore);
            deliveryInstance =this::anvilTrigger;
        }
        else{
            deliveryInstance =this::signTrigger;
        }
    }

    private void signTrigger(Player p){
        new DeliverySignGUI(p);
    }

    private void anvilTrigger(Player p){
        new AnvilGUI(p, paper.clone(), (reply) ->{
            new DeliveryAdminMenu(p, reply.replace(" ", "").equals("")?null:reply);
            return null;
        });
    }

    private void chatTrigger(Player p){
        p.sendMessage(utils.chat("&7&m----------------"));
        p.sendMessage(utils.chat("&fPlease enter the player's"));
        p.sendMessage(utils.chat("&fname whose deliveeries you"));
        p.sendMessage(utils.chat("&fwant to manage."));
        new ChatListener(p, (reply) -> {
            new DeliveryAdminMenu(p, reply.replace(" ", "").equals("")?null:reply);
        });
    }

}
