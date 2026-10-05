package echo.music.iad1tya.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import echo.music.iad1tya.db.MusicDatabase
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Live 128bit game state, recomputed whenever the play history changes. Null until loaded. */
@HiltViewModel
class GameViewModel @Inject constructor(database: MusicDatabase) : ViewModel() {
  val state: StateFlow<GameState?> =
    database
      .events()
      .map { events ->
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
        )
      }
      .flowOn(Dispatchers.Default)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Highest level the player has already been told about, so "LEVEL UP!" shows once per level. */
val GameLastLevelKey = androidx.datastore.preferences.core.intPreferencesKey("game_last_level")
