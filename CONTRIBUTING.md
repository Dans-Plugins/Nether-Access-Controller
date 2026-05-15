# Contributing

## Thank You

Thank you for your interest in contributing to Nether Access Controller! This guide will help you get started.

## Links

- [Website](https://dansplugins.com)
- [Discord](https://discord.gg/xXtuAQ2)

## Requirements

- A GitHub account
- Git installed on your local machine
- A Java IDE or text editor
- A basic understanding of Java

## Getting Started

1. [Sign up for GitHub](https://github.com/signup) if you don't have an account.
2. Fork the repository by clicking **Fork** at the top right of the repo page.
3. Clone your fork: `git clone https://github.com/<your-username>/Nether-Access-Controller.git`
4. Open the project in your IDE.
5. Build the plugin: `mvn clean package`
   If you encounter errors, please open an issue.

## Identifying What to Work On

### Issues

Work items are tracked as [GitHub issues](https://github.com/Dans-Plugins/Nether-Access-Controller/issues).

### Milestones

Issues are grouped into [milestones](https://github.com/Dans-Plugins/Nether-Access-Controller/milestones) representing upcoming releases.

## Making Changes

1. Make sure an issue exists for the work. If not, create one.
2. Switch to `main`: `git checkout main`
3. Create a branch: `git checkout -b <branch-name>`
4. Make your changes.
5. Test your changes.
6. Commit: `git commit -m "Description of changes"`
7. Push: `git push origin <branch-name>`
8. Open a pull request against `main`, link the related issue with `#<number>`.
9. Address review feedback.

## Testing

Run the build with:

Linux / macOS:

    mvn clean package

Windows:

    mvn clean package

For manual testing, place the built JAR from `target/` into a local Spigot server's `plugins` folder and restart the server.

## Questions

Ask in the [Discord server](https://discord.gg/xXtuAQ2).
