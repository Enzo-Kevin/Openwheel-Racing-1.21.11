# Oil and Refining Mechanism Spec

## Purpose

- What role should crude oil play in the racing progression loop?

  A source of resource for pavement and fuel

- Should oil be required for tyres, fuel, asphalt, composites, or all of these?

  I think so, we can also involve some (expensive) alternative crafting methods from original Minecraft

- Is this meant to be a lightweight crafting step or a major automation chain?

  Something like a furnace

## World Generation

- Should crude oil appear as an ore, fluid pocket, surface seep, biome feature, structure loot, or trade?

  Fluid but ore-similar generation logic, will appear more in some biomes (deserts and mountains), can occur in structural loot (but very rare) and trade (but only with wandering traders, not villagers)

- Which dimensions and biomes should contain it? 

  Overworld, all biomes but more prevalent in mountains and deserts

- How rare should it be relative to coal, iron, redstone, or diamonds?

  Vein rarity similar to iron, but aboundance for a vein should be like coal or even more

- At what Y-levels or terrain conditions should it spawn?

  y<=20 for all

- Should it require a special tool or block to extract?

  Bucket is OK

## Extraction

- What block/item does the player use to collect crude oil?

  Bucket, like gathering lava

- Should extraction consume durability, energy, buckets, pipes, or time?

  No

- Can crude oil be moved as a fluid, stored as an item, or both?

  Fluid and fluid bucket, but it behaves more like lava than water as it cannot be infinitely generated from 2 buckets

- What happens if the player breaks an oil source/block incorrectly?

  If poured by water, it will turn into water; if poured by lava or ignited, it will explode like TNT

## Refining Process

- What refining block or workstation should exist?

  A single refining block like a furnace

- What inputs does refining require?

  Something to burn and a bucket of crude oil

- How long should one refining cycle take?

  300 ticks

- Should refining need fuel, heat, redstone, water, catalysts, or power?

  Fuel for burning

- Should refining have multiple recipes or one generic process?

  One generic purpose, but not everything will come out, there is randomness in its output

## Outputs

- What products come from crude oil?
  - Gas (item fuel), can be put into a furnace to smelt into plastic
  - Petrol (liquid fuel), used to burn in cars and other places
  - Crude Rubber (item), can be crafted with bone meal (or any more preferrable additional) to form rubber
  - Diesel (liquid fuel), used to burn in diesel engines and other places but not openwheel cars
  - Asphalt binder (block), can be crafted with gravel, sand or cobblestone to form asphalt
  
- Are outputs deterministic or probabilistic?

  Probabilistic, there will be 4 things generated (not necessarily distinct) that fit in the 5 slots

- Should output ratios be simple or realistic?

  Random ratio just for now

## Gameplay Feedback

- What UI should refining expose?

  Something like a furnace, the difference is the outcome part will become 5 slots with a refinery tower shape

- What particles/sounds should communicate active refining?

  Similar to furnace

- How should incomplete, blocked, or invalid refining be shown?

  just stop refining until the user gets it right, like furnace

## Balance Targets

- How much crude oil should be needed for one car?

  TBD

- How much crude oil should be needed for a small test track?

  TBD

- How much time should refining take in early game?

  TBD

- Should late-game refining be automatable or faster?

  Yes, with red stone

## Acceptance Criteria

- What must be implemented for the first crude oil/refining prototype to count as complete?

  User can mine crude oil in the wild and refine it

- What can be deferred?

  Detailed ratio tuning
