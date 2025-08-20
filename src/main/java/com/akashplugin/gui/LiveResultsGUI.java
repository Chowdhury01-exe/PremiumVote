package com.akashplugin.gui;

import com.akashplugin.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.Map;

public class LiveResultsGUI implements Listener {

    private final VoteManager manager;
    private final com.akashplugin.PremiumVote plugin;
    private final SignInput signInput;

    public LiveResultsGUI(VoteManager manager, com.akashplugin.PremiumVote plugin , SignInput signInput) {
        this.plugin = plugin;
        this.manager = manager;
        this.signInput = signInput;
    }

    public void open(Player player) {
        if (!manager.isOngoing()) {
            player.sendMessage(ChatColor.RED + "No election is currently running.");
            return;
        }

        player.openInventory(createInventory());
    }

    private Inventory createInventory() {
        Map<String, Integer> results = manager.getResults();
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "📊 Live Results");

        int index = 0;
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + entry.getKey());
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getKey()));
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Votes: " + entry.getValue()));
                head.setItemMeta(meta);
            }
            gui.setItem(index++, head);
        }

        // Back button
        gui.setItem(53, GUIUtils.createItem(Material.ARROW, "⬅ Back to Menu"));

        return gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "📊 Live Results")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (name.equals("⬅ Back to Menu")) {
            new MainMenuGUI(manager, plugin, signInput).open(player);
        }
    }

    /**
     * Optional: Refresh GUI for a player
     */
    public void refresh(Player player) {
        if (player.getOpenInventory().getTitle().equals(ChatColor.DARK_PURPLE + "📊 Live Results")) {
            player.getOpenInventory().getTopInventory().setContents(createInventory().getContents());
        }
    }
}
