package models

import kotlinx.serialization.Serializable

@Serializable
data class IconMetadata(
    val name: String,
    val version: Int,
    val popularity: Int,
    val codepoint: Int,
    val categories: List<String>,
    val tags: List<String> = emptyList(),
    val unsupported_families: List<String> = emptyList()
)

@Serializable
data class MaterialIconsMetadata(
    val host: String,
    val asset_url_pattern: String,
    val families: List<String>,
    val icons: List<IconMetadata>
)
