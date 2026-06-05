import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import shared.generated.resources.Res
import shared.generated.resources.app_icon

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun main() = application {
  Window(
      onCloseRequest = ::exitApplication,
      title = "Mnemo",
      icon = painterResource(Res.drawable.app_icon),
  ) {
    val workspaceRoot = getWorkspaceRoot()
    val windowSizeClass = calculateWindowSizeClass()

    App(workspaceRoot, windowSizeClass)
  }
}
