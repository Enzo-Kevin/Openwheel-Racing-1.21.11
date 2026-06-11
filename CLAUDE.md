# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Openwheel Racing is a Minecraft Forge mod for Minecraft `1.21.11` using Forge `61.1.0`, official mappings, and Java 21. The Gradle project name is `openwheel-racing`; the mod id is `openwheelracing`.

The mod implements a prototype open-wheel racing loop: car assembly, a driveable prototype car entity, racing track blocks, lap timing, setup tuning, crash damage/tyre wear, crude oil/refining resources, and crafting progression.

## Common commands

```bash
./gradlew build            # Compile Java, process resources, assemble jar
./gradlew runClient        # Launch the Forge client in run/
./gradlew runServer        # Launch a dedicated server with --nogui
./gradlew runGameTestServer # Launch Forge game test server
./gradlew runData          # Generate data into src/generated/resources
```

There is no separate lint task or test suite currently configured beyond Gradle's default lifecycle. `./gradlew build` is the main validation command.

`runClient` requires a working GUI display. In headless/remote sessions it can fail before mod loading with `glfwGetPrimaryMonitor failed`; that is an environment/display issue, not necessarily a mod crash.

## Architecture

### Registration entry point

`src/main/java/com/openwheelracing/OpenwheelRacing.java` is the Forge mod entry point. It registers all DeferredRegister-backed systems on the mod bus:

- `OWRDataComponents`
- `OWREntities`
- `OWRItems`
- `OWRBlocks`
- `OWRBlockEntities`
- `OWRMenus`
- `OWRRecipes`
- `OWRCreativeTabs`

It also registers refinery fuel handling on `FurnaceFuelBurnTimeEvent.BUS` and initializes the custom network channel in common setup.

### Registries

The `registry/` package owns object registration and should be updated whenever adding content:

- `OWRBlocks` registers blocks and their block items.
- `OWRItems` registers standalone items and the prototype car item.
- `OWREntities` registers the prototype car entity.
- `OWRBlockEntities` registers workstation/refinery block entities.
- `OWRMenus` registers container menu types.
- `OWRRecipes` registers the custom `car_assembly` recipe type/serializer.
- `OWRDataComponents` stores item-side car setup/damage/tyre wear data.
- `OWRCreativeTabs` controls creative tab visibility/order.

### Client-side systems

Client-only code is under `client/`:

- `OpenwheelRacingClient` registers menu screens, entity renderer, HUD overlay, and client tick input hooks.
- `OpenwheelRacingClientEvents` handles key mapping registration.
- `CarHudOverlay` renders the in-car HUD.
- `CarAssemblyScreen` and `RefineryScreen` render the workstation/refinery UIs.
- `OWRClientInputHandler` sends shift/exit actions from key state.
- `OpenwheelCarRenderer` renders the prototype car entity.

Forge event-bus strict runtime checks are enabled in `build.gradle`; be careful that event listeners are registered on the correct bus. Some Forge client events in this project are registered through their static `BUS` fields rather than auto-subscribed methods.

### Gameplay content

`content/entity/OpenwheelCarEntity.java` contains most vehicle simulation and race logic: acceleration/gearing, steering, surface grip/drag, tyre wear, damage, barrier impact reduction, lap timing, off-track invalidation, checkpoint/start-finish crossing, and persistence.

`content/car/PrototypeCarSetup.java` defines persistent setup values and their multipliers. Current setup ranges are:

- Power mode: `0..3`
- Tyre compound index: `0..4` displayed as C1-C5
- Aero preset: `0..4`
- Gearing preset: `0..2`

`content/item/PrototypeCarItem.java` spawns the entity and persists setup/damage/tyre wear on the item through data components.

### Blocks and workstations

- `CarAssemblyWorkstationBlock` + `CarAssemblyWorkstationBlockEntity` + `CarAssemblyMenu` implement car assembly and setup/repair interaction.
- `RefineryBlock` + `RefineryBlockEntity` + `RefineryMenu` implement furnace-like oil refining with crude input, fuel input, and five output slots.
- `DirectionalTrackBlock` provides horizontal facing for directional track blocks.
- `LapMarkerBlock` extends the directional track block and calls car lap/checkpoint handlers using the marker's facing direction.
- `CrudeOilBlock` handles crude oil deposit behavior.

Directional blocks (`kerb`, `checkpoint`, `start_finish`) have `facing=north/east/south/west` blockstate variants. Their source textures should be drawn north-facing; blockstates rotate the model for other directions.

### Networking

`network/OWRNetwork.java` defines the Forge `SimpleChannel` for client-to-server workstation actions. Current messages are setup tuning and car repair from `CarAssemblyScreen`.

### Resources and data

Resources live in `src/main/resources`:

- `assets/openwheelracing/blockstates/` maps block states to models.
- `assets/openwheelracing/models/block/` and `models/item/` define rendering models.
- `assets/openwheelracing/textures/` contains block/item/gui textures.
- `assets/openwheelracing/lang/en_us.json` and `zh_cn.json` should stay key-aligned.
- `data/openwheelracing/recipe/` contains vanilla and custom recipes.
- `data/openwheelracing/loot_table/blocks/` contains block drops.
- `data/openwheelracing/worldgen/`, `forge/biome_modifier/`, and biome tags define crude oil generation.

`docs/mechanism-specs/` contains the design specs for gameplay mechanisms. Check these before changing mechanics, balance, or progression. `docs/asset-checklist.md` documents expected asset files, GUI coordinates, and model texture bindings.

## Localization

When adding a translatable name/tooltip/container/key, update both:

```text
src/main/resources/assets/openwheelracing/lang/en_us.json
src/main/resources/assets/openwheelracing/lang/zh_cn.json
```

Keep the key sets identical. A quick parity check:

```bash
python3 - <<'PY'
import json
from pathlib import Path
base = Path('src/main/resources/assets/openwheelracing/lang')
en = json.loads((base / 'en_us.json').read_text())
zh = json.loads((base / 'zh_cn.json').read_text())
print('missing zh', sorted(set(en) - set(zh)))
print('extra zh', sorted(set(zh) - set(en)))
PY
```

## Asset/model notes

Block item models generally inherit their block models so block items render as 3D block views. If a block appears with the wrong texture in-game, first check its `models/block/*.json` texture bindings, then its blockstate variant rotations.

For directional top textures such as checkpoint/start-finish arrows or kerb stripe orientation, draw the texture in the north-facing default orientation and rely on blockstate `y` rotations.

## Generated and runtime files

Gradle run outputs and Minecraft runtime logs are under `run/`; these are noisy and commonly modified by launching the client/server. Generated data output goes to `src/generated/resources` when using `./gradlew runData`.
