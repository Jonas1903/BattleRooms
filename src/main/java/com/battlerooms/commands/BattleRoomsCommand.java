package com.battlerooms.commands;

import com.battlerooms.BattleRooms;
import com.battlerooms.managers.RoomManager;
import com.battlerooms.models.BattleRoom;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all BattleRooms commands
 */
public class BattleRoomsCommand implements CommandExecutor, TabCompleter {

    private final BattleRooms plugin;
    private final RoomManager roomManager;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "create", "setpos1", "setpos2", "setgate1", "setgate2", "save", "delete", "list", "reload", "cancel"
    );

    private static final List<String> ROOM_TYPES = Arrays.asList("1v1", "2v2");

    public BattleRoomsCommand(BattleRooms plugin) {
        this.plugin = plugin;
        this.roomManager = plugin.getRoomManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("battlerooms.admin")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "setpos1" -> handleSetPos1(player);
            case "setpos2" -> handleSetPos2(player);
            case "setgate1" -> handleSetGate1(player);
            case "setgate2" -> handleSetGate2(player);
            case "save" -> handleSave(player);
            case "cancel" -> handleCancel(player);
            case "delete" -> handleDelete(player, args);
            case "list" -> handleList(player);
            case "reload" -> handleReload(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== BattleRooms Commands ===");
        player.sendMessage("§e/battlerooms create <1v1|2v2> <name> §7- Start creating a new room");
        player.sendMessage("§e/battlerooms setpos1 §7- Set first corner of room region");
        player.sendMessage("§e/battlerooms setpos2 §7- Set second corner of room region");
        player.sendMessage("§e/battlerooms setgate1 §7- Set first corner of gate area");
        player.sendMessage("§e/battlerooms setgate2 §7- Set second corner of gate area");
        player.sendMessage("§e/battlerooms save §7- Save the room configuration");
        player.sendMessage("§e/battlerooms cancel §7- Cancel room creation");
        player.sendMessage("§e/battlerooms delete <name> §7- Delete a room");
        player.sendMessage("§e/battlerooms list §7- List all configured rooms");
        player.sendMessage("§e/battlerooms reload §7- Reload configuration from file");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /battlerooms create <1v1|2v2> <name>");
            return;
        }

        String typeStr = args[1];
        String name = args[2];

        BattleRoom.RoomType type = BattleRoom.RoomType.fromString(typeStr);
        if (type == null) {
            player.sendMessage("§cInvalid room type! Use 1v1 or 2v2.");
            return;
        }

        if (roomManager.roomExists(name)) {
            player.sendMessage("§cA room with that name already exists!");
            return;
        }

        if (roomManager.isCreating(player)) {
            player.sendMessage("§cYou are already creating a room! Use /battlerooms save or /battlerooms cancel first.");
            return;
        }

        roomManager.startCreating(player, type, name);
        player.sendMessage("§aStarted creating room: §f" + name + " §a(" + type.getDisplayName() + ")");
        player.sendMessage("§7Now set the room region using:");
        player.sendMessage("§7  /battlerooms setpos1 §7- First corner");
        player.sendMessage("§7  /battlerooms setpos2 §7- Second corner");
    }

    private void handleSetPos1(Player player) {
        BattleRoom room = roomManager.getCreatingRoom(player);
        if (room == null) {
            player.sendMessage("§cYou are not creating a room! Use /battlerooms create first.");
            return;
        }

        room.setPos1(player.getLocation());
        player.sendMessage("§aPosition 1 set to your current location!");
        checkProgress(player, room);
    }

    private void handleSetPos2(Player player) {
        BattleRoom room = roomManager.getCreatingRoom(player);
        if (room == null) {
            player.sendMessage("§cYou are not creating a room! Use /battlerooms create first.");
            return;
        }

        room.setPos2(player.getLocation());
        player.sendMessage("§aPosition 2 set to your current location!");
        checkProgress(player, room);
    }

    private void handleSetGate1(Player player) {
        BattleRoom room = roomManager.getCreatingRoom(player);
        if (room == null) {
            player.sendMessage("§cYou are not creating a room! Use /battlerooms create first.");
            return;
        }

        room.setGate1(player.getLocation());
        player.sendMessage("§aGate position 1 set to your current location!");
        checkProgress(player, room);
    }

    private void handleSetGate2(Player player) {
        BattleRoom room = roomManager.getCreatingRoom(player);
        if (room == null) {
            player.sendMessage("§cYou are not creating a room! Use /battlerooms create first.");
            return;
        }

        room.setGate2(player.getLocation());
        player.sendMessage("§aGate position 2 set to your current location!");
        checkProgress(player, room);
    }

    private void checkProgress(Player player, BattleRoom room) {
        StringBuilder status = new StringBuilder("§7Progress: ");
        status.append(room.getPos1() != null ? "§aPos1 ✓ " : "§cPos1 ✗ ");
        status.append(room.getPos2() != null ? "§aPos2 ✓ " : "§cPos2 ✗ ");
        status.append(room.getGate1() != null ? "§aGate1 ✓ " : "§cGate1 ✗ ");
        status.append(room.getGate2() != null ? "§aGate2 ✓" : "§cGate2 ✗");
        player.sendMessage(status.toString());

        if (room.isComplete()) {
            player.sendMessage("§aAll positions set! Use §f/battlerooms save §ato save the room.");
        }
    }

    private void handleSave(Player player) {
        if (!roomManager.isCreating(player)) {
            player.sendMessage("§cYou are not creating a room!");
            return;
        }

        BattleRoom room = roomManager.getCreatingRoom(player);
        if (!room.isComplete()) {
            player.sendMessage("§cRoom is not complete! Set all positions first.");
            checkProgress(player, room);
            return;
        }

        if (roomManager.saveCreatingRoom(player)) {
            player.sendMessage("§aRoom §f" + room.getName() + " §asaved successfully!");
        } else {
            player.sendMessage("§cFailed to save room!");
        }
    }

    private void handleCancel(Player player) {
        if (!roomManager.isCreating(player)) {
            player.sendMessage("§cYou are not creating a room!");
            return;
        }

        roomManager.cancelCreating(player);
        player.sendMessage("§cRoom creation cancelled.");
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /battlerooms delete <name>");
            return;
        }

        String name = args[1];
        if (roomManager.deleteRoom(name)) {
            player.sendMessage("§aRoom §f" + name + " §adeleted successfully!");
        } else {
            player.sendMessage("§cRoom not found: " + name);
        }
    }

    private void handleList(Player player) {
        var rooms = roomManager.getRooms();
        if (rooms.isEmpty()) {
            player.sendMessage("§7No rooms configured.");
            return;
        }

        player.sendMessage("§6§l=== Configured Rooms ===");
        for (BattleRoom room : rooms.values()) {
            String stateColor = switch (room.getState()) {
                case WAITING -> "§a";
                case ACTIVE -> "§c";
                case COOLDOWN -> "§e";
            };
            player.sendMessage("§f" + room.getName() + " §7(" + room.getType().getDisplayName() + ") - " 
                    + stateColor + room.getState().name());
        }
    }

    private void handleReload(Player player) {
        roomManager.reloadRooms();
        player.sendMessage("§aConfiguration reloaded!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("battlerooms.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                return ROOM_TYPES.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("delete")) {
                return roomManager.getRooms().keySet().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
