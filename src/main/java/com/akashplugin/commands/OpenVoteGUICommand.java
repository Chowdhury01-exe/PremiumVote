package com.akashplugin.commands;

import com.akashplugin.VoteManager;
import com.akashplugin.gui.MainMenuGUI;
import com.akashplugin.gui.SignInput;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenVoteGUICommand implements CommandExecutor {

    private final VoteManager manager;
    private final com.akashplugin.PremiumVote plugin;
    private final SignInput signInput;

    public OpenVoteGUICommand(VoteManager manager , com.akashplugin.PremiumVote plugin, SignInput signInput) {
        this.signInput = signInput;
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        new MainMenuGUI(manager, plugin ,signInput).open(player);
        return true;
    }
}
