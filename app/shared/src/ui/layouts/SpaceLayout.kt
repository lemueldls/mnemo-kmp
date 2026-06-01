package ui.layouts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import ui.components.TopBar

@Composable
fun SpaceLayout(
    isExpanded: Boolean,
    drawerState: DrawerState,
    scope: CoroutineScope,
    navigationContent: @Composable () -> Unit,
    onNavigateUp: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
      topBar = {
        TopBar(
            drawerState = drawerState,
            scope = scope,
            isExpanded = isExpanded,
            onNavigateUp = onNavigateUp,
        )
      },
      bottomBar = { bottomBar() },
  ) { paddingValues ->
    content(paddingValues)
  }
}
