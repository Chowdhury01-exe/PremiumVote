package com.akashplugin.commands;


import com.akashplugin.PremiumVote;
import com.akashplugin.VoteManager;
import com.akashplugin.gui.MainMenuGUI;
import com.akashplugin.gui.SignInput;
import com.akashplugin.gui.VoteGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class VoteCommand implements CommandExecutor {

    private final VoteManager manager;
    private final PremiumVote plugin;
    private final SignInput signInput;

    public VoteCommand(VoteManager manager, PremiumVote plugin, SignInput signInput) {
        this.plugin = plugin;
        this.signInput = signInput;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /election <start|run|vote|results>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                new MainMenuGUI(manager, plugin, signInput).open(player);
                break;

            case "start":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.BLUE + "Usage: /election start <name> <minutes>");
                    return true;
                }
                String name = args[1];
                int minutes;
                try {
                    minutes = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.DARK_BLUE + "Minutes must be a number!");
                    return true;
                }

                if (manager.startElection(name, minutes)) {
                    player.sendMessage(ChatColor.GREEN + "Election \"" + name + "\" started for " + minutes + " minutes!");
                } else {
                    player.sendMessage(ChatColor.RED + "An election is already ongoing!");
                }
                break;

            case "run":
                if (manager.registerCandidate(player)) {
                    player.sendMessage(ChatColor.AQUA + "You registered as a candidate!");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not register (maybe election not active or already ended).");
                }
                break;

            case "vote":
                new VoteGUI(manager, plugin, signInput).openVoteMenu(player);
                break;

            case "stop":
                if (manager.isOngoing()) {
                    manager.endElection();
                    player.sendMessage(ChatColor.DARK_GREEN + "You have stopped the election manually.");
                } else {
                    player.sendMessage(ChatColor.RED + "There is no ongoing election to stop.");
                }
                break;

            case "results":
                if (!manager.isOngoing()) {
                    player.sendMessage(ChatColor.DARK_GREEN + "No ongoing election.");
                    return true;
                }
                Map<String, Integer> results = manager.getResults();
                player.sendMessage(ChatColor.GOLD + "--- Election Results ---");
                results.forEach((candidate, votes) -> {
                    player.sendMessage(ChatColor.YELLOW + candidate + ": " + votes + " votes");
                });
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: start, run, vote, results");
        }

        return true;
    }
}
