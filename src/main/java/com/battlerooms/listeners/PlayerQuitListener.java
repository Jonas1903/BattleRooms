package com.battlerooms.listeners;

import com.battlerooms.BattleRooms;
import com.battlerooms.managers.RoomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for player quits/disconnects in battle rooms
 */
public class PlayerQuitListener implements Listener {

    private final RoomManager roomManager;

    public PlayerQuitListener(BattleRooms plugin) {
        this.roomManager = plugin.getRoomManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Cancel any room creation in progress
        if (roomManager.isCreating(player)) {
            roomManager.cancelCreating(player);
        }
        
        // Handle quit as death if in active room
        roomManager.handlePlayerQuit(player);
    }
}
