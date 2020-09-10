package me.qKing12.AuctionMaster.InputGUIs.EditDurationGUI;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.InputGUIs.AnvilGUI;
import me.qKing12.AuctionMaster.InputGUIs.ChatListener;
import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Menus.AdminMenus.ViewAuctionAdminMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static me.qKing12.AuctionMaster.AuctionMaster.utilsAPI;

public class EditDurationGUI {
    private ItemStack paper;

    public interface EditDuration{
        void openGUI(Player p, Auction auction, String goBackTo, boolean rightClick);
    }

    public static EditDuration editDuration;

    public EditDurationGUI(){
        if(AuctionMaster.plugin.getConfig().getBoolean("use-chat-instead-sign")){
            editDuration =this::chatTrigger;
        }
        else if(AuctionMaster.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !AuctionMaster.hasProtocolLib){
            paper = new ItemStack(Material.PAPER);
            ArrayList<String> lore=new ArrayList<>();
            lore.add(utils.chat("&fEnter minutes"));
            lore.add(utils.chat("&fExamples: 20"));
            lore.add(utils.chat("&for -20 to speed"));
            paper= AuctionMaster.itemConstructor.getItem(paper, " ", lore);
            editDuration =this::anvilTrigger;
        }
        else{
            editDuration =this::signTrigger;
        }
    }

    private void signTrigger(Player p, Auction auction, String goBackTo, boolean rightClick){
        new EditDurationSignGUI(p, auction, goBackTo, rightClick);
    }

    private void anvilTrigger(Player p, Auction auction, String goBackTo, boolean rightClick){
        new AnvilGUI(p, paper, (reply) ->{
            try{
                int timeInput = Integer.parseInt(reply);
                if(rightClick)
                    auction.addMinutesToAuction(timeInput);
                else
                    auction.setEndingDate(ZonedDateTime.now().toInstant().toEpochMilli()+timeInput*60000);
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, "&cInvalid number."));
            }
            new ViewAuctionAdminMenu(p, auction, goBackTo);
            return null;
        });
    }

    private void chatTrigger(Player p, Auction auction, String goBackTo, boolean rightClick){
        p.sendMessage(utilsAPI.chat(p, "Enter minutes"));
        p.sendMessage(utilsAPI.chat(p, "Examples: 20"));
        p.sendMessage(utilsAPI.chat(p, "or -20 to speed"));
        p.closeInventory();
        new ChatListener(p, (reply) -> {
            try{
                int timeInput = Integer.parseInt(reply);
                if(rightClick)
                    auction.addMinutesToAuction(timeInput);
                else
                    auction.setEndingDate(ZonedDateTime.now().toInstant().toEpochMilli()+timeInput*60000);
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, "&cInvalid number."));
            }
            new ViewAuctionAdminMenu(p, auction, goBackTo);
        });
    }
}
