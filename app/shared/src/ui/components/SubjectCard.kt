package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import models.Subject
import ui.Theme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.more_options

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectCardsRow(subjects: List<Subject>, isCompact: Boolean) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        subjects.forEach { subject ->
            SubjectCard(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(min = 288.dp),
                subject = subject
            )
        }
    }
}

@Composable
fun SubjectCard(modifier: Modifier = Modifier, subject: Subject) {
    Theme(subject.accentColor) {
        Card(
            onClick = {},
            modifier = modifier.height(100.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MaterialSymbol(
                        name = subject.icon,
                        contentDescription = subject.title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                        MaterialSymbol(
                            name = "more_vert",
                            contentDescription = stringResource(Res.string.more_options),
                            size = 20.dp,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = subject.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            }
        }
    }
}
