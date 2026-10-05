package echo.music.iad1tya.game

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
  // Monday 2026-10-05
  private val today = LocalDate.of(2026, 10, 5)

  private fun play(day: LocalDate, hour: Int, minutes: Int, song: String = "s1", artist: String = "a1") =
    Play(day.atTime(hour, 0), minutes * 60_000L, song, "Song $song", listOf(artist), listOf("Artist $artist"))

  @Test
  fun emptyHistoryIsLevelOne() {
    val s = computeGameState(emptyList(), today)
    assertEquals(1, s.level)
    assertEquals(0, s.totalXp)
    assertEquals(0, s.currentStreak)
    assertEquals("NEW PLAYER", s.className)
    assertFalse(s.badges.first { it.id == "first" }.unlocked)
  }

  @Test
  fun xpIsMinutesPlusNewArtistBonus() {
    // 30 min, 2 artists -> 30 + 2*10 = 50 XP; no quests done.
    val s = computeGameState(listOf(play(today, 12, 20), play(today, 13, 10, "s2", "a2")), today, questsEnabled = false)
    assertEquals(50, s.totalXp)
    assertEquals(2, s.distinctArtists)
  }

  @Test
  fun levelsUpAtThreshold() {
    // Level 1 -> 2 costs 100 XP. 90 min + 1 artist (10) = 100 XP.
    val s = computeGameState(listOf(play(today, 12, 90)), today, questsEnabled = false)
    assertEquals(2, s.level)
    assertEquals(0, s.xpIntoLevel)
    assertEquals(GameRules.xpToNext(2), s.xpToNext)
  }

  @Test
  fun streakCountsConsecutiveDaysAndTodayIsStillOpen() {
    val plays = (1..4).map { play(today.minusDays(it.toLong()), 12, 15) } // 4 days before today
    val s = computeGameState(plays, today)
    assertEquals(4, s.currentStreak) // today not played yet: streak not broken
    assertEquals(4, s.bestStreak)
    val withToday = computeGameState(plays + play(today, 9, 15), today)
    assertEquals(5, withToday.currentStreak)
  }

  @Test
  fun shortDaysDoNotCountAndGapsBreakStreak() {
    val plays =
      listOf(
        play(today.minusDays(5), 12, 15),
        play(today.minusDays(4), 12, 15),
        play(today.minusDays(3), 12, 5), // under 10 minutes: not a streak day
        play(today.minusDays(2), 12, 15),
        play(today.minusDays(1), 12, 15),
      )
    val s = computeGameState(plays, today)
    assertEquals(2, s.currentStreak)
    assertEquals(2, s.bestStreak)
  }

  @Test
  fun weeklyQuestsTrackThisWeekAndPayOut() {
    // This week (Mon): 5 new artists -> quest done, +100 XP.
    val plays = (1..5).map { play(today, 10 + it, 1, "s$it", "a$it") }
    val s = computeGameState(plays, today)
    val q = s.quests.first { it.title.startsWith("Find 5") }
    assertTrue(q.done)
    // 5 min + 5 artists*10 + 100 weekly quest + 50 for "Discover 5 artists" (first quest-line step)
    assertEquals(205, s.totalXp)
    // The artist quest line has already moved on to its next step.
    assertTrue(s.quests.any { !it.weekly && it.title == "Discover 10 artists" && it.progress == 5 })
  }

  @Test
  fun nightOwlBadge() {
    val s = computeGameState(listOf(play(today, 1, 35)), today)
    assertTrue(s.badges.first { it.id == "night" }.unlocked)
    assertFalse(s.badges.first { it.id == "early" }.unlocked)
  }

  @Test
  fun topTracksSortedByPlays() {
    val plays = listOf(play(today, 9, 3, "x"), play(today, 10, 3, "y"), play(today, 11, 3, "y"))
    val s = computeGameState(plays, today)
    assertEquals("Song y", s.topTracks.first().title)
    assertEquals(2, s.topTracks.first().plays)
  }

  @Test
  fun levelsNeverCap() {
    // ~5,000 hours of listening: far past any badge, still levelling.
    val plays = (0 until 2000).map { play(today.minusDays(it.toLong()), 12, 150, "s$it", "a${it % 40}") }
    val s = computeGameState(plays, today, questsEnabled = false)
    assertTrue(s.level > 100)
    assertTrue(s.xpToNext > GameRules.xpToNext(100))
    assertTrue(s.xpIntoLevel in 0 until s.xpToNext)
  }

  @Test
  fun questLinesAlwaysHaveANextStep() {
    // Beyond the hand-written goals the ladder keeps growing.
    val base = listOf(1, 3, 5)
    assertEquals(5, ladderGoal(base, 2))
    assertTrue(ladderGoal(base, 3) > 5)
    assertTrue(ladderGoal(base, 10) > ladderGoal(base, 9))
    // Someone with 1,000 artists still gets an unfinished artist quest.
    val plays = (0 until 1000).map { play(today, 12, 1, "s$it", "a$it") }
    val q = computeGameState(plays, today).quests.first { it.title.startsWith("Discover") && !it.weekly }
    assertTrue(q.goal > 1000)
    assertFalse(q.done)
  }

  @Test
  fun questsOffMeansNoQuestsAndNoQuestXp() {
    val plays = (1..5).map { play(today, 10 + it, 1, "s$it", "a$it") }
    val s = computeGameState(plays, today, questsEnabled = false)
    assertTrue(s.quests.isEmpty())
    assertEquals(5 + 5 * GameRules.XP_NEW_ARTIST, s.totalXp)
  }
}
