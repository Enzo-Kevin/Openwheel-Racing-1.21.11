# Product Requirements Document: Open-Wheel Racing Prototype

**Version**: 1.0  
**Date**: 2026-06-10  
**Author**: Sarah (Product Owner)  
**Quality Score**: 93/100

---

## Executive Summary

This prototype defines the first playable version of an open-wheel car racing mod for Minecraft Forge 1.21.11. The goal is to prove that players can build racing infrastructure, assemble and tune an open-wheel car through custom crafting systems, and complete a fun timed lap on a custom-built circuit.

The MVP is intentionally single-player-first. It should validate the core fantasy of Minecraft-scale formula-style racing: crafting mechanical components, constructing track blocks, tuning the car, entering a circuit, and driving a satisfying lap. Small private-server multiplayer is a later expansion, not a requirement for the prototype.

The prototype prioritizes systems and gameplay clarity over visual polish. Assets should use a Minecraft-native style, with simple but recognizable open-wheel cars, track blocks, pit lane blocks, start/finish blocks, and checkpoint blocks.

---

## Problem Statement

**Current Situation**: Minecraft does not provide a native system for high-speed, tunable racing vehicles, purpose-built motorsport tracks, lap timing, or pit-lane gameplay. Existing movement systems are not designed around open-wheel racing, car setup, or player-built circuits.

**Proposed Solution**: Build a Forge 1.21.11 prototype mod where a player can craft racing components, assemble an open-wheel car using a custom functional block, build a custom track using dedicated track blocks, tune the car, and complete a timed single-lap run with checkpoints and pit-lane interaction.

**Business Impact**: The prototype should establish whether the mod concept is fun and technically feasible before expanding into multiplayer racing, progression, advanced damage, AI opponents, or public release polish.

---

## Success Metrics

**Primary KPIs:**
- **Self-serve build and lap**: A player can assemble a car, place/build a track, and complete a timed lap without developer assistance.
- **Playable circuit**: Custom track blocks and marker blocks support at least one complete circuit layout with a start/finish and checkpoint sequence.
- **Tuning feels meaningful**: Engine, tires, aerodynamics, and gearing changes create noticeable handling or lap-time differences during playtesting.
- **Fun timed lap**: The prototype succeeds if a player can complete a satisfying single-lap time trial loop.
- **Playtest stability**: The prototype can be played repeatedly in a local development environment without major crashes or progression-blocking bugs.

**Validation**: Validate through repeated local playtests using at least one hand-built prototype circuit. Record whether a player can complete the full loop: craft/assemble car → build/enter track → tune setup → complete timed lap → optionally use pit lane for repair or setup changes.

---

## User Personas

### Primary: Solo Mod Player / Builder-Racer
- **Role**: Minecraft player who enjoys building, vehicles, and racing systems.
- **Goals**: Build a track, assemble a racing car, tune it, and drive faster laps.
- **Pain Points**: Vanilla Minecraft lacks high-speed controllable cars, motorsport track systems, lap timing, and meaningful car setup.
- **Technical Level**: Intermediate Minecraft player; comfortable with crafting and block-based systems.

### Secondary: Future Private Server Host
- **Role**: Player who may later host a small private racing server for friends.
- **Goals**: Build shared circuits and run friendly races for 2–10 players.
- **Pain Points**: Needs stable car behavior and track rules before multiplayer is worth adding.
- **Technical Level**: Intermediate to advanced; comfortable installing mods and running a private server.

---

## User Stories & Acceptance Criteria

### Story 1: Assemble an Open-Wheel Car

**As a** solo mod player  
**I want to** craft components and assemble an open-wheel car at a custom functional block  
**So that** the racing car feels like a built machine rather than a creative-only spawn item.

**Acceptance Criteria:**
- [ ] The player can access a custom car assembly block or workstation.
- [ ] The workstation supports creating one prototype open-wheel car from defined components.
- [ ] The assembled car can be placed or spawned into the world as a drivable entity.
- [ ] Creative-only spawning is allowed for testing, but the prototype loop must support the custom workstation path.

### Story 2: Build a Custom Racing Track

**As a** builder-racer  
**I want to** place custom track blocks and race marker blocks  
**So that** I can create a playable circuit inside Minecraft.

**Acceptance Criteria:**
- [ ] The mod includes custom track blocks suitable for a simple racing surface.
- [ ] The player can place a start/finish block.
- [ ] The player can place checkpoint blocks that define a valid lap path.
- [ ] At least one complete test circuit can be built using the provided blocks.

### Story 3: Drive a Simulation-Inspired Open-Wheel Car

**As a** racer  
**I want to** drive a car with simulation-inspired handling  
**So that** speed, grip, braking, and setup choices matter.

**Acceptance Criteria:**
- [ ] The car supports acceleration, braking, steering, and stopping.
- [ ] Handling should feel more simulation-like than a simple minecart, with traction, speed, and braking tradeoffs.
- [ ] Crashes or collisions may slow or stop the car, but advanced crash physics are not required.
- [ ] The car remains controllable enough for a player to complete a timed lap.

### Story 4: Tune Car Setup

**As a** racer  
**I want to** tune engine power, tires, aerodynamics, and gearing  
**So that** setup choices affect how the car drives and performs.

**Acceptance Criteria:**
- [ ] Engine/power tuning affects acceleration and/or top speed.
- [ ] Tire/grip tuning affects traction, braking, or cornering behavior.
- [ ] Aerodynamic tuning affects cornering/downforce and top-speed tradeoffs.
- [ ] Gearing affects acceleration curve or maximum speed behavior.
- [ ] Setup changes are noticeable during playtesting, even if the initial simulation is simplified.

### Story 5: Complete a Timed Single Lap

**As a** racer  
**I want to** start a lap, pass checkpoints, and finish at the start/finish line  
**So that** I can measure my driving performance.

**Acceptance Criteria:**
- [ ] The start/finish block can begin and complete a lap.
- [ ] Checkpoints validate that the player follows the intended circuit order.
- [ ] A lap timer UI displays the current lap time and/or completed lap result.
- [ ] The MVP supports single-lap time attack first, not full race weekends.

### Story 6: Use Pit Lane for Repair and Setup Changes

**As a** racer  
**I want to** enter a pit lane area to repair wear/damage or change setup  
**So that** pit stops become part of the motorsport loop.

**Acceptance Criteria:**
- [ ] Pit lane zone blocks can identify a pit area.
- [ ] The car supports simple durability.
- [ ] Pit interaction can repair durability or wear.
- [ ] Pit interaction can support tire/setup changes.
- [ ] Fuel or energy systems are not required for the prototype.

---

## Functional Requirements

### Core Features

**Feature 1: Car Assembly Workstation**
- Description: A custom block that serves as the primary path for creating the prototype open-wheel car.
- User flow: Player places workstation → inserts required components/materials → assembles car → receives or spawns car.
- Edge cases: Missing components should prevent assembly; invalid recipes should not consume materials.
- Error handling: Use clear UI feedback or simple blocked behavior when the assembly cannot proceed.

**Feature 2: Open-Wheel Car Entity**
- Description: A drivable car entity with Minecraft-native visual style and simulation-inspired controls.
- User flow: Player places car → enters car → drives using standard movement controls → exits car.
- Edge cases: Car gets stuck, collides, flips, or reaches invalid terrain.
- Error handling: Prototype may provide simple recovery/reset behavior if the car becomes unusable.

**Feature 3: Tuning System**
- Description: A setup system covering engine/power, tires/grip, aerodynamics, and gearing.
- User flow: Player opens tuning interface or workstation → selects setup values/components → applies setup → tests on track.
- Edge cases: Extreme setups should remain playable and not break movement simulation.
- Error handling: Invalid setups should be rejected or clamped to supported prototype ranges.

**Feature 4: Custom Track Blocks**
- Description: Purpose-built blocks for racing surfaces and circuit construction.
- User flow: Player places track blocks → lays out circuit → places marker blocks → drives the lap.
- Edge cases: Tracks may be incomplete, too short, or missing markers.
- Error handling: Race timing should not start unless minimum required markers exist.

**Feature 5: Race Marker Blocks**
- Description: Start/finish and checkpoint blocks that define a valid lap.
- User flow: Player drives through start/finish → passes checkpoints in order → crosses start/finish again → receives lap time.
- Edge cases: Player skips checkpoints or drives markers out of order.
- Error handling: Lap remains invalid until required checkpoints are passed in sequence.

**Feature 6: Lap Timer UI**
- Description: Minimal UI showing current lap timing and completed lap result.
- User flow: Timing starts at start/finish → UI updates during lap → result appears after lap completion.
- Edge cases: Player exits car, leaves track, or invalidates lap.
- Error handling: Timer should stop, reset, or mark the lap invalid depending on prototype behavior.

**Feature 7: Pit Lane and Pit Stop Interaction**
- Description: Pit lane zone blocks and pit interaction for repair and setup/tire changes.
- User flow: Player enters pit lane → slows/stops in pit area → repairs car or changes setup → returns to track.
- Edge cases: Player enters pit lane during an active lap or tries to change setup outside pit zone.
- Error handling: Prototype may invalidate the lap or allow a controlled pit stop depending on configured behavior.

### Out of Scope
- Career mode, championships, economy progression, licenses, teams, or long-term player progression.
- Advanced visual damage, part-by-part crash physics, deformation, or highly realistic collision modeling.
- Public multiplayer, server matchmaking, anti-cheat, or large-scale networking support.
- Full race weekend format, qualifying sessions, multi-lap race management, or standings.
- AI opponents.
- Fuel or energy systems for the prototype.
- High-fidelity non-Minecraft-style vehicle models.

---

## Technical Constraints

### Performance
- Target Minecraft Forge 1.21.11 with Java 21.
- The prototype should run acceptably in a local single-player development environment.
- Vehicle movement and lap timing should avoid excessive per-tick overhead.
- Track marker checks should be lightweight enough for repeated playtesting.

### Security
- No external network services are required for the prototype.
- No account, authentication, telemetry, or personal data systems are required.
- Future multiplayer should be designed later with server-authoritative behavior in mind, but public multiplayer is out of scope for this PRD.

### Integration
- **Minecraft Forge 1.21.11**: Primary modding platform.
- **Minecraft client and integrated server**: MVP target environment is local single-player.
- **Creative inventory / commands**: May be used to speed prototype testing, but should not replace the custom workstation path.

### Technology Stack
- Minecraft 1.21.11
- Forge 61.x
- Java 21
- Gradle / ForgeGradle
- Minecraft-native block, item, entity, UI, and data generation systems where appropriate

---

## MVP Scope & Phasing

### Phase 1: MVP Prototype
- Minecraft-native open-wheel car entity.
- Custom car assembly workstation.
- Basic craft/component path for car creation.
- Custom track surface blocks.
- Start/finish block.
- Checkpoint blocks.
- Single-lap time attack flow.
- Minimal lap timer UI.
- Simulation-inspired driving baseline.
- Tuning for engine/power, tires/grip, aerodynamics, and gearing.
- Simple car durability.
- Pit lane zone and pit stop repair/setup interaction.

**MVP Definition**: A single player can build a simple track, assemble and tune one open-wheel car, drive a single timed lap through checkpoints, and use a pit lane for repair or setup changes.

### Phase 2: Private Server Expansion
- Small private-server support for 2–10 players.
- Multiplayer race start flow.
- Shared lap timing and results.
- Basic server-authoritative validation for race events.
- Improved car reset/recovery tools for multiplayer playtests.

### Future Considerations
- Multi-lap races.
- Practice / qualifying / race weekend formats.
- AI opponents.
- Career or progression systems.
- More detailed car models and liveries.
- More advanced damage and component wear.
- Fuel or energy systems.
- Public distribution polish for CurseForge / Modrinth.

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|------------|--------|---------------------|
| Vehicle physics are difficult to make fun and stable in Minecraft | High | High | Start with a simplified simulation model, playtest early, and tune for controllability before realism. |
| Scope expands beyond prototype needs | Medium | High | Keep MVP focused on single-player, one car type, single-lap timing, and core crafting/building loop. |
| Tuning systems become too complex before the driving model is stable | Medium | Medium | Implement bounded tuning ranges and prioritize noticeable differences over full realism. |
| Track validation becomes cumbersome for players | Medium | Medium | Use simple start/finish and checkpoint blocks with clear invalid-lap behavior. |
| Pit lane mechanics distract from core driving loop | Medium | Medium | Limit pit stops to simple durability repair and setup/tire changes in the prototype. |
| Multiplayer assumptions leak into MVP design | Low | Medium | Treat multiplayer as Phase 2; avoid building public-server systems until single-player loop is validated. |

---

## Dependencies & Blockers

**Dependencies:**
- Forge 1.21.11 mod workspace and Java 21 build setup.
- Definitions for custom blocks, items, entities, and UI screens.
- Prototype assets for Minecraft-native car and track visuals.
- A test circuit layout for validation.

**Known Blockers:**
- Final car physics model is not yet defined.
- Exact crafting recipes and component names are not yet specified.
- UI layout for car assembly, tuning, lap timer, and pit interaction is not yet designed.

---

## Appendix

### Glossary
- **Open-wheel car**: A formula-style racing car with exposed wheels and aerodynamic bodywork.
- **Track block**: A custom block used to construct racing surfaces or circuit elements.
- **Start/finish block**: A marker block that starts and completes timed laps.
- **Checkpoint block**: A marker block used to validate the correct route through a circuit.
- **Pit lane zone**: A marked area where repair or setup changes are allowed.
- **Setup**: The car's tuning configuration, including engine, tires, aerodynamics, and gearing.
- **Single-lap time attack**: A race mode where the player attempts to complete one valid lap as quickly as possible.

### References
- Project stack: Minecraft Forge 1.21.11, Java 21, ForgeGradle.
- Prototype priority: crafting-first MVP with a fun timed lap validation loop.

---

*This PRD was created through interactive requirements gathering with quality scoring to ensure comprehensive coverage of business, functional, UX, and technical dimensions.*
