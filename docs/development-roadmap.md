# Openwheel Racing Development Roadmap

**Version**: 1.0  
**Date**: 2026-06-10  
**Minecraft Target**: Forge 1.21.11  

---

## Product Direction

Openwheel Racing should grow from a crafting-first prototype into a Minecraft-native motorsport mod: players discover and process materials, craft car parts, assemble and tune open-wheel cars, build circuits, run timed laps, and eventually race with friends.

The development strategy should prioritize playable vertical slices over large unfinished systems. Each phase should add one clear loop that can be tested in-game.

---

## Current Baseline

Already implemented:

- Mod rebrand to **Openwheel Racing**.
- English prototype PRD.
- Car assembly workstation.
- Hybrid crafting chain:
  - carbon fiber
  - chassis
  - engine
  - tires
  - aero kit
  - gearbox
  - steering controls
- Prototype car spawn item.
- Item-stored car setup data.
- Tooltip display for setup properties.
- Single-seat prototype car entity.
- Basic WASD driving behavior.
- Gear/RPM/speed/tyre/damage state.
- Shift up/down keybinds.
- Exit car keybind.
- Text HUD via debug overlay.
- Basic damage and tyre wear effects.

Known limitations:

- Car currently uses a placeholder/no-op renderer.
- Controls are prototype-level and need stronger server-authoritative networking for multiplayer.
- No proper tuning UI yet.
- No track blocks, race timing, checkpoints, or pit lane behavior yet.
- No crude oil worldgen or refining chain yet.

---

## Phase 1: Drivable Prototype Hardening

**Goal**: Make the current prototype car visible, testable, and reliable enough for repeated single-player playtests.

### Features

- Add a visible placeholder open-wheel car renderer.
- Improve mounting/dismounting behavior.
- Add basic car collision feedback.
- Stabilize WASD driving feel.
- Tune acceleration, braking, steering, gear ratios, RPM, and speed limits.
- Move HUD out of debug text into a normal gameplay HUD overlay.
- Add basic sounds if useful:
  - engine idle
  - acceleration
  - shift
  - collision

### Success Criteria

- Player can spawn, see, mount, drive, shift, crash, and exit the car reliably.
- Car movement is fun enough for a test lap on flat terrain.
- Damage and tyre wear visibly affect performance.

---

## Phase 2: Car Setup and Tuning Loop

**Goal**: Turn setup data into a real gameplay system rather than static tooltip values.

### Features

- Add tuning UI or workstation upgrade flow.
- Allow editing setup values:
  - power
  - grip
  - aero
  - gearing
- Store setup on the car item.
- Copy setup from item to spawned entity.
- Optionally write runtime condition back to item when the car is recovered later.
- Add clearer tooltip formatting:
  - setup values
  - expected behavior tradeoffs
  - condition/wear status if applicable

### Success Criteria

- Different setup values noticeably affect lap behavior.
- Player can understand setup tradeoffs from the tooltip/UI.
- Setup survives item storage and entity spawning.

---

## Phase 3: Track Building Blocks

**Goal**: Let players build a dedicated racing circuit with mod blocks instead of relying on generic terrain.

### Features

- Add racing surface blocks:
  - asphalt track block
  - kerb block
  - barrier block
  - pit lane block
- Add marker blocks:
  - start/finish block
  - checkpoint block
  - pit box/pit service block
- Add block recipes.
- Start with simple block visuals and collision.

### Success Criteria

- Player can build a recognizable circuit with mod blocks.
- Car drives better or more consistently on track surfaces than rough terrain.
- Track blocks support later timing/checkpoint logic.

---

## Phase 4: Timed Lap System

**Goal**: Deliver the first real racing gameplay loop: build track, drive valid lap, receive lap time.

### Features

- Start/finish block starts and completes lap timing.
- Checkpoint blocks validate route order.
- Text lap timer HUD.
- Best lap tracking for the current session/world.
- Invalid lap handling when checkpoints are skipped.
- Optional reset behavior for failed laps.

### Success Criteria

- Player can complete a valid timed single lap.
- Skipping checkpoints invalidates the lap.
- Lap timing is clear and reliable.

---

## Phase 5: Pit Lane and Wear Gameplay

**Goal**: Make damage/wear management part of the racing loop.

### Features

- Pit lane zone blocks.
- Pit stop interaction.
- Repair car damage.
- Restore or change tyres.
- Allow setup changes only in pit lane or workstation.
- Optional lap invalidation or timing penalty for pit use.

### Success Criteria

- Damage and tyre wear create a reason to use the pit lane.
- Pit stops repair or alter the car in a predictable way.
- Pit systems do not interrupt the basic timed-lap loop.

---

## Phase 6: Crude Oil Resource and Refining Chain

**Goal**: Add a Minecraft-native industrial supply chain for racing materials and track construction.

### Concept

Crude oil should appear as a natural underground liquid below **Y=20**, with rarity roughly comparable to iron availability. It should support a refining/separation chain that produces racing-relevant materials:

- rubber
- gasoline
- diesel
- asphalt

This system should connect racing gameplay to exploration, mining, fluid handling, and industrial crafting.

### World Generation

- Add crude oil as a natural fluid or oil deposit feature.
- Generate below Y=20.
- Rarity target: close to iron-level availability, but likely more clustered so players discover deposits rather than isolated single blocks.
- Prefer underground pockets/reservoirs over surface pools.
- Avoid excessive worldgen performance cost.

### Processing Chain

Possible first implementation:

1. **Crude Oil Fluid**
   - collected with bucket or pump later.
2. **Crude Oil Separator / Refinery Block**
   - input: crude oil
   - outputs: rubber precursor, gasoline, diesel, asphalt binder
3. **Rubber**
   - used for tyres and seals.
4. **Gasoline**
   - future high-performance fuel.
5. **Diesel**
   - future heavy machinery/refinery fuel.
6. **Asphalt**
   - used for track blocks.

### Integration With Racing Systems

- Rubber should replace or supplement leather/wool in tyre recipes.
- Asphalt should become the main track block material.
- Gasoline may become optional racing fuel in a later phase.
- Diesel may power refinery machines or future logistics vehicles.

### Success Criteria

- Player can find crude oil underground.
- Player can process crude oil into at least rubber and asphalt.
- Rubber and asphalt become useful in car/track crafting.
- Gasoline/diesel can exist as items or fluids before full fuel gameplay is added.

---

## Phase 7: Survival Progression Pass

**Goal**: Make the mod feel coherent in survival mode.

### Features

- Rebalance recipes around oil products and racing materials.
- Add progression gates:
  - basic car parts
  - advanced carbon fiber parts
  - rubber tyres
  - asphalt track construction
- Add advancements:
  - find crude oil
  - refine rubber
  - assemble first car
  - build first circuit
  - complete first timed lap
- Improve item names/tooltips.

### Success Criteria

- A survival player can understand how to progress from resource discovery to first timed lap.
- Recipes feel Minecraft-native rather than creative-only placeholders.

---

## Phase 8: Small Private Multiplayer

**Goal**: Support the planned private-server expansion for 2–10 players.

### Features

- Server-authoritative vehicle controls and gear shifting.
- Race session controller block.
- Multiplayer lap timing.
- Starting grid or countdown system.
- Basic results screen/chat output.
- Safer collision handling and reset commands.

### Success Criteria

- 2–10 players can race on a private server without major desync.
- Race timing and checkpoint validation work server-side.
- Players can start, finish, and compare race results.

---

## Phase 9: Polish and Public Release Preparation

**Goal**: Prepare the mod for wider testing or public distribution.

### Features

- Better car model and animations.
- Improved track textures and block variants.
- Config options for worldgen rarity and driving difficulty.
- JEI/REI compatibility consideration.
- Datagen for recipes, loot tables, tags, and models.
- Crash/bug hardening.
- Documentation for installation, controls, and progression.

### Success Criteria

- Mod is understandable to new players.
- Common crashes and confusing behaviors are resolved.
- Public build has coherent visuals, recipes, controls, and config.

---

## Near-Term Recommended Order

1. Add visible car renderer.
2. Move HUD out of debug overlay into normal gameplay HUD.
3. Improve driving feel and gear behavior.
4. Add track/asphalt blocks.
5. Add start/finish and checkpoint lap timing.
6. Add crude oil worldgen and basic refining.
7. Replace placeholder tyre/track recipes with rubber/asphalt-based recipes.

---

## Design Principles

- Build playable vertical slices.
- Prefer Minecraft-native blocks/items before complex simulation systems.
- Keep systems data-driven where useful, but avoid premature frameworks.
- Make every new resource feed a racing purpose.
- Keep single-player stable before expanding multiplayer.
- Make setup and wear understandable through tooltips/HUD before adding deeper simulation.
