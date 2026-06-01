package ui.screens

import LocalWindowSizeClass
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import models.MockData
import models.SideSheetItem
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.tasks
import shared.generated.resources.today
import ui.Theme
import ui.components.MaterialSymbol
import ui.components.TopBar
import ui.layouts.DefaultLayout
import ui.layouts.SpaceLayout
import ui.navigation.MainNavigationSheetContent
import ui.navigation.Screen
import ui.navigation.ScreenLayout
import ui.navigation.layout
import ui.navigation.rememberSpaceNavBackStack
import ui.navigation.spaceIdOrNull

@Composable
fun MainScreen() {
  val backStack = rememberSpaceNavBackStack(Screen.Home)

  val windowSizeClass = LocalWindowSizeClass.current
  val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
  val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
  val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val sideSheetItems =
      listOf(
          SideSheetItem(
              icon = {
                MaterialSymbol(
                    "calendar_today",
                    contentDescription = stringResource(Res.string.today),
                )
              },
              label = { Text(stringResource(Res.string.today)) },
          ),
          SideSheetItem(
              icon = {
                MaterialSymbol(
                    "pinboard",
                    contentDescription = stringResource(Res.string.tasks),
                )
              },
              label = { Text(stringResource(Res.string.tasks)) },
          ),
      )

  val navigationContent =
      @Composable {
        MainNavigationSheetContent(
            currentScreen = backStack.last(),
            onScreenSelected = { screen ->
              if (backStack.last() != screen) {
                backStack.add(screen)
              }
              if (!isExpanded) {
                scope.launch { drawerState.close() }
              }
            },
        )
      }

  val screenContent =
      @Composable { paddingValues: PaddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          Card(
              modifier =
                  Modifier.weight(1.0F)
                      .then(
                          if (isCompact) {
                            Modifier.padding(0.dp)
                          } else {
                            Modifier.padding(
                                top = 12.dp,
                                bottom = 12.dp,
                                start = 12.dp,
                                end = 0.dp,
                            )
                          }
                      )
                      .fillMaxSize(),
              colors = CardDefaults.outlinedCardColors(),
              border = if (isCompact) null else CardDefaults.outlinedCardBorder(),
          ) {
            SharedTransitionLayout {
              NavDisplay(
                  backStack = backStack,
                  sharedTransitionScope = this,
              ) { key ->
                NavEntry(key) { screen ->
                  when (screen.layout()) {
                    ScreenLayout.Default ->
                        when (screen) {
                          Screen.Home ->
                              HomeScreen(isCompact) { selected ->
                                if (backStack.last() != selected) backStack.add(selected)
                              }
                          Screen.Calendar -> CalendarScreen(modifier = Modifier.padding(12.dp))
                          else -> {
                            // Fallback for future screens that map to default layout
                          }
                        }

                    ScreenLayout.Space -> SpaceScreen(screen.spaceIdOrNull() ?: "")
                  }
                }
              }
            }
          }

          if (isMedium || isExpanded) {
            NavigationRail {
              sideSheetItems.forEach { item ->
                NavigationRailItem(
                    icon = item.icon,
                    label = item.label,
                    selected = false,
                    onClick = {},
                )
              }
            }
          }
        }
      }

  val bottomBar: @Composable () -> Unit = {
    if (isCompact) {
      NavigationBar {
        sideSheetItems.forEach { item ->
          NavigationBarItem(
              icon = item.icon,
              label = item.label,
              selected = false,
              onClick = {},
          )
        }
      }
    }
  }

  val defaultTopBar: @Composable () -> Unit = {
    TopBar(
        drawerState = drawerState,
        scope = scope,
        isExpanded = isExpanded,
    )
  }

  val current = backStack.last()
  val spaceForTheme =
      if (current.layout() == ScreenLayout.Space) {
        current.spaceIdOrNull()?.let { id -> MockData.spaces.find { it.id == id } }
      } else null

  // Choose layout and supply appropriate top bar/back handling
  if (spaceForTheme != null) {
    Theme(spaceForTheme.accentColor) {
      SpaceLayout(
          isExpanded = isExpanded,
          drawerState = drawerState,
          scope = scope,
          navigationContent = navigationContent,
          onNavigateUp = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
          bottomBar = bottomBar,
          content = screenContent,
      )
    }
  } else {
    DefaultLayout(
        isExpanded = isExpanded,
        drawerState = drawerState,
        scope = scope,
        navigationContent = navigationContent,
        topBar = defaultTopBar,
        bottomBar = bottomBar,
        content = screenContent,
    )
  }
}
