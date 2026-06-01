package ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
sealed interface Screen : NavKey {
  @Serializable data object Home : Screen

  @Serializable data object Calendar : Screen

  @Serializable data class SpaceDetail(val id: String) : Screen
}

/** Layout categories group screens by how they should be presented by the host. */
enum class ScreenLayout {
  Default, // regular app pages (home, calendar, etc.)
  Space, // full-page space detail (themes the whole route)
}

/** Return the layout category for a screen. */
fun Screen.layout(): ScreenLayout =
    when (this) {
      is Screen.SpaceDetail -> ScreenLayout.Space
      else -> ScreenLayout.Default
    }

/** Helper to extract the space id when applicable. */
fun Screen.spaceIdOrNull(): String? = (this as? Screen.SpaceDetail)?.id

@Composable
fun rememberSpaceNavBackStack(vararg elements: Screen): NavBackStack<Screen> {
  return rememberSerializable(serializer = serializer()) {
    NavBackStack(*elements)
  }
}
