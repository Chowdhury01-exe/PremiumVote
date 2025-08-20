package com.akashplugin.gui;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

public class SignInput implements Listener {

    private final Plugin plugin;

    private static class TempPlacement {
        final Location loc;
        final Material originalType;
        TempPlacement(Location loc, Material originalType) {
            this.loc = loc;
            this.originalType = originalType;
        }
    }

    private final Map<UUID, Consumer<String>> pendingInputs = new HashMap<>();
    private final Map<UUID, TempPlacement> placements = new HashMap<>();

    public SignInput(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openSignInput(Player player, Consumer<String> onInput) {
        // Find a nearby AIR block with a solid block beneath it
        Location signLoc = findAirWithSolidBelow(player);
        if (signLoc == null) {
            player.sendMessage(ChatColor.RED + "No safe spot to open a sign here. Move to a flat area and try again.");
            return;
        }

        Block signBlock = signLoc.getBlock();
        Material originalType = signBlock.getType();

        // Extra safety: only place if it’s AIR (we will not overwrite your terrain)
        if (originalType != Material.AIR) {
            player.sendMessage(ChatColor.RED + "No air block available for the sign. Try moving a step.");
            return;
        }

        // Place temporary sign **only in air**
        signBlock.setType(Material.OAK_SIGN, false);

        UUID id = player.getUniqueId();
        pendingInputs.put(id, onInput);
        placements.put(id, new TempPlacement(signLoc, originalType));

        // Open next tick so the server/state is ready
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!(signBlock.getState() instanceof Sign)) {
                    player.sendMessage(ChatColor.RED + "Error opening sign input.");
                    cleanup(player, true);
                    return;
                }

                Sign sign = (Sign) signBlock.getState();

                // Make editable (1.20+ will close instantly otherwise)
                try { sign.setEditable(true); } catch (Throwable ignored) {}
                try {
                    // If your API has a waxed flag, un-wax
                    Sign.class.getMethod("setWaxed", boolean.class).invoke(sign, false);
                } catch (Throwable ignored) {}

                sign.update(true, false);

                try {
                    player.openSign(sign);
                } catch (Throwable t) {
                    player.sendMessage(ChatColor.RED + "Your server API doesn’t support opening signs here.");
                    cleanup(player, true);
                    return;
                }

                player.sendMessage(ChatColor.GREEN + "Type the election name on the sign and close it.");
            }
        }.runTaskLater(plugin, 1L);
    }

    // Try a few reasonable spots around the player; must be AIR with solid below
    private Location findAirWithSolidBelow(Player p) {
        Location base = p.getLocation();
        List<Location> candidates = new ArrayList<>();

        // front of player at foot level and one up
        candidates.add(base.clone().add(base.getDirection().setY(0).normalize()).getBlock().getLocation());
        candidates.add(base.clone().add(base.getDirection().setY(0).normalize()).add(0, 1, 0).getBlock().getLocation());

        // at player feet + head
        candidates.add(base.getBlock().getLocation());
        candidates.add(base.clone().add(0, 1, 0).getBlock().getLocation());

        // simple ring around
        int[][] offs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] o : offs) {
            candidates.add(base.clone().add(o[0], 0, o[1]).getBlock().getLocation());
            candidates.add(base.clone().add(o[0], 1, o[1]).getBlock().getLocation());
        }

        for (Location loc : candidates) {
            Block b = loc.getBlock();
            Block below = b.getRelative(0, -1, 0);
            if (b.getType() == Material.AIR && below.getType().isSolid()) {
                return loc;
            }
        }
        return null;
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        TempPlacement placement = placements.get(id);
        if (placement == null) return;

        // Must be our temp sign
        if (!event.getBlock().getLocation().equals(placement.loc)) return;

        // Merge lines
        StringBuilder sb = new StringBuilder();
        for (String line : event.getLines()) {
            if (line != null && !line.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(line);
            }
        }
        String input = sb.toString().trim();

        if (input.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Input cannot be empty! Please try again.");
            return; // keep open
        }

        Consumer<String> cb = pendingInputs.remove(id);
        placements.remove(id);

        // Restore original block (AIR) — we only ever placed into AIR
        event.getBlock().setType(Material.AIR);

        if (cb != null) cb.accept(input);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer(), true);
    }

    private void cleanup(Player player, boolean restore) {
        UUID id = player.getUniqueId();
        TempPlacement placement = placements.remove(id);
        pendingInputs.remove(id);

        if (restore && placement != null) {
            Block b = placement.loc.getBlock();
            // If we left a sign behind, put it back to what it was (AIR)
            if (b.getType().name().endsWith("_SIGN")) {
                b.setType(placement.originalType);
            }
        }
    }
}
