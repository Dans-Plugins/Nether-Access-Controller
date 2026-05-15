# Commands Reference

All commands use the `/nac` base command.

## General Commands

### /nac

**Description:** Displays the plugin version and developer information.
**Permission:** None
**Usage:** `/nac`

### /nac help

**Description:** Lists all available commands.
**Permission:** `nac.help`
**Usage:** `/nac help`

## Whitelist Commands

### /nac allow \<playerName\>

**Description:** Adds a player to the nether access whitelist, allowing them to create and use nether portals.
**Permission:** `nac.allow`
**Usage:** `/nac allow <playerName>`
**Example:** `/nac allow Steve`

### /nac deny \<playerName\>

**Description:** Removes a player from the nether access whitelist, preventing them from creating and using nether portals.
**Permission:** `nac.deny`
**Usage:** `/nac deny <playerName>`
**Example:** `/nac deny Steve`

### /nac list

**Description:** Displays all players currently on the nether access whitelist.
**Permission:** `nac.list`
**Usage:** `/nac list`

## Configuration Commands

### /nac config show

**Description:** Displays all current configuration settings and their values.
**Permission:** `nac.config`
**Usage:** `/nac config show`

### /nac config set \<option\> \<value\>

**Description:** Sets a configuration option to the specified value. Use single quotes around values that contain spaces.
**Permission:** `nac.config`
**Usage:** `/nac config set <option> <value>`
**Example:** `/nac config set preventPortalUsage true`
**Example:** `/nac config set denyUsageMessage 'You cannot enter the nether.'`
