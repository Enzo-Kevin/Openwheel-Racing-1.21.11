# Openwheel Racing

Openwheel Racing is a Minecraft Forge mod for Minecraft 1.21.11 that adds an open-wheel racing progression loop: craft and assemble a prototype race car, tune its setup, build circuits, manage race markers, refine crude oil resources, and race laps with timing, tyre wear, crash damage, and surface-dependent grip.

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

Run the Forge game test server:

```bash
./gradlew runGameTestServer
```

Run data generation:

```bash
./gradlew runData
```

The built jar is generated under `build/libs/`.

## Features

- Prototype open-wheel car entity with acceleration, gearing, steering, tyre wear, damage, barrier impacts, and persistence back to the car item.
- Car assembly workstation for custom car assembly, setup tuning, and repairs.
- Race director block plus racing surfaces including asphalt, pit lane, pit stop marks, kerbs, checkpoints, barriers, and start/finish markers.
- Lap timing, checkpoint validation, off-track invalidation, pit lane support, and in-car HUD feedback.
- Crude oil deposits, refinery processing, fuel items, and crafting progression resources.
- Custom renderer, workstation screens, recipes, loot tables, world generation, sounds, and English/Chinese localization.

## Project layout

```text
src/main/java/com/openwheelracing/   Java source code
src/main/resources/                  Mod assets, recipes, loot tables, worldgen, and metadata
src/generated/resources/             Generated data output
docs/mechanism-specs/                Gameplay mechanism design notes
docs/asset-checklist.md              Asset and model checklist
run/                                 Local Minecraft runtime directory
```

## Development notes

- `./gradlew build` is the main validation command.
- `runClient` requires a working GUI display. In headless environments it may fail before mod loading with a GLFW display error.
- Forge event-bus strict runtime checks are enabled, so register listeners on the correct bus.
- When adding localized names, tooltips, containers, or key bindings, update both `en_us.json` and `zh_cn.json`.
- Directional block textures should be authored in their north-facing orientation; blockstates rotate other directions.
- Check `docs/mechanism-specs/` before changing gameplay mechanics, balance, or progression.

## Version

Current project version: `1.3.0`.

## License

See the license files in this repository for details.

Note that the car modeling was derived from `"NEW F1 CAR 2026" (2026) by Abu Saif is licensed under Creative Commons Attribution (http://creativecommons.org/licenses/by/4.0/)`. Our works include rotating it and remeshing to 10% the original density