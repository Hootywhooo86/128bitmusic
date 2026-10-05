package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.ui.theme.PixelCornerShape
import echo.music.iad1tya.ui.theme.PixelPalette
import echo.music.iad1tya.ui.theme.PixelPaletteKey
import echo.music.iad1tya.ui.theme.PixelifySansFontFamily
import echo.music.iad1tya.ui.theme.PressStart2PFontFamily
import echo.music.iad1tya.utils.rememberPreference

/** Two-column grid of 128bit palettes. Each card previews the palette in its own colors. */
@Composable
fun PixelPalettePicker(modifier: Modifier = Modifier) {
  val (selected, onSelect) = rememberPreference(PixelPaletteKey, PixelPalette.CLASSIC.name)

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    PixelPalette.entries.chunked(2).forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        row.forEach { palette ->
          PaletteCard(
            palette = palette,
            selected = palette.name == selected,
            onClick = { onSelect(palette.name) },
            modifier = Modifier.weight(1f)
          )
        }
        if (row.size == 1) Spacer(Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun PaletteCard(
  palette: PixelPalette,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = PixelCornerShape(8.dp)
  val border = if (selected) MaterialTheme.colorScheme.primary else palette.surface
  Column(
    modifier =
      modifier
        .clip(shape)
        .background(palette.background)
        .border(3.dp, border, shape)
        .clickable(role = Role.RadioButton, onClick = onClick)
        .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      palette.swatches.forEach { c ->
        Box(
          Modifier.size(18.dp)
            .background(c)
            .border(2.dp, palette.surface)
        )
      }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = (if (selected) "▶ " else "") + palette.label,
        color = if (palette == PixelPalette.ECHO_DYNAMIC) palette.ink else palette.primary,
        fontFamily = PressStart2PFontFamily,
        fontSize = 9.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    Text(
      text = palette.blurb(),
      color = palette.ink.copy(alpha = 0.75f),
      fontFamily = PixelifySansFontFamily,
      fontSize = 12.sp,
      lineHeight = 14.sp,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun PixelPalette.blurb(): String =
  when (this) {
    PixelPalette.CLASSIC -> "Teal, orange and pink. The 128bit house colors."
    PixelPalette.CARTRIDGE -> "Accent color comes from the current cover art."
    PixelPalette.DOT_MATRIX -> "Green-screen handheld."
    PixelPalette.POCKET -> "Light grey shell, magenta buttons."
    PixelPalette.VAPOR -> "Hot pink and cyan on deep purple."
    PixelPalette.AMBER -> "Old terminal glow."
    PixelPalette.RED_SCREEN -> "Red on black, nothing else."
    PixelPalette.PICO -> "Fantasy-console navy with bright pops."
    PixelPalette.CONSOLE -> "Grey console with red, blue and gold."
    PixelPalette.ARCTIC -> "Light and icy blue."
    PixelPalette.ECHO_DYNAMIC -> "Echo's original colors. Mode and color picker below apply."
  }
