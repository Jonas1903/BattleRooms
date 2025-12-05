package com.battlerooms.listeners;

import com.battlerooms.BattleRooms;
import com.battlerooms.managers.RoomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Protects room boundaries from being broken
 */
public class BlockProtectionListener implements Listener {

    private final RoomManager roomManager;

    public BlockProtectionListener(BattleRooms plugin) {
        this.roomManager = plugin.getRoomManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // OPs can break any block
        if (player.isOp()) {
            return;
        }

        // Check if the block is a protected boundary
        if (roomManager.isProtectedBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot break the room structure!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // OPs can place any block
        if (player.isOp()) {
            return;
        }

        // Check if the block would be placed on a protected boundary
        // This prevents players from modifying the room structure
        if (roomManager.isProtectedBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot modify the room structure!");
        }
    }
}
