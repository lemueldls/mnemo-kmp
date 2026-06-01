package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.MockData
import ui.components.MaterialSymbol

@Composable
fun SpaceScreen(id: String) {
  val space = MockData.spaces.find { it.id == id }
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Top,
  ) {
    if (space != null) {
      MaterialSymbol(name = space.icon, contentDescription = space.title)
      Text(text = space.title, style = MaterialTheme.typography.headlineSmall)
    } else {
      Text(text = id, style = MaterialTheme.typography.headlineSmall)
    }
  }
}
