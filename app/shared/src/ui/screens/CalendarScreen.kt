package ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.calendar

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
  ) {
    Text(
        text = stringResource(Res.string.calendar),
        style = MaterialTheme.typography.headlineLarge,
    )
  }
}
