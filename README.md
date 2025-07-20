# DupeGuard - Minecraft Spigot Plugin

Advanced item duplication detection and prevention plugin for Spigot 1.20+

## Features

- **Real-time Monitoring**: Checks player inventories every second for suspicious item amounts
- **Customizable GUI**: Easy-to-use interface for managing monitored items
- **Automatic Actions**: Configurable alerts and automatic temporary bans
- **Comprehensive Logging**: Detailed logs of all ban actions with player info and coordinates
- **Performance Optimized**: Designed to handle 100+ players without lag
- **Full Item Support**: Works with all item types including custom items with NBT data

## Installation

1. Download the compiled JAR file
2. Place it in your server's `plugins` folder
3. Restart or reload your server
4. Configure the plugin using `/dg edit` and editing `config.yml`

## Commands

- `/dupeguard edit` or `/dg edit` - Opens the item management GUI (Permission: `dupeguard.admin`)
- `/dupeguard reload` or `/dg reload` - Reloads the plugin configuration (Permission: `dupeguard.admin`)
- `/dupeguard list` or `/dg list` - Lists all monitored items (Permission: `dupeguard.admin`)

## Permissions

- `dupeguard.admin` - Access to all admin commands (Default: OP)
- `dupeguard.alert` - Receive duplication alerts (Default: OP)
- `dupeguard.bypass` - Bypass duplication checks (Default: false)

## Configuration

Edit `config.yml` to customize:

```yaml
detection:
  max-items-before-alert: 64      # Alert threshold
  max-items-before-ban: 256       # Auto-ban threshold

auto-ban:
  enabled: true                   # Enable/disable auto-banning
  duration-minutes: 60            # Ban duration

messages:
  ban-message: "Your ban message here"
  alert-message: "Alert message with %placeholders%"
```

## How to Use

1. Run `/dg edit` to open the GUI
2. Add items to monitor by placing them in the GUI
3. Click the emerald block to save and reload
4. The plugin will now monitor all players for those items

## File Structure

```
plugins/
└── DupeGuard/
    ├── config.yml     # Main configuration
    ├── items.yml      # Monitored items storage
    └── bans.log       # Ban history log
```

## Building from Source

Requirements:
- Java 17+
- Gradle 7+

```bash
git clone <repository>
cd DupeGuard
gradle build
```

The compiled JAR will be in `build/libs/`

## Performance Notes

- Optimized for servers with 100+ players
- Uses efficient item comparison algorithms
- Implements smart caching to reduce overhead
- Asynchronous operations where possible

## Support

For issues or feature requests, please use the issue tracker.
