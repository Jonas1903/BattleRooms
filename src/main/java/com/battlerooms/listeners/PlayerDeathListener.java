package com.battlerooms.listeners;

import com.battlerooms.BattleRooms;
import com.battlerooms.managers.RoomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listens for player deaths in battle rooms
 */
public class PlayerDeathListener implements Listener {

    private final RoomManager roomManager;

    public PlayerDeathListener(BattleRooms plugin) {
        this.roomManager = plugin.getRoomManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        roomManager.handlePlayerDeath(player);
    }
}
