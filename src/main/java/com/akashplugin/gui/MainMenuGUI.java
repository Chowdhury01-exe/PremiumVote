package com.akashplugin.gui;

import com.akashplugin.VoteManager;
import com.akashplugin.PremiumVote;
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

public class MainMenuGUI implements Listener {

    private final VoteManager manager;
    private final PremiumVote plugin;
    private final SignInput signInput;

    public MainMenuGUI(VoteManager manager, PremiumVote plugin, SignInput signInput) {
        this.manager = manager;
        this.plugin = plugin;
        this.signInput = signInput;
    }

    public void open(Player player) {
        if (!player.hasPermission("premiumvote.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to access the voting menu.");
            return;
        }

        Inventory menu = Bukkit.createInventory(null, 45, ChatColor.DARK_AQUA + "\uD83D\uDCCB Election Menu");

        // Filler items
        ItemStack blackGlass = GUIUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack saffronGlass = GUIUtils.createItem(Material.ORANGE_STAINED_GLASS_PANE, " ");
        ItemStack whiteGlass = GUIUtils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ");
        ItemStack greenGlass = GUIUtils.createItem(Material.GREEN_STAINED_GLASS_PANE, " ");
        ItemStack blueGlass = GUIUtils.createItem(Material.BLUE_STAINED_GLASS_PANE, " "); // Ashoka Chakra

        // Fill everything with black first
        for (int i = 0; i < 45; i++) {
            menu.setItem(i, blackGlass);
        }

        // Fill Indian flag colors in the center 3 rows
        for (int slot = 9; slot <= 17; slot++) menu.setItem(slot, saffronGlass); // Saffron
        for (int slot = 18; slot <= 26; slot++) menu.setItem(slot, whiteGlass);  // White
        for (int slot = 27; slot <= 35; slot++) menu.setItem(slot, greenGlass);  // Green

        // Add Ashoka Chakra in the center of white row
        menu.setItem(22, blueGlass);

        // Functional items overwrite fillers
        if (player.hasPermission("premiumvote.vote"))
            menu.setItem(10, GUIUtils.createItem(Material.BOOK, "\uD83D\uDDF3️ Vote Now"));

        if (player.hasPermission("premiumvote.run"))
            menu.setItem(28, GUIUtils.createItem(Material.IRON_SWORD, "\uD83D\uDCBC Run for Election"));

        if (player.hasPermission("premiumvote.viewresults.live"))
            menu.setItem(12, GUIUtils.createItem(Material.WRITABLE_BOOK, "\uD83D\uDCCA Live Results"));

        if (player.hasPermission("premiumvote.viewresults.past"))
            menu.setItem(30, GUIUtils.createItem(Material.CHEST, "\uD83D\uDCC1 Past Results"));

        if (player.hasPermission("premiumvote.admin.start"))
            menu.setItem(16, GUIUtils.createItem(Material.EMERALD_BLOCK, "\uD83D\uDE80 Start Election"));

        if (player.hasPermission("premiumvote.admin.stop"))
            menu.setItem(34, GUIUtils.createItem(Material.BARRIER, "\uD83D\uDEAB Stop Election"));

        if (player.hasPermission("premiumvote.use")) {
            menu.setItem(41, GUIUtils.createItem(Material.RED_CONCRETE, "\uD83D\uDCCB Close Menu"));
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_AQUA + "\uD83D\uDCCB Election Menu")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        // Strip color & non-letter characters for matching
        String rawName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String name = rawName.replaceAll("[^a-zA-Z ]", "").trim();

        switch (name) {
            case "Vote Now":
                if (player.hasPermission("premiumvote.vote")) {
                    new VoteGUI(manager, plugin, signInput).openVoteMenu(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to vote.");
                }
                break;

            case "Run for Election":
                if (player.hasPermission("premiumvote.run")) {
                    new RunForElectionGUI(manager, plugin, signInput).open(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to run for election.");
                }
                break;

            case "Live Results":
                if (player.hasPermission("premiumvote.viewresults.live")) {
                    new LiveResultsGUI(manager, plugin, signInput).open(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view live results.");
                }
                break;

            case "Past Results":
                if (player.hasPermission("premiumvote.viewresults.past")) {
                    new PastResultsGUI(manager, plugin, signInput).open(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view past results.");
                }
                break;

            case "Start Election":
                if (player.hasPermission("premiumvote.admin.start")) {
                    new StartElectionGUI(manager, plugin, signInput).open(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to start elections.");
                }
                break;

            case "Stop Election":
                if (player.hasPermission("premiumvote.admin.stop")) {
                    new EndElectionGUI(manager, plugin, signInput).open(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to stop elections.");
                }
                break;

            case "Close Menu":
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                break;
        }
    }
}
