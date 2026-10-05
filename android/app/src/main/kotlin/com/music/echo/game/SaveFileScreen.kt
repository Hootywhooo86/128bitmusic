package echo.music.iad1tya.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.theme.PixelCornerShape
import kotlin.math.floor

/** 128bit "Save File": level, class, streak, quests, badges and top tracks. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveFileScreen(navController: NavController, viewModel: GameViewModel = hiltViewModel()) {
  val loaded by viewModel.state.collectAsState()
  val s = loaded ?: GameState.Empty

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("SAVE FILE", style = MaterialTheme.typography.headlineSmall) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { inner ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(inner),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item { Hero(s) }
      item { StreakPanel(s) }
      item { QuestsPanel(s) }
      item { BadgesPanel(s) }
      item { StatsPanel(s) }
      if (s.topTracks.isNotEmpty()) item { InventoryPanel(s) }
    }
  }
}

@Composable
private fun Panel(title: String, content: @Composable () -> Unit) {
  val shape = PixelCornerShape(8.dp)
  Column(
    Modifier.fillMaxWidth()
      .clip(shape)
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .border(2.dp, MaterialTheme.colorScheme.outlineVariant, shape)
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
    content()
  }
}

/** Row of square cells filled up to [fraction]. */
@Composable
fun PixelBlocks(fraction: Float, color: Color, height: Dp = 12.dp, cells: Int = 20) {
  val empty = color.copy(alpha = 0.2f)
  Canvas(Modifier.fillMaxWidth().height(height)) {
    val gap = 2.dp.toPx()
    val w = (size.width - gap * (cells - 1)) / cells
    val filled = floor(fraction.coerceIn(0f, 1f) * cells).toInt()
    for (i in 0 until cells) {
      drawRect(
        if (i < filled) color else empty,
        topLeft = Offset(i * (w + gap), 0f),
        size = androidx.compose.ui.geometry.Size(w, size.height)
      )
    }
  }
}

@Composable
private fun Hero(s: GameState) {
  Panel("PLAYER") {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
      Image(
        painter = painterResource(R.drawable.ic_128bit_logo_foreground),
        contentDescription = null,
        modifier = Modifier.size(84.dp).clip(PixelCornerShape(8.dp)).background(Color.Black)
      )
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("LV ${s.level}", style = MaterialTheme.typography.headlineLarge)
        Text(s.className, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text("%,d XP total".format(s.totalXp), style = MaterialTheme.typography.bodySmall)
      }
    }
    PixelBlocks(s.levelProgress, MaterialTheme.colorScheme.secondary, height = 14.dp)
    Text(
      "%,d / %,d XP to LV %d".format(s.xpIntoLevel, s.xpToNext, s.level + 1),
      style = MaterialTheme.typography.labelMedium
    )
  }
}

@Composable
private fun StreakPanel(s: GameState) {
  Panel("STREAK") {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Icon(
        Icons.Rounded.LocalFireDepartment,
        contentDescription = null,
        tint = if (s.currentStreak > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
        modifier = Modifier.size(40.dp)
      )
      Column {
        Text(
          if (s.currentStreak == 1) "1 DAY" else "${s.currentStreak} DAYS",
          style = MaterialTheme.typography.headlineMedium
        )
        Text(
          "Best: ${s.bestStreak} days · listen ${GameRules.STREAK_DAY_MINUTES}+ min a day to keep it going",
          style = MaterialTheme.typography.bodySmall
        )
      }
    }
  }
}

@Composable
private fun QuestsPanel(s: GameState) {
  Panel("WEEKLY QUESTS") {
    s.quests.forEach { q ->
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (q.done) {
            Icon(
              Icons.Rounded.CheckCircle,
              contentDescription = "Done",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(6.dp))
          }
          Text(q.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
          Text(
            "${q.progress}/${q.goal} · +${q.rewardXp} XP",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
          )
        }
        PixelBlocks(q.progress.toFloat() / q.goal, MaterialTheme.colorScheme.primary, height = 8.dp, cells = q.goal * 3)
      }
    }
    Text("New quests every Monday.", style = MaterialTheme.typography.bodySmall)
  }
}

private fun badgeIcon(id: String): ImageVector =
  when (id) {
    "first" -> Icons.Rounded.Album
    "night" -> Icons.Rounded.Bedtime
    "early" -> Icons.Rounded.WbTwilight
    "crate" -> Icons.Rounded.TravelExplore
    "library" -> Icons.Rounded.LibraryMusic
    "marathon" -> Icons.Rounded.DirectionsRun
    "loyal" -> Icons.Rounded.Favorite
    "fire" -> Icons.Rounded.LocalFireDepartment
    "unstoppable" -> Icons.Rounded.Whatshot
    else -> Icons.Rounded.MilitaryTech
  }

@Composable
private fun BadgesPanel(s: GameState) {
  Panel("BADGES · ${s.badges.count { it.unlocked }} OF ${s.badges.size}") {
    s.badges.chunked(2).forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        row.forEach { b ->
          val shape = PixelCornerShape(6.dp)
          val accent = if (b.unlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
          Row(
            Modifier.weight(1f).clip(shape).border(2.dp, accent, shape).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              if (b.unlocked) badgeIcon(b.id) else Icons.Rounded.Lock,
              contentDescription = null,
              tint = accent,
              modifier = Modifier.size(24.dp)
            )
            Column {
              Text(
                b.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (b.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                b.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )
            }
          }
        }
        if (row.size == 1) Box(Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun StatsPanel(s: GameState) {
  Panel("STATS") {
    StatRow("Time listened", "%.1f h".format(s.totalMinutes / 60f))
    StatRow("Songs played", "%,d".format(s.distinctSongs))
    StatRow("Artists found", "%,d".format(s.distinctArtists))
  }
}

@Composable
private fun StatRow(label: String, value: String) {
  Row(Modifier.fillMaxWidth()) {
    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
  }
}

@Composable
private fun InventoryPanel(s: GameState) {
  Panel("INVENTORY · TOP TRACKS") {
    s.topTracks.forEachIndexed { i, t ->
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          "%02d".format(i + 1),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.secondary
        )
        Column(Modifier.weight(1f)) {
          Text(t.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Text(
            t.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Text("${t.plays} plays", style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}
