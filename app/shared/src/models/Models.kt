package models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

data class Subject(
    val title: String,
    val icon: String,
    val accentColor: Color
)

data class ReviewCardData(
    val date: String,
    val title: String,
    val content: String,
    val icon: String,
    val accentColor: Color
)

data class TaskCardData(
    val content: String,
    val backgroundColor: Color
)

data class SideSheetItem(
    val icon: @Composable () -> Unit,
    val label: @Composable (() -> Unit)? = null,
)
