# Pixelfying Echo Music: the 128bit skin + game layer

Echo Music (https://github.com/EchoMusicApp/Echo-Music) is a polished Kotlin/Compose
Android player built on YouTube Music. It already does the hard parts: streaming, offline,
synced lyrics, Echo Find (song ID), Echo Brain (smart queue), Listen Together, Spotify
import, a stats screen, and a full play-history database.

What it's missing is the **128bit feel**: chunky pixels, hard edges, chiptune energy,
and a reason to come back every day. So the plan has two layers:

1. **The skin.** A "128bit" look that sits next to Echo's existing Material and
   Apple-style options. You turn it on in Appearance settings.
2. **The game layer.** XP, levels, streaks, achievements and quests, all computed
   from Echo's play history.

Interactive mockup: [`mockups/echo-128bit.html`](mockups/echo-128bit.html). Open it in a
browser and press play.

---

## Why this fits Echo's code

Echo is already built around swappable styles. Each of these is an enum in
`core/.../constants/PreferenceKeys.kt`, so pixel mode adds one entry to each and does not
fork any screens:

| Echo already has | We add | Where it lands |
|---|---|---|
| `PlayerBackgroundStyle` (DEFAULT, GRADIENT, BLUR, GLOW_ANIMATED, APPLE_MUSIC, LIVE_MESH, LIQUID_GLASS) | `PIXEL` | `ui/player/Player.kt`, `MiniPlayer.kt`, `Queue.kt`, `ui/theme/PlayerSliderColors.kt` |
| `SliderStyle` (DEFAULT, WAVY, SLIM) | `BLOCKS` | `ui/player/Player.kt` (the `when (sliderStyle)` around line 2034), new `ui/component/BlockSlider.kt` |
| `LyricsAnimationStyle` (… KARAOKE, APPLE, METRO_LYRICS) | `ARCADE` | `ui/component/Lyrics.kt` |
| `AppFont` (SYSTEM, GOOGLE_SANS, OUTFIT, …) | `PRESS_START_2P` | `app/src/main/res/font/`, `ui/theme/Theme.kt` |
| `echomusicTheme(...)` with dynamic color | `pixelMode: Boolean` branch | `ui/theme/Theme.kt` |
| `Event(songId, timestamp, playTime)` table | nothing new; we read it | `core/.../db/entities/Event.kt` → new `game/` package |

Everything stays opt-in. Material and Apple users see no change.

---

## Layer 1: the skin

### Palette (fixed, not dynamic)
Pixel mode skips Material You's wallpaper colors and uses the 128bit family palette
(same tokens as our landing page):

| Role | Color | Use |
|---|---|---|
| background | `#0B0B16` | app ground |
| surface | `#141428` | panels, sheets |
| primary | `#2DD4BF` teal | progress, active tab, now-playing |
| secondary | `#FF9F1C` orange | buttons, XP |
| tertiary | `#FF5D8F` pink | likes, highlights, level-up |
| outline | `#26264A` | 3dp panel borders |

In `Theme.kt`: when `pixelMode`, return a fixed `darkColorScheme(...)` with these values
instead of `dynamicDarkColorScheme` / `rememberDynamicColorScheme`.

### Shape: square corners and hard shadows
- Echo's DESIGN.md calls for 24dp corners throughout. Pixel mode sets **every**
  `MaterialTheme.shapes` slot to `RectangleShape` (0dp).
- New `Modifier.pixelPanel()`: solid fill + 3dp border + a hard 4dp offset shadow drawn
  as a second rect (no blur, ever).
- **Liquid Glass is turned off in pixel mode.** Blur and refraction don't belong in a
  pixel look. `GlassEffect.kt` already falls back when `isGlassSupported()` is false, so
  we add `&& !pixelMode` to that check.
- The floating tab bar (`ui/component/floatingtabbar/FloatingTabBar.kt`) becomes a flat
  bar with square tabs and a 2dp pixel underline on the active tab.

### Type
- Bundle **Press Start 2P** (SIL OFL, OK to ship under GPL) in `res/font/`.
- Use it for **headings, labels, numbers and the now-playing title only**. Body text,
  song lists and lyrics stay in the readable font, because pixel type at 14sp is tiring
  to read. In `Type.kt`, pixel mode swaps only the `display*`, `headline*`, `title*` and
  `label*` styles.

### Album art: pixelated covers
- New `PixelArt.kt`: downsample the cover to 32×32 (option for 16 or 64), dither with a
  4×4 Bayer matrix into a 16-color palette pulled from the cover (`PlayerColorExtractor.kt`
  already extracts colors), then draw with `FilterQuality.None` so it scales up crisp.
- Cache the pixelated bitmap per album ID so it's computed once.
- `PlayerBackgroundStyle.PIXEL`: the same pixelated art, blown up behind the player at
  low opacity, plus an optional 2px scanline overlay.

### Player controls
- **`SliderStyle.BLOCKS`**: the seek bar becomes 32 square cells that fill one at a
  time, with the time shown as `01:42 / 03:58` in Press Start.
- **Visualizer**: a 12-bar stepped spectrum above the controls. Bars move in 8 fixed
  height steps, not smoothly.
- **Buttons**: square, with the same "press down" as the landing page (4dp shadow that
  collapses on tap) plus a light haptic tick.
- **Icons**: a 16×16 pixel icon set (play, pause, skip, shuffle, repeat, heart, queue,
  lyrics) as vector drawables via Echo's existing `scripts/compose_svg_drawable.py`.

### Lyrics: `LyricsAnimationStyle.ARCADE`
- The active line is set in Press Start, and words light up teal one at a time in
  stepped jumps (Echo already has word-level timing for KARAOKE).
- Past lines dim to `#9A97B8`. Upcoming lines type in with a blinking block cursor.

### Motion
- Swap Echo's Material "emphasized" easing for **stepped** motion in pixel mode: a
  tween runs as 4–6 discrete frames (`keyframes` or a `StepEasing(steps)` helper).
  It's cheaper to run and looks properly retro.
- Respect the system "remove animations" setting: no steps, just cut.

### Sound (optional, off by default)
- 8-bit UI blips for like, add to queue and level-up. They only play when music
  is paused or ducked, so they never interrupt the song.

---

## Layer 2: the game layer

All of this works offline from the existing `event` table, so no new tracking or
database migration is needed for v1.

### XP and levels
- **1 XP per full minute listened** (sum of `Event.playTime`).
- Bonuses: +25 for the first play of a new artist, +10 for finishing an album in order,
  +50 for each song identified with Echo Find.
- Level curve: `xpForLevel(n) = 100 * n^1.5`, so early levels come fast and later ones
  take weeks.
- An XP bar sits under the mini player, and a "LEVEL UP" banner shows when you cross a
  level.

### Streaks
- One listening day = at least 10 minutes. A streak flame on Home shows how many days
  in a row.
- One "streak freeze" earned per 7-day streak, so a single missed day doesn't reset it.

### Achievements (examples)
| Badge | Unlock |
|---|---|
| NIGHT OWL | 30 min of listening between 00:00 and 04:00 |
| CRATE DIGGER | 5 new artists in one week |
| DEEP CUT | Play a track with under 10k views |
| SHAZAM SNIPER | Identify 10 songs with Echo Find |
| CO-OP | Finish a full Listen Together session |
| COMPLETIONIST | Play an album start to finish, no skips |
| LOYALIST | Same artist, 7 days in a row |

### Stats become a character sheet
- `StatsScreen.kt` / `ListeningSummaryScreen.kt` get a pixel layout styled like a save
  file: level, total XP, top artist as your "class" (the genre is your class, e.g.
  "LVL 14 SYNTH MAGE"), top 5 tracks as an inventory, and badges.
- "Wrapped" becomes an **end-of-level screen** you can open any week, not just in
  December.

### Quests (ties into the 128bit Music brief)
- Weekly quests: "Build a 90s road-trip mix", "Find 5 new artists", "Listen to a full
  album". Finishing one awards XP.
- Echo Brain's suggestions can be shown as a **side quest**: "Echo Brain thinks you'll
  like X. Play it to earn +15 XP."

### 128bitlife hookup
- Emit the shared events from the README (`track.played`, `playlist.created`,
  `streak.milestone`, `discovery.saved`) so XP also counts in 128bitlife.
- v1 stays local-only. Syncing comes later and should be opt-in.

---

## Build order

| Phase | What ships | Why first |
|---|---|---|
| **1. Instant pixel** | Palette, square corners, Press Start headings, block seek bar, pixelated cover | Most of the visual change for little code: about 5 files touched |
| **2. Full skin** | `PIXEL` player background, ARCADE lyrics, pixel icon set, stepped motion, flat tab bar, glass off | Makes the whole app feel consistent, not just the player |
| **3. Game core** | XP, levels, streaks from `event` history; XP bar; level-up banner | This is what brings you back every day |
| **4. Character sheet** | Pixel stats screen, achievements, end-of-level recap | Gives you something to show your friends |
| **5. Quests + 128bitlife** | Weekly quests, Echo Brain side quests, shared events | Links Echo into the rest of the 128bit apps |

---

## Things to decide before building

1. **Upstream PR or fork?** Echo is **GPL-3.0**, so a fork must stay open source under
   GPL-3 with credit. The cleaner path is to build this as an opt-in theme and PR it to
   Echo, since every change above slots into Echo's existing style enums. Echo's
   `AGENT.md` has house rules for that PR: update `AGENT.md` and `DESIGN.md` with any new
   design patterns, add the change to `upcomingupdate.json`, credit sources in the README's
   Special Thanks, and build with `./gradlew :app:compileUniversalGmsDebugKotlin`.
2. **How this relates to the 128bit Music brief.** Our README puts YouTube Music
   last ("unofficial, fragile, if ever"). Echo is built entirely on YouTube Music's
   unofficial API, so it carries that risk. A reasonable split: Echo with the 128bit skin
   becomes the **player**, and 128bit Music stays the **stats and quests hub** across
   Spotify, Apple Music and Echo. Echo reports its plays as 128bit events.
3. **How far to take the pixels.** Phase 1 alone may be enough. Decide after living with
   it for a week.
