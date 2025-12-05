package com.battlerooms.listeners;

import com.battlerooms.BattleRooms;
import com.battlerooms.managers.ConfigManager;
import com.battlerooms.managers.RoomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Blocks commands for players in active battle rooms
 */
public class CommandBlockListener implements Listener {

    private final RoomManager roomManager;
    private final ConfigManager configManager;

    public CommandBlockListener(BattleRooms plugin) {
        this.roomManager = plugin.getRoomManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // OPs can always use commands
        if (player.isOp() || player.hasPermission("battlerooms.bypass")) {
            return;
        }

        // Check if player is in an active room
        if (roomManager.isPlayerInActiveRoom(player)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("commands-disabled"));
        }
    }
}
