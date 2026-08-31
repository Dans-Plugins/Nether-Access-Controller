# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- An unreadable `allowedPlayers.json` no longer displaces one that was set aside on an earlier occasion. The destination was the fixed path `allowedPlayers.json.unreadable`, and the rename replaced whatever was already there without a word, so a second unreadable save file destroyed the only copy of the first — the very file the rename exists to preserve. The destination is now numbered upwards (`allowedPlayers.json.unreadable.2`, `.3`, and so on) until a free one is found, the path used is named in the server log, and the rename is given up on rather than performed if no free destination exists.
- `/nac list` and a bare `/nac` no longer report themselves to the server as having been used incorrectly. Both returned `false` from `onCommand` after doing what was asked of them, which is the value that asks the server to print the command's usage string. Nothing was printed in practice, because `plugin.yml` declares no `usage` key for the command; adding one would have made a successful `/nac list` start printing usage text after the whitelist.
- `/nac config set` no longer accepts an unrecognised value for a boolean option. Any value other than `true` was previously stored as `false` and reported as `Boolean set.` in green, so `/nac config set preventPortalCreation yes` — or `1`, or `enabled`, or a mistyped `ture` — silently turned portal-creation enforcement off and let every player create nether portals. Only `true` and `false` are accepted now, in any casing; anything else is refused with a message naming the accepted values, and the stored value is left as it was.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get netheraccesscontroller --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- The plugin no longer fails to start when `allowedPlayers.json` cannot be read. An empty, truncated or otherwise malformed save file previously threw out of startup, and a server that cannot enable the plugin registers none of its listeners — so every player could create and use nether portals, whitelist or not. Such a file is now reported in the server log, renamed to `allowedPlayers.json.unreadable` so that the save on shutdown cannot overwrite it, and startup continues with an empty whitelist. Save data that parses but carries no whitelist is handled the same way, in place of the previous behaviour where the first access check afterwards threw and let the player through.
- The plugin no longer fails to start when `config.yml` exists without a `version` key. The missing key is now treated as a version mismatch, which is the case that already rewrites the missing defaults, rather than being dereferenced. The consequence of the previous behaviour was the same loss of all portal restrictions described above.

### Changed

- `ConfigService` no longer carries the unreachable integer and double branches of `/nac config set`, and lists `debugMode` through the same boolean accessor as the two options beside it. Option names are now compared exactly, matching the case-sensitive lookup that already rejects a differently-cased name before those comparisons are reached. No operator-visible behaviour changes.

### Security

- `/nac config` no longer runs for players who lack the `nac.config` permission. The permission was checked and the denial message was sent, but the result was discarded and the command executed anyway, so any player could read the configuration with `/nac config show` or disable whitelist enforcement outright with `/nac config set preventPortalCreation false`. Servers that have not granted `nac.config` beyond operators are advised to check `config.yml` for unexpected values.

## [2.0.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- Nether-Access-Controller is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The major version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `2.0.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Added

- Unit tests (JUnit 5) covering the whitelist logic in `PersistentData`
- `.gitignore` for Maven build output (`target/`) and common editor/IDE artifacts

## [1.1.0]

### Added

- Configuration commands (`/nac config show`, `/nac config set`)
- Customisable denial messages (`denyUsageMessage`, `denyCreationMessage`)
- Debug mode for troubleshooting event handling

## [1.0.0]

### Added

- Initial release
- Nether access whitelist system
- Portal creation prevention
- Portal usage prevention
- Commands: `/nac help`, `/nac allow`, `/nac deny`, `/nac list`
- bStats integration
