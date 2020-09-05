package me.qKing12.AuctionMaster;

import me.qKing12.AuctionMaster.FilesHandle.ConfigLoad;
import me.qKing12.AuctionMaster.Menus.AdminMenus.DeliveryAdminMenu;
import me.qKing12.AuctionMaster.Menus.AdminMenus.EndedAuctionsMenu;
import me.qKing12.AuctionMaster.Menus.AdminMenus.MainAdminMenu;
import me.qKing12.AuctionMaster.Menus.DeliveryPlayerMenu;
import me.qKing12.AuctionMaster.Menus.MainAuctionMenu;
import me.qKing12.AuctionMaster.Menus.ViewAuctionMenu;
import me.qKing12.AuctionMaster.Menus.ViewPlayerAuctions;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

import static me.qKing12.AuctionMaster.AuctionMaster.*;
import static org.bukkit.Bukkit.getServer;

public class Commands implements CommandExecutor {


    public Commands() {
        plugin.getCommand("auction").setExecutor(this);
        plugin.getCommand("ahadmin").setExecutor(this);
        plugin.getCommand("ahview").setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("auction")) {
                String canAuction = plugin.getConfig().getString("auction-use-permission");
                String canUseCommand = plugin.getConfig().getString("auction-command-use-permission");

                if(AuctionMaster.deliveries!=null && args.length>0 && args[0].equalsIgnoreCase("delivery")){
                    utils.playSound(p, "ah-delivery-command");
                    new DeliveryPlayerMenu(p, false);
                    return true;
                }

                if(canAuction.equals("none") || p.hasPermission(canAuction)) {
                    if(canUseCommand.equals("none") || p.hasPermission(canUseCommand)) {
                        if (args.length == 0) {
                            utils.playSound(p, "ah-command");
                            if(plugin.getConfig().getBoolean("auction-command-menu")) {
                                new MainAuctionMenu(p);
                            }
                            else
                                p.sendMessage(utilsAPI.chat(p, plugin.getConfig().getString("auction-command-missing")));
                            return true;
                        }
                        else {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                utils.playSound(p, "ah-player");
                                try {
                                    new ViewPlayerAuctions(p, Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString());
                                } catch (Exception e) {
                                    p.sendMessage(utilsAPI.chat(p, plugin.getConfig().getString("no-auctions-message")));
                                }
                            });
                        }
                        return true;
                    }
                    else{
                        p.sendMessage(utilsAPI.chat(p, plugin.getConfig().getString("auction-command-deny")));
                        return true;
                    }
                }
                else{
                    p.sendMessage(utilsAPI.chat(p, plugin.getConfig().getString("auction-no-permission")));
                    return true;
                }
            }
            else if (cmd.getName().equalsIgnoreCase("ahview")) {
                if(args.length>0){
                    try{
                        new ViewAuctionMenu(p, AuctionMaster.auctionsHandler.auctions.get(args[0]), "Close", 0);
                    }catch(Exception x){
                        p.sendMessage(utilsAPI.chat(p, AuctionMaster.bidsRelatedCfg.getString("too-late-to-open-now")));
                    }
                }
            }
            else if(cmd.getName().equalsIgnoreCase("ahadmin")) {
                if(!((Player) sender).hasPermission(plugin.getConfig().getString("admin-perks-use-permission"))){
                    sender.sendMessage(utilsAPI.chat((Player)sender, plugin.getConfig().getString("admin-perks-deny")));
                    return true;
                }
                if (args.length > 0) {
                    if(args[0].equalsIgnoreCase("reload")){
                        plugin.reloadConfig();
                        adminCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "admin_config.yml"));
                        buyItNowCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "buyItNow.yml"));
                        armorCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus/armor.yml"));
                        auctionsManagerCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "auctions_manager.yml"));
                        bidsRelatedCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "bids_related.yml"));
                        menusCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus.yml"));
                        blocksCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus/blocks.yml"));
                        consumablesCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus/consumables.yml"));
                        currencyCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "currency.yml"));
                        othersCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus/others.yml"));
                        soundsCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "sounds.yml"));
                        toolsCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus/tools.yml"));
                        weaponsCfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "menus/weapons.yml"));
                        configLoad=new ConfigLoad();
                        p.sendMessage(utils.chat("&aPlugin reloaded!"));
                        p.sendMessage(utils.chat("&aAdditional Note: &fReload works only on messages and some small settings. Things the plugin considers important need a restart to update."));
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("resetOwnAuctions")){
                        if(args.length>1){
                            String uuid=null;
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if(p2!=null){
                                uuid=p2.getUniqueId().toString();
                            }
                            else if(args[1].length()>16){
                                uuid=args[1];
                            }
                            if(uuid!=null){
                                auctionsDatabase.resetOwnAuctions(uuid);
                                if(auctionsHandler.ownAuctions.containsKey(uuid))
                                    auctionsHandler.ownAuctions.remove(uuid);
                                p.sendMessage(utils.chat("&aDone!"));
                            }
                        }
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("resetBiddedAuctions")){
                        if(args.length>1){
                            String uuid=null;
                            Player p2 = Bukkit.getPlayerExact(args[1]);
                            if(p2!=null){
                                uuid=p2.getUniqueId().toString();
                            }
                            else if(args[1].length()>16){
                                uuid=args[1];
                            }
                            if(uuid!=null){
                                auctionsDatabase.resetOwnBids(uuid);
                                if(auctionsHandler.bidAuctions.containsKey(uuid))
                                    auctionsHandler.bidAuctions.remove(uuid);
                                p.sendMessage(utils.chat("&aDone!"));
                            }
                        }
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("createNPC")){
                        if(AuctionMaster.auctionNPC!=null)
                            AuctionMaster.auctionNPC.createNpc(p);
                        else
                            p.sendMessage(utils.chat("&cYou either don't have Citizens Plugin or don't have auction-npc-use set to true in config.yml!"));
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("debugNames")){
                        if(AuctionMaster.auctionNPC!=null) {
                            AuctionMaster.auctionNPC.debugHolos();
                            p.sendMessage(utils.chat("&aDone!"));
                        }
                        else
                            p.sendMessage(utils.chat("&cYou either don't have Citizens Plugin or don't have auction-npc-use set to true in config.yml!"));
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("delivery")){
                        if(args.length>1)
                            new DeliveryAdminMenu(p, args[1]);
                        else
                            new DeliveryAdminMenu(p, null);
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("manage")) {
                        new EndedAuctionsMenu(p, 0);
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("give")) {
                        if (args.length >= 2) {
                            Player p2 = getServer().getPlayer(args[1]);
                            if (p2 == null) {
                                sender.sendMessage(utils.chat("&cInvalid player."));
                                return false;
                            }
                            if (args.length == 3) {
                                try {
                                    p2.getInventory().addItem(utils.itemFromBase64(args[2].replace("\\n", "\r\n").replace("%nll%", "\r\n")));
                                    sender.sendMessage(utils.chat("&aDone!"));
                                    return true;
                                } catch (Exception e) {
                                    sender.sendMessage(utils.chat("&cThe given item is invalid."));
                                }
                            } else
                                sender.sendMessage(utils.chat("&cPlease specify an item to give"));
                        }
                        sender.sendMessage(utils.chat("&cUsage: /ahadmin give <player> <base64 item>"));
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("forceopen")) {
                        if (args.length >= 2) {
                            Player p2 = getServer().getPlayer(args[1]);
                            if (p2 == null) {
                                sender.sendMessage(utils.chat("&cInvalid player."));
                                return false;
                            }
                            new MainAuctionMenu(p2);
                            sender.sendMessage(utils.chat("&aMenu forced open!"));
                        } else
                            sender.sendMessage(utils.chat("&cUsage: /ahadmin forceopen <player>"));
                        return true;
                    } else if (args.length == 3 && args[0].equalsIgnoreCase("transfer")) {
                        Player p2 = getServer().getPlayer(args[2]);
                        if (p2 == null) {
                            sender.sendMessage(utils.chat("&cThe player you transfer the auctions to has to be online."));
                            return false;
                        }
                        //transferAuctions(Main.dataManager.dataGet(args[1]), p);
                        sender.sendMessage(utils.chat("&aTransfered auctions from " + args[1] + " to " + p2.getName()));
                        return true;
                    }
                    else{
                        p.sendMessage(utils.chat("&7&m---------------------------------------"));
                        p.sendMessage(utils.chat("&c/ahadmin createnpc &8- &7Creates a AuctionMaster NPC"));
                        p.sendMessage(utils.chat("&c/ahadmin debugNames &8- &7Debug names of all NPCs"));
                        p.sendMessage(utils.chat("&c/ahadmin delivery <player name> &8- &7Open Delivery Menu with a Selected Player"));
                        p.sendMessage(utils.chat("&c/ahadmin manage ended &8- &7Open the Manage Auctions Menu for Ended Auctions"));
                        p.sendMessage(utils.chat("&c/ahadmin forceopen <player> &8- &7Force opens the auction menu to a player"));
                        p.sendMessage(utils.chat("&c/ahadmin reload &8- &7Reloads the plugin"));
                        p.sendMessage(utils.chat("&c/ahadmin give <player> <base64 item> &8- &7(Console Only) Advanced command, please read it''s use on spigot page"));
                        p.sendMessage(utils.chat("&7&m---------------------------------------"));
                    }
                }
                else{
                    new MainAdminMenu(p);
                }
            }
        }
        else {
            if(cmd.getName().equalsIgnoreCase("ahadmin")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("give")) {
                        if (args.length >= 2) {
                            Player p = getServer().getPlayer(args[1]);
                            if (p == null) {
                                sender.sendMessage(utils.chat("&cInvalid player."));
                                return false;
                            }
                            if (args.length == 3) {
                                try {
                                    p.getInventory().addItem(utils.itemFromBase64(args[2].replace("\\n", "\r\n").replace("%nll%", "\r\n")));
                                    sender.sendMessage(utils.chat("&aDone!"));
                                    return true;
                                } catch (Exception e) {
                                    sender.sendMessage(utils.chat("&cThe given item is invalid."));
                                }
                            } else
                                sender.sendMessage(utils.chat("&cPlease specify an item to give"));
                        }
                        sender.sendMessage(utils.chat("&cUsage: /ahadmin give <player> <base64 item>"));
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("forceopen")) {
                        if (args.length >= 2) {
                            Player p = getServer().getPlayer(args[1]);
                            if (p == null) {
                                sender.sendMessage(utils.chat("&cInvalid player."));
                                return false;
                            }
                            new MainAuctionMenu(p);
                            sender.sendMessage(utils.chat("&aMenu forced open!"));
                        } else
                            sender.sendMessage(utils.chat("&cUsage: /ahadmin forceopen <player>"));
                        return true;
                    } else if (args.length == 3 && args[0].equalsIgnoreCase("transfer")) {
                        Player p = getServer().getPlayer(args[2]);
                        if (p == null) {
                            sender.sendMessage(utils.chat("&cThe player you transfer the auctions to has to be online."));
                            return false;
                        }
                        //transferAuctions(Main.dataManager.dataGet(args[1]), p);
                        sender.sendMessage(utils.chat("&aTransfered auctions from " + args[1] + " to " + p.getName()));
                        return true;
                    }
                }
                sender.sendMessage(utils.chat("&cOnly commands usable in console:"));
                sender.sendMessage(utils.chat("&c/ahadmin give <player> <base64 item>"));
                sender.sendMessage(utils.chat("&c/ahadmin forceopen <player>"));
            }
        }

        return false;
    }
}
