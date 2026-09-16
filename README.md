# Nether Access Controller

## Description

Nether Access Controller is a Minecraft plugin that allows operators to control access to the nether. It is essentially a whitelist for nether access. Operators can allow or deny access and view who has access. Both nether portal creation and usage can be prevented through the use of this tool.

## Installation

### First Time Installation

1. Download the plugin from [SpigotMC](https://www.spigotmc.org/resources/nether-access-controller.95905/).
2. Place the jar in the `plugins` folder of your server.
3. Restart your server.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [Commands Reference](COMMANDS.md) – Complete list of all commands
- [Configuration Guide](CONFIG.md) – Detailed configuration options

### Wiki & Additional Resources

- [Wiki Guide](https://github.com/Dans-Plugins/Nether-Access-Controller/wiki/Guide)
- [FAQ](https://github.com/Dans-Plugins/Nether-Access-Controller/wiki/FAQ)

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Dans-Plugins/Nether-Access-Controller/issues/new).

- [Known Bugs](https://github.com/Dans-Plugins/Nether-Access-Controller/issues?q=is%3Aopen+is%3Aissue+label%3Abug)

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)
- [Notes for Developers](https://github.com/Dans-Plugins/Nether-Access-Controller/wiki/Developer-Notes)

## Testing

### Build & Test

Linux / macOS:

    mvn clean package

Windows:

    mvn clean package

If you see `BUILD SUCCESS`, the build has passed.

## Development

### Local Testing

1. Build the plugin: `mvn clean package`
2. Copy the JAR from `target/` into your local Spigot server's `plugins` folder.
3. Restart the server.

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|--------------------|
| Daniel Stephenson | Creator |

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).

You are free to use, modify, and distribute this software, provided that:

- Source code is made available under the same license when distributed.
- Changes are documented and attributed.
- No additional restrictions are applied.

See the [LICENSE](LICENSE) file for the full text of the GPL-3.0 license.

## Project Status

This project is in active development.

### bStats

You can view the bStats page for the plugin [here](https://bstats.org/plugin/bukkit/Nether%20Access%20Controller/12673).

## Usage reporting

Usage reporting is on by default: NetherAccessController sends its name, version and command names (a `startup` event when it enables and a `command` event each time one of its commands is used) to <https://trace.danielstephenson.dev> so it is known which plugins are actually in use. Nothing about players, worlds, IPs or the server is sent, and nothing typed after a command is sent either.

To turn it off:

- for this plugin: `usage-reporting.enabled: false` in `plugins/NetherAccessController/config.yml`
- for every plugin on the server that reports this way: `enabled: false` in `plugins/trace/config.yml` (created the first time such a plugin enables)
- for the whole server process: the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`

Each startup logs whether reporting is on or, if it is off, why. Details: <https://github.com/Stephenson-Software/trace#usage-reporting>

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a release-by-release summary of changes.
