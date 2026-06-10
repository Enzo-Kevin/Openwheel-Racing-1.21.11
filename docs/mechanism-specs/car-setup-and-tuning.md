# Car Setup and Tuning Mechanism Spec

## Purpose

- What should setup tuning let players express or optimize?

  Adjust engine/chassis/aero parameters for different feelings

- Should tuning be arcade-simple, sim-inspired, or somewhere between?

  Simulation physics

## Tunable Values

For each value, define range, effect, UI label, and downside.

| Value | Range | Positive Effect | Tradeoff | Notes |
| --- | --- | --- | --- | --- |
| Power Mode | Mode 0-3 | Higher the mode, higher output | Higher the mode, higher fuel consumption | Discrete max power output power, peak 750kW physics |
| Tyre Setup | C1-C5 | Softer higher grip | Softer faster wear | Not tunable, once made a tyre cannot be changed into other types |
| Aero | Preset 0-4 | Higher the mode, higher downforce | Higher the mode, higher drag | Will turn into separate front wing and rear wing later |
| Gearing | Preset 0-2 | Higher the mode, higher top speed | Higher the mode, higher ratio gives slower acceleration | Probably will turn into continuous ratio tuning later |

## UI Flow

- Where should tuning happen?

  On the car assembly block for now

## Costs and Limits

- Should tuning be free, consume parts, require time, or require track testing?

  Tuning is free for MVP, but later there might be like furnace tuning effective time cost

- Can players change setup anywhere?

  Anywhere with the assembly block for now, but might not be later

- Should setup changes persist on the item and spawned entity?

  Yes

## Feedback

- How should players understand the effect of setup changes?

  They will see through the HUD and driving experience feedback, like you can feel like the car is not accelerating that fast or reaches a top speed earlier or later

- Should the HUD show setup values?

  Yes, but on the left side or right side, not blocking primary HUD

- Should tooltips describe tradeoffs in plain language?

  There should be a handbook saying so

## Acceptance Criteria

- What must be implemented for setup tuning to become a real gameplay loop?

  All parts adjustable as described above
