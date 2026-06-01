package ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Font
import shared.generated.resources.MaterialSymbolsOutlined
import shared.generated.resources.Res

@Composable
fun MaterialSymbol(
    name: String,
    contentDescription: String? = name,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
) {
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  val iconFontFamily = FontFamily(Font(Res.font.MaterialSymbolsOutlined))

  val fontSizeSp =
      remember(size, density) {
        with(density) { size.toSp() }
      }

  val iconPainter =
      remember(name, textMeasurer, iconFontFamily) {
        FontIconPainter(
            iconText = name,
            textMeasurer = textMeasurer,
            fontFamily = iconFontFamily,
            fontSize = fontSizeSp,
        )
      }

  Icon(
      painter = iconPainter,
      contentDescription = contentDescription,
      modifier = Modifier.size(size),
      tint = tint,
  )
}

class FontIconPainter(
    private val iconText: String,
    private val textMeasurer: TextMeasurer,
    private val fontFamily: FontFamily,
    private val fontSize: TextUnit,
) : Painter() {

  override val intrinsicSize: androidx.compose.ui.geometry.Size
    get() = androidx.compose.ui.geometry.Size.Unspecified

  override fun DrawScope.onDraw() {
    val style =
        TextStyle(
            fontFamily = fontFamily,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
        )

    val textLayoutResult =
        textMeasurer.measure(
            text = iconText,
            style = style,
            constraints =
                Constraints(
                    maxWidth = size.width.toInt(),
                    maxHeight = size.height.toInt(),
                ),
        )

    val x = (size.width - textLayoutResult.size.width) / 2f
    val y = (size.height - textLayoutResult.size.height) / 2f

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x, y),
    )
  }
}
