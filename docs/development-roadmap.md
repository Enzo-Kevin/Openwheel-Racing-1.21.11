# Openwheel Racing Development Roadmap

**Version**: 1.1  
**Date**: 2026-06-17  
**Minecraft Target**: Forge 1.21.11  

---

## Product Direction

Openwheel Racing should grow from a crafting-first prototype into a Minecraft-native motorsport mod: players discover and process materials, craft car parts, assemble and tune open-wheel cars, build circuits, run timed laps, and eventually race with friends.

The development strategy should prioritize playable vertical slices over large unfinished systems. Each phase should add one clear loop that can be tested in-game.

---

## Current Baseline

Already implemented:

- Mod rebrand to **Openwheel Racing**.
- English prototype PRD and mechanism specs.
- Repository guidance in `CLAUDE.md`.
- Asset checklist in `docs/asset-checklist.md`.
- English and Simplified Chinese localization files with matching key sets.
- Car assembly workstation with recipe-backed assembly.
- Free setup tuning and basic repair controls in the car assembly UI.
- Furnace-like refinery block with crude input, furnace-fuel input, and five output slots.
- Hybrid crafting/progression chain:
  - crude oil chunks/bucket
  - gas, petrol, diesel
  - crude rubber and rubber
  - asphalt binder
  - carbon fiber
  - racing electronics
  - chassis
  - engine
  - tyres
  - aero kit
  - gearbox
  - steering controls
- Crude oil deposit worldgen below Y=20, with oil-rich biome bonus generation.
- Prototype car spawn item.
- Item-stored car setup, damage, and tyre wear data.
- Tooltip display for setup and condition properties.
- Single-seat prototype car entity.
- Visible placeholder open-wheel car renderer.
- WASD driving behavior with gear/RPM/speed/tyre/damage state.
- Shift up/down and exit-car keybinds.
- Normal gameplay HUD overlay for speed, gear, RPM, tyres, damage, lap, best lap, and setup.
- Track-building blocks:
  - asphalt track
  - kerb
  - barrier
  - pit lane
  - directional start/finish
  - directional checkpoint
- Directional kerb/checkpoint/start-finish blockstates and 3D block item models.
- Surface effects for track grip/drag, kerb grip/wear penalty, pit lane speed limit, and barrier impact absorption.
- Basic lap timing with directional start/finish and checkpoint validation.
- Race Director block, UI, race-control rules, lap review, manual invalidation, and persistent per-player best-lap records.
- Track editor UI with preset/import-assisted circuit construction.
- Pit lane speed limiting plus sneak/right-click pit service for damage and tyre restoration.
- Lap invalidation for reverse marker passes, leaving track entirely, driver exit, or driver death.
- Basic damage and tyre wear effects, warnings, crash feedback, and destruction material drops.
- Block/item models and temporary textures sufficient to avoid missing-texture rendering.

Known limitations:

- Car renderer is still a placeholder code-rendered model, not a polished textured model.
- Controls remain prototype-level and are not yet server-authoritative enough for multiplayer racing.
- Lap timing still lacks explicit ordered checkpoint IDs/sectors and richer leaderboard presentation, though race-director persistence and recent-lap review now exist.
- Pit lane service exists for damage and tyre restoration, but there is no fuel, timed pit-stop, pit-box, or tyre-compound-change loop yet.
- Refinery outputs are random item outputs; no fluid system or detailed ratios yet.
- Crude oil is currently ore/deposit-like, not a real fluid pocket.
- Survival progression has recipes, but lacks advancements/tutorial guidance.
- Many item textures still use placeholders or need final art.

---

## Phase 1: Drivable Prototype Hardening — Mostly Complete

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

## Phase 2: Car Setup and Tuning Loop — MVP Complete

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

## Phase 3: Track Building Blocks — MVP Complete

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

## Phase 4: Timed Lap System — Prototype Complete

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

## Phase 5: Pit Lane and Wear Gameplay — MVP Started

**Goal**: Make damage/wear management part of the racing loop.

### Features

- [x] Pit lane zone blocks.
- [x] Pit stop interaction.
- [x] Repair car damage.
- [x] Restore tyre condition.
- [ ] Change tyre compound in a pit-service flow.
- [ ] Allow setup changes only in pit lane or workstation.
- [ ] Optional lap invalidation or timing penalty for pit use.

### Success Criteria

- Damage and tyre wear create a reason to use the pit lane.
- Pit stops repair or alter the car in a predictable way.
- Pit systems do not interrupt the basic timed-lap loop.

---

## Phase 6: Crude Oil Resource and Refining Chain — MVP Complete

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

## Phase 7: Survival Progression Pass — In Progress

**Goal**: Make the mod feel coherent in survival mode.

### Features

- Rebalance recipes around oil products and racing materials.
- Add progression gates:
  - basic car parts
  - advanced carbon fiber parts
  - rubber tyres
  - asphalt track construction
- Add advancements:
  - [x] find crude oil
  - [x] refine rubber
  - [x] assemble first car
  - [x] build first circuit
  - [x] complete first timed lap
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

1. Expand pit-stop gameplay beyond instant service:
   - tyre-compound changes
   - optional timed service or pit-box requirement
   - refuel once fuel consumption exists
2. Improve lap timing with ordered checkpoint routes, sectors, and richer leaderboard presentation.
3. Replace remaining placeholder item/block art and add a textured car model.
4. Add a clear car recovery/pick-up flow that writes entity condition back to the item.
5. Harden controls/networking for future small private multiplayer.
6. Add config/datagen support once gameplay data stabilizes.

---

## Design Principles

- Build playable vertical slices.
- Prefer Minecraft-native blocks/items before complex simulation systems.
- Keep systems data-driven where useful, but avoid premature frameworks.
- Make every new resource feed a racing purpose.
- Keep single-player stable before expanding multiplayer.
- Make setup and wear understandable through tooltips/HUD before adding deeper simulation.
