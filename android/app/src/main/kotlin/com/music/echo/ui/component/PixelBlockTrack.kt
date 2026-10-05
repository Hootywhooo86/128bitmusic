package echo.music.iad1tya.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.max

/**
 * 128bit seek bar track: a row of square cells that fill one at a time. The cell under the
 * playhead is drawn in [headColor] so the position reads at a glance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelBlockTrack(
  sliderState: SliderState,
  activeColor: Color,
  inactiveColor: Color = activeColor.copy(alpha = 0.22f),
  headColor: Color = activeColor,
  height: Dp = 12.dp,
  cellWidth: Dp = 8.dp,
  gap: Dp = 2.dp,
  modifier: Modifier = Modifier,
) {
  val range = sliderState.valueRange
  val span = range.endInclusive - range.start
  val fraction = if (span <= 0f) 0f else ((sliderState.value - range.start) / span).coerceIn(0f, 1f)

  Canvas(modifier.fillMaxWidth().height(height)) {
    val gapPx = gap.toPx()
    val target = cellWidth.toPx() + gapPx
    val count = max(4, floor((size.width + gapPx) / target).toInt())
    val cell = (size.width - gapPx * (count - 1)) / count
    val filled = fraction * count
    val head = floor(filled).toInt().coerceAtMost(count - 1)
    for (i in 0 until count) {
      val color =
        when {
          i < head -> activeColor
          i == head && fraction > 0f -> headColor
          else -> inactiveColor
        }
      drawRect(color, topLeft = Offset(i * (cell + gapPx), 0f), size = Size(cell, size.height))
    }
  }
}
