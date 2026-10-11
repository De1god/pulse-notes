package com.example.pulsenotes.ui.search

import com.example.pulsenotes.Note
import com.example.pulsenotes.TextNote
import com.example.pulsenotes.ChecklistNote
import com.example.pulsenotes.VoiceNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

data class SearchFilter(
    val query: String = "",
    val selectedTag: String = "All",
    val showStarredOnly: Boolean = false,
    val selectedColor: String? = null,
    val selectedType: NoteType? = null,
    val hasReminder: Boolean = false,
    val sortBy: SortOption = SortOption.LastInteracted
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() 
            || selectedTag != "All" 
            || showStarredOnly 
            || selectedColor != null 
            || selectedType != null 
            || hasReminder
}

enum class NoteType {
    TEXT, CHECKLIST, VOICE
}

enum class SortOption(val displayName: String) {
    LastInteracted("Last Interacted"),
    Title("Title (A-Z)"),
    TitleDesc("Title (Z-A)"),
    Created("Created (Newest)"),
    CreatedAsc("Created (Oldest)"),
    Reminder("Reminder (Soonest)"),
    Color("Color")
}

fun List<Note>.applyFilter(filter: SearchFilter): List<Note> {
    var filtered = this.filter { it.deletedAt == null }

    // Apply tag filter
    filtered = when (filter.selectedTag) {
        "Recent" -> filtered.sortedByDescending { it.lastInteractedAt }.take(10)
        "Starred" -> filtered.filter { it.isStarred }
        "Voices" -> filtered.filter { it is VoiceNote }
        "Tasks" -> filtered.filter { it is ChecklistNote }
        "All" -> filtered
        else -> filtered.filter { it.customTags.contains(filter.selectedTag) }
    }

    // Apply text search
    if (filter.query.isNotBlank()) {
        val query = filter.query.lowercase()
        filtered = filtered.filter { note ->
            note.title.lowercase().contains(query) ||
            when (note) {
                is TextNote -> note.content.lowercase().contains(query)
                is ChecklistNote -> note.items.any { it.text.lowercase().contains(query) }
                is VoiceNote -> note.subtitle?.lowercase()?.contains(query) == true
                else -> false
            }
        }
    }

    // Apply starred filter
    if (filter.showStarredOnly) {
        filtered = filtered.filter { it.isStarred }
    }

    // Apply color filter
    filter.selectedColor?.let { color ->
        filtered = filtered.filter { it.colorString == color }
    }

    // Apply type filter
    filter.selectedType?.let { type ->
        filtered = when (type) {
            NoteType.TEXT -> filtered.filter { it is TextNote }
            NoteType.CHECKLIST -> filtered.filter { it is ChecklistNote }
            NoteType.VOICE -> filtered.filter { it is VoiceNote }
        }
    }

    // Apply reminder filter
    if (filter.hasReminder) {
        filtered = filtered.filter { it.reminderTime != null }
    }

    // Apply sorting
    filtered = when (filter.sortBy) {
        SortOption.LastInteracted -> filtered.sortedByDescending { it.lastInteractedAt }
        SortOption.Title -> filtered.sortedBy { it.title.lowercase() }
        SortOption.TitleDesc -> filtered.sortedByDescending { it.title.lowercase() }
        SortOption.Created -> filtered.sortedByDescending { it.lastInteractedAt } // Using lastInteracted as proxy
        SortOption.CreatedAsc -> filtered.sortedBy { it.lastInteractedAt }
        SortOption.Reminder -> filtered.sortedBy { it.reminderTime ?: Long.MAX_VALUE }
        SortOption.Color -> filtered.sortedBy { it.colorString }
    }

    return filtered
}