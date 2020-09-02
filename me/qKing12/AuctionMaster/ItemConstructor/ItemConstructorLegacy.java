package me.qKing12.AuctionMaster.ItemConstructor;

import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static me.qKing12.AuctionMaster.Utils.SkullTexture.getSkull;

public class ItemConstructorLegacy implements ItemConstructor {

    public ItemStack getItem(Material material, String name, ArrayList<String> lore){
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
        meta.setDisplayName(utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(Material material, short data, String name, ArrayList<String> lore){
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(String material, String name, ArrayList<String> lore){
        ItemStack item = getItemFromMaterial(material);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItem(ItemStack itemFrom, String name, ArrayList<String> lore){
        ItemStack item = itemFrom.clone();
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(utils.chat(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getItemFromMaterial(String material){
        if(material.startsWith("skull:") || material.startsWith("head:"))
            return getSkull(material.split(":")[1]);
        Material mat = Material.getMaterial(material);
        if(mat!=null)
            return new ItemStack(mat, 1);
        else{
            short data;
            if(material.split(":").length==2)
                data = Short.parseShort(material.split(":")[1]);
            else
                data = 0;
            int id = Integer.parseInt(material.split(":")[0]);
            String idFinal = utils.getIdF(id);
            if(data==0)
                return new ItemStack(Material.getMaterial(idFinal), 1);
            else
                return new ItemStack(Material.getMaterial(idFinal), 1, data);
        }
    }

}

