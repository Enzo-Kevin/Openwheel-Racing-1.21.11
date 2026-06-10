# Openwheel Racing Asset Checklist

This document lists the art files the mod expects or should be updated to expect. Use Minecraft-style pixel art. Default size is **16x16 PNG** unless a different size is noted.

## Directory

Put assets under:

```text
src/main/resources/assets/openwheelracing/textures/
```

## Priority order

1. Track blocks: asphalt, kerb, start/finish, checkpoint, pit lane, barrier.
2. Refining/resource blocks: refinery, crude oil deposit.
3. New oil/refining/progression items.
4. Existing car component item redraws.
5. GUI backgrounds.
6. Future car entity texture/model.

## Block textures

### Track and race-control blocks

| File | Size | Used by | Art direction |
| --- | ---: | --- | --- |
| `textures/block/asphalt_track.png` | 16x16 | Asphalt Track | Very dark asphalt with subtle grey speckles/noise. Tile cleanly. Do not use pure black. |
| `textures/block/pit_lane.png` | 16x16 | Pit Lane | Asphalt-like, slightly lighter than main track, subtle grid/service-lane markings. |
| `textures/block/kerb_side.png` | 16x16 | Kerb side face | Concrete/rubber side with a hint of red-white striping or neutral edge detail. |
| `textures/block/kerb_top.png` | 16x16 | Kerb top face | Clear red/white racing kerb stripes. Draw the default orientation as north-facing; blockstates rotate it for east/south/west. |
| `textures/block/start_finish_side.png` | 16x16 | Start/Finish side face | Asphalt/concrete side. Avoid full checkerboard on side faces. |
| `textures/block/start_finish_top.png` | 16x16 | Start/Finish top face | Black-white checker pattern. Draw the forward arrow pointing north in the source texture; blockstates rotate it. |
| `textures/block/checkpoint.png` | 16x16 | Checkpoint | Asphalt base with white line/text-like marking and a north-pointing direction arrow; blockstates rotate it. |
| `textures/block/barrier.png` | 16x16 | Barrier | Dark grey crash barrier. Layered metal/rubber impact-absorbing look. |

### Workstation and resource blocks

| File | Size | Used by | Art direction |
| --- | ---: | --- | --- |
| `textures/block/car_assembly_workstation.png` | 16x16 | Car Assembly Workstation | Garage workbench/assembly machine. Metal surface, tool/part hints. Existing file can be redrawn. |
| `textures/block/refinery.png` | 16x16 | Refinery | Furnace-like refinery/tower/pipe motif. Dark metal with orange heat/oil-industrial accents. |
| `textures/block/crude_oil_deposit.png` | 16x16 | Crude Oil Deposit | Stone/deepslate-like base with glossy black oil pockets or veins. Should read as oil-bearing rock. |

## Item textures

### Existing car component items

These files already exist, but they should be redrawn if the style feels inconsistent.

| File | Size | Used by | Art direction |
| --- | ---: | --- | --- |
| `textures/item/prototype_car_spawn.png` | 16x16 | Prototype Car Spawn | Small open-wheel car icon, top or three-quarter view. |
| `textures/item/chassis.png` | 16x16 | Chassis | Lightweight race frame/tub, dark metal or carbon. |
| `textures/item/engine.png` | 16x16 | Engine | Compact race engine/motor block. |
| `textures/item/tires.png` | 16x16 | Tires | Set/stack of black slick racing tyres. |
| `textures/item/aero_kit.png` | 16x16 | Aero Kit | Front/rear wing silhouette, carbon/dark grey. |
| `textures/item/gearbox.png` | 16x16 | Gearbox | Mechanical casing/gears. |
| `textures/item/steering_controls.png` | 16x16 | Steering Controls | Steering wheel or wheel plus pedals. |
| `textures/item/carbon_fiber.png` | 16x16 | Carbon Fiber | Black woven diagonal pattern with subtle highlights. |

### New oil/refining/progression items

These currently use vanilla placeholder textures and should get custom art.

| File | Size | Used by | Art direction |
| --- | ---: | --- | --- |
| `textures/item/crude_oil_chunk.png` | 16x16 | Crude Oil Chunk | Black tarry lump/ore chunk with glossy highlights. |
| `textures/item/crude_oil_bucket.png` | 16x16 | Crude Oil Bucket | Bucket filled with black oil. Similar silhouette to vanilla bucket, black liquid. |
| `textures/item/gas.png` | 16x16 | Gas | Small gas canister or pale fuel vapor item. Must not look like gunpowder. |
| `textures/item/petrol_can.png` | 16x16 | Petrol Can | Red/yellow jerrycan or fuel bottle. Should read as racing fuel. |
| `textures/item/diesel_can.png` | 16x16 | Diesel Can | Dark green/grey jerrycan, distinct from petrol. |
| `textures/item/crude_rubber.png` | 16x16 | Crude Rubber | Sticky brown/black rubber glob. |
| `textures/item/rubber.png` | 16x16 | Rubber | Clean black rubber sheet, ingot, or roll. |
| `textures/item/asphalt_binder.png` | 16x16 | Asphalt Binder | Black tar/binder item, bucket, pellet, or lump. |
| `textures/item/plastic.png` | 16x16 | Plastic | White/light grey plastic sheet or pellets. |
| `textures/item/racing_electronics.png` | 16x16 | Racing Electronics | Circuit board with redstone/quartz/diamond highlights. |

## GUI textures

The current screens are code-drawn rectangles for quick iteration. Draw these if you want a polished UI background. After these exist, the screen code should be updated to load them directly.

| File | Size | Used by | Art direction |
| --- | ---: | --- | --- |
| `textures/gui/car_assembly_workstation.png` | 176x178 | Car assembly screen | Garage workbench panel. Six component slots on left, output/progress center, setup controls on right, player inventory below. |
| `textures/gui/refinery.png` | 176x166 | Refinery screen | Furnace-like refinery UI. Crude input upper-left, fuel lower-left, horizontal progress toward five output slots in a tower cluster. |

### Current car assembly UI layout

Canvas: `176x178`

- Component input slots:
  - `(35,17)`, `(53,17)`, `(71,17)`
  - `(35,35)`, `(53,35)`, `(71,35)`
- Output slot: `(125,26)`
- Progress bar: from `(95,30)` to `(119,35)`
- Setup controls:
  - Labels start near x `124`
  - Minus buttons at x `136`
  - Plus buttons at x `156`
  - Rows y `14`, `27`, `40`, `53`
- Repair button: `(123,70)` size `45x14`
- Player inventory begins at y `96`
- Hotbar y `154`

### Current refinery UI layout

Canvas: `176x166`

- Crude input slot: `(35,17)`
- Fuel input slot: `(35,53)`
- Output slots:
  - top: `(116,8)`
  - middle row: `(98,29)`, `(116,29)`, `(134,29)`
  - bottom: `(116,50)`
- Progress bar background: `(59,31)` to `(87,37)`
- Progress fill: from `(61,33)`, width up to `24`, height `2`
- Burn bar: `(38,37)` to `(52,51)`, vertical fill from bottom upward
- Player inventory begins at y `84`
- Hotbar y `142`

## Future entity texture

The car renderer is currently code-rendered rather than using a standard texture/model pipeline. If/when we switch to a textured model, draw this:

| File | Suggested size | Used by | Art direction |
| --- | ---: | --- | --- |
| `textures/entity/prototype_car.png` | 64x32 or 128x64 | Prototype car entity | Open-wheel car atlas, simple F1/kart style, readable from gameplay camera. |

## Model JSON status

The block models are already wired to the custom block texture paths listed below. Block item models inherit their block models, so asphalt, kerb, start/finish, checkpoint, pit lane, barrier, refinery, crude oil deposit, and car assembly workstation render as 3D block items in inventories/hands.

### Block models

```text
models/block/asphalt_track.json       uses openwheelracing:block/asphalt_track
models/block/pit_lane.json            uses openwheelracing:block/pit_lane
models/block/checkpoint.json          uses openwheelracing:block/checkpoint
models/block/barrier.json             uses openwheelracing:block/barrier
models/block/refinery.json            uses openwheelracing:block/refinery
models/block/crude_oil_deposit.json   uses openwheelracing:block/crude_oil_deposit
models/block/kerb.json                uses openwheelracing:block/kerb_side + openwheelracing:block/kerb_top
models/block/start_finish.json        uses openwheelracing:block/start_finish_side + openwheelracing:block/start_finish_top
```

### Item models

```text
models/item/crude_oil_chunk.json      -> openwheelracing:item/crude_oil_chunk
models/item/crude_oil_bucket.json     -> openwheelracing:item/crude_oil_bucket
models/item/gas.json                  -> openwheelracing:item/gas
models/item/petrol_can.json           -> openwheelracing:item/petrol_can
models/item/diesel_can.json           -> openwheelracing:item/diesel_can
models/item/crude_rubber.json         -> openwheelracing:item/crude_rubber
models/item/rubber.json               -> openwheelracing:item/rubber
models/item/asphalt_binder.json       -> openwheelracing:item/asphalt_binder
models/item/plastic.json              -> openwheelracing:item/plastic
models/item/racing_electronics.json   -> openwheelracing:item/racing_electronics
```

## Exact full PNG checklist

```text
textures/block/asphalt_track.png
textures/block/pit_lane.png
textures/block/kerb_side.png
textures/block/kerb_top.png
textures/block/start_finish_side.png
textures/block/start_finish_top.png
textures/block/checkpoint.png
textures/block/barrier.png
textures/block/car_assembly_workstation.png
textures/block/refinery.png
textures/block/crude_oil_deposit.png

textures/item/prototype_car_spawn.png
textures/item/chassis.png
textures/item/engine.png
textures/item/tires.png
textures/item/aero_kit.png
textures/item/gearbox.png
textures/item/steering_controls.png
textures/item/carbon_fiber.png
textures/item/crude_oil_chunk.png
textures/item/crude_oil_bucket.png
textures/item/gas.png
textures/item/petrol_can.png
textures/item/diesel_can.png
textures/item/crude_rubber.png
textures/item/rubber.png
textures/item/asphalt_binder.png
textures/item/plastic.png
textures/item/racing_electronics.png

textures/gui/car_assembly_workstation.png
textures/gui/refinery.png
```
