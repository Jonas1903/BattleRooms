package com.battlerooms.managers;

import com.battlerooms.BattleRooms;
import com.battlerooms.models.BattleRoom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and saving room configurations
 */
public class ConfigManager {

    private final BattleRooms plugin;
    private FileConfiguration config;

    public ConfigManager(BattleRooms plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public int getCooldownTime() {
        return config.getInt("cooldown-time", 30);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "Message not found: " + key)
                .replace("&", "§");
    }

    /**
     * Load all rooms from configuration
     */
    public Map<String, BattleRoom> loadRooms() {
        Map<String, BattleRoom> rooms = new HashMap<>();
        ConfigurationSection roomsSection = config.getConfigurationSection("rooms");

        if (roomsSection == null) {
            return rooms;
        }

        for (String roomName : roomsSection.getKeys(false)) {
            ConfigurationSection roomSection = roomsSection.getConfigurationSection(roomName);
            if (roomSection == null) continue;

            String typeStr = roomSection.getString("type");
            String worldName = roomSection.getString("world");

            BattleRoom.RoomType type = BattleRoom.RoomType.fromString(typeStr);
            if (type == null || worldName == null) continue;

            World world = Bukkit.getWorld(worldName);

            Location pos1 = loadLocation(roomSection.getConfigurationSection("pos1"), world);
            Location pos2 = loadLocation(roomSection.getConfigurationSection("pos2"), world);
            Location gate1 = loadLocation(roomSection.getConfigurationSection("gate1"), world);
            Location gate2 = loadLocation(roomSection.getConfigurationSection("gate2"), world);

            BattleRoom room = new BattleRoom(roomName, type, worldName, pos1, pos2, gate1, gate2);
            rooms.put(roomName.toLowerCase(), room);

            plugin.getLogger().info("Loaded room: " + roomName + " (" + type.getDisplayName() + ")");
        }

        return rooms;
    }

    /**
     * Save a room to configuration
     */
    public void saveRoom(BattleRoom room) {
        String path = "rooms." + room.getName();

        config.set(path + ".type", room.getType().getDisplayName());
        config.set(path + ".world", room.getWorldName());

        saveLocation(path + ".pos1", room.getPos1());
        saveLocation(path + ".pos2", room.getPos2());
        saveLocation(path + ".gate1", room.getGate1());
        saveLocation(path + ".gate2", room.getGate2());

        saveConfig();
    }

    /**
     * Delete a room from configuration
     */
    public void deleteRoom(String roomName) {
        config.set("rooms." + roomName, null);
        saveConfig();
    }

    private Location loadLocation(ConfigurationSection section, World world) {
        if (section == null || world == null) return null;

        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");

        return new Location(world, x, y, z);
    }

    private void saveLocation(String path, Location location) {
        if (location == null) {
            config.set(path, null);
            return;
        }

        config.set(path + ".x", location.getBlockX());
        config.set(path + ".y", location.getBlockY());
        config.set(path + ".z", location.getBlockZ());
    }
}
