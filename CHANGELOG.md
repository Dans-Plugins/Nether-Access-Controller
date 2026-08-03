# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

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
