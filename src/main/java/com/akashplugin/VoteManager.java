package com.akashplugin;

import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class VoteManager implements Listener {
    private boolean ongoing = false;
    private String electionName;
    private long endTime;

    private final Set<String> candidates = new HashSet<>();
    private final Map<UUID, String> votes = new HashMap<>();

    // Chat input tracking
    private final Map<UUID, Consumer<String>> nameInputListeners = new HashMap<>();
    public int getTotalVotes() {
        return votes.size();
    }
    public String getElectionName() {
        return this.electionName == null ? "" : this.electionName;
    }

    public void setElectionName(String name) {
        this.electionName = name;
    }

    private void openSignEditor(Player player) {
        player.closeInventory();

        // Get a temporary sign block state
        Sign sign = (Sign) Bukkit.createBlockData(Material.OAK_SIGN).createBlockState();

        // Set the lines (optional)
        sign.setLine(0, "");
        sign.setLine(1, "Type Election");
        sign.setLine(2, "Name Here");
        sign.setLine(3, "");

        // Open sign editor
        player.openSign(sign);
    }

    public boolean startElection(String name, int minutes) {
        if (ongoing) return false;

        this.electionName = name;
        this.endTime = System.currentTimeMillis() + (minutes * 60 * 1000);
        this.candidates.clear();
        this.votes.clear();
        this.ongoing = true;

        Bukkit.getScheduler().runTaskLater(PremiumVote.getInstance(), this::endElection, minutes * 20L * 60);

        return true;
    }
    private final JavaPlugin plugin;
    public VoteManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public JavaPlugin getPlugin() {
        return plugin;
    }
    public boolean registerCandidate(Player player) {
        if (!(ongoing || System.currentTimeMillis() > endTime)) return false;
        return candidates.add(player.getName());
    }

    public Set<String> getCandidates() {
        return candidates;
    }

    public boolean castVote(Player voter, String candidate) {
        if (!ongoing || System.currentTimeMillis() > endTime) return false;
        if (!candidates.contains(candidate)) return false;
        if (votes.containsKey(voter.getUniqueId())) return false;

        votes.put(voter.getUniqueId(), candidate);
        return true;
    }

    public Map<String, Integer> getResults() {
        Map<String, Integer> results = new HashMap<>();
        for (String c : candidates) {
            results.put(c, 0);
        }
        for (String candidate : votes.values()) {
            results.put(candidate, results.get(candidate) + 1);
        }
        return results;
    }

    public boolean isOngoing() {
        return ongoing && System.currentTimeMillis() < endTime;
    }
    public void startElection() {
        // Your existing election start logic
        ongoing = true; // or whatever flag you use
        // Example:
        candidates.clear();
        votes.clear();
        // You can also broadcast or log here if needed
    }

    public void endElection() {
        if (!ongoing) return;
        ongoing = false;

        Map<String, Integer> results = getResults();

        List<Map.Entry<String, Integer>> sortedResults = new ArrayList<>(results.entrySet());
        sortedResults.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Bukkit.broadcastMessage("§6§l==============================");
        Bukkit.broadcastMessage("§e§l📊 Election Results: " + electionName + " 📊");

        if (sortedResults.isEmpty()) {
            Bukkit.broadcastMessage("§cNo candidates participated in this election!");
            Bukkit.broadcastMessage("§6§l==============================");
            return;
        }

        int place = 1;
        for (Map.Entry<String, Integer> entry : sortedResults) {
            String color;
            switch (place) {
                case 1: color = "§6§l"; break;
                case 2: color = "§f§l"; break;
                case 3: color = "§c§l"; break;
                default: color = "§7"; break;
            }

            Bukkit.broadcastMessage(color + place + ". " + entry.getKey() + " §7- §a" + entry.getValue() + " votes");
            place++;
        }

        Map.Entry<String, Integer> winner = sortedResults.get(0);
        Bukkit.broadcastMessage("§b§l🏆 Winner: " + winner.getKey() + " §7with §a" + winner.getValue() + " votes!");
        Bukkit.broadcastMessage("§6§l==============================");

        saveElectionResults(results, winner.getKey());
    }

    public void saveElectionResults(Map<String, Integer> results, String winner) {
        File file = new File(PremiumVote.getInstance().getDataFolder(), "vote_results.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = "elections." + electionName + "." + System.currentTimeMillis();
        config.set(path + ".winner", winner);
        config.set(path + ".results", results);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== Chat Input Capture ======
    public void waitForElectionName(Player player, Consumer<String> callback) {
        nameInputListeners.put(player.getUniqueId(), callback);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (nameInputListeners.containsKey(uuid)) {
            event.setCancelled(true);
            String input = event.getMessage();
            Consumer<String> callback = nameInputListeners.remove(uuid);
            if (callback != null) {
                callback.accept(input);
            }
        }
    }
}
