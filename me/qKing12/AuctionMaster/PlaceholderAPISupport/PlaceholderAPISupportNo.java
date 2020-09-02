package me.qKing12.AuctionMaster.PlaceholderAPISupport;

import com.google.common.base.Charsets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlaceholderAPISupportNo implements PlaceholderAPISupport {

    public String chat (Player p, String s){
        if(s==null)
            return ChatColor.translateAlternateColorCodes('&', "&cConfig Missing Text");
        byte[] temp = s.getBytes(Charsets.UTF_8);
        return ChatColor.translateAlternateColorCodes('&', new String(temp, Charsets.UTF_8));
    }

}
