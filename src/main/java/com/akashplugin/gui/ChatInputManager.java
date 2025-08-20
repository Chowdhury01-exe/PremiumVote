package com.akashplugin.gui;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChatInputManager {

    // Stores a mapping of players → what to do with their next message
    private static final Map<Player, Consumer<String>> inputMap = new HashMap<>();

    /**
     * Wait for a specific player's next chat message.
     * @param player The player to listen for
     * @param callback The action to run when the message is received
     */
    public static void waitForInput(Player player, Consumer<String> callback) {
        inputMap.put(player, callback);
    }

    /**
     * Handles the player's message if they are in "input mode".
     * @param player The player who chatted
     * @param message The message they sent
     * @return true if the message was consumed and should be cancelled
     */
    public static boolean handleChat(Player player, String message) {
        if (inputMap.containsKey(player)) {
            Consumer<String> action = inputMap.remove(player); // Remove so it only triggers once
            action.accept(message); // Pass the message to the callback
            return true;
        }
        return false;
    }
}
