# Configuration Guide

Configuration can be modified in-game using `/nac config set <option> <value>` or by editing `plugins/NetherAccessController/config.yml` directly.

A change made with `/nac config set` takes effect at once, except for the three `usage-reporting` options, which are read only when the plugin enables: a change to them is saved straight away but takes effect after the server is restarted (#105). The file is only read when the plugin enables, so a change made by editing it takes effect after the server is restarted. Edit the file while the server is stopped: if `/nac config set` is used while the server is running, the plugin's copy of the config, which lacks the hand edits, is written over the file straight away and again when the server stops.

Option names are case-sensitive in both places.

Options marked below as boolean accept only `true` or `false`, in any casing. Any other value is refused by `/nac config set` and the current setting is kept.

## version

**Type:** string
**Default:** the running plugin version, prefixed with `v`
**Description:** The plugin version that last wrote the file, written with a leading `v` (for example `v2.2.2`), which is also how `/nac config show` displays it. It is not a setting: `/nac config set version` is refused with `Cannot set version.`, and a hand-edited value is replaced by the running version on the next start. A file whose version differs from the running plugin, or that has none, is one of the cases in which the plugin writes any missing option back into the file with its default.

**Example:**

```yaml
version: v2.2.2
```

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
**Description:** When enabled, prevents players who are not on the whitelist from using existing nether portals. If disabled, only portal creation is controlled. While it is enabled, a whitelisted player who goes through a nether portal is told `You step through the portal.`; end portals are not affected.

**Example:**

```yaml
preventPortalUsage: false
```

## preventPortalCreation

**Type:** boolean
**Default:** `true`
**Description:** When enabled, prevents players who are not on the whitelist from creating nether portals with flint and steel or a fire charge on obsidian. Any click on obsidian is refused while flint and steel or a fire charge is held in either hand; the same click by a whitelisted player is allowed and they are told `You light the portal.`, whether or not a portal is actually lit. A portal lit without a player, by a dispenser or spreading fire, is not prevented (#89).

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

When the plugin is enabled, and each time a player uses `/nac`, a small event is sent to the author's
[trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which plugins
are actually in use. Commands run from the console, a command block or another plugin are not reported. An event carries the plugin's name, the event name (`startup` or `command`), the plugin
version, and for a `command` event the command name, plus a random server ID (the `server-id` line in `plugins/trace/config.yml`, which identifies no person, account or IP address; deleting the line gets a new one) — nothing about players or the world.
Identical events within 60 seconds of the first are counted and sent as one event, tagged
`coalesced` with the count; `startup` is never held back.
Sending happens off the main thread, never delays a tick, and is dropped silently if the server
cannot be reached. Set `usage-reporting.enabled` to `false` to turn it off, in `config.yml` or with
`/nac config set usage-reporting.enabled false`. Either way, reporting stops only after the server is
restarted: `/nac config set` saves the new value and `/nac config show` lists it at once, but events
keep being sent until the plugin next enables (#105).

It can also be turned off for every plugin on the server that reports this way, with `enabled: false`
in `plugins/trace/config.yml` (created the first time such a plugin enables; plugins never turn it
back on), or for the whole server process with the environment variable `TRACE_USAGE_REPORTING=off`
or `DO_NOT_TRACK=1`. Each startup logs whether reporting is on or, if it is off, why. The
`usage-reporting` block is written into `config.yml` on the first enable that finds it missing, so
the switch is visible on servers upgraded from before it existed. Details:
<https://danielstephenson.dev/usage-reporting>.

Servers upgraded from a version before this block existed do not need to add it: the plugin reads the
bundled defaults for any key the file lacks, so reporting is active until it is turned off, and the
block itself is written into the file on the first enable that finds it missing.
