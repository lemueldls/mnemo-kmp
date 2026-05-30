package ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import models.IconMetadata
import models.MaterialIconsMetadata
import org.jetbrains.compose.resources.ExperimentalResourceApi
import shared.generated.resources.Res

class IconSearchProvider(private val scope: CoroutineScope) {
    private var allIcons: List<IconMetadata> = emptyList()
    var filteredIcons by mutableStateOf<List<IconMetadata>>(allIcons)
        private set

    var isLoading by mutableStateOf(true)
        private set

    private val json = Json {
        ignoreUnknownKeys = true
    }

    init {
        scope.launch {
            loadMetadata()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadMetadata() {
        withContext(Dispatchers.Default) {
            try {
                val bytes = Res.readBytes("files/metadata.json")
                val content = bytes.decodeToString()
                val metadata = json.decodeFromString<MaterialIconsMetadata>(content)
                // Filter for Material Symbols Outlined specifically if needed,
                // or just take all unique names that support it
                allIcons = metadata.icons.filter { icon ->
                    icon.unsupported_families.none { it.contains("Symbols Outlined") }
                }.distinctBy { it.name }

                filteredIcons = allIcons
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            filteredIcons = allIcons
            return
        }

        val lowerQuery = query.lowercase()
        filteredIcons = allIcons.filter { icon ->
            icon.name.contains(lowerQuery) ||
            icon.tags.any { it.contains(lowerQuery) } ||
            icon.categories.any { it.lowercase().contains(lowerQuery) }
        }.sortedByDescending { icon ->
            // Simple ranking: exact name match first
            if (icon.name == lowerQuery) 3
            else if (icon.name.startsWith(lowerQuery)) 2
            else 1
        }
    }
}
