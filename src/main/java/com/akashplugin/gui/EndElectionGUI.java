package com.akashplugin.gui;

import com.akashplugin.PremiumVote;
import com.akashplugin.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EndElectionGUI implements Listener {

    private final VoteManager manager;
    private final PremiumVote plugin;
    private final SignInput signInput;

    public EndElectionGUI(VoteManager manager, PremiumVote plugin, SignInput signInput) {
        this.plugin = plugin;
        this.signInput = signInput;
        this.manager = manager;
    }

    public void open(Player player) {


        if (!manager.isOngoing()) {
            player.sendMessage(ChatColor.RED + "No election is currently running.");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "⛔ Stop Election");


        gui.setItem(4, GUIUtils.createItem(Material.WRITABLE_BOOK,
                ChatColor.YELLOW + "Current Status",
                ChatColor.GRAY + "Ongoing: " + manager.isOngoing(),
                ChatColor.GRAY + "Total Candidates: " + manager.getCandidates().size(),
                ChatColor.GRAY + "Votes so far: " + manager.getTotalVotes()));

        gui.setItem(11, GUIUtils.createItem(Material.BARRIER, "§cConfirm - Stop Election"));
        gui.setItem(15, GUIUtils.createItem(Material.ARROW, "§7Cancel"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.RED + "⛔ Stop Election")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        switch (name) {
            case "Confirm - Stop Election":
                if (!player.hasPermission("premiumvote.admin.stop")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to stop an election.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    break;
                }
                if (!manager.isOngoing()) {
                    player.sendMessage(ChatColor.YELLOW + "There is no ongoing election to stop.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                } else {
                    manager.endElection();
                    player.sendMessage(ChatColor.GREEN + "Election has been stopped.");
                    Bukkit.broadcastMessage(ChatColor.RED + "📢 The election has ended. Check results now!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                }
                player.closeInventory();
                break;

            case "Cancel":
                player.sendMessage(ChatColor.GRAY + "Cancelled.");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                player.closeInventory();
                break;
        }
    }
}
