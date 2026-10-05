package echo.music.iad1tya.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import echo.music.iad1tya.R
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ---------------------------------------------------------------------------------------------
// 128bit: palettes
// ---------------------------------------------------------------------------------------------

val PixelPaletteKey = stringPreferencesKey("pixel_palette")

/**
 * Fixed 128bit color palettes. [ECHO_DYNAMIC] hands control back to Echo's original
 * Material You / seed-color theming; [CARTRIDGE] keeps the 128bit backgrounds but takes its
 * accent from the current song's cover art.
 */
enum class PixelPalette(
  val label: String,
  val isDark: Boolean,
  val background: Color,
  val surface: Color,
  val ink: Color,
  val primary: Color,
  val secondary: Color,
  val tertiary: Color,
) {
  CLASSIC("128BIT", true, Color(0xFF0B0B16), Color(0xFF141428), Color(0xFFF4F1FF),
    Color(0xFF2DD4BF), Color(0xFFFF9F1C), Color(0xFFFF5D8F)),
  CARTRIDGE("CARTRIDGE", true, Color(0xFF0B0B16), Color(0xFF141428), Color(0xFFF4F1FF),
    Color(0xFF2DD4BF), Color(0xFFFF9F1C), Color(0xFFFF5D8F)),
  DOT_MATRIX("DOT MATRIX", true, Color(0xFF0F380F), Color(0xFF1E4D1E), Color(0xFFE0F8D0),
    Color(0xFF9BBC0F), Color(0xFF8BAC0F), Color(0xFFC6E35A)),
  POCKET("POCKET", false, Color(0xFFEDEBDD), Color(0xFFDAD6C2), Color(0xFF22201A),
    Color(0xFFA3195B), Color(0xFF3E4A8C), Color(0xFF2E7D4F)),
  VAPOR("VAPORWAVE", true, Color(0xFF1A1033), Color(0xFF2A1B4D), Color(0xFFFFF6FF),
    Color(0xFFFF71CE), Color(0xFF01CDFE), Color(0xFF05FFA1)),
  AMBER("AMBER CRT", true, Color(0xFF120A00), Color(0xFF241600), Color(0xFFFFE3B0),
    Color(0xFFFFB000), Color(0xFFFF7A00), Color(0xFFFFD580)),
  RED_SCREEN("RED SCREEN", true, Color(0xFF000000), Color(0xFF1C0000), Color(0xFFFF9C9C),
    Color(0xFFFF2A2A), Color(0xFFB00000), Color(0xFFFF7A7A)),
  PICO("PICO", true, Color(0xFF1D2B53), Color(0xFF2A3A6E), Color(0xFFFFF1E8),
    Color(0xFFFF004D), Color(0xFFFFA300), Color(0xFF00E436)),
  CONSOLE("CONSOLE", true, Color(0xFF1C1C1C), Color(0xFF2E2E2E), Color(0xFFFCFCFC),
    Color(0xFFE40058), Color(0xFF3CBCFC), Color(0xFFF8B800)),
  ARCTIC("ARCTIC", false, Color(0xFFEAF6FF), Color(0xFFD4E8F7), Color(0xFF0E1A2B),
    Color(0xFF0066CC), Color(0xFF00A0A0), Color(0xFF7A4DFF)),
  ECHO_DYNAMIC("ECHO COLORS", true, Color(0xFF0B0B16), Color(0xFF141428), Color(0xFFF4F1FF),
    DefaultThemeColor, DefaultThemeColor, DefaultThemeColor);

  /** Swatches shown in the palette picker. */
  val swatches: List<Color>
    get() = listOf(background, primary, secondary, tertiary)
}

private fun onColor(c: Color, light: Color, dark: Color): Color =
  if (c.luminance() > 0.4f) dark else light

/** Builds a full Material color scheme from a palette so every stock Echo component picks it up. */
fun pixelColorScheme(palette: PixelPalette, coverAccent: Color? = null): ColorScheme {
  val bg = palette.background
  val surface = palette.surface
  val ink = palette.ink
  val primary =
    if (palette == PixelPalette.CARTRIDGE && coverAccent != null) coverAccent else palette.primary
  val secondary = palette.secondary
  val tertiary = palette.tertiary
  val light = if (palette.isDark) ink else bg
  val dark = if (palette.isDark) bg else ink
  val error = if (palette.isDark) Color(0xFFFF5A5A) else Color(0xFFC62828)

  fun container(c: Color) = lerp(surface, c, 0.32f)

  return if (palette.isDark) {
    darkColorScheme(
      primary = primary,
      onPrimary = onColor(primary, light, dark),
      primaryContainer = container(primary),
      onPrimaryContainer = ink,
      inversePrimary = lerp(primary, bg, 0.4f),
      secondary = secondary,
      onSecondary = onColor(secondary, light, dark),
      secondaryContainer = container(secondary),
      onSecondaryContainer = ink,
      tertiary = tertiary,
      onTertiary = onColor(tertiary, light, dark),
      tertiaryContainer = container(tertiary),
      onTertiaryContainer = ink,
      background = bg,
      onBackground = ink,
      surface = bg,
      onSurface = ink,
      surfaceVariant = lerp(surface, ink, 0.10f),
      onSurfaceVariant = lerp(ink, bg, 0.30f),
      surfaceTint = primary,
      inverseSurface = ink,
      inverseOnSurface = bg,
      error = error,
      onError = onColor(error, light, dark),
      errorContainer = container(error),
      onErrorContainer = ink,
      outline = lerp(surface, ink, 0.35f),
      outlineVariant = lerp(surface, ink, 0.18f),
      scrim = Color.Black,
      surfaceBright = lerp(surface, ink, 0.16f),
      surfaceDim = bg,
      surfaceContainerLowest = lerp(bg, surface, 0.3f),
      surfaceContainerLow = lerp(bg, surface, 0.6f),
      surfaceContainer = surface,
      surfaceContainerHigh = lerp(surface, ink, 0.06f),
      surfaceContainerHighest = lerp(surface, ink, 0.12f),
    )
  } else {
    lightColorScheme(
      primary = primary,
      onPrimary = onColor(primary, light, dark),
      primaryContainer = container(primary),
      onPrimaryContainer = ink,
      inversePrimary = lerp(primary, bg, 0.4f),
      secondary = secondary,
      onSecondary = onColor(secondary, light, dark),
      secondaryContainer = container(secondary),
      onSecondaryContainer = ink,
      tertiary = tertiary,
      onTertiary = onColor(tertiary, light, dark),
      tertiaryContainer = container(tertiary),
      onTertiaryContainer = ink,
      background = bg,
      onBackground = ink,
      surface = bg,
      onSurface = ink,
      surfaceVariant = lerp(surface, ink, 0.06f),
      onSurfaceVariant = lerp(ink, bg, 0.30f),
      surfaceTint = primary,
      inverseSurface = ink,
      inverseOnSurface = bg,
      error = error,
      onError = onColor(error, light, dark),
      errorContainer = container(error),
      onErrorContainer = ink,
      outline = lerp(surface, ink, 0.45f),
      outlineVariant = lerp(surface, ink, 0.22f),
      scrim = Color.Black,
      surfaceBright = bg,
      surfaceDim = lerp(surface, ink, 0.08f),
      surfaceContainerLowest = bg,
      surfaceContainerLow = lerp(bg, surface, 0.5f),
      surfaceContainer = surface,
      surfaceContainerHigh = lerp(surface, ink, 0.05f),
      surfaceContainerHighest = lerp(surface, ink, 0.10f),
    )
  }
}

// ---------------------------------------------------------------------------------------------
// 128bit: type
// ---------------------------------------------------------------------------------------------

/** Chunky display face for big titles. Wide, so it runs at smaller sizes than Echo's fonts. */
val PressStart2PFontFamily = FontFamily(Font(R.font.press_start_2p, FontWeight.Normal))

/** Readable pixel face for everything else: lists, body text, lyrics. */
@OptIn(ExperimentalTextApi::class)
val PixelifySansFontFamily =
  FontFamily(
    listOf(400, 500, 600, 700).map { w ->
      Font(
        resId = R.font.pixelify_sans,
        weight = FontWeight(w),
        variationSettings = FontVariation.Settings(FontVariation.weight(w))
      )
    }
  )

fun pixelTypography(): Typography {
  val base = getTypography(PixelifySansFontFamily, PixelifySansFontFamily)
  fun display(size: Int, line: Int) =
    base.displayLarge.copy(
      fontFamily = PressStart2PFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = size.sp,
      lineHeight = line.sp,
      letterSpacing = 0.sp
    )
  return base.copy(
    displayLarge = display(30, 42),
    displayMedium = display(24, 34),
    displaySmall = display(20, 28),
    headlineLarge = display(18, 26),
    headlineMedium = display(16, 24),
    headlineSmall = display(14, 22),
  )
}

// ---------------------------------------------------------------------------------------------
// 128bit: shapes
// ---------------------------------------------------------------------------------------------

/** Size of one "screen pixel" used to step corners. Set from the real density by the theme. */
object PixelGrid {
  @Volatile var unitPx: Float = 8f
}

/**
 * Drop-in replacement for RoundedCornerShape: same corner sizes, but each corner is drawn as a
 * staircase on a [PixelGrid] so curves read as pixel art. It is a [CornerBasedShape], so code
 * that copies or inspects corner sizes keeps working.
 */
class PixelCornerShape(
  topStart: CornerSize,
  topEnd: CornerSize,
  bottomEnd: CornerSize,
  bottomStart: CornerSize,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

  override fun createOutline(
    size: Size,
    topStart: Float,
    topEnd: Float,
    bottomEnd: Float,
    bottomStart: Float,
    layoutDirection: LayoutDirection,
  ): Outline {
    if (topStart + topEnd + bottomEnd + bottomStart == 0f) {
      return Outline.Rectangle(size.toRect())
    }
    val ltr = layoutDirection == LayoutDirection.Ltr
    val tl = if (ltr) topStart else topEnd
    val tr = if (ltr) topEnd else topStart
    val br = if (ltr) bottomEnd else bottomStart
    val bl = if (ltr) bottomStart else bottomEnd
    val w = size.width
    val h = size.height

    val path = Path()
    var started = false
    fun pt(x: Float, y: Float) {
      if (!started) {
        path.moveTo(x, y)
        started = true
      } else path.lineTo(x, y)
    }

    // Top-left: from the left edge up to the top edge.
    stairs(tl).let { (s, insets) ->
      if (insets.isEmpty()) pt(0f, 0f)
      for (i in insets.indices.reversed()) {
        pt(insets[i], (i + 1) * s)
        pt(insets[i], i * s)
      }
    }
    // Top-right: down from the top edge.
    stairs(tr).let { (s, insets) ->
      if (insets.isEmpty()) pt(w, 0f)
      for (i in insets.indices) {
        pt(w - insets[i], i * s)
        pt(w - insets[i], (i + 1) * s)
      }
    }
    // Bottom-right: down to the bottom edge.
    stairs(br).let { (s, insets) ->
      if (insets.isEmpty()) pt(w, h)
      for (i in insets.indices.reversed()) {
        pt(w - insets[i], h - (i + 1) * s)
        pt(w - insets[i], h - i * s)
      }
    }
    // Bottom-left: back up the left edge.
    stairs(bl).let { (s, insets) ->
      if (insets.isEmpty()) pt(0f, h)
      for (i in insets.indices) {
        pt(insets[i], h - i * s)
        pt(insets[i], h - (i + 1) * s)
      }
    }
    path.close()
    return Outline.Generic(path)
  }

  /**
   * For a corner of radius [r], returns the step height and, per step (0 = outermost row), how
   * far that row is pushed in from the edge. Insets are snapped to whole steps.
   */
  private fun stairs(r: Float): Pair<Float, FloatArray> {
    val unit = max(PixelGrid.unitPx, 1f)
    if (r < unit * 0.6f) return 0f to FloatArray(0)
    val n = max(1, (r / unit).roundToInt())
    val s = r / n
    val insets =
      FloatArray(n) { i ->
        val dy = r - (i + 0.5f) * s
        val raw = r - sqrt(max(0f, r * r - dy * dy))
        (raw / s).roundToInt() * s
      }
    return s to insets
  }

  override fun copy(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize,
  ): PixelCornerShape = PixelCornerShape(topStart, topEnd, bottomEnd, bottomStart)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PixelCornerShape) return false
    return topStart == other.topStart && topEnd == other.topEnd &&
      bottomEnd == other.bottomEnd && bottomStart == other.bottomStart
  }

  override fun hashCode(): Int {
    var result = topStart.hashCode()
    result = 31 * result + topEnd.hashCode()
    result = 31 * result + bottomEnd.hashCode()
    result = 31 * result + bottomStart.hashCode()
    return result
  }

  override fun toString(): String =
    "PixelCornerShape(topStart = $topStart, topEnd = $topEnd, bottomEnd = $bottomEnd, " +
      "bottomStart = $bottomStart)"
}

private fun Size.toRect() = androidx.compose.ui.geometry.Rect(0f, 0f, width, height)

// Same overload set as RoundedCornerShape so call sites swap 1:1.
fun PixelCornerShape(corner: CornerSize) = PixelCornerShape(corner, corner, corner, corner)

fun PixelCornerShape(size: Dp) = PixelCornerShape(CornerSize(size))

fun PixelCornerShape(size: Float) = PixelCornerShape(CornerSize(size))

fun PixelCornerShape(percent: Int) = PixelCornerShape(CornerSize(percent))

fun PixelCornerShape(
  topStart: Dp = 0.dp,
  topEnd: Dp = 0.dp,
  bottomEnd: Dp = 0.dp,
  bottomStart: Dp = 0.dp,
) = PixelCornerShape(CornerSize(topStart), CornerSize(topEnd), CornerSize(bottomEnd), CornerSize(bottomStart))

fun PixelCornerShape(
  topStart: Float = 0.0f,
  topEnd: Float = 0.0f,
  bottomEnd: Float = 0.0f,
  bottomStart: Float = 0.0f,
) = PixelCornerShape(CornerSize(topStart), CornerSize(topEnd), CornerSize(bottomEnd), CornerSize(bottomStart))

fun PixelCornerShape(
  topStartPercent: Int = 0,
  topEndPercent: Int = 0,
  bottomEndPercent: Int = 0,
  bottomStartPercent: Int = 0,
) =
  PixelCornerShape(
    CornerSize(topStartPercent),
    CornerSize(topEndPercent),
    CornerSize(bottomEndPercent),
    CornerSize(bottomStartPercent)
  )

/** Pixel-art circle: a fully stepped 50% corner. Replaces CircleShape. */
val PixelCircleShape = PixelCornerShape(50)

val PixelShapes =
  Shapes(
    extraSmall = PixelCornerShape(4.dp),
    small = PixelCornerShape(8.dp),
    medium = PixelCornerShape(12.dp),
    large = PixelCornerShape(16.dp),
    extraLarge = PixelCornerShape(24.dp),
  )
