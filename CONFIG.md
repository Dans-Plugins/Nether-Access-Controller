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
