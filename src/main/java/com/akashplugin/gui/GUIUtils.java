package com.akashplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GUIUtils {
    

    // Name only
    public static ItemStack createItem(Material material, String name) {
        return createItem(material, name, Collections.emptyList());
    }

    // Name + single lore line
    public static ItemStack createItem(Material material, String name, String lore) {
        return createItem(material, name, Collections.singletonList(lore));
    }

    // Name + multiple lore lines (varargs so you can pass any number)
    public static ItemStack createItem(Material material, String name, String... loreLines) {
        return createItem(material, name, Arrays.asList(loreLines));
    }
    public static ItemStack createPlayerHead(String playerName, String displayName, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            meta.setDisplayName(displayName);
            if (lore != null) meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    // Name + lore list
    public static ItemStack createItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null && !loreLines.isEmpty()) {
                meta.setLore(loreLines);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
