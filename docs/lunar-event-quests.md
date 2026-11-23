# Lunar-event quest bridge

Lavendre Core records witnessing an active lunar event as a hidden advancement.
QuestLog can use its existing `questlog:advancement` objective to watch that
advancement. Neither mod needs a direct dependency on QuestLog.

The bridge is enabled only when `enhancedcelestials2core` is installed. It reads
the server's actual lunar forecast once per second; it does not infer an event
from the time of day or moon phase, and it does not alter the forecast. Only
players currently in the affected dimension receive credit. Joining, respawning,
or entering that dimension during an active event also counts. Ordinary nights,
upcoming scheduled events, and events hidden by the forecast's weather rules do
not count. There is no requirement to look at the moon or remain outdoors.

Advancement progress persists through reconnects and server restarts. Repeated
events do not grant rewards repeatedly. These advancements have no display,
toast, chat announcement, sound, or rewards; the quest controls its own rewards
and completion sound.

## Included advancement IDs

- `lavendre_core:lunar_events/any`: any active non-default lunar event.
- `lavendre_core:lunar_events/lullabyofthenewmoon/blue_moon`
- `lavendre_core:lunar_events/lullabyofthenewmoon/eternal_moon`
- `lavendre_core:lunar_events/lullabyofthenewmoon/frost_moon`
- `lavendre_core:lunar_events/lullabyofthenewmoon/iridescent_moon`
- `lavendre_core:lunar_events/lullabyofthenewmoon/super_blue_moon`
- `lavendre_core:lunar_events/lullabyofthenewmoon/super_eternal_moon`
- `lavendre_core:lunar_events/lullabyofthenewmoon/super_frost_moon`

Example QuestLog objective (the same condition can also be used as a requirement):

```json
{
  "type": "questlog:advancement",
  "advancement": "lavendre_core:lunar_events/lullabyofthenewmoon/blue_moon",
  "icon": { "item": "minecraft:lapis_lazuli" }
}
```

No existing quest files are changed by the bridge. QuestLog can recognize the
advancement after the event, so a quest unlocked later can still count an event
already witnessed. This records witnessing, not surviving until the event ends.

## Additional events

For event `example:another_moon`, add a hidden advancement at
`data/lavendre_core/advancement/lunar_events/example/another_moon.json`:

```json
{
  "criteria": {
    "witnessed": { "trigger": "minecraft:impossible" }
  },
  "requirements": [["witnessed"]]
}
```

No Java changes are needed. Use
`lavendre_core:lunar_events/example/another_moon` in the quest. A missing
event-specific advancement is simply skipped; the general `any` advancement
still works. These bridge advancements are standard mod data, independent of
the optional merged/developer-mode packs that supply the lunar events.

## In-game verification

In an isolated test world with the lunar event pack enabled, force a Blue Moon
using the event mod's command suggestions and wait for it to be active. After
at most one second, the `any` and `blue_moon` advancements should be complete
for players in that dimension. A QuestLog objective using the example above
should then complete. Also check an ordinary day, another dimension, joining
an ongoing event, and loading without the event mod installed. Do not grant
these hidden advancements manually during that test.
