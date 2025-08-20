package com.akashplugin;

import com.akashplugin.commands.*;
import com.akashplugin.gui.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class PremiumVote extends JavaPlugin {
    private static PremiumVote instance;
    private VoteManager voteManager;
    private SignInput signInput;

    @Override
    public void onEnable() {
        instance = this;

        voteManager = new VoteManager(this);

        // Create a single SignInput instance (registers itself as listener)
        signInput = new SignInput(this);

        getLogger().info("PremiumVote has been enabled.");

        // Register commands with SignInput passed
        if (getCommand("vote") != null) {
            getCommand("vote").setExecutor(new OpenVoteGUICommand(voteManager, this, signInput));
        }

        if (getCommand("election") != null) {
            getCommand("election").setExecutor(new VoteCommand(voteManager, this, signInput));
        }

        if (getCommand("results") != null) {
            getCommand("results").setExecutor(new ResultsCommand());
            getCommand("results").setTabCompleter(new ResultsTabCompleter());
        }

        // Register GUI listeners and pass SignInput where required
        getServer().getPluginManager().registerEvents(new LiveResultsGUI(voteManager, this, signInput), this);
        getServer().getPluginManager().registerEvents(new VoteGUI(voteManager, this, signInput), this);
        getServer().getPluginManager().registerEvents(new MainMenuGUI(voteManager, this, signInput), this);
        getServer().getPluginManager().registerEvents(new EndElectionGUI(voteManager, this, signInput), this);

        // Pass SignInput to StartElectionGUI and register
        StartElectionGUI startElectionGUI = new StartElectionGUI(voteManager, this, signInput);
        getServer().getPluginManager().registerEvents(startElectionGUI, this);

        getServer().getPluginManager().registerEvents(new PastResultsGUI(voteManager, this, signInput), this);
        getServer().getPluginManager().registerEvents(new RunForElectionGUI(voteManager, this, signInput), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("PremiumVote has been disabled.");
    }

    public static PremiumVote getInstance() {
        return instance;
    }
}
