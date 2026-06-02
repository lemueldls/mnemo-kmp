package widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProviders
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import ui.Eigengrau
import world.mnemo.app.MainActivity

class ActivityGraphWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      Widget()
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    provideContent {
      Widget()
    }
  }

  @Composable
  private fun Widget() {
    val lightColorSchene =
        rememberDynamicColorScheme(
            seedColor = Eigengrau,
            isDark = false,
            isAmoled = true,
            style = PaletteStyle.Expressive,
        )
    val darkColorSchene =
        rememberDynamicColorScheme(
            seedColor = Eigengrau,
            isDark = true,
            isAmoled = true,
            style = PaletteStyle.Expressive,
        )

    val colors = ColorProviders(light = lightColorSchene, dark = darkColorSchene)

    GlanceTheme(colors) {
      Column(
          modifier = GlanceModifier.fillMaxSize(),
          verticalAlignment = Alignment.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(text = "Where to?", modifier = GlanceModifier.padding(12.dp))
        Row(horizontalAlignment = Alignment.CenterHorizontally) {
          Button(
              text = "Home",
              onClick = actionStartActivity<MainActivity>(),
          )
          Button(
              text = "Work",
              onClick = actionStartActivity<MainActivity>(),
          )
        }
      }
    }
  }
}

class ActivityGraphWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = ActivityGraphWidget()
}
