package com.akashplugin.commands;

import com.akashplugin.PremiumVote;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ResultsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender.hasPermission("vote.results.use"))) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify the name of the election\n" +
                    ChatColor.GOLD +"or use 'list' to see available elections.");

            return true;
        }

        File file = new File(PremiumVote.getInstance().getDataFolder(), "vote_results.yml");
        if (!file.exists()) {
            sender.sendMessage(ChatColor.RED + "No past elections recorded yet.");
            return true;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);


        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            ConfigurationSection electionsSection = config.getConfigurationSection("elections");
            if (electionsSection == null || electionsSection.getKeys(false).isEmpty()) {
                sender.sendMessage(ChatColor.RED + "There are no saved elections.");
                return true;
            }

            Set<String> electionNames = electionsSection.getKeys(false);
            sender.sendMessage(ChatColor.GOLD + "📁 " + ChatColor.YELLOW + "Available Elections:");
            for (String name : electionNames) {
                sender.sendMessage(ChatColor.AQUA + "- " + name);
            }

            return true;
        }

        if(!config.contains("elections." + args[0])) {
            sender.sendMessage(ChatColor.RED + "No results found for election: " + args[0]);
            return true;
        }


        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /results <electionName> or /results list");
            return true;
        }

        String electionName = args[0];

        if (!config.contains("elections." + electionName)) {
            sender.sendMessage(ChatColor.RED + "No results found for election: " + electionName);
            return true;
        }

        ConfigurationSection electionsSection = config.getConfigurationSection("elections." + electionName);
        if (electionsSection == null) {
            sender.sendMessage(ChatColor.RED + "No past records found for election: " + electionName);
            return true;
        }

        String latestKey = electionsSection.getKeys(false).stream()
                .max(String::compareTo)
                .orElse(null);

        if (latestKey == null) {
            sender.sendMessage(ChatColor.RED + "No past records found for election: " + electionName);
            return true;
        }

        ConfigurationSection resultSection = electionsSection.getConfigurationSection(latestKey);
        if (resultSection == null) {
            sender.sendMessage(ChatColor.RED + "Corrupted or missing result data.");
            return true;
        }

        String winner = resultSection.getString("winner");
        ConfigurationSection resultsSection = resultSection.getConfigurationSection("results");

        sender.sendMessage(ChatColor.GOLD + "§m------------------------");
        sender.sendMessage(ChatColor.YELLOW + "📊 Results for Election: " + ChatColor.AQUA + electionName);
        sender.sendMessage(ChatColor.GRAY + "(Timestamp: " + latestKey + ")");

        if (resultsSection != null) {
            Map<String, Integer> results = new LinkedHashMap<>();
            for (String candidate : resultsSection.getKeys(false)) {
                results.put(candidate, resultsSection.getInt(candidate));
            }

            results.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> {
                        sender.sendMessage(ChatColor.GREEN + entry.getKey() +
                                ChatColor.GRAY + " - " +
                                ChatColor.AQUA + entry.getValue() + " votes");
                    });
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "🏆 Winner: " + ChatColor.GOLD + winner);
        sender.sendMessage(ChatColor.GOLD + "§m------------------------");

        return true;
    }
}
