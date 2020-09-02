package me.qKing12.AuctionMaster.InputGUIs.DurationSelectGUI;

import me.qKing12.AuctionMaster.InputGUIs.AnvilGUI;
import me.qKing12.AuctionMaster.InputGUIs.ChatListener;
import me.qKing12.AuctionMaster.Main;
import me.qKing12.AuctionMaster.Menus.CreateAuctionMainMenu;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Main.utilsAPI;

public class SelectDurationGUI {
    private ItemStack paper;

    public interface SelectDuration{
        void openGUI(Player p, int maximum_hours, boolean minutes);
    }

    public static SelectDuration selectDuration;

    public SelectDurationGUI(){
        if(Main.plugin.getConfig().getBoolean("use-chat-instead-sign")){
            selectDuration=this::chatTrigger;
        }
        else if(Main.plugin.getConfig().getBoolean("use-anvil-instead-sign") || !Main.hasProtocolLib){
            paper = new ItemStack(Material.PAPER);
            ArrayList<String> lore=new ArrayList<>();
            for(String line : Main.auctionsManagerCfg.getStringList("duration-sign-message"))
                lore.add(utils.chat(line));
            paper=Main.itemConstructor.getItem(paper, " ", lore);
            selectDuration=this::anvilTrigger;
        }
        else{
            selectDuration=this::signTrigger;
        }
    }

    private void signTrigger(Player p, int maximum_hours, boolean minutes){
        new SelectDurationSignGUI(p, maximum_hours, minutes);
    }

    private void anvilTrigger(Player p, int maximum_hours, boolean minutes){
        ItemStack paperClone = paper.clone();
        ItemMeta meta = paperClone.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        for(String line : meta.getLore())
            lore.add(line.replace("%time-format%", minutes?Main.configLoad.minutes:Main.configLoad.hours));
        new AnvilGUI(p, paperClone, (reply) ->{
            try{
                int timeInput = Integer.parseInt(reply);
                if(minutes){
                    if(timeInput>59 || timeInput<1){
                        p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("duration-sign-deny")));
                    }
                    else {
                        Main.auctionsHandler.startingDuration.put(p.getUniqueId().toString(), timeInput*60000);
                    }
                }
                else{
                    if(timeInput>168 || timeInput<1){
                        p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("duration-sign-deny")));
                    }
                    else{
                        if(maximum_hours!=-1 && maximum_hours<timeInput)
                            p.sendMessage(utilsAPI.chat(p, Main.plugin.getConfig().getString("duration-limit-reached-message")));
                        else
                            Main.auctionsHandler.startingDuration.put(p.getUniqueId().toString(), timeInput*3600000);
                    }
                }
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("duration-sign-deny")));
            }

            new CreateAuctionMainMenu(p);
            return null;
        });
    }

    private void chatTrigger(Player p, int maximum_hours, boolean minutes){
        for(String line : Main.auctionsManagerCfg.getStringList("duration-sign-message"))
            p.sendMessage(utils.chat(line.replace("%time-format%", minutes?Main.configLoad.minutes:Main.configLoad.hours)));
        new ChatListener(p, (reply) ->{
            try{
                int timeInput = Integer.parseInt(reply);
                if(minutes){
                    if(timeInput>59 || timeInput<1){
                        p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("duration-sign-deny")));
                    }
                    else {
                        Main.auctionsHandler.startingDuration.put(p.getUniqueId().toString(), timeInput*60000);
                    }
                }
                else{
                    if(timeInput>168 || timeInput<1){
                        p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("duration-sign-deny")));
                    }
                    else{
                        if(maximum_hours!=-1 && maximum_hours<timeInput)
                            p.sendMessage(utilsAPI.chat(p, Main.plugin.getConfig().getString("duration-limit-reached-message")));
                        else
                            Main.auctionsHandler.startingDuration.put(p.getUniqueId().toString(), timeInput*3600000);
                    }
                }
            }catch(Exception x){
                p.sendMessage(utilsAPI.chat(p, Main.auctionsManagerCfg.getString("duration-sign-deny")));
            }

            new CreateAuctionMainMenu(p);
        });
    }

}
