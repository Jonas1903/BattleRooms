package com.battlerooms;

import com.battlerooms.commands.BattleRoomsCommand;
import com.battlerooms.listeners.*;
import com.battlerooms.managers.ConfigManager;
import com.battlerooms.managers.RoomManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for BattleRooms
 * A PvP arena plugin with 1v1 and 2v2 battle rooms for Minecraft 1.21.8
 */
public class BattleRooms extends JavaPlugin {

    private ConfigManager configManager;
    private RoomManager roomManager;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        roomManager = new RoomManager(this, configManager);
        roomManager.loadRooms();

        // Register commands
        BattleRoomsCommand commandExecutor = new BattleRoomsCommand(this);
        getCommand("battlerooms").setExecutor(commandExecutor);
        getCommand("battlerooms").setTabCompleter(commandExecutor);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);

        getLogger().info("BattleRooms has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BattleRooms has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }
}
