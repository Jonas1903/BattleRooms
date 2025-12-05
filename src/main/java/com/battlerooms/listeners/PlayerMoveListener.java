package com.battlerooms.listeners;

import com.battlerooms.BattleRooms;
import com.battlerooms.managers.RoomManager;
import com.battlerooms.models.BattleRoom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens for player movement to detect room entry/exit
 */
public class PlayerMoveListener implements Listener {

    private final RoomManager roomManager;

    public PlayerMoveListener(BattleRooms plugin) {
        this.roomManager = plugin.getRoomManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player actually moved to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player entered or left any room
        BattleRoom fromRoom = roomManager.getRoomAtLocation(event.getFrom());
        BattleRoom toRoom = roomManager.getRoomAtLocation(event.getTo());

        // Player leaving a room
        if (fromRoom != null && toRoom == null) {
            roomManager.handlePlayerLeaveRoom(player, fromRoom);
        }

        // Player entering a room
        if (toRoom != null && (fromRoom == null || !fromRoom.getName().equals(toRoom.getName()))) {
            roomManager.handlePlayerEnterRoom(player, toRoom);
        }
    }
}
