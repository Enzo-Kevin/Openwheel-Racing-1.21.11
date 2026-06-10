# Damage and Recovery Mechanism Spec

## Purpose

- What should crash damage add to gameplay?

  Avoid players aggressively driving and mimic risk tradeoff

- Should damage be a light penalty, a serious repair loop, or a race-ending threat?

  A percentage harm on the car and potential race-ending threat, also harms driver

## Damage Sources

- What should damage the car?

  High-speed collision with walls, high distance drops, and high-relative-speed collision with other entities. Fire, explosion and mobs also do damage.

## Effects

- How should damage affect speed, acceleration, steering, braking, and tyre wear?

  Impact car and driver HP, speed damage linearly (just for now)

- Should damage have thresholds with warnings or visible effects?

  Yes, a visible car damage percentage sign and hurting effect

- Should the car be destroyed at 100% damage?

  Yes, dropping into the raw materials they are composed of

## Repair and Recovery

- How can the player recover a damaged car?

  Workstation repair and pit stop repair, require some raw materials

## Feedback

- What sounds/particles/messages should crashes produce?

  explosion effects basically

- Should damaged cars render differently?

  Show it cracked effects

- Should the HUD warn at specific thresholds?

  Yes

## Acceptance Criteria

- What is the minimum satisfying damage/recovery loop?

  Different collision observe different harms, and also car repairing and full damage observed
