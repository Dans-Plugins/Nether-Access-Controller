# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get netheraccesscontroller --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- The plugin no longer fails to start when `allowedPlayers.json` cannot be read. An empty, truncated or otherwise malformed save file previously threw out of startup, and a server that cannot enable the plugin registers none of its listeners — so every player could create and use nether portals, whitelist or not. Such a file is now reported in the server log, renamed to `allowedPlayers.json.unreadable` so that the save on shutdown cannot overwrite it, and startup continues with an empty whitelist. Save data that parses but carries no whitelist is handled the same way, in place of the previous behaviour where the first access check afterwards threw and let the player through.
- The plugin no longer fails to start when `config.yml` exists without a `version` key. The missing key is now treated as a version mismatch, which is the case that already rewrites the missing defaults, rather than being dereferenced. The consequence of the previous behaviour was the same loss of all portal restrictions described above.

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
