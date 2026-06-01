package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivityGraph(modifier: Modifier = Modifier) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text("Mon", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("Wed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("Fri", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(modifier = Modifier.width(8.dp))
    BoxWithConstraints(modifier = Modifier.weight(1f)) {
      val tileSize = 10.dp
      val spacing = 4.dp
      val columns = ((maxWidth + spacing) / (tileSize + spacing)).toInt()

      Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        repeat(columns) { columnIndex ->
          Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            repeat(5) { rowIndex ->
              val alpha =
                  if (columnIndex > columns / 2) (rowIndex + columnIndex) % 4 * 0.2f + 0.1f
                  else 0.05f

              Box(
                  modifier =
                      Modifier.size(tileSize)
                          .clip(RoundedCornerShape(2.dp))
                          .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
              )
            }
          }
        }
      }
    }
  }
}
