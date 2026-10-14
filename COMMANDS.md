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

**Description:** Adds a player to the nether access whitelist, allowing them to create and use nether portals. The player must have joined the server at least once; the name is matched without regard to case, against online players first and then players who have played before. A name the server has never seen is refused with `That player wasn't found.`, and a player already on the whitelist is reported as already allowed.
**Permission:** `nac.allow`
**Usage:** `/nac allow <playerName>`
**Example:** `/nac allow Steve`

### /nac deny \<playerName\>

**Description:** Removes a player from the nether access whitelist, preventing them from creating and using nether portals. The player is looked up the same way as for `/nac allow`, and a player not on the whitelist is reported as already not allowed.
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

**Description:** Sets a configuration option to the specified value. The new text for `denyUsageMessage` and `denyCreationMessage` must be enclosed in single quotes, even when it is one word; everything between the first and the last single quote is used, so apostrophes inside the message are kept. Every other option takes a single word as its value. Option names are case-sensitive and must be spelled as `/nac config show` lists them: `preventportalusage` is refused with `That config option wasn't found.` The new value takes effect at once and is written to `config.yml` straight away.
**Permission:** `nac.config`
**Usage:** `/nac config set <option> <value>`
**Example:** `/nac config set preventPortalUsage true`
**Example:** `/nac config set denyUsageMessage 'You don't have access to the nether.'`
