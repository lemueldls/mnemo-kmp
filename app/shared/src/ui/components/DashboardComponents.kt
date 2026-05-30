package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shared.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.settings
import shared.generated.resources.storage
import shared.generated.resources.tasks
import shared.generated.resources.today
import ui.components.ActivityGraph


@Composable
fun DashboardHeader(isCompact: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        ActivityGraph(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun RightSidebar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            IconButton(onClick = {}) {
                MaterialSymbol(
                    name = "settings",
                    contentDescription = stringResource(Res.string.settings),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            RightSidebarIcon(
                icon = "calendar",
                label = stringResource(Res.string.today),
                contentDescription = stringResource(Res.string.today)
            )
            Spacer(modifier = Modifier.height(24.dp))
            RightSidebarIcon(
                icon = "tasks",
                label = stringResource(Res.string.tasks),
                contentDescription = stringResource(Res.string.tasks)
            )
            Spacer(modifier = Modifier.height(24.dp))
            RightSidebarIcon(
                icon = "chart",
                label = stringResource(Res.string.storage),
                contentDescription = stringResource(Res.string.storage)
            )
        }
    }
}

@Composable
fun RightSidebarIcon(icon: String, label: String, contentDescription: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {}) {
            MaterialSymbol(
                name = icon,
                contentDescription = contentDescription,
//                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
