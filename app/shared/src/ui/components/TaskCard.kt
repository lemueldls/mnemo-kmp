package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shared.generated.resources.Res
import models.TaskCardData
import ui.Theme
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.tasks

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksSection(tasks: List<TaskCardData>, isCompact: Boolean) {
    Column {
        Text(
            text = stringResource(Res.string.tasks),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            tasks.forEachIndexed { index, task ->
                TaskCard(
                    modifier = Modifier.then(
                        if (isCompact) Modifier.fillMaxWidth() else Modifier.weight(
                            if (index == 1) 1.2f else 1f,
                            fill = false
                        )
                    ),
                    task = task
                )
            }
        }
    }
}

@Composable
fun TaskCard(modifier: Modifier = Modifier, task: TaskCardData) {
    Theme(task.backgroundColor) {
        Card(
            modifier = modifier.height(300.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text(
                    text = task.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
