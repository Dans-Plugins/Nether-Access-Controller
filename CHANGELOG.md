# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get netheraccesscontroller --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

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
