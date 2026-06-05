import android.content.Context

fun getWorkspaceRoot(context: Context): String {
  return context.filesDir.absolutePath
}
