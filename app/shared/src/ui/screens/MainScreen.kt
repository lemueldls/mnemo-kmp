package ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import LocalWindowSizeClass
import kotlinx.coroutines.launch
import models.SideSheetItem
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.tasks
import shared.generated.resources.today
import ui.components.MaterialSymbol
import ui.components.TopBar
import ui.navigation.MainNavigationSheetContent
import ui.navigation.Screen
import ui.navigation.navConfig

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(navConfig, Screen.Home)

    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sideSheetItems = listOf(
        SideSheetItem(
            icon = {
                MaterialSymbol(
                    "calendar_today",
                    contentDescription = stringResource(Res.string.today)
                )
            },
            label = { Text(stringResource(Res.string.today)) },
        ),
        SideSheetItem(
            icon = {
                MaterialSymbol(
                    "pinboard",
                    contentDescription = stringResource(Res.string.tasks)
                )
            },
            label = { Text(stringResource(Res.string.tasks)) },
        ),
    )

    val navigationContent = @Composable {
        MainNavigationSheetContent(
            currentScreen = backStack.last() as Screen,
            onScreenSelected = { screen ->
                if (backStack.last() != screen) {
                    backStack.add(screen)
                }
                if (!isExpanded) {
                    scope.launch { drawerState.close() }
                }
            }
        )
    }

    val screenContent = @Composable { paddingValues: PaddingValues ->
        Row(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.0F)
                    .then(
                        if (isCompact) {
                            Modifier.padding(0.dp)
                        } else {
                            Modifier.padding(
                                top = 12.dp,
                                bottom = 12.dp,
                                start = 12.dp,
                                end = 0.dp
                            )
                        }
                    )
                    .fillMaxSize(),
                colors = CardDefaults.outlinedCardColors(),
                border = if (isCompact) null else CardDefaults.outlinedCardBorder()
            ) {
                NavDisplay(
                    backStack = backStack,
                ) { key ->
                    NavEntry(key) { screen ->
                        when (screen) {
                            Screen.Home -> HomeScreen(isCompact)
                            Screen.Calendar -> CalendarScreen(modifier = Modifier.padding(12.dp))
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
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    val mainScaffold = @Composable {
        Scaffold(
            topBar = {
                TopBar(
                    drawerState = drawerState,
                    scope = scope,
                    isExpanded = isExpanded,
                )
            },
            bottomBar = {
                if (isCompact) {
                    NavigationBar {
                        sideSheetItems.forEach { item ->
                            NavigationBarItem(
                                icon = item.icon,
                                label = item.label,
                                selected = false,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            screenContent(paddingValues)
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
            mainScaffold()
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
            mainScaffold()
        }
    }
}
