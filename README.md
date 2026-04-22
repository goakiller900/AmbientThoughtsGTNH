# Ambient Thoughts GTNH

Ambient Thoughts is a server-side Minecraft 1.7.10 / GTNH utility mod that sends occasional atmospheric chat messages to players.

The mod is designed to feel a bit personal, a bit weird, and a bit alive.

It supports:
- ambient timed messages
- delayed join messages
- editable JSON message pools
- placeholder replacement like `{player}`, `{biome}`, `{dimension}`, `{activity}`, `{machine}`, `{item}`
- activity-aware messages
- GTNH-flavored message categories
- per-player cooldowns
- repeat protection
- reload command without server restart

## Features

### Timed ambient messages
The mod periodically selects one online player and sends them a message in chat.

The interval is configurable with:
- `minMinutesBetweenMessages`
- `maxMinutesBetweenMessages`

### Join messages
Players can receive a delayed welcome-back style message after joining.

This is configurable with:
- `enableJoinMessages`
- `joinMessageDelaySeconds`
- `joinMessageCooldownSeconds`

### Placeholder support
Messages can use:
- `{player}`
- `{biome}`
- `{dimension}`
- `{activity}`
- `{machine}`
- `{item}`

### Activity detection
The mod tries to detect what a player is doing, such as:
- mining
- building
- farming
- exploring
- combat
- organizing chests
- working on nearby machinery
- fishing

### GTNH-aware flavor
If the player's context looks strongly GTNH-like, the mod can prefer GTNH-themed message categories more often.

### External JSON messages
On first run, the mod copies its default `messages.json` into the config folder so the server owner can edit messages without rebuilding the mod.

Runtime path:
`config/ambientthoughts/messages.json`

### Reload command
The mod supports:

`/ambientthoughtsreload`

This reloads:
- config values
- external `messages.json`

without requiring a full restart.

## Install

1. Build the mod jar
2. Place the jar in the server `mods` folder
3. Start the server once
4. Edit:
  - `config/ambientthoughts.cfg`
  - `config/ambientthoughts/messages.json`
5. Use `/ambientthoughtsreload` after making changes

## Build

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat build
