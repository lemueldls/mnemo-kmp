import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ui.Theme
import ui.screens.MainScreen

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App(windowSizeClass: WindowSizeClass) {
    val settings = remember { mutableStateOf(Settings()) }

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass,
        LocalSettings provides settings,
    ) {
        Theme {
            MainScreen()
        }
    }
}
