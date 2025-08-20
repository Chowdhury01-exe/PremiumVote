package com.akashplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResultsTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("list"); // 🆕 add `list`

            File file = new File(PremiumVote.getInstance().getDataFolder(), "vote_results.yml");
            if (!file.exists()) return suggestions;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (config.contains("elections")) {
                suggestions.addAll(config.getConfigurationSection("elections").getKeys(false));
            }
        }

        return suggestions;
    }
}
