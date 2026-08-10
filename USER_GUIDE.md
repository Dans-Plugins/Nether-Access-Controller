# User Guide

## Prerequisites

- A Spigot or Paper Minecraft server (API version 1.13 or later)
- Operator (op) permissions on the server

## First Steps

After installing the plugin and restarting your server, nether portal **creation** is prevented by default for all non-allowed players. Portal **usage** prevention is disabled by default and must be enabled via configuration.

Use `/nac help` in-game or from the console to see all available commands.

## Common Scenarios

### Allowing a Player to Access the Nether

```
/nac allow <playerName>
```

This adds the player to the nether access whitelist. They will be able to create and use nether portals (depending on your configuration).

### Denying a Player Nether Access

```
/nac deny <playerName>
```

This removes the player from the nether access whitelist.

### Viewing the Whitelist

```
/nac list
```

This displays all players currently allowed to access the nether.

### Preventing Portal Usage

By default, only portal creation is blocked. To also block portal usage:

```
/nac config set preventPortalUsage true
```

### Customising Denial Messages

```
/nac config set denyUsageMessage 'You are not allowed to enter the nether.'
/nac config set denyCreationMessage 'You cannot create nether portals.'
```

Use single quotes around messages that contain spaces.

### Recovering an Unreadable Whitelist

The whitelist is stored in `plugins/NetherAccessController/allowedPlayers.json`. If that file cannot be read at startup — because it is empty, or was truncated by a crash part-way through a save — the plugin reports it in the server log and renames it to `allowedPlayers.json.unreadable`, then starts with an empty whitelist so that portal restrictions stay in force.

No player has nether access until the whitelist is rebuilt. Rebuild it with `/nac allow <playerName>`, or stop the server, repair the renamed file by hand and restore it to its original name before starting again.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `nac.help` | op | Access the help command |
| `nac.list` | op | View the nether access whitelist |
| `nac.allow` | op | Allow a player nether access |
| `nac.deny` | op | Deny a player nether access |
| `nac.config` | op | View and modify configuration |
