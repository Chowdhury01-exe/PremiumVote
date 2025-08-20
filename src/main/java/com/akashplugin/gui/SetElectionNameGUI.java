package com.akashplugin.gui;

import com.akashplugin.VoteManager;
import com.akashplugin.PremiumVote;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.akashplugin.gui.*;
import com.akashplugin.commands.*;
import com.akashplugin.*;

public class SetElectionNameGUI {

    private final VoteManager manager;
    private final PremiumVote plugin;
    private final StartElectionGUI startElectionGUI;
    private final SignInput signInput;

    // Constructor now requires SignInput instance passed in
    public SetElectionNameGUI(VoteManager manager, PremiumVote plugin, StartElectionGUI startElectionGUI, SignInput signInput) {
        this.manager = manager;
        this.plugin = plugin;
        this.startElectionGUI = startElectionGUI;
        this.signInput = signInput;  // Use the passed SignInput instance
    }

    public void open(Player player) {
        signInput.openSignInput(player, electionName -> {
            // Callback when player finishes sign input
            manager.setElectionName(electionName);
            player.sendMessage(ChatColor.GREEN + "✅ Election name set to: " + ChatColor.AQUA + electionName);

            // Reopen StartElectionGUI with new election name
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                startElectionGUI.open(player);
            });
        });
    }
}

