# Command-Line Interface (CLI)

The SpinoChart CLI is a standalone, self-executable binary that generates performance dashboards from Gatling simulation logs without requiring Gradle or Maven.

---

## Installation

### Direct Binary Download

Download the self-executing binary directly from GitHub Releases:

```bash
curl -LO https://github.com/development-and-dinosaurs/spinochart/releases/latest/download/spinochart
chmod +x spinochart
```

The binary is a self-contained executable packaging the CLI and all dependencies. It requires only **Java 17 or higher** on your `PATH`.

### Adding to PATH (Optional)

Move the binary to any directory in your system `$PATH` for system-wide access:

```bash
sudo mv spinochart /usr/local/bin/
# or for local user:
mkdir -p ~/.local/bin && mv spinochart ~/.local/bin/
```

Verify the installation:

```bash
spinochart --help
```

---

## Usage

### Zero-Configuration Scan

When invoked without arguments, SpinoChart automatically inspects standard Gatling output directories:

```bash
spinochart
```

Directories searched by default:
* `results/` (Gatling standalone bundle / CLI)
* `build/reports/gatling/` (Gradle Gatling plugin)
* `target/gatling/` (Maven Gatling plugin)

For every `simulation.log` found, SpinoChart writes an `index.html` report in that simulation's directory.

### Specifying a Custom Path

You can point SpinoChart to an explicit directory or file:

```bash
# Scan a specific directory tree:
spinochart path/to/results/

# Generate a report for a specific simulation.log:
spinochart path/to/simulation.log

# Specify custom output path:
spinochart path/to/simulation.log custom-report.html
```

---

## Options & Flags

```text
Usage: spinochart [OPTIONS] [PATH] [OUTPUT]

Arguments:
  [PATH]                     Directory or simulation.log file to process (default: scans standard Gatling paths)
  [OUTPUT]                   Custom output HTML file (used only when PATH points to a single log file)

Options:
  -rf, --results-folder <dir> Gatling results folder to scan (matches Gatling CLI flag)
  -t, --theme <name>          Report theme to use: "spino" (default) or "classic"
  -o, --output-name <file>    Report file name (default: index.html)
  -v, --verbose               Enable verbose log output
  -h, --help                  Show help message and exit
```

---

## CI / CD Integration

Because the CLI is a single file with zero external runtime dependencies, it integrates cleanly into CI pipelines.

### GitHub Actions Example

```yaml
name: Performance Tests

on: [push, pull_request]

jobs:
  gatling:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run Gatling Simulations
        run: ./gradlew gatlingRun

      - name: Download SpinoChart CLI
        run: |
          curl -LO https://github.com/development-and-dinosaurs/spinochart/releases/latest/download/spinochart
          chmod +x spinochart

      - name: Generate Reports
        run: ./spinochart build/reports/gatling/

      - name: Upload HTML Report
        uses: actions/upload-artifact@v4
        with:
          name: gatling-spinochart-report
          path: build/reports/gatling/**/index.html
```
