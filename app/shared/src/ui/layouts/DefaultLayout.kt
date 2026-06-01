package ui.layouts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope

@Composable
fun DefaultLayout(
    isExpanded: Boolean,
    drawerState: DrawerState,
    scope: CoroutineScope,
    navigationContent: @Composable () -> Unit,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
  val scaffold =
      @Composable {
        Scaffold(
            topBar = { topBar() },
            bottomBar = { bottomBar() },
        ) { paddingValues ->
          content(paddingValues)
        }
      }

  if (isExpanded) {
    PermanentNavigationDrawer(
        drawerContent = {
          PermanentDrawerSheet {
            navigationContent()
          }
        }
    ) {
      scaffold()
    }
  } else {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet {
            navigationContent()
          }
        },
    ) {
      scaffold()
    }
  }
}
