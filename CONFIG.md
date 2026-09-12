# Configuration Guide

Configuration can be modified in-game using `/nac config set <option> <value>` or by editing `plugins/NetherAccessController/config.yml` directly.

Options marked below as boolean accept only `true` or `false`, in any casing. Any other value is refused by `/nac config set` and the current setting is kept.

## debugMode

**Type:** boolean
**Default:** `false`
**Description:** Enables debug logging to the server console when portal and interaction events fire. Useful for troubleshooting.

**Example:**

```yaml
debugMode: false
```

## preventPortalUsage

**Type:** boolean
**Default:** `false`
**Description:** When enabled, prevents players who are not on the whitelist from using existing nether portals. If disabled, only portal creation is controlled.

**Example:**

```yaml
preventPortalUsage: false
```

## preventPortalCreation

**Type:** boolean
**Default:** `true`
**Description:** When enabled, prevents players who are not on the whitelist from creating nether portals with flint and steel on obsidian.

**Example:**

```yaml
preventPortalCreation: true
```

## denyUsageMessage

**Type:** string
**Default:** `"You're unable to use nether portals."`
**Description:** The message sent to players when they are denied access to a nether portal. Only applies when `preventPortalUsage` is enabled.

**Example:**

```yaml
denyUsageMessage: "You're unable to use nether portals."
```

## denyCreationMessage

**Type:** string
**Default:** `"You're unable to create nether portals."`
**Description:** The message sent to players when they are denied the ability to create a nether portal. Only applies when `preventPortalCreation` is enabled.

**Example:**

```yaml
denyCreationMessage: "You're unable to create nether portals."
```

## usage-reporting.enabled

**Type:** boolean
**Default:** `true`
**Description:** Whether the plugin reports usage events (see below). Set to `false` to turn it off.

**Example:**

```yaml
usage-reporting:
  enabled: true
```

## usage-reporting.endpoint

**Type:** string
**Default:** `https://trace.danielstephenson.dev`
**Description:** The trace server events are sent to.

## usage-reporting.key

**Type:** string
**Default:** the plugin's key
**Description:** Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as NetherAccessController. Empty means reporting is off regardless of `usage-reporting.enabled`.

## Usage reporting

When the plugin is enabled, and each time `/nac` is used, a small event is sent to the author's
[trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which plugins
are actually in use. An event carries the plugin's name, the event name (`startup` or `command`), and
either the plugin version or the command name — nothing about players, the world, or the server.
Sending happens off the main thread, never delays a tick, and is dropped silently if the server
cannot be reached. Set `usage-reporting.enabled` to `false` to turn it off, in `config.yml` or with
`/nac config set usage-reporting.enabled false`.

Servers upgraded from a version before this block existed do not need to add it: the plugin reads the
bundled defaults for any key the file lacks, so reporting is active until it is turned off.
