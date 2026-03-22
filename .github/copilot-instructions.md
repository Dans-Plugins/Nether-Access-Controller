# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java
- Build tool: Maven
- Target platform: Spigot / Paper (Minecraft plugin)
- Minimum API version: 1.13

## Project Structure

- `src/main/java/` – Plugin source code
- `src/main/resources/` – `plugin.yml` and resource files
- `src/test/java/` – Unit tests (create this directory as needed)

### Key Packages

- `dansplugins.netheraccesscontroller` – Main plugin class
- `dansplugins.netheraccesscontroller.commands` – Command implementations
- `dansplugins.netheraccesscontroller.data` – Data models
- `dansplugins.netheraccesscontroller.listeners` – Event listeners
- `dansplugins.netheraccesscontroller.services` – Business logic services
- `dansplugins.netheraccesscontroller.utils` – Utility classes

## Coding Conventions

- Messages are currently hardcoded in Java; denial messages are configurable via `config.yml`.
- Follow the existing package structure when adding new classes.
- Annotate every command executor and event listener with `@Override` where applicable.

## Contribution Workflow

- Branch from `develop` for all changes.
- Open a pull request against `develop`, not `main`.
- Reference the related GitHub issue in every pull request description.
