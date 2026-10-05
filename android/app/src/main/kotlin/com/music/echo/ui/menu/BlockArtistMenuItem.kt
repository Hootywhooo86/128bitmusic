package echo.music.iad1tya.ui.menu

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.datastore.preferences.core.edit
import echo.music.iad1tya.constants.BlockedArtistsKey
import echo.music.iad1tya.ui.component.Material3MenuItemData
import echo.music.iad1tya.utils.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 128bit: "Block artist" for song menus. Uses the same blocked-artist list as the Block button on
 * artist pages, so it can be undone under Settings → Content → Blocked artists. Returns null when
 * the artist has no YouTube ID to block by.
 */
fun blockArtistMenuItem(
  context: Context,
  artistId: String?,
  artistName: String,
  onBlocked: () -> Unit = {},
): Material3MenuItemData? {
  if (artistId.isNullOrBlank()) return null
  return Material3MenuItemData(
    title = { Text(text = "Block artist") },
    description = { Text(text = artistName) },
    icon = { Icon(imageVector = Icons.Default.Block, contentDescription = null) },
    onClick = {
      CoroutineScope(Dispatchers.IO).launch {
        context.dataStore.edit { prefs ->
          val current = prefs[BlockedArtistsKey] ?: emptySet()
          if (current.none { it == artistId || it.startsWith("$artistId||") }) {
            prefs[BlockedArtistsKey] = current + "$artistId||$artistName"
          }
        }
      }
      Toast.makeText(
          context,
          "Blocked $artistName. Undo in Settings → Content → Blocked artists.",
          Toast.LENGTH_LONG
        )
        .show()
      onBlocked()
    }
  )
}
