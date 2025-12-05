# BattleRooms

A Minecraft 1.21.8 PvP arena plugin with 1v1 and 2v2 battle rooms.

## Description

BattleRooms is a PvP arena plugin that allows server administrators to create and manage battle rooms where players can engage in 1v1 or 2v2 combat. The plugin automatically seals rooms when the required number of players enter and reopens them after the battle concludes.

## Features

- **Two Room Types**: Support for 1v1 (2 players) and 2v2 (4 players) battles
- **Automatic Room Management**: Rooms automatically close when filled and reopen after battles
- **Gate System**: Blue glass barriers seal the entrance/exits when battles begin
- **Block Protection**: Room walls, floor, and ceiling are protected from destruction (except by OPs)
- **Command Blocking**: Players cannot use commands while in an active battle (except OPs)
- **Disconnect Handling**: Disconnecting players are counted as defeated
- **Configurable Cooldown**: Adjustable time before rooms reopen after a battle

## Installation

1. Download the BattleRooms.jar file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure rooms using in-game commands

## Commands

| Command | Description |
|---------|-------------|
| `/battlerooms create <1v1\|2v2> <name>` | Start creating a new room |
| `/battlerooms setpos1` | Set the first corner of the room region |
| `/battlerooms setpos2` | Set the second corner of the room region |
| `/battlerooms setgate1` | Set the first corner of the gate/entrance area |
| `/battlerooms setgate2` | Set the second corner of the gate/entrance area |
| `/battlerooms save` | Save the room configuration |
| `/battlerooms cancel` | Cancel room creation in progress |
| `/battlerooms delete <name>` | Delete a room |
| `/battlerooms list` | List all configured rooms |
| `/battlerooms reload` | Reload configuration from file |

**Aliases**: `/br`, `/arena`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `battlerooms.admin` | Access to all BattleRooms admin commands | OP |
| `battlerooms.bypass` | Bypass command restrictions in rooms | OP |

## Configuration

The `config.yml` file contains the following settings:

```yaml
# Room cooldown time in seconds (time before room reopens after a match ends)
cooldown-time: 30

# Customizable messages
messages:
  room-closed: "&cThis room is currently in a battle!"
  commands-disabled: "&cYou cannot use commands while in a battle!"
  player-wins: "&a%winner% has won the battle in %room%!"
  team-wins: "&aTeam has won the battle in %room%!"
  room-reopening: "&eThe room %room% is reopening..."

# Room configurations (auto-generated)
rooms: {}
```

## Room Setup Guide

1. Build your arena with walls, floor, and ceiling
2. Create a gate/entrance area that will be sealed with blue glass during battles
3. Use `/battlerooms create <1v1|2v2> <roomname>` to start room creation
4. Stand at one corner of the room and use `/battlerooms setpos1`
5. Stand at the opposite corner and use `/battlerooms setpos2`
6. Stand at one corner of the gate area and use `/battlerooms setgate1`
7. Stand at the opposite corner of the gate and use `/battlerooms setgate2`
8. Use `/battlerooms save` to save the room

## Build Instructions

### Requirements
- Java 22
- Maven 3.6+
- IntelliJ IDEA (recommended)

### Building with Maven

```bash
# Clone the repository
git clone https://github.com/Jonas1903/BattleRooms.git
cd BattleRooms

# Build the plugin
mvn clean package

# The compiled JAR will be in target/BattleRooms-1.0.0.jar
```

### Building with IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select "Open" and navigate to the BattleRooms directory
3. IntelliJ will automatically detect the Maven project
4. Wait for Maven to download dependencies
5. Open the Maven tool window (View → Tool Windows → Maven)
6. Double-click on "Lifecycle" → "package" to build
7. Find the compiled JAR in the `target` folder

## Technical Details

- **Minecraft Version**: 1.21.8
- **API**: Paper/Spigot 1.21
- **Java Version**: 22
- **Build Tool**: Maven

## Room States

- **WAITING**: Room is open and waiting for players
- **ACTIVE**: Required players reached, room is sealed, battle in progress
- **COOLDOWN**: Battle ended, room will reopen after the configured cooldown time

## License

This project is open source and available under the MIT License.
