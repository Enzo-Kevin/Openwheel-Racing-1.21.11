# Openwheel Racing

Openwheel Racing is a Minecraft Forge mod for Minecraft 1.21.11 that adds an open-wheel racing progression loop: craft and assemble a prototype race car, tune its setup, build circuits, refine crude oil resources, and race laps with timing, tyre wear, and crash damage.

## Requirements

- Java 21
- Minecraft 1.21.11
- Minecraft Forge 61.1.0

The Gradle wrapper is included, so a separate Gradle installation is not required.

## Getting started

Build the mod jar:

```bash
./gradlew build
```

Run a local Minecraft client:

```bash
./gradlew runClient
```

Run a dedicated server:

```bash
./gradlew runServer
```

Run data generation:

```bash
./gradlew runData
```

The built jar is generated under `build/libs/`.

## Features

- Prototype open-wheel car entity with acceleration, gearing, steering, tyre wear, damage, and surface-dependent grip.
- Car assembly workstation for crafting, setup tuning, and repairs.
- Racing blocks including track, kerbs, checkpoints, barriers, and start/finish markers.
- Lap timing, checkpoint validation, and off-track invalidation logic.
- Crude oil deposits, refinery processing, and progression resources.
- Custom HUD, workstation screens, renderer, recipes, loot tables, and localization.

## Project layout

```text
src/main/java/com/openwheelracing/   Java source code
src/main/resources/                  Mod assets, recipes, loot tables, and metadata
src/generated/resources/             Generated data output
docs/mechanism-specs/                Gameplay mechanism design notes
docs/asset-checklist.md              Asset and model checklist
run/                                 Local Minecraft runtime directory
```

## Development notes

- `./gradlew build` is the main validation command.
- `runClient` requires a working GUI display. In headless environments it may fail before mod loading with a GLFW display error.
- When adding localized names, tooltips, containers, or key bindings, update both `en_us.json` and `zh_cn.json`.
- Directional block textures should be authored in their north-facing orientation; blockstates rotate other directions.

## License

See the license files in this repository for details.
