package com.akashplugin.gui;


import com.akashplugin.gui.SignInput;
import com.akashplugin.gui.SetElectionNameGUI;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

import static com.akashplugin.gui.GUIUtils.createItem;

public class StartElectionGUI implements Listener {

    private final VoteManager manager;
    private final Plugin plugin;
    private final SignInput signInput;
    private final Map<Player, ElectionSetup> setupMap = new HashMap<>();

    private static final String TITLE_START = "🗳 Start Election";
    private static final String TITLE_DURATION = "⏳ Set Duration";

    public StartElectionGUI(VoteManager manager, Plugin plugin, SignInput signInput) {
        this.manager = manager;
        this.plugin = plugin;
        this.signInput = signInput;
    }

    private static class ElectionSetup {
        String name = "";
        int durationMinutes = 5;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + TITLE_START);

        // Fill all slots with gray glass first
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Then place actual menu items
        inv.setItem(11, createItem(Material.NAME_TAG, ChatColor.YELLOW + "Set Election Name"));
        inv.setItem(13, createItem(Material.CLOCK, ChatColor.AQUA + "Set Duration"));
        inv.setItem(15, createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Confirm - Start Election"));
        inv.setItem(26, createItem(Material.BARRIER, ChatColor.RED + "Cancel"));

        player.openInventory(inv);
    }

    private void openDurationGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + TITLE_DURATION);

        // Fill with glass first
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(10, createItem(Material.CLOCK, ChatColor.AQUA + "5 Minutes"));
        inv.setItem(12, createItem(Material.CLOCK, ChatColor.AQUA + "10 Minutes"));
        inv.setItem(14, createItem(Material.CLOCK, ChatColor.AQUA + "15 Minutes"));
        inv.setItem(16, createItem(Material.CLOCK, ChatColor.AQUA + "30 Minutes"));
        inv.setItem(26, createItem(Material.BARRIER, ChatColor.RED + "Back"));

        player.openInventory(inv);
    }


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle()).trim();

        if (!title.contains("Start Election") && !title.contains("Set Duration")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).trim();
        ElectionSetup setup = setupMap.computeIfAbsent(player, p -> new ElectionSetup());

        // START ELECTION GUI
        if (title.contains("Start Election")) {
            switch (name) {
                case "Set Election Name":
                    player.closeInventory();
                    Bukkit.getScheduler().runTask(plugin, () -> signInput.openSignInput(player, input -> {
                        setup.name = input.trim();
                        player.sendMessage(ChatColor.GREEN + "Election name set to: " + setup.name);
                        open(player);
                    }));
                    break;


                case "Set Duration":
                    Bukkit.getScheduler().runTask(plugin, () -> openDurationGUI(player));
                    break;

                case "Confirm - Start Election":
                    if (!player.hasPermission("premiumvote.admin.start")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to start an election.");
                        break;
                    }
                    if (setup.name.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Please set an election name first.");
                        break;
                    }
                    if (manager.isOngoing()) {
                        player.sendMessage(ChatColor.YELLOW + "An election is already ongoing!");
                    } else {
                        manager.startElection(setup.name, setup.durationMinutes);
                        player.sendMessage(ChatColor.GREEN + "Election '" + setup.name + "' started for " + setup.durationMinutes + " minutes!");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "📢 A new election '" + setup.name + "' has started! Use the voting menu to participate.");
                    }
                    setupMap.remove(player);
                    player.closeInventory();
                    break;

                case "Cancel":
                    player.sendMessage(ChatColor.GRAY + "Cancelled.");
                    setupMap.remove(player);
                    player.closeInventory();
                    break;
            }
        }

        // SET DURATION GUI
        else if (title.contains("Set Duration")) {
            if (name.endsWith("Minutes")) {
                try {
                    int minutes = Integer.parseInt(name.split(" ")[0]);
                    setup.durationMinutes = minutes;
                    player.sendMessage(ChatColor.GREEN + "Duration set to " + minutes + " minutes.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid duration selected.");
                }
                open(player); // return to start election menu
            } else if (name.equals("Back")) {
                open(player);
            }
        }
    }
}
