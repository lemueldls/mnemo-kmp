package ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.monkopedia.kodemirror.commands.defaultKeymap
import com.monkopedia.kodemirror.commands.history
import com.monkopedia.kodemirror.commands.indentWithTab
import com.monkopedia.kodemirror.lang.markdown.markdown
import com.monkopedia.kodemirror.materialtheme.rememberMaterialEditorTheme
import com.monkopedia.kodemirror.search.search
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.keymapOf
import com.monkopedia.kodemirror.view.lineNumbers
import com.monkopedia.kodemirror.view.rememberEditorSession
import models.MockData

@Composable
fun SpaceScreen(id: String) {
  val focusRequester = remember { FocusRequester() }
  val space = MockData.spaces.find { it.id == id }

  val materialTheme = rememberMaterialEditorTheme()
  val session =
      rememberEditorSession(
          doc = "Hello",
          extensions =
              materialTheme +
                  lineNumbers +
                  history() +
                  markdown().extension +
                  search() +
                  keymapOf(defaultKeymap + indentWithTab),
      )

  // focusRequester.requestFocus()

  KodeMirror(
      session = session,
      modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
  )
}
