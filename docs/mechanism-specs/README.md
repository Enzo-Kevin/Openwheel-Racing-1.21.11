# Mechanism Specifications

Use this folder to define gameplay mechanisms before implementation. Each file is intentionally question-driven so design choices are explicit before code is added.

## Suggested Fill Order

1. `resource-progression.md` — defines how resources feed racing systems.
2. `oil-and-refining.md` — answers the next roadmap blocker.
3. `rubber-and-tyres.md` — connects refining/resources to car parts and tyre wear.
4. `track-surfaces.md` — clarifies block roles and driving effects.
5. `lap-timing.md` — defines race rules beyond the current prototype timer.
6. `car-setup-and-tuning.md` — defines tuning UI, allowed ranges, and tradeoffs.
7. `damage-and-recovery.md` — defines crash consequences and recovery loops.
8. `progression-and-crafting.md` — defines recipe balance and survival pacing.

## Per-Mechanism Checklist

For each mechanism, fill in:

- Player goal: What should the player be trying to accomplish?
- Inputs: What items, blocks, UI actions, or world conditions feed the mechanism?
- Process: What steps happen, and how long do they take?
- Outputs: What does the player receive or unlock?
- Feedback: What should the player see, hear, or read?
- Failure cases: What can go wrong, and how should the game respond?
- Balance targets: How common, expensive, fast, or powerful should it be?
- Dependencies: Which other mechanisms must exist first?
- Acceptance criteria: What must be true before the mechanism is considered done?
