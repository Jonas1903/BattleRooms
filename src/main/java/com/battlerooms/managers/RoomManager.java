package com.battlerooms.managers;

import com.battlerooms.BattleRooms;
import com.battlerooms.models.BattleRoom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages battle rooms, their states, and player tracking
 */
public class RoomManager {

    private final BattleRooms plugin;
    private final ConfigManager configManager;
    private final Map<String, BattleRoom> rooms;
    private final Map<UUID, BattleRoom> creatingRoom; // Players currently creating rooms

    public RoomManager(BattleRooms plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.rooms = new HashMap<>();
        this.creatingRoom = new HashMap<>();
    }

    public void loadRooms() {
        rooms.clear();
        rooms.putAll(configManager.loadRooms());
        plugin.getLogger().info("Loaded " + rooms.size() + " battle rooms.");
    }

    public void reloadRooms() {
        configManager.reloadConfig();
        loadRooms();
    }

    public Map<String, BattleRoom> getRooms() {
        return rooms;
    }

    public BattleRoom getRoom(String name) {
        return rooms.get(name.toLowerCase());
    }

    public boolean roomExists(String name) {
        return rooms.containsKey(name.toLowerCase());
    }

    /**
     * Start creating a new room
     */
    public void startCreating(Player player, BattleRoom.RoomType type, String name) {
        BattleRoom room = new BattleRoom(name, type, player.getWorld().getName());
        creatingRoom.put(player.getUniqueId(), room);
    }

    /**
     * Get the room a player is currently creating
     */
    public BattleRoom getCreatingRoom(Player player) {
        return creatingRoom.get(player.getUniqueId());
    }

    /**
     * Check if a player is creating a room
     */
    public boolean isCreating(Player player) {
        return creatingRoom.containsKey(player.getUniqueId());
    }

    /**
     * Cancel room creation for a player
     */
    public void cancelCreating(Player player) {
        creatingRoom.remove(player.getUniqueId());
    }

    /**
     * Save and finalize a room being created
     */
    public boolean saveCreatingRoom(Player player) {
        BattleRoom room = creatingRoom.get(player.getUniqueId());
        if (room == null || !room.isComplete()) {
            return false;
        }

        rooms.put(room.getName().toLowerCase(), room);
        configManager.saveRoom(room);
        creatingRoom.remove(player.getUniqueId());
        return true;
    }

    /**
     * Delete a room
     */
    public boolean deleteRoom(String name) {
        BattleRoom room = rooms.remove(name.toLowerCase());
        if (room != null) {
            configManager.deleteRoom(name);
            return true;
        }
        return false;
    }

    /**
     * Get the room a player is currently in
     */
    public BattleRoom getPlayerRoom(Player player) {
        for (BattleRoom room : rooms.values()) {
            if (room.getPlayersInRoom().contains(player.getUniqueId())) {
                return room;
            }
        }
        return null;
    }

    /**
     * Get the room at a specific location
     */
    public BattleRoom getRoomAtLocation(Location location) {
        for (BattleRoom room : rooms.values()) {
            if (room.isInRoom(location)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Check if a player is in any active room
     */
    public boolean isPlayerInActiveRoom(Player player) {
        BattleRoom room = getPlayerRoom(player);
        return room != null && room.getState() == BattleRoom.RoomState.ACTIVE;
    }

    /**
     * Handle player entering a room
     */
    public void handlePlayerEnterRoom(Player player, BattleRoom room) {
        if (room.getState() != BattleRoom.RoomState.WAITING) {
            return;
        }

        // Check if player is already in this room
        if (room.getPlayersInRoom().contains(player.getUniqueId())) {
            return;
        }

        room.addPlayer(player.getUniqueId());
        player.sendMessage("§aYou have entered the " + room.getName() + " room (" + 
                room.getType().getDisplayName() + ")");

        // Check if room should close
        if (room.getPlayerCount() >= room.getType().getRequiredPlayers()) {
            activateRoom(room);
        }
    }

    /**
     * Handle player leaving a room
     */
    public void handlePlayerLeaveRoom(Player player, BattleRoom room) {
        if (!room.getPlayersInRoom().contains(player.getUniqueId())) {
            return;
        }

        // Only remove if room is waiting
        if (room.getState() == BattleRoom.RoomState.WAITING) {
            room.removePlayer(player.getUniqueId());
        }
    }

    /**
     * Activate a room (seal it and start the match)
     */
    private void activateRoom(BattleRoom room) {
        room.setState(BattleRoom.RoomState.ACTIVE);
        room.closeGate();

        // Notify players
        for (UUID playerId : room.getPlayersInRoom()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§c§lThe battle has begun! The room is now sealed!");
            }
        }

        plugin.getLogger().info("Room " + room.getName() + " is now active with " + 
                room.getPlayerCount() + " players.");
    }

    /**
     * Handle a player dying or quitting in a room
     */
    public void handlePlayerDeath(Player player) {
        BattleRoom room = getPlayerRoom(player);
        if (room == null || room.getState() != BattleRoom.RoomState.ACTIVE) {
            return;
        }

        room.removePlayer(player.getUniqueId());

        // Check win conditions
        checkWinCondition(room, player);
    }

    /**
     * Handle a player quitting while in a room
     */
    public void handlePlayerQuit(Player player) {
        BattleRoom room = getPlayerRoom(player);
        if (room == null) {
            return;
        }

        if (room.getState() == BattleRoom.RoomState.ACTIVE) {
            // Count as death
            room.removePlayer(player.getUniqueId());
            checkWinCondition(room, player);
        } else {
            room.removePlayer(player.getUniqueId());
        }
    }

    /**
     * Check if there's a winner
     */
    private void checkWinCondition(BattleRoom room, Player loser) {
        int remainingPlayers = room.getPlayerCount();

        if (room.getType() == BattleRoom.RoomType.ONE_V_ONE) {
            // In 1v1, if one player remains, they win
            if (remainingPlayers == 1) {
                UUID winnerId = room.getPlayersInRoom().iterator().next();
                Player winner = Bukkit.getPlayer(winnerId);
                announceWinner(room, winner);
                startCooldown(room);
            } else if (remainingPlayers == 0) {
                // Both died somehow
                announceNoWinner(room);
                startCooldown(room);
            }
        } else {
            // In 2v2, check team remaining
            // For simplicity, if only 1 or less remain, end the match
            if (remainingPlayers <= 1) {
                if (remainingPlayers == 1) {
                    UUID winnerId = room.getPlayersInRoom().iterator().next();
                    Player winner = Bukkit.getPlayer(winnerId);
                    announceWinner(room, winner);
                } else {
                    announceNoWinner(room);
                }
                startCooldown(room);
            }
        }
    }

    private void announceWinner(BattleRoom room, Player winner) {
        String message = configManager.getMessage("player-wins")
                .replace("%winner%", winner != null ? winner.getName() : "Unknown")
                .replace("%room%", room.getName());
        Bukkit.broadcastMessage(message);
    }

    private void announceNoWinner(BattleRoom room) {
        Bukkit.broadcastMessage("§eThe battle in " + room.getName() + " has ended with no winner!");
    }

    /**
     * Start the cooldown period before reopening the room
     */
    private void startCooldown(BattleRoom room) {
        room.setState(BattleRoom.RoomState.COOLDOWN);
        room.clearPlayers();

        int cooldownTime = configManager.getCooldownTime();

        new BukkitRunnable() {
            @Override
            public void run() {
                reopenRoom(room);
            }
        }.runTaskLater(plugin, cooldownTime * 20L);

        // Notify about reopening
        String message = configManager.getMessage("room-reopening").replace("%room%", room.getName());
        Bukkit.broadcastMessage(message);
    }

    /**
     * Reopen a room after cooldown
     */
    private void reopenRoom(BattleRoom room) {
        room.openGate();
        room.setState(BattleRoom.RoomState.WAITING);
        room.clearPlayers();

        plugin.getLogger().info("Room " + room.getName() + " has reopened.");
    }

    /**
     * Check if a block is a protected boundary block
     */
    public boolean isProtectedBlock(Location location) {
        for (BattleRoom room : rooms.values()) {
            if (room.isOnBoundary(location) || room.isGateBlock(location)) {
                return true;
            }
        }
        return false;
    }
}
