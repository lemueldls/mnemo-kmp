package ui.components

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import LocalSettings
import ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    drawerState: DrawerState,
    scope: CoroutineScope,
    isExpanded: Boolean = false,
) {
    val settings = LocalSettings.current

    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            if (!isExpanded) {
                IconButton(onClick = {
                    scope.launch {
                        if (drawerState.isClosed) {
                            drawerState.open()
                        } else {
                            drawerState.close()
                        }
                    }
                }) {
                    MaterialSymbol(name="menu", contentDescription = "Menu")
                }
            }
        },
        actions = {
            IconButton(onClick = {
                val currentMode = settings.value.themeMode
                val newMode = when (currentMode) {
                    ThemeMode.Light -> ThemeMode.Dark
                    ThemeMode.Dark -> ThemeMode.System
                    ThemeMode.System -> ThemeMode.Light
                }
                settings.value = settings.value.copy(themeMode = newMode)
            }) {
                val icon = when (settings.value.themeMode) {
                    ThemeMode.Light -> "dark_mode"
                    ThemeMode.Dark -> "contrast"
                    ThemeMode.System -> "light_mode"
                }

                MaterialSymbol(icon, contentDescription = "Toggle Theme")
            }
            IconButton(onClick = {}) {
                MaterialSymbol(
                    "settings",
                    contentDescription = stringResource(Res.string.settings)
                )
            }
        },
    )
}
