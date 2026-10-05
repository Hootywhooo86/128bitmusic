package echo.music.iad1tya.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware

/**
 * 128bit player background: the song's cover decoded at [pixels]×[pixels] and scaled up with
 * nearest-neighbour filtering, so it turns into big chunky pixels. Dimmed and given faint
 * scanlines so text on top stays readable.
 */
@Composable
fun PixelCoverBackground(
  thumbnailUrl: String,
  modifier: Modifier = Modifier,
  pixels: Int = 24,
  dim: Float = 0.55f,
) {
  val context = LocalContext.current
  Box(modifier.fillMaxSize()) {
    AsyncImage(
      model =
        ImageRequest.Builder(context)
          .data(thumbnailUrl)
          .size(pixels, pixels)
          // Own cache entry, so Coil doesn't hand back a sharp full-size copy from memory.
          .memoryCacheKey("128bit-pixel-$pixels:$thumbnailUrl")
          .allowHardware(false)
          .build(),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      filterQuality = FilterQuality.None,
      modifier = Modifier.fillMaxSize()
    )
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
    Canvas(Modifier.fillMaxSize()) {
      val step = 3.dp.toPx()
      val line = 1.dp.toPx()
      var y = 0f
      while (y < size.height) {
        drawRect(Color.Black.copy(alpha = 0.18f), Offset(0f, y), Size(size.width, line))
        y += step
      }
    }
  }
}
