package com.example.pulsenotes.data

import android.content.Context
import android.net.Uri
import com.example.pulsenotes.Note
import com.example.pulsenotes.TextNote
import com.example.pulsenotes.ChecklistNote
import com.example.pulsenotes.VoiceNote
import com.example.pulsenotes.noteToJson
import com.example.pulsenotes.jsonToNote
import org.json.JSONObject
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat {
    MARKDOWN,
    JSON
}

object ExportImportManager {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    fun exportToClipboard(context: Context, notes: List<Note>, trashNotes: List<Note>, customTags: List<String>) {
        val json = buildJsonBackup(notes, trashNotes, customTags)
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("PulseNotes Backup", json.toString(2))
        clipboardManager.setPrimaryClip(clipData)
    }

    fun restoreFromClipboard(context: Context): Triple<List<Note>, List<Note>, List<String>>? {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = clipboardManager.primaryClip ?: return null
        if (clipData.itemCount == 0) return null
        val text = clipData.getItemAt(0).text?.toString() ?: return null
        return parseJsonBackup(text)
    }

    fun exportToFile(context: Context, uri: Uri, notes: List<Note>, format: ExportFormat): Boolean {
        return try {
            val content = when (format) {
                ExportFormat.MARKDOWN -> buildMarkdownExport(notes)
                ExportFormat.JSON -> buildJsonBackup(notes, emptyList(), emptyList()).toString(2)
            }
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun importFromFile(context: Context, uri: Uri): Triple<List<Note>, List<Note>, List<String>>? {
        return try {
            val content = buildString {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            append(line)
                            append('\n')
                        }
                    }
                }
            }
            parseJsonBackup(content.trim())
        } catch (e: Exception) {
            null
        }
    }

    fun buildJsonBackup(notes: List<Note>, trashNotes: List<Note>, customTags: List<String>): JSONObject {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val notesArray = JSONArray()
        notes.forEach { notesArray.put(noteToJson(it)) }
        root.put("notes", notesArray)

        val trashArray = JSONArray()
        trashNotes.forEach { trashArray.put(noteToJson(it)) }
        root.put("trashNotes", trashArray)

        val tagsArray = JSONArray()
        customTags.forEach { tagsArray.put(it) }
        root.put("customTags", tagsArray)

        return root
    }

    fun parseJsonBackup(jsonText: String): Triple<List<Note>, List<Note>, List<String>>? {
        try {
            val root = JSONObject(jsonText)
            val notes = mutableListOf<Note>()
            val notesArray = root.optJSONArray("notes")
            if (notesArray != null) {
                for (i in 0 until notesArray.length()) {
                    notes.add(jsonToNote(notesArray.getJSONObject(i)))
                }
            }
            val trashNotes = mutableListOf<Note>()
            val trashArray = root.optJSONArray("trashNotes")
            if (trashArray != null) {
                for (i in 0 until trashArray.length()) {
                    trashNotes.add(jsonToNote(trashArray.getJSONObject(i)))
                }
            }
            val customTags = mutableListOf<String>()
            val tagsArray = root.optJSONArray("customTags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    customTags.add(tagsArray.getString(i))
                }
            }
            return Triple(notes, trashNotes, customTags)
        } catch (e: Exception) {
            return null
        }
    }

    fun buildMarkdownExport(notes: List<Note>): String {
        val sb = StringBuilder()
        sb.appendLine("# PulseNotes Export")
        sb.appendLine("> Exported on ${dateFormat.format(Date())}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        val activeNotes = notes.filter { it.deletedAt == null }
        val sortedNotes = activeNotes.sortedByDescending { it.lastInteractedAt }

        sortedNotes.forEachIndexed { index, note ->
            sb.appendLine("## ${note.title.ifBlank { "Untitled" }}")
            sb.appendLine()
            sb.appendLine("- **Type:** ${noteTypeLabel(note)}")
            sb.appendLine("- **Created:** ${dateFormat.format(Date(note.lastInteractedAt))}")
            if (note.isStarred) sb.appendLine("- ⭐ Starred")
            if (note.isPinned) sb.appendLine("- 📌 Pinned")
            if (note.reminderTime != null) sb.appendLine("- ⏰ Reminder: ${dateFormat.format(Date(note.reminderTime!!))}")
            if (note.customTags.isNotEmpty()) sb.appendLine("- **Tags:** ${note.customTags.joinToString(", ")}")
            sb.appendLine()

            when (note) {
                is TextNote -> {
                    if (note.content.isNotBlank()) {
                        sb.appendLine(note.content)
                        sb.appendLine()
                    }
                }
                is ChecklistNote -> {
                    note.items.forEach { item ->
                        val checkbox = if (item.isChecked) "- [x]" else "- [ ]"
                        sb.appendLine("$checkbox ${item.text}")
                    }
                    sb.appendLine()
                }
                is VoiceNote -> {
                    sb.appendLine("🎤 Voice Recording")
                    if (!note.subtitle.isNullOrBlank()) {
                        sb.appendLine("> ${note.subtitle}")
                    }
                    sb.appendLine()
                }
            }

            if (index < sortedNotes.size - 1) {
                sb.appendLine("---")
                sb.appendLine()
            }
        }

        if (sortedNotes.isEmpty()) {
            sb.appendLine("*No notes to export.*")
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun noteTypeLabel(note: Note): String = when (note) {
        is TextNote -> if (note.imageUri != null) "Text Note (with image)" else "Text Note"
        is ChecklistNote -> "Checklist"
        is VoiceNote -> "Voice Note"
    }
}