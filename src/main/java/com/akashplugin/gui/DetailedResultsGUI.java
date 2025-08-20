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
import java.util.*;

public class DetailedResultsGUI implements Listener {

    private final VoteManager manager;
    private final String electionName;
    private final com.akashplugin.PremiumVote plugin;
    private final SignInput signInput;
    public DetailedResultsGUI(VoteManager manager, String electionName, com.akashplugin.PremiumVote plugin, SignInput signInput) {
        this.signInput = signInput;
        this.plugin = plugin;
        this.manager = manager;
        this.electionName = electionName;
    }

    public void open(Player player) {
        File file = new File(PremiumVote.getInstance().getDataFolder(), "vote_results.yml");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "No past results available.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Find election section
        if (!config.contains("elections." + electionName)) {
            player.sendMessage(ChatColor.RED + "No results found for " + electionName);
            return;
        }

        // Find latest timestamp
        Set<String> timestamps = config.getConfigurationSection("elections." + electionName).getKeys(false);
        if (timestamps.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No past records found for " + electionName);
            return;
        }

        String latestKey = timestamps.stream().max(String::compareTo).orElse(null);
        if (latestKey == null || !config.contains("elections." + electionName + "." + latestKey + ".results")) {
            player.sendMessage(ChatColor.RED + "No result data found for " + electionName);
            return;
        }

        Map<String, Integer> results = new HashMap<>();
        for (String candidate : config.getConfigurationSection("elections." + electionName + "." + latestKey + ".results").getKeys(false)) {
            results.put(candidate, config.getInt("elections." + electionName + "." + latestKey + ".results." + candidate));
        }

        // Sort by votes (highest first)
        List<Map.Entry<String, Integer>> sortedResults = new ArrayList<>(results.entrySet());
        sortedResults.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Winner
        String winner = config.getString("elections." + electionName + "." + latestKey + ".winner");

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "📊 " + electionName);

        // Fill with glass first
        ItemStack filler = GUIUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        int index = 0;
        for (Map.Entry<String, Integer> entry : sortedResults) {
            if (index >= 53) break;

            String candidate = entry.getKey();
            int votes = entry.getValue();

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Votes: " + ChatColor.AQUA + votes);
            lore.add(ChatColor.GRAY + "Rank: " + (index + 1));
            if (candidate.equalsIgnoreCase(winner)) {
                lore.add(ChatColor.GOLD + "🏆 Winner!");
            }

            ItemStack icon;
            if (candidate.equalsIgnoreCase(winner)) {
                // Winner gets a PLAYER_HEAD with their skin
                icon = GUIUtils.createPlayerHead(candidate, ChatColor.YELLOW + candidate, lore);
            } else {
                // Others get paper icons
                icon = GUIUtils.createItem(Material.PAPER, ChatColor.YELLOW + candidate, lore);
            }

            gui.setItem(index++, icon);
        }

        // Back button
        gui.setItem(53, GUIUtils.createItem(Material.ARROW, ChatColor.YELLOW + "⬅ Back to Past Results"));

        player.openInventory(gui);
    }


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(ChatColor.DARK_GREEN + "📊 ")) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (itemName.equals("⬅ Back to Past Results")) {
            new PastResultsGUI(manager, plugin , signInput).open(player);
        }
    }
}
