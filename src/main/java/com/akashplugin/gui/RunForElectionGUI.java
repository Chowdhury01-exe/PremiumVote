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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RunForElectionGUI implements Listener {

    private final VoteManager manager;
    private static final String GUI_TITLE = ChatColor.GOLD + "💼 Confirm Candidacy";
    private final PremiumVote plugin;
    private final SignInput signInput;

    public RunForElectionGUI(VoteManager manager, PremiumVote plugin, SignInput signInput) {
        this.signInput = signInput;
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);
        if(!manager.isOngoing()) {
            player.sendMessage(ChatColor.RED + "No election is currently running.");
            return;
        }
        boolean alreadyCandidate = manager.getCandidates().contains(player.getUniqueId().toString());

        // Status item in center
        gui.setItem(13, GUIUtils.createItem(
                Material.WRITABLE_BOOK,
                ChatColor.YELLOW + "Your Status",
                ChatColor.GRAY + "Already Candidate: " + (alreadyCandidate ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                ChatColor.GRAY + "Total Candidates: " + ChatColor.AQUA + manager.getCandidates().size(),
                ChatColor.GRAY + "Ongoing Election: " + (manager.isOngoing() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
        ));

        // Confirm button
        if (alreadyCandidate) {
            gui.setItem(11, GUIUtils.createItem(
                    Material.RED_STAINED_GLASS_PANE,
                    ChatColor.DARK_RED + "Already a Candidate",
                    ChatColor.GRAY + "You cannot register twice."
            ));
        } else {
            gui.setItem(11, GUIUtils.createItem(
                    Material.EMERALD_BLOCK,
                    ChatColor.GREEN + "Confirm - Run for Election",
                    ChatColor.GRAY + "Click to register yourself",
                    ChatColor.GRAY + "as a candidate in this election."
            ));
        }

        // Cancel button
        gui.setItem(15, GUIUtils.createItem(Material.BARRIER, ChatColor.RED + "Cancel", ChatColor.GRAY + "Return to the main menu"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true); // stop item movement

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (name.contains("Confirm - Run for Election")) {
            if (!player.hasPermission("premiumvote.run")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to run for election.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            boolean success = manager.registerCandidate(player);

            if (success) {
                player.sendMessage(ChatColor.GREEN + "✅ You are now a candidate in the election!");
                Bukkit.broadcastMessage(ChatColor.GOLD + "📢 " + player.getName() + " has joined the election as a candidate!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            } else {
                player.sendMessage(ChatColor.YELLOW + "⚠ You are already a candidate.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
            player.closeInventory();

        } else if (name.contains("Already a Candidate")) {
            player.sendMessage(ChatColor.RED + "You are already a candidate. You cannot register twice.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);

        } else if (name.contains("Cancel")) {
            player.sendMessage(ChatColor.GRAY + "Cancelled.");
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true); // stop dragging in GUI
        }
    }
}
