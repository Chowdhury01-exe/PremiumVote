package com.akashplugin.gui;

import com.akashplugin.VoteManager;
import com.akashplugin.PremiumVote;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PastResultsGUI implements Listener {

    private final VoteManager manager;
    private final com.akashplugin.PremiumVote plugin;
    private final SignInput signInput;

    public PastResultsGUI(VoteManager manager, com.akashplugin.PremiumVote plugin, SignInput signInput) {
        this.signInput = signInput;
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player) {
        File file = new File(PremiumVote.getInstance().getDataFolder(), "vote_results.yml");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "No past results available.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("elections")) {
            player.sendMessage(ChatColor.RED + "No past results available.");
            return;
        }

        // Sort elections by date (newest first)
        List<String> elections = new ArrayList<>(config.getConfigurationSection("elections").getKeys(false));
        elections.sort((a, b) -> {
            String dateA = config.getString("elections." + a + ".date", "");
            String dateB = config.getString("elections." + b + ".date", "");
            return dateB.compareTo(dateA);
        });

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "📁 Past Elections");

        int index = 0;
        for (String electionName : elections) {
            if (index >= 53) break; // last slot for back button

            String date = config.getString("elections." + electionName + ".date", "Unknown Date");
            Map<String, Integer> results = new HashMap<>();

            if (config.contains("elections." + electionName + ".results")) {
                for (String candidate : config.getConfigurationSection("elections." + electionName + ".results").getKeys(false)) {
                    results.put(candidate, config.getInt("elections." + electionName + ".results." + candidate));
                }
            }

            // Sort top 3 candidates
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Held on: " + date);

            results.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(3)
                    .forEach(entry ->
                            lore.add(ChatColor.YELLOW + entry.getKey() + ChatColor.GRAY + ": " + ChatColor.AQUA + entry.getValue() + " votes")
                    );

            if (results.isEmpty()) {
                lore.add(ChatColor.RED + "No results recorded.");
            }

            gui.setItem(index++, GUIUtils.createItem(Material.PAPER, ChatColor.GOLD + electionName, lore));
        }

        // Back button
        gui.setItem(53, GUIUtils.createItem(Material.ARROW, ChatColor.YELLOW + "⬅ Back to Menu"));
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_BLUE + "📁 Past Elections")) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (itemName.equals("⬅ Back to Menu")) {
            new MainMenuGUI(manager, plugin, signInput).open(player);
        } else {
            // Open detailed results for the selected election
            DetailedResultsGUI detailedGUI = new DetailedResultsGUI(manager, itemName, plugin, signInput);
            Bukkit.getPluginManager().registerEvents(detailedGUI, plugin);
            detailedGUI.open(player);

        }
    }
}
