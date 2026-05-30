package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.MockData
import ui.components.DashboardHeader
import ui.components.ReviewSection
import ui.components.SubjectCardsRow
import ui.components.TasksSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(isCompact: Boolean) {
    Column(
        modifier = Modifier
            .padding(
                horizontal = 12.dp,
            )
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        DashboardHeader(isCompact = isCompact)
        Spacer(modifier = Modifier.height(12.dp))
        SubjectCardsRow(subjects = MockData.subjects, isCompact = isCompact)
        Spacer(modifier = Modifier.height(12.dp))
        ReviewSection(reviews = MockData.reviews, isCompact = isCompact)
        Spacer(modifier = Modifier.height(12.dp))
        TasksSection(tasks = MockData.tasks, isCompact = isCompact)
        Spacer(modifier = Modifier.height(12.dp))
    }
}
