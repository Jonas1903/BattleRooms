package com.battlerooms.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a battle room with its configuration and state
 */
public class BattleRoom {

    public enum RoomType {
        ONE_V_ONE(2, "1v1"),
        TWO_V_TWO(4, "2v2");

        private final int requiredPlayers;
        private final String displayName;

        RoomType(int requiredPlayers, String displayName) {
            this.requiredPlayers = requiredPlayers;
            this.displayName = displayName;
        }

        public int getRequiredPlayers() {
            return requiredPlayers;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static RoomType fromString(String type) {
            if (type.equalsIgnoreCase("1v1") || type.equalsIgnoreCase("one_v_one")) {
                return ONE_V_ONE;
            } else if (type.equalsIgnoreCase("2v2") || type.equalsIgnoreCase("two_v_two")) {
                return TWO_V_TWO;
            }
            return null;
        }
    }

    public enum RoomState {
        WAITING,   // Room is open, waiting for players
        ACTIVE,    // Required players reached, room is sealed
        COOLDOWN   // Match ended, waiting to reopen
    }

    private final String name;
    private final RoomType type;
    private final String worldName;
    private Location pos1;
    private Location pos2;
    private Location gate1;
    private Location gate2;
    private RoomState state;
    private final Set<UUID> playersInRoom;
    private final Map<Location, Material> originalGateBlocks;

    public BattleRoom(String name, RoomType type, String worldName) {
        this.name = name;
        this.type = type;
        this.worldName = worldName;
        this.state = RoomState.WAITING;
        this.playersInRoom = new HashSet<>();
        this.originalGateBlocks = new HashMap<>();
    }

    public BattleRoom(String name, RoomType type, String worldName,
                      Location pos1, Location pos2, Location gate1, Location gate2) {
        this(name, type, worldName);
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.gate1 = gate1;
        this.gate2 = gate2;
    }

    public String getName() {
        return name;
    }

    public RoomType getType() {
        return type;
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getGate1() {
        return gate1;
    }

    public void setGate1(Location gate1) {
        this.gate1 = gate1;
    }

    public Location getGate2() {
        return gate2;
    }

    public void setGate2(Location gate2) {
        this.gate2 = gate2;
    }

    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    public Set<UUID> getPlayersInRoom() {
        return playersInRoom;
    }

    public void addPlayer(UUID playerId) {
        playersInRoom.add(playerId);
    }

    public void removePlayer(UUID playerId) {
        playersInRoom.remove(playerId);
    }

    public void clearPlayers() {
        playersInRoom.clear();
    }

    public int getPlayerCount() {
        return playersInRoom.size();
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null && gate1 != null && gate2 != null;
    }

    /**
     * Check if a location is within the room region
     */
    public boolean isInRoom(Location location) {
        if (pos1 == null || pos2 == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    /**
     * Check if a block is part of the room boundary (wall, floor, or ceiling)
     */
    public boolean isOnBoundary(Location location) {
        if (pos1 == null || pos2 == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // Check if it's on the boundary
        boolean onXBoundary = (x == minX || x == maxX);
        boolean onYBoundary = (y == minY || y == maxY);
        boolean onZBoundary = (z == minZ || z == maxZ);

        // Must be within the room region
        boolean inXRange = x >= minX && x <= maxX;
        boolean inYRange = y >= minY && y <= maxY;
        boolean inZRange = z >= minZ && z <= maxZ;

        if (!inXRange || !inYRange || !inZRange) return false;

        // A block is on the boundary if at least one of its coordinates is at the boundary
        return onXBoundary || onYBoundary || onZBoundary;
    }

    /**
     * Close the room by placing blue glass at the gate
     */
    public void closeGate() {
        if (gate1 == null || gate2 == null) return;

        World world = getWorld();
        if (world == null) return;

        originalGateBlocks.clear();

        int minX = Math.min(gate1.getBlockX(), gate2.getBlockX());
        int maxX = Math.max(gate1.getBlockX(), gate2.getBlockX());
        int minY = Math.min(gate1.getBlockY(), gate2.getBlockY());
        int maxY = Math.max(gate1.getBlockY(), gate2.getBlockY());
        int minZ = Math.min(gate1.getBlockZ(), gate2.getBlockZ());
        int maxZ = Math.max(gate1.getBlockZ(), gate2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    Block block = world.getBlockAt(loc);
                    originalGateBlocks.put(loc.clone(), block.getType());
                    block.setType(Material.BLUE_STAINED_GLASS);
                }
            }
        }
    }

    /**
     * Open the room by restoring original blocks at the gate
     */
    public void openGate() {
        World world = getWorld();
        if (world == null) return;

        for (Map.Entry<Location, Material> entry : originalGateBlocks.entrySet()) {
            world.getBlockAt(entry.getKey()).setType(entry.getValue());
        }
        originalGateBlocks.clear();
    }

    /**
     * Check if a location is part of the gate
     */
    public boolean isGateBlock(Location location) {
        if (gate1 == null || gate2 == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        int minX = Math.min(gate1.getBlockX(), gate2.getBlockX());
        int maxX = Math.max(gate1.getBlockX(), gate2.getBlockX());
        int minY = Math.min(gate1.getBlockY(), gate2.getBlockY());
        int maxY = Math.max(gate1.getBlockY(), gate2.getBlockY());
        int minZ = Math.min(gate1.getBlockZ(), gate2.getBlockZ());
        int maxZ = Math.max(gate1.getBlockZ(), gate2.getBlockZ());

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
