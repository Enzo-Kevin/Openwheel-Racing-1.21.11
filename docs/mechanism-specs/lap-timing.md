# Lap Timing Mechanism Spec

## Purpose

- Should lap timing support casual solo play, competitive time trials, multiplayer racing, or all later?

  All, solo first

- What counts as a valid lap?

  From starting counter block to finish counter block, and the car must always at least have some part landing on track blocks, even a tiny bit. If the entire car is off track, that will count as four-wheel-off-track and an invalid lap. and the starting and ending block will both have direction guard, if reverse passing it will not count (to avoid spinning around the starting line to count as a finished lap)

## Track Definition

- How does the player define a track?

  Continuous-placed track blocks is OK, no additional track objects verification.

- Is one checkpoint enough, or should tracks support multiple ordered checkpoints?

  MVP need not checkpoints first, and well, we will spec this later

- Should checkpoint order be configured manually, auto-detected, or inferred from placement?

  Manually

- Can multiple tracks exist in one world?

  Of course

## Timing Rules

- When does a lap start?

  When the car passes the starting counter in the right approx. direction

- When does a lap finish?

  When the car passes the finishing counter in the right approx. direction

- What invalidates a lap?

  Leaving track entirely, or reverse-passing

- Should reversing through checkpoints count?

  No

- Should leaving the car, breaking blocks, teleporting, or dying cancel the lap?

  Yes

## Results

- Should best laps be stored per car, per player, per track, or globally?

  Stored per player, and record can be cleared

- Should lap times persist across world reloads?

  Yes

- Should the game show sector times, deltas, leaderboard, or only current/best lap?

  Only current/best for now because track logic is still very coarse now

## Feedback

- What should the HUD show while timing?

  A real-time current timing and a past laps record board, may involve delta later

- What messages/sounds should trigger on start, checkpoint, invalid lap, and finish?

  a "INVALID LAP" title for invalid laps with reason attached below; other will trigger message telling the user about current status, and by finish, the message will contain the previous lap time.

## Acceptance Criteria

- What is enough for the next timing prototype?

  Off-track penalties, successful lap timing

- What should wait for multiplayer or track-definition systems?

  Delta, fill leaderboard, checkpoint logic, multiplayer time trail, ...
