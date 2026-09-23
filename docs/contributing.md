# Contributing

Contributions to SpinoChart are welcome! Whether reporting a bug, proposing a feature, or submitting a pull request, here is how to get started.

---

## Prerequisites

* **JDK 21** or higher
* **Git**

---

## Project Structure

The project is structured as a multi-module Gradle build:

```text
spinochart/
├── modules/
│   ├── spinochart-core/           # Parsing engine, data models, SVG charts, themes
│   ├── spinochart-cli/            # Command-line interface and standalone binary
│   └── spinochart-gradle-plugin/  # Gradle plugin implementation and tasks
├── samples/
│   └── sample-simulation/         # Sample Gatling simulation for manual testing
└── buildSrc/                      # Shared conventions and publishing plugins
```

---

## Common Commands

### Build Everything

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Code Formatting

Code style is enforced via [Spotless](https://github.com/diffplug/spotless) with Google Java/Kotlin formatting standards.

Check formatting:

```bash
./gradlew spotlessCheck
```

Automatically apply formatting fixes:

```bash
./gradlew spotlessApply
```

### Build CLI Binary Locally

Assemble the self-executing CLI binary:

```bash
./gradlew :modules:spinochart-cli:installCli
```

The resulting binary will be located at:
```text
modules/spinochart-cli/build/bin/spinochart
```

---

## Submitting Pull Requests

1. Fork the repository on GitHub.
2. Create a topic branch: `git checkout -b feature/my-feature`.
3. Make your changes and add tests where appropriate.
4. Verify tests and style: `./gradlew check`.
5. Commit and push your branch: `git push origin feature/my-feature`.
6. Open a Pull Request against `main`.
