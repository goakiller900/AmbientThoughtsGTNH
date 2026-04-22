# Ambient Thoughts GTNH

Ambient Thoughts GTNH is a server-side Minecraft 1.7.10 / GT New Horizons utility mod that sends occasional atmospheric chat messages to players.

The goal is simple: make the server feel a little more alive.

It can send:
- ambient timed messages
- delayed join messages
- activity-aware messages
- GTNH-flavored messages when the context looks GregTech-ish

It also supports editable JSON message pools, placeholder replacement, reload commands, cooldowns, repeat protection, and context detection.

---

## Features

### Ambient timed messages
The mod periodically selects one online player and sends them a message in chat.

The interval is configurable with:
- `minMinutesBetweenMessages`
- `maxMinutesBetweenMessages`

### Delayed join messages
Players can receive a welcome-back style message a few seconds after logging in instead of instantly on join.

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
- fighting for your life
- organizing chests
- fishing
- working on nearby machinery

### GTNH-aware flavor
If the detected context strongly looks like GTNH, the mod can prefer GTNH-themed categories more often.

### External JSON message file
On first run, the mod copies its default message file into the config folder so you can edit messages without rebuilding the mod.

Runtime path:

`config/ambientthoughts/messages.json`

### Reload command
The mod supports:

`/ambientthoughtsreload`

This reloads:
- config values
- external `messages.json`

without requiring a full restart.

### Manual test command
The mod supports:

`/ambientthoughtsmsg <player>`

This immediately sends a test message to the specified player using their current detected context.

---

## Current behavior

At runtime, the mod roughly does this:

1. load config
2. ensure `config/ambientthoughts/messages.json` exists
3. load messages from JSON
4. detect player context
5. choose a fitting message
6. replace placeholders
7. send the message to one player
8. avoid repeats and cooldown spam

---

## Install

1. Build the mod jar
2. Place the jar in the server `mods` folder
3. Start the server once
4. Edit:
  - `config/ambientthoughts.cfg`
  - `config/ambientthoughts/messages.json`
5. Use `/ambientthoughtsreload` after making changes

---

## Build

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat build
