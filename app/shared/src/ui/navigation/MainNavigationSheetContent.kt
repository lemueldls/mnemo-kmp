package ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import models.MockData
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.add
import shared.generated.resources.calendar
import shared.generated.resources.home
import shared.generated.resources.spaces
import ui.Theme
import ui.components.MaterialSymbol
import ui.components.SemanticIconSearch

@Composable
fun MainNavigationSheetContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
) {
  var showIconSearch by remember { mutableStateOf(false) }

  if (showIconSearch) {
    Dialog(
        onDismissRequest = { showIconSearch = false },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      Surface(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          shape = MaterialTheme.shapes.extraLarge,
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 6.dp,
      ) {
        Column {
          Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = "Select Icon",
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(onClick = { showIconSearch = false }) {
              MaterialSymbol("close", contentDescription = "Close")
            }
          }
          SemanticIconSearch(
              onIconSelected = { icon ->
                // TODO: Implement space creation with selected icon
                showIconSearch = false
              },
              modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }

  Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
    NavigationDrawerItem(
        label = { Text(stringResource(Res.string.home)) },
        selected = currentScreen == Screen.Home,
        onClick = { onScreenSelected(Screen.Home) },
        icon = {
          MaterialSymbol(
              "home",
              contentDescription = stringResource(Res.string.home),
          )
        },
    )
    NavigationDrawerItem(
        label = { Text(stringResource(Res.string.calendar)) },
        selected = currentScreen == Screen.Calendar,
        onClick = { onScreenSelected(Screen.Calendar) },
        icon = {
          MaterialSymbol(
              "date_range",
              contentDescription = stringResource(Res.string.calendar),
          )
        },
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = stringResource(Res.string.spaces),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      IconButton(onClick = { showIconSearch = true }, modifier = Modifier.size(24.dp)) {
        MaterialSymbol(
            "add",
            contentDescription = stringResource(Res.string.add),
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    MockData.spaces.forEach { space ->
      Theme(space.accentColor) {
        NavigationDrawerItem(
            label = { Text(space.title) },
            icon = {
              MaterialSymbol(
                  name = space.icon,
                  contentDescription = space.title,
                  tint = MaterialTheme.colorScheme.primary,
              )
            },
            selected = false,
            onClick = {},
        )
      }
    }
  }
}
