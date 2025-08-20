package com.akashplugin.gui;

import com.akashplugin.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.List;

public class VoteGUI implements Listener {

    private final VoteManager manager;
    private final com.akashplugin.PremiumVote plugin;
    private final SignInput signInput;

    public VoteGUI(VoteManager manager, com.akashplugin.PremiumVote plugin, SignInput signInput) {
        this.signInput = signInput;
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openVoteMenu(Player player) {
        if (!manager.isOngoing()) {
            player.sendMessage(ChatColor.RED + "No election is currently running.");
            return;
        }

        int size = ((manager.getCandidates().size() / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_GREEN + "Vote for a Candidate");

        for (String candidate : manager.getCandidates()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(candidate);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);
                meta.setDisplayName(ChatColor.YELLOW + candidate);
                int currentVotes = manager.getResults().getOrDefault(candidate, 0);
                List<String> lore = Collections.singletonList(ChatColor.GRAY + "Current Votes: " + currentVotes);
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            gui.addItem(head);
        }

        // Add Back button
        gui.setItem(size - 1, GUIUtils.createItem(Material.ARROW, "⬅ Back to Menu"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onVoteClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Vote for a Candidate")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player voter = (Player) event.getWhoClicked();
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (itemName.equals("⬅ Back to Menu")) {
            new MainMenuGUI(manager, plugin, signInput).open(voter);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                String candidate = meta.getOwningPlayer().getName();

                if (voter.getName().equalsIgnoreCase(candidate)) {
                    voter.sendMessage(ChatColor.RED + "You cannot vote for yourself!");
                    voter.closeInventory();
                    return;
                }

                if (manager.castVote(voter, candidate)) {
                    voter.sendMessage(ChatColor.GREEN + "You voted for " + candidate + "!");
                } else {
                    voter.sendMessage(ChatColor.RED + "Vote failed! Maybe you already voted?");
                }
                voter.closeInventory();
            }
        }
    }
}
