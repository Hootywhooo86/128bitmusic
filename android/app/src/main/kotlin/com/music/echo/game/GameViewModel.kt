package echo.music.iad1tya.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import echo.music.iad1tya.db.MusicDatabase
import echo.music.iad1tya.utils.dataStore
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Live 128bit game state, recomputed whenever the play history changes. Null until loaded. */
@HiltViewModel
class GameViewModel
@Inject
constructor(
  database: MusicDatabase,
  @ApplicationContext context: Context,
) : ViewModel() {
  val state: StateFlow<GameState?> =
    combine(database.events(), context.dataStore.data.map { it[QuestsEnabledKey] ?: true }.distinctUntilChanged()) {
        events,
        questsEnabled ->
        computeGameState(
          events.map { e ->
            Play(
              at = e.event.timestamp,
              playTimeMs = e.event.playTime,
              songId = e.song.id,
              songTitle = e.song.song.title,
              artistIds = e.song.artists.map { it.id },
              artistNames = e.song.artists.map { it.name },
            )
          },
          LocalDate.now(),
          questsEnabled,
        )
      }
      .flowOn(Dispatchers.Default)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Highest level the player has already been told about, so "LEVEL UP!" shows once per level. */
val GameLastLevelKey = androidx.datastore.preferences.core.intPreferencesKey("game_last_level")

/** Settings → 128bit → Quests. Off hides quests and stops quest XP. */
val QuestsEnabledKey = androidx.datastore.preferences.core.booleanPreferencesKey("game_quests_enabled")
