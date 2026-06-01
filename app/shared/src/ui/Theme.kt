package ui

import LocalSettings
import Settings
import ThemeMode
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle

val Eigengrau = Color(0xFF16161D)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Theme(
    seedColor: Color = Eigengrau,
    settings: Settings = LocalSettings.current.value,
    content: @Composable () -> Unit,
) {
  val darkTheme =
      when (settings.themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
      }

  DynamicMaterialExpressiveTheme(
      seedColor = seedColor,
      isDark = darkTheme,
      isAmoled = true,
      animate = true,
      style = PaletteStyle.Expressive,
      content = content,
  )
}
