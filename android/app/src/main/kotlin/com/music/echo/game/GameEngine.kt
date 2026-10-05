package echo.music.iad1tya.game

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 128bit game layer. Everything here is computed from the play history Echo already records
 * (one row per play: song, artists, when, how long), so there is nothing extra to track or
 * migrate. Pure Kotlin, no Android, so it is unit-tested directly.
 */

/** One listening event, flattened from Echo's `event` table. */
data class Play(
  val at: LocalDateTime,
  val playTimeMs: Long,
  val songId: String,
  val songTitle: String,
  val artistIds: List<String>,
  val artistNames: List<String>,
)

object GameRules {
  /** XP for every full minute listened. */
  const val XP_PER_MINUTE = 1
  /** Bonus the first time you ever play an artist. */
  const val XP_NEW_ARTIST = 10
  /** Minutes in a day that count it toward your streak. */
  const val STREAK_DAY_MINUTES = 10

  /** XP needed to go from [level] to level + 1. Early levels are quick, later ones take weeks. */
  fun xpToNext(level: Int): Int = (100 * level.toDouble().pow(1.5)).roundToInt()
}

data class Badge(
  val id: String,
  val title: String,
  val description: String,
  val unlocked: Boolean,
)

data class Quest(
  val title: String,
  val progress: Int,
  val goal: Int,
  val rewardXp: Int,
) {
  val done: Boolean
    get() = progress >= goal
}

data class TopTrack(val title: String, val artist: String, val plays: Int)

data class GameState(
  val totalXp: Int,
  val level: Int,
  val xpIntoLevel: Int,
  val xpToNext: Int,
  val currentStreak: Int,
  val bestStreak: Int,
  val totalMinutes: Long,
  val distinctSongs: Int,
  val distinctArtists: Int,
  val className: String,
  val badges: List<Badge>,
  val quests: List<Quest>,
  val topTracks: List<TopTrack>,
) {
  val levelProgress: Float
    get() = if (xpToNext <= 0) 0f else xpIntoLevel.toFloat() / xpToNext

  companion object {
    val Empty = computeGameState(emptyList(), LocalDate.now())
  }
}

private fun weekStart(date: LocalDate): LocalDate =
  date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** Quests for the week starting [week], given that week's plays and which artists were new. */
private fun questsFor(weekPlays: List<Play>, newArtistsThisWeek: Int): List<Quest> {
  val minutes = (weekPlays.sumOf { it.playTimeMs } / 60_000).toInt()
  val activeDays =
    weekPlays
      .groupBy { it.at.toLocalDate() }
      .count { (_, p) -> p.sumOf { it.playTimeMs } / 60_000 >= GameRules.STREAK_DAY_MINUTES }
  return listOf(
    Quest("Find 5 new artists", newArtistsThisWeek.coerceAtMost(5), 5, 100),
    Quest("Listen for 5 hours", (minutes / 60).coerceAtMost(5), 5, 75),
    Quest("Listen on 5 different days", activeDays.coerceAtMost(5), 5, 75),
  )
}

fun computeGameState(plays: List<Play>, today: LocalDate): GameState {
  val sorted = plays.sortedBy { it.at }

  // XP from listening time and first-time artists.
  val totalMinutes = sorted.sumOf { it.playTimeMs } / 60_000
  val seenArtists = HashSet<String>()
  val newArtistWeek = HashMap<LocalDate, Int>()
  for (p in sorted) {
    for (id in p.artistIds) {
      if (seenArtists.add(id)) {
        val w = weekStart(p.at.toLocalDate())
        newArtistWeek[w] = (newArtistWeek[w] ?: 0) + 1
      }
    }
  }
  var xp = (totalMinutes * GameRules.XP_PER_MINUTE).toInt() + seenArtists.size * GameRules.XP_NEW_ARTIST

  // Quest rewards: every week's completed quests pay out, so XP never goes backwards.
  val byWeek = sorted.groupBy { weekStart(it.at.toLocalDate()) }
  val thisWeek = weekStart(today)
  for ((week, weekPlays) in byWeek) {
    if (week == thisWeek) continue
    xp += questsFor(weekPlays, newArtistWeek[week] ?: 0).filter { it.done }.sumOf { it.rewardXp }
  }
  val currentQuests = questsFor(byWeek[thisWeek].orEmpty(), newArtistWeek[thisWeek] ?: 0)
  xp += currentQuests.filter { it.done }.sumOf { it.rewardXp }

  // Level from cumulative XP.
  var level = 1
  var remaining = xp
  while (remaining >= GameRules.xpToNext(level)) {
    remaining -= GameRules.xpToNext(level)
    level++
  }

  // Streaks: consecutive days with at least STREAK_DAY_MINUTES of listening.
  val dayMinutes =
    sorted.groupBy { it.at.toLocalDate() }.mapValues { (_, p) -> p.sumOf { it.playTimeMs } / 60_000 }
  val activeDays =
    dayMinutes.filterValues { it >= GameRules.STREAK_DAY_MINUTES }.keys.toSortedSet()
  var best = 0
  var run = 0
  var prev: LocalDate? = null
  for (d in activeDays) {
    run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
    best = maxOf(best, run)
    prev = d
  }
  // Today still counts as "in progress": a streak only breaks once a whole day is missed.
  var cursor = if (today in activeDays) today else today.minusDays(1)
  var current = 0
  while (cursor in activeDays) {
    current++
    cursor = cursor.minusDays(1)
  }

  // Top tracks.
  val topTracks =
    sorted
      .groupBy { it.songId }
      .values
      .sortedByDescending { it.size }
      .take(5)
      .map { TopTrack(it.first().songTitle, it.first().artistNames.joinToString(), it.size) }

  // Badges.
  fun minutesBetween(fromHour: Int, toHour: Int) =
    sorted.filter { it.at.hour in fromHour until toHour }.sumOf { it.playTimeMs } / 60_000
  val artistPlays = sorted.flatMap { it.artistIds }.groupingBy { it }.eachCount()
  val maxDayMinutes = dayMinutes.values.maxOrNull() ?: 0
  val distinctSongs = sorted.map { it.songId }.toSet().size
  val badges =
    listOf(
      Badge("first", "FIRST PRESS", "Play your first song", sorted.isNotEmpty()),
      Badge("night", "NIGHT OWL", "30 min between midnight and 4am", minutesBetween(0, 4) >= 30),
      Badge("early", "EARLY BIRD", "30 min between 5am and 8am", minutesBetween(5, 8) >= 30),
      Badge("crate", "CRATE DIGGER", "Play 25 different artists", seenArtists.size >= 25),
      Badge("library", "DEEP LIBRARY", "Play 250 different songs", distinctSongs >= 250),
      Badge("marathon", "MARATHON", "3 hours in one day", maxDayMinutes >= 180),
      Badge("loyal", "LOYALIST", "50 plays of one artist", (artistPlays.values.maxOrNull() ?: 0) >= 50),
      Badge("fire", "ON FIRE", "7-day streak", best >= 7),
      Badge("unstoppable", "UNSTOPPABLE", "30-day streak", best >= 30),
      Badge("lv10", "LEVEL 10", "Reach level 10", level >= 10),
    )

  // Class: what kind of listener you are, from your habits.
  val topArtistShare =
    if (sorted.isEmpty()) 0f
    else (artistPlays.values.maxOrNull() ?: 0).toFloat() / sorted.size
  val nightShare =
    if (totalMinutes == 0L) 0f else minutesBetween(0, 5).toFloat() / totalMinutes
  val className =
    when {
      sorted.isEmpty() -> "NEW PLAYER"
      nightShare >= 0.25f -> "NIGHT OWL"
      topArtistShare >= 0.3f -> "SUPERFAN"
      seenArtists.size >= 100 -> "CRATE DIGGER"
      best >= 14 -> "DAILY GRINDER"
      else -> "BARD"
    }

  return GameState(
    totalXp = xp,
    level = level,
    xpIntoLevel = remaining,
    xpToNext = GameRules.xpToNext(level),
    currentStreak = current,
    bestStreak = best,
    totalMinutes = totalMinutes,
    distinctSongs = distinctSongs,
    distinctArtists = seenArtists.size,
    className = className,
    badges = badges,
    quests = currentQuests,
    topTracks = topTracks,
  )
}
