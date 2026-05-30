import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

val LocalSettings = compositionLocalOf<MutableState<Settings>> {
    error("No Settings provided")
}

data class Settings(
    val themeMode: ThemeMode = ThemeMode.System,
)

enum class ThemeMode {
    Light, Dark, System
}
