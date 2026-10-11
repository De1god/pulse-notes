package com.example.pulsenotes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.pulsenotes.theme.PulseNotesTheme
import com.example.pulsenotes.ui.search.SearchFilter
import com.example.pulsenotes.ui.search.NoteType
import com.example.pulsenotes.ui.search.SortOption
import com.example.pulsenotes.ui.search.applyFilter
import com.example.pulsenotes.ui.richtext.RichTextEditor
import com.example.pulsenotes.ui.richtext.buildRichTextString
import java.io.File
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.json.JSONObject
import org.json.JSONArray
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.stringResource
import android.app.LocaleManager
import android.os.LocaleList
import java.util.Locale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.outlined.FlipCameraAndroid

sealed interface Note {
    val id: String
    val title: String
    val colorString: String
    val isStarred: Boolean
    val isPinned: Boolean
    val isArchived: Boolean
    val customTags: Set<String>
    val lastInteractedAt: Long
    val reminderTime: Long?
    val deletedAt: Long?
}

data class TextNote(
    override val id: String,
    override val title: String,
    val content: String,
    override val colorString: String,
    override val isStarred: Boolean = false,
    override val isPinned: Boolean = false,
    override val isArchived: Boolean = false,
    override val customTags: Set<String> = emptySet(),
    override val lastInteractedAt: Long = System.currentTimeMillis(),
    override val reminderTime: Long? = null,
    override val deletedAt: Long? = null,
    val imageUri: String? = null,
    val richTextSpans: List<com.example.pulsenotes.data.richtext.RichTextSpan> = emptyList()
) : Note

data class ChecklistNote(
    override val id: String,
    override val title: String,
    val items: List<ChecklistItem>,
    override val colorString: String,
    override val isStarred: Boolean = false,
    override val isPinned: Boolean = false,
    override val isArchived: Boolean = false,
    override val customTags: Set<String> = emptySet(),
    override val lastInteractedAt: Long = System.currentTimeMillis(),
    override val reminderTime: Long? = null,
    override val deletedAt: Long? = null
) : Note

data class ChecklistItem(val text: String, val isChecked: Boolean)

data class VoiceNote(
    override val id: String,
    override val title: String,
    val subtitle: String? = null,
    val showLargeWaveform: Boolean = false,
    override val colorString: String,
    override val isStarred: Boolean = false,
    override val isPinned: Boolean = false,
    override val isArchived: Boolean = false,
    override val customTags: Set<String> = emptySet(),
    val audioFilePath: String? = null,
    override val lastInteractedAt: Long = System.currentTimeMillis(),
    override val reminderTime: Long? = null,
    override val deletedAt: Long? = null
) : Note

fun Note.copyWith(
    title: String? = null,
    isStarred: Boolean? = null,
    isPinned: Boolean? = null,
    isArchived: Boolean? = null,
    colorString: String? = null,
    customTags: Set<String>? = null,
    audioFilePath: String? = null,
    lastInteractedAt: Long? = null,
    reminderTime: Long? = null,
    clearReminder: Boolean = false,
    deletedAt: Long? = null,
    imageUri: String? = null
): Note {
    val newTitle = title ?: this.title
    val newStarred = isStarred ?: this.isStarred
    val newPinned = isPinned ?: this.isPinned
    val newArchived = isArchived ?: this.isArchived
    val newColor = colorString ?: this.colorString
    val newCustomTags = customTags ?: this.customTags
    val newInteracted = lastInteractedAt ?: this.lastInteractedAt
    val newReminder = if (clearReminder) null else (reminderTime ?: this.reminderTime)
    val newDeletedAt = deletedAt ?: this.deletedAt
    return when (this) {
        is TextNote -> this.copy(title = newTitle, isStarred = newStarred, isPinned = newPinned, isArchived = newArchived, colorString = newColor, customTags = newCustomTags, lastInteractedAt = newInteracted, reminderTime = newReminder, deletedAt = newDeletedAt, imageUri = imageUri ?: this.imageUri)
        is ChecklistNote -> this.copy(title = newTitle, isStarred = newStarred, isPinned = newPinned, isArchived = newArchived, colorString = newColor, customTags = newCustomTags, lastInteractedAt = newInteracted, reminderTime = newReminder, deletedAt = newDeletedAt)
        is VoiceNote -> this.copy(title = newTitle, isStarred = newStarred, isPinned = newPinned, isArchived = newArchived, colorString = newColor, customTags = newCustomTags, audioFilePath = audioFilePath ?: this.audioFilePath, lastInteractedAt = newInteracted, reminderTime = newReminder, deletedAt = newDeletedAt)
    }
}

fun Note.copyWithTextContent(content: String): Note {
    return if (this is TextNote) this.copy(content = content, lastInteractedAt = System.currentTimeMillis()) else this
}

fun Note.copyWithRichText(content: String, richTextSpans: List<com.example.pulsenotes.data.richtext.RichTextSpan>): Note {
    return if (this is TextNote) this.copy(
        content = content,
        richTextSpans = richTextSpans,
        lastInteractedAt = System.currentTimeMillis()
    ) else this
}

data class Tag(val name: String, val count: Int? = null, val hasStar: Boolean = false, val isCustom: Boolean = false)

fun noteToJson(note: Note): JSONObject {
    val json = JSONObject()
    json.put("id", note.id)
    json.put("title", note.title)
    json.put("colorString", note.colorString)
    json.put("isStarred", note.isStarred)
    json.put("isPinned", note.isPinned)
    json.put("isArchived", note.isArchived)
    
    val tagsArray = JSONArray()
    note.customTags.forEach { tagsArray.put(it) }
    json.put("customTags", tagsArray)
    json.put("lastInteractedAt", note.lastInteractedAt)
    if (note.reminderTime != null) {
        json.put("reminderTime", note.reminderTime)
    } else {
        json.put("reminderTime", JSONObject.NULL)
    }
    if (note.deletedAt != null) {
        json.put("deletedAt", note.deletedAt)
    } else {
        json.put("deletedAt", JSONObject.NULL)
    }
    
    when (note) {
        is TextNote -> {
            json.put("type", "text")
            json.put("content", note.content)
            json.put("imageUri", note.imageUri ?: JSONObject.NULL)
            // Serialize rich text spans
            val spansArray = JSONArray()
            note.richTextSpans.forEach { span ->
                val spanObj = JSONObject()
                spanObj.put("start", span.start)
                spanObj.put("end", span.end)
                spanObj.put("formatType", when (span.format) {
                    is com.example.pulsenotes.data.richtext.TextFormat.Bold -> "bold"
                    is com.example.pulsenotes.data.richtext.TextFormat.Italic -> "italic"
                    is com.example.pulsenotes.data.richtext.TextFormat.Underline -> "underline"
                    is com.example.pulsenotes.data.richtext.TextFormat.Strikethrough -> "strikethrough"
                    is com.example.pulsenotes.data.richtext.TextFormat.Heading1 -> "heading1"
                    is com.example.pulsenotes.data.richtext.TextFormat.Heading2 -> "heading2"
                    is com.example.pulsenotes.data.richtext.TextFormat.BulletPoint -> "bullet"
                    is com.example.pulsenotes.data.richtext.TextFormat.Code -> "code"
                })
                spansArray.put(spanObj)
            }
            json.put("richTextSpans", spansArray)
        }
        is ChecklistNote -> {
            json.put("type", "checklist")
            val itemsArray = JSONArray()
            note.items.forEach { item ->
                val itemObj = JSONObject()
                itemObj.put("text", item.text)
                itemObj.put("isChecked", item.isChecked)
                itemsArray.put(itemObj)
            }
            json.put("items", itemsArray)
        }
        is VoiceNote -> {
            json.put("type", "voice")
            json.put("subtitle", note.subtitle ?: JSONObject.NULL)
            json.put("showLargeWaveform", note.showLargeWaveform)
            json.put("audioFilePath", note.audioFilePath ?: JSONObject.NULL)
        }
    }
    return json
}

fun jsonToNote(json: JSONObject): Note {
    val id = json.getString("id")
    val title = json.getString("title")
    val colorString = json.getString("colorString")
    val isStarred = json.optBoolean("isStarred", false)
    val isPinned = json.optBoolean("isPinned", false)
    val isArchived = json.optBoolean("isArchived", false)
    
    val tagsSet = mutableSetOf<String>()
    val tagsArray = json.optJSONArray("customTags")
    if (tagsArray != null) {
        for (i in 0 until tagsArray.length()) {
            tagsSet.add(tagsArray.getString(i))
        }
    }
    val lastInteractedAt = json.optLong("lastInteractedAt", System.currentTimeMillis())
    val reminderTime = if (json.isNull("reminderTime")) null else json.getLong("reminderTime")
    val deletedAt = if (json.isNull("deletedAt")) null else json.getLong("deletedAt")
    
    return when (val type = json.getString("type")) {
        "text" -> {
            val spans = mutableListOf<com.example.pulsenotes.data.richtext.RichTextSpan>()
            val spansArray = json.optJSONArray("richTextSpans")
            if (spansArray != null) {
                for (i in 0 until spansArray.length()) {
                    val spanObj = spansArray.getJSONObject(i)
                    val format = when (spanObj.getString("formatType")) {
                        "bold" -> com.example.pulsenotes.data.richtext.TextFormat.Bold
                        "italic" -> com.example.pulsenotes.data.richtext.TextFormat.Italic
                        "underline" -> com.example.pulsenotes.data.richtext.TextFormat.Underline
                        "strikethrough" -> com.example.pulsenotes.data.richtext.TextFormat.Strikethrough
                        "heading1" -> com.example.pulsenotes.data.richtext.TextFormat.Heading1
                        "heading2" -> com.example.pulsenotes.data.richtext.TextFormat.Heading2
                        "bullet" -> com.example.pulsenotes.data.richtext.TextFormat.BulletPoint
                        "code" -> com.example.pulsenotes.data.richtext.TextFormat.Code
                        else -> com.example.pulsenotes.data.richtext.TextFormat.Bold
                    }
                    spans.add(
                        com.example.pulsenotes.data.richtext.RichTextSpan(
                            start = spanObj.getInt("start"),
                            end = spanObj.getInt("end"),
                            format = format
                        )
                    )
                }
            }
            TextNote(
                id = id,
                title = title,
                content = json.getString("content"),
                colorString = colorString,
                isStarred = isStarred,
                isPinned = isPinned,
                isArchived = isArchived,
                customTags = tagsSet,
                lastInteractedAt = lastInteractedAt,
                reminderTime = reminderTime,
                deletedAt = deletedAt,
                imageUri = if (json.isNull("imageUri")) null else json.getString("imageUri"),
                richTextSpans = spans
            )
        }
        "checklist" -> {
            val itemsList = mutableListOf<ChecklistItem>()
            val itemsArray = json.getJSONArray("items")
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                itemsList.add(
                    ChecklistItem(
                        text = itemObj.getString("text"),
                        isChecked = itemObj.getBoolean("isChecked")
                    )
                )
            }
            ChecklistNote(
                id = id,
                title = title,
                items = itemsList,
                colorString = colorString,
                isStarred = isStarred,
                isPinned = isPinned,
                isArchived = isArchived,
                customTags = tagsSet,
                lastInteractedAt = lastInteractedAt,
                reminderTime = reminderTime,
                deletedAt = deletedAt
            )
        }
        "voice" -> {
            VoiceNote(
                id = id,
                title = title,
                subtitle = if (json.isNull("subtitle")) null else json.getString("subtitle"),
                showLargeWaveform = json.optBoolean("showLargeWaveform", false),
                colorString = colorString,
                isStarred = isStarred,
                isPinned = isPinned,
                isArchived = isArchived,
                customTags = tagsSet,
                audioFilePath = if (json.isNull("audioFilePath")) null else json.getString("audioFilePath"),
                lastInteractedAt = lastInteractedAt,
                reminderTime = reminderTime,
                deletedAt = deletedAt
            )
        }
        else -> throw IllegalArgumentException("Unknown note type: $type")
    }
}

fun copyUriToInternalStorage(context: Context, uri: android.net.Uri): android.net.Uri? {
    return try {
        val storagePath = File(context.filesDir, "note_images")
        storagePath.mkdirs()
        val file = File(storagePath, "image_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            java.io.FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        android.net.Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun createTempPhotoUri(context: Context): Pair<File, android.net.Uri>? {
    return try {
        val storagePath = File(context.filesDir, "note_images")
        storagePath.mkdirs()
        val file = File(storagePath, "image_${System.currentTimeMillis()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        Pair(file, uri)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveBitmapToInternalStorage(context: Context, bitmap: android.graphics.Bitmap): android.net.Uri? {
    return try {
        val storagePath = File(context.filesDir, "note_images")
        storagePath.mkdirs()
        val file = File(storagePath, "image_${System.currentTimeMillis()}.jpg")
        java.io.FileOutputStream(file).use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
        }
        android.net.Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

data class DrawingLine(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

fun saveDrawingToDisk(context: Context, lines: List<DrawingLine>, width: Int, height: Int): android.net.Uri? {
    if (width <= 0 || height <= 0) return null
    return try {
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        lines.forEach { line ->
            if (line.points.size > 1) {
                paint.color = line.color.toArgb()
                paint.strokeWidth = line.strokeWidth
                val path = android.graphics.Path()
                path.moveTo(line.points[0].x, line.points[0].y)
                for (i in 1 until line.points.size) {
                    path.lineTo(line.points[i].x, line.points[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }
        val storagePath = File(context.filesDir, "note_drawings")
        storagePath.mkdirs()
        val file = File(storagePath, "drawing_${System.currentTimeMillis()}.png")
        java.io.FileOutputStream(file).use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        }
        android.net.Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun rememberImageBitmapFromUri(uriString: String?): androidx.compose.ui.graphics.ImageBitmap? {
    if (uriString == null) return null
    val context = LocalContext.current
    return remember(uriString) {
        try {
            val uri = android.net.Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                android.graphics.BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun saveNotesToDisk(context: Context, notes: List<Note>, trashNotes: List<Note>, customTags: List<String>) {
    try {
        val root = JSONObject()
        
        val notesArray = JSONArray()
        notes.forEach { notesArray.put(noteToJson(it)) }
        root.put("notes", notesArray)
        
        val trashArray = JSONArray()
        trashNotes.forEach { trashArray.put(noteToJson(it)) }
        root.put("trashNotes", trashArray)
        
        val tagsArray = JSONArray()
        customTags.forEach { tagsArray.put(it) }
        root.put("customTags", tagsArray)
        
        val file = File(context.filesDir, "notes_data.json")
        file.writeText(root.toString())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun saveSettingsToDisk(context: Context, settings: Map<String, Boolean>) {
    try {
        val root = JSONObject()
        settings.forEach { (key, value) ->
            root.put(key, value)
        }
        val file = File(context.filesDir, "settings_data.json")
        file.writeText(root.toString())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadSettingsFromDisk(context: Context): Map<String, Boolean> {
    try {
        val file = File(context.filesDir, "settings_data.json")
        if (!file.exists()) return emptyMap()
        val content = file.readText()
        if (content.isBlank()) return emptyMap()
        val root = JSONObject(content)
        val map = mutableMapOf<String, Boolean>()
        root.keys().forEach { key ->
            map[key] = root.getBoolean(key)
        }
        return map
    } catch (_: Exception) {
        return emptyMap()
    }
}

fun loadNotesFromDisk(context: Context): Triple<List<Note>, List<Note>, List<String>>? {
    try {
        val file = File(context.filesDir, "notes_data.json")
        if (!file.exists()) return null
        
        val content = file.readText()
        if (content.isBlank()) return null
        
        val root = JSONObject(content)
        
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
        e.printStackTrace()
        return null
    }
}

fun scheduleAlarm(context: Context, note: Note, absoluteTimeMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("noteId", note.id)
        putExtra("title", "Reminder: ${note.title}")
        val contentText = when (note) {
            is TextNote -> note.content
            is ChecklistNote -> note.items.joinToString { it.text }
            is VoiceNote -> note.subtitle ?: "Voice Recording"
            else -> "Reminder set for this note"
        }
        putExtra("content", contentText)
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        note.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    val triggerTime = absoluteTimeMillis
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    } else {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

fun cancelAlarm(context: Context, note: Note) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        note.id.hashCode(),
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetReminderDialog(
    note: Note,
    onDismiss: () -> Unit,
    onSetReminder: (Long) -> Unit,
    onCancelReminder: () -> Unit
) {
    // Step: 0 = date picker, 1 = time picker
    var step by remember { mutableStateOf(0) }

    val now = remember { java.util.Calendar.getInstance() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
    // Default time: current hour + 1, rounded to nearest 5 min
    val defaultHour = remember { (now.get(java.util.Calendar.HOUR_OF_DAY) + 1) % 24 }
    val defaultMinute = remember { (now.get(java.util.Calendar.MINUTE) / 5) * 5 }
    val timePickerState = rememberTimePickerState(
        initialHour = defaultHour,
        initialMinute = defaultMinute,
        is24Hour = false
    )

    // Compute absolute timestamp from selected date + time
    fun computeReminderTime(): Long {
        val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = selectedDateMillis
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH)
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

        val localCal = java.util.Calendar.getInstance()
        localCal.set(year, month, day, timePickerState.hour, timePickerState.minute, 0)
        localCal.set(java.util.Calendar.MILLISECOND, 0)
        return localCal.timeInMillis
    }

    val reminderTimeMs = computeReminderTime()
    val isInPast = reminderTimeMs <= System.currentTimeMillis()

    // Format helpers
    val dateFormatter = remember { java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.getDefault()) }
    val timeFormatter = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = if (step == 0) "Pick a Date" else "Pick a Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Active reminder status banner
                if (note.reminderTime != null) {
                    val reminderDate = java.util.Date(note.reminderTime!!)
                    val isExpired = note.reminderTime!! <= System.currentTimeMillis()
                    val statusColor = if (isExpired) MaterialTheme.colorScheme.error else Color(0xFF8DF0B0)
                    val bgColor = if (isExpired) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else Color(0xFF8DF0B0).copy(alpha = 0.15f)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor, MaterialTheme.shapes.medium)
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isExpired) "Reminder expired" else "Reminder active",
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dateFormatter.format(reminderDate)} · ${timeFormatter.format(reminderDate)}",
                                color = statusColor.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(statusColor.copy(alpha = 0.2f))
                                .clickable { onCancelReminder() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Remove",
                                color = statusColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "ReminderStep"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> {
                            DatePicker(
                                state = datePickerState,
                                showModeToggle = false,
                                title = null,
                                headline = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        1 -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Show selected date as a chip
                                val selectedDateText = datePickerState.selectedDateMillis?.let {
                                    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                    cal.timeInMillis = it
                                    dateFormatter.format(cal.time)
                                } ?: "Today"
                                
                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.large)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .clickable { step = 0 }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            selectedDateText,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                TimePicker(
                                    state = timePickerState,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Past-time warning
                                if (isInPast) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), MaterialTheme.shapes.medium)
                                            .padding(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "This time is in the past",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(8.dp))

                if (step == 0) {
                    TextButton(
                        onClick = { step = 1 },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text(
                            "Next",
                            color = if (datePickerState.selectedDateMillis != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TextButton(onClick = { step = 0 }) {
                        Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onSetReminder(reminderTimeMs) },
                        enabled = !isInPast
                    ) {
                        Text(
                            "Set Reminder",
                            color = if (!isInPast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun TrashScreen(
    trashNotes: List<Note>,
    onBack: () -> Unit,
    onRestoreNote: (Note) -> Unit,
    onDeletePermanently: (Note) -> Unit,
    onEmptyTrash: () -> Unit
) {
    val context = LocalContext.current
    var showEmptyConfirm by remember { mutableStateOf(false) }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = { Text("Empty Trash?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("All notes in Trash will be permanently deleted. This cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            confirmButton = {
                TextButton(onClick = {
                    onEmptyTrash()
                    showEmptyConfirm = false
                }) {
                    Text("Empty All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Trash",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (trashNotes.isNotEmpty()) {
                TextButton(onClick = { showEmptyConfirm = true }) {
                    Text("Empty Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Subtitle warning
        Text(
            text = "Items in Trash will be permanently deleted after 30 days.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        if (trashNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Trash is empty",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(trashNotes, key = { it.id }) { note ->
                    TrashNoteCard(
                        note = note,
                        onRestore = { onRestoreNote(note) },
                        onDeleteForever = { onDeletePermanently(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrashNoteCard(
    note: Note,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    val cardColor = try {
        Color(note.colorString.toColorInt())
    } catch (_: Exception) {
        Color.LightGray
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Permanently?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("This note will be permanently deleted from your device.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            confirmButton = {
                TextButton(onClick = {
                    onDeleteForever()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column {
                if (note is VoiceNote) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF111111),
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        WaveformIcon(modifier = Modifier.size(16.dp))
                    }
                    if (note.subtitle != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(note.subtitle, color = Color(0xDD000000), fontSize = 14.sp)
                    }
                } else {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(bottom = 8.dp),
                        lineHeight = 22.sp
                    )
                    when (note) {
                        is TextNote -> {
                            Text(
                                text = note.content,
                                fontSize = 14.sp,
                                color = Color(0xDD000000),
                                lineHeight = 18.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        is ChecklistNote -> {
                            note.items.take(3).forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = item.text,
                                        fontSize = 14.sp,
                                        color = if (item.isChecked) Color(0x88000000) else Color(0xDD000000),
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (note.items.size > 3) {
                                Text("+${note.items.size - 3} more items", fontSize = 12.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                            }
                        }
                        else -> {}
                    }
                }

                Spacer(Modifier.height(16.dp))

                val deletedTime = note.deletedAt ?: System.currentTimeMillis()
                val elapsedMs = System.currentTimeMillis() - deletedTime
                val limitMs = 30L * 24 * 60 * 60 * 1000L
                val remainingMs = limitMs - elapsedMs
                val remainingDays = (remainingMs / (24L * 60 * 60 * 1000)).coerceAtLeast(0)
                
                val daysText = if (remainingDays > 0) "$remainingDays days left" else "Last day left"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = daysText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.12f), CircleShape)
                                .bounceClickable { onRestore() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Restore,
                                contentDescription = "Restore",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.12f), CircleShape)
                                .bounceClickable { showDeleteConfirm = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = "Delete Forever",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    remindersEnabled: Boolean,
    onRemindersEnabledChange: (Boolean) -> Unit,
    defaultAlarmSound: Boolean,
    onDefaultAlarmSoundChange: (Boolean) -> Unit,
    autoRenameVoiceNotes: Boolean,
    onAutoRenameVoiceNotesChange: (Boolean) -> Unit,
    defaultViewIsGrid: Boolean,
    onDefaultViewIsGridChange: (Boolean) -> Unit,
    onExportBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onClearAllData: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showLanguageDialog) {
        val languages = listOf(
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "de" to "Deutsch",
            "it" to "Italiano",
            "pt" to "Português"
        )
        val currentTag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            localeManager.applicationLocales.get(0)?.language ?: "en"
        } else {
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            prefs.getString("app_lang", "en") ?: "en"
        }

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    languages.forEach { (tag, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .bounceClickable {
                                    setAppLanguage(context, tag)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (tag == currentTag),
                                onClick = {
                                    setAppLanguage(context, tag)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Wipe All Data?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("This will permanently delete all active notes, tags, and items in the trash. This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            confirmButton = {
                TextButton(onClick = {
                    onClearAllData()
                    showClearConfirm = false
                }) {
                    Text("Wipe Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            // General Section
            Text(
                text = stringResource(R.string.settings_general), 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.titleSmall, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            SettingsToggleRow(
                title = "Startup Layout",
                subtitle = "Start screen in Grid View instead of List View",
                value = defaultViewIsGrid,
                onValueChange = onDefaultViewIsGridChange
            )
            
            SettingsToggleRow(
                title = "Prompt to Rename Voice Notes",
                subtitle = "Ask to rename voice recordings upon completion",
                value = autoRenameVoiceNotes,
                onValueChange = onAutoRenameVoiceNotesChange
            )

            Spacer(Modifier.height(8.dp))
            SettingsClickableRow(
                title = stringResource(R.string.select_language),
                subtitle = stringResource(R.string.select_language_subtitle) + " (${getCurrentLanguageName(context)})",
                onClick = { showLanguageDialog = true }
            )
            
            Spacer(Modifier.height(16.dp))

            // Reminders Section
            Text(
                text = stringResource(R.string.settings_reminders), 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.titleSmall, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsToggleRow(
                title = "Enable Reminders",
                subtitle = "Show scheduled alarm reminders and push alerts",
                value = remindersEnabled,
                onValueChange = onRemindersEnabledChange
            )
            
            SettingsToggleRow(
                title = "High Priority Alarm Ringtone",
                subtitle = "Play loud system alarm clock tone instead of standard notification alert",
                value = defaultAlarmSound,
                onValueChange = onDefaultAlarmSoundChange
            )

            Spacer(Modifier.height(16.dp))

            // Data Section
            Text(
                text = "Data Management", 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.titleSmall, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsClickableRow(
                title = "Backup to Clipboard",
                subtitle = "Copy all notes and tags as a backup JSON string",
                onClick = onExportBackup
            )

            SettingsClickableRow(
                title = "Restore from Clipboard",
                subtitle = "Paste and load backup JSON string to restore progress",
                onClick = onRestoreBackup
            )

            SettingsClickableRow(
                title = "Wipe All App Data",
                subtitle = "Danger: Permanently deletes all notes, tags, and trash",
                onClick = { showClearConfirm = true },
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClickable { onValueChange(!value) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    contentColor: Color = Color.Unspecified
) {
    val actualContentColor = if (contentColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else contentColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = actualContentColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 1200f
        ),
        label = "bounce"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.bounceCombinedClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = 1200f
        ),
        label = "bounce"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

fun setAppLanguage(context: Context, languageTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
        localeManager.applicationLocales = LocaleList.forLanguageTags(languageTag)
    } else {
        val locale = Locale(languageTag)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_lang", languageTag).apply()
        
        if (context is MainActivity) {
            context.recreate()
        }
    }
}

fun getCurrentLanguageName(context: Context): String {
    val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
        localeManager.applicationLocales.get(0)?.language ?: "en"
    } else {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.getString("app_lang", "en") ?: "en"
    }
    return when (tag) {
        "es" -> "Español"
        "fr" -> "Français"
        "de" -> "Deutsch"
        "it" -> "Italiano"
        "pt" -> "Português"
        else -> "English"
    }
}

class MainActivity : ComponentActivity() {
    private val quickNoteTriggered = mutableStateOf(false)
    private val noteIdToOpen = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == "com.example.pulsenotes.ACTION_QUICK_NOTE") {
            quickNoteTriggered.value = true
            intent.action = null
        }
        val noteId = intent.getStringExtra("noteId")
        if (noteId != null) {
            noteIdToOpen.value = noteId
            intent.removeExtra("noteId")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val lang = prefs.getString("app_lang", null)
            if (lang != null) {
                val locale = Locale(lang)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        }
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            PulseNotesTheme {
                val focusManager = LocalFocusManager.current
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { focusManager.clearFocus() })
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val loadedData = remember { loadNotesFromDisk(context) }
                    
                    val sampleNotes = remember { 
                        val list = mutableStateListOf<Note>()
                        if (loadedData != null) {
                            list.addAll(loadedData.first)
                        }
                        list
                    }
                    val trashNotes = remember { 
                        val list = mutableStateListOf<Note>()
                        if (loadedData != null) {
                            list.addAll(loadedData.second)
                        }
                        list
                    }
                    val customUserTags = remember { 
                        val list = mutableStateListOf<String>()
                        if (loadedData != null) {
                            list.addAll(loadedData.third)
                        }
                        list
                    }
                    
                    var selectedNoteId by remember { mutableStateOf<String?>(null) }
                    var isCreatingNewNote by remember { mutableStateOf<Note?>(null) }

                    LaunchedEffect(quickNoteTriggered.value) {
                        if (quickNoteTriggered.value) {
                            isCreatingNewNote = TextNote(
                                id = java.util.UUID.randomUUID().toString(),
                                title = "",
                                content = "",
                                colorString = "#ffffff"
                            )
                            quickNoteTriggered.value = false
                        }
                    }

                    LaunchedEffect(noteIdToOpen.value) {
                        noteIdToOpen.value?.let { noteId ->
                            selectedNoteId = noteId
                            noteIdToOpen.value = null
                        }
                    }

                    val pickImageLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            val savedUri = copyUriToInternalStorage(context, uri)
                            if (savedUri != null) {
                                isCreatingNewNote = TextNote(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = "",
                                    content = "",
                                    colorString = "#ffffff",
                                    imageUri = savedUri.toString()
                                )
                            }
                        }
                    }

                    var currentPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

                    val takePhotoLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.TakePicture()
                    ) { success ->
                        if (success) {
                            currentPhotoUri?.let { uri ->
                                isCreatingNewNote = TextNote(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = "",
                                    content = "",
                                    colorString = "#ffffff",
                                    imageUri = uri.toString()
                                )
                            }
                        }
                    }

                    var showCameraScreen by remember { mutableStateOf(false) }

                    val cameraPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            showCameraScreen = true
                        } else {
                            android.widget.Toast.makeText(context, "Camera permission is required", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    var showTrashScreen by remember { mutableStateOf(false) }
                    var showSettingsScreen by remember { mutableStateOf(false) }
                    var showDrawingScreen by remember { mutableStateOf(false) }

                    val loadedSettings = remember { loadSettingsFromDisk(context) }
                    var remindersEnabled by remember { mutableStateOf(loadedSettings["remindersEnabled"] ?: true) }
                    var defaultAlarmSound by remember { mutableStateOf(loadedSettings["defaultAlarmSound"] ?: true) }
                    var autoRenameVoiceNotes by remember { mutableStateOf(loadedSettings["autoRenameVoiceNotes"] ?: true) }
                    var defaultViewIsGrid by remember { mutableStateOf(loadedSettings["defaultViewIsGrid"] ?: true) }

                    val updateSetting = { key: String, value: Boolean ->
                        when (key) {
                            "remindersEnabled" -> remindersEnabled = value
                            "defaultAlarmSound" -> defaultAlarmSound = value
                            "autoRenameVoiceNotes" -> autoRenameVoiceNotes = value
                            "defaultViewIsGrid" -> defaultViewIsGrid = value
                        }
                        val map = mapOf(
                            "remindersEnabled" to remindersEnabled,
                            "defaultAlarmSound" to defaultAlarmSound,
                            "autoRenameVoiceNotes" to autoRenameVoiceNotes,
                            "defaultViewIsGrid" to defaultViewIsGrid
                        )
                        saveSettingsToDisk(context, map)
                    }
                    
                    // Intent extras are consumed in handleIntent
                    
                    LaunchedEffect(Unit) {
                        val now = System.currentTimeMillis()
                        val limitMs = 30L * 24 * 60 * 60 * 1000L
                        val (toKeep, toDelete) = trashNotes.partition {
                            val deletedTime = it.deletedAt ?: now
                            (now - deletedTime) < limitMs
                        }
                        if (toDelete.isNotEmpty()) {
                            trashNotes.clear()
                            trashNotes.addAll(toKeep)
                            saveNotesToDisk(context, sampleNotes, trashNotes, customUserTags)
                        }
                    }
                    
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                                saveNotesToDisk(context, sampleNotes, trashNotes, customUserTags)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }
                    
                    LaunchedEffect(sampleNotes.size, trashNotes.size, customUserTags.size) {
                        saveNotesToDisk(context, sampleNotes, trashNotes, customUserTags)
                    }
                    
                    LaunchedEffect(selectedNoteId, isCreatingNewNote) {
                        if (selectedNoteId == null && isCreatingNewNote == null) {
                            saveNotesToDisk(context, sampleNotes, trashNotes, customUserTags)
                        }
                    }

                    val noteToDisplay = isCreatingNewNote ?: sampleNotes.find { it.id == selectedNoteId }

                    if (showCameraScreen) {
                        BackHandler {
                            showCameraScreen = false
                        }
                        CameraScreen(
                            onDismiss = { showCameraScreen = false },
                            onPhotoCaptured = { uri ->
                                showCameraScreen = false
                                isCreatingNewNote = TextNote(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = "",
                                    content = "",
                                    colorString = "#ffffff",
                                    imageUri = uri.toString()
                                )
                            }
                        )
                    } else if (showDrawingScreen) {
                        BackHandler {
                            showDrawingScreen = false
                        }
                        DrawingScreen(
                            onBack = { showDrawingScreen = false },
                            onSave = { uri ->
                                showDrawingScreen = false
                                isCreatingNewNote = TextNote(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = "Sketch Note",
                                    content = "",
                                    colorString = "#ffffff",
                                    imageUri = uri.toString()
                                )
                            }
                        )
                    } else if (showSettingsScreen) {
                        BackHandler {
                            showSettingsScreen = false
                        }
                        SettingsScreen(
                            onBack = { showSettingsScreen = false },
                            remindersEnabled = remindersEnabled,
                            onRemindersEnabledChange = { updateSetting("remindersEnabled", it) },
                            defaultAlarmSound = defaultAlarmSound,
                            onDefaultAlarmSoundChange = { updateSetting("defaultAlarmSound", it) },
                            autoRenameVoiceNotes = autoRenameVoiceNotes,
                            onAutoRenameVoiceNotesChange = { updateSetting("autoRenameVoiceNotes", it) },
                            defaultViewIsGrid = defaultViewIsGrid,
                            onDefaultViewIsGridChange = { updateSetting("defaultViewIsGrid", it) },
                            onExportBackup = {
                                try {
                                    val root = JSONObject()
                                    val notesArray = JSONArray()
                                    sampleNotes.forEach { notesArray.put(noteToJson(it)) }
                                    root.put("notes", notesArray)
                                    val trashArray = JSONArray()
                                    trashNotes.forEach { trashArray.put(noteToJson(it)) }
                                    root.put("trashNotes", trashArray)
                                    val tagsArray = JSONArray()
                                    customUserTags.forEach { tagsArray.put(it) }
                                    root.put("customTags", tagsArray)
                                    
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("PulseNotes Backup", root.toString())
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to export backup", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRestoreBackup = {
                                try {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = clipboardManager.primaryClip
                                    if (clipData != null && clipData.itemCount > 0) {
                                        val text = clipData.getItemAt(0).text?.toString()
                                        if (!text.isNullOrEmpty()) {
                                            val root = JSONObject(text)
                                            
                                            // Parse active notes
                                            val newNotes = mutableListOf<Note>()
                                            val notesArray = root.optJSONArray("notes")
                                            if (notesArray != null) {
                                                for (i in 0 until notesArray.length()) {
                                                    newNotes.add(jsonToNote(notesArray.getJSONObject(i)))
                                                }
                                            }
                                            
                                            // Parse trash notes
                                            val newTrash = mutableListOf<Note>()
                                            val trashArray = root.optJSONArray("trashNotes")
                                            if (trashArray != null) {
                                                for (i in 0 until trashArray.length()) {
                                                    newTrash.add(jsonToNote(trashArray.getJSONObject(i)))
                                                }
                                            }
                                            
                                            // Parse custom tags
                                            val newTags = mutableListOf<String>()
                                            val tagsArray = root.optJSONArray("customTags")
                                            if (tagsArray != null) {
                                                for (i in 0 until tagsArray.length()) {
                                                    newTags.add(tagsArray.getString(i))
                                                }
                                            }
                                            
                                            // Commit the changes to states
                                            sampleNotes.clear()
                                            sampleNotes.addAll(newNotes)
                                            
                                            trashNotes.clear()
                                            trashNotes.addAll(newTrash)
                                            
                                            customUserTags.clear()
                                            customUserTags.addAll(newTags)
                                            
                                            // Save to disk
                                            saveNotesToDisk(context, sampleNotes, trashNotes, customUserTags)
                                            Toast.makeText(context, "Backup successfully restored!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "No text found in clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Invalid backup format in clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onClearAllData = {
                                sampleNotes.clear()
                                trashNotes.clear()
                                customUserTags.clear()
                                saveNotesToDisk(context, sampleNotes, trashNotes, customUserTags)
                                Toast.makeText(context, "All app data cleared", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else if (showTrashScreen) {
                        BackHandler {
                            showTrashScreen = false
                        }
                        TrashScreen(
                            trashNotes = trashNotes,
                            onBack = { showTrashScreen = false },
                            onRestoreNote = { note ->
                                trashNotes.remove(note)
                                sampleNotes.add(0, note.copyWith(deletedAt = null, lastInteractedAt = System.currentTimeMillis()))
                            },
                            onDeletePermanently = { note ->
                                trashNotes.remove(note)
                            },
                            onEmptyTrash = {
                                trashNotes.clear()
                            }
                        )
                    } else if (noteToDisplay == null) {
                        NotesScreen(
                            notes = sampleNotes,
                            trashCount = trashNotes.size,
                            customUserTags = customUserTags,
                            onNoteClick = { note ->
                                val index = sampleNotes.indexOfFirst { it.id == note.id }
                                if (index != -1) {
                                    sampleNotes[index] = sampleNotes[index].copyWith(lastInteractedAt = System.currentTimeMillis())
                                }
                                selectedNoteId = note.id
                            },
                            onNewNote = { isCreatingNewNote = it },
                            onSaveVoiceNote = { note ->
                                sampleNotes.add(0, note.copyWith(lastInteractedAt = System.currentTimeMillis()))
                            },
                            onDeleteNotes = { idsToDelete ->
                                val notesToTrash = sampleNotes.filter { it.id in idsToDelete }.map {
                                    it.copyWith(deletedAt = System.currentTimeMillis())
                                }
                                trashNotes.addAll(notesToTrash)
                                sampleNotes.removeAll { it.id in idsToDelete }
                            },
                            onTagToggle = { tagName, targetNoteIds, isAdding ->
                                if (isAdding && !customUserTags.contains(tagName)) {
                                    customUserTags.add(tagName)
                                }
                                for (i in sampleNotes.indices) {
                                    if (sampleNotes[i].id in targetNoteIds) {
                                        val updatedTags = if (isAdding) {
                                            sampleNotes[i].customTags + tagName
                                        } else {
                                            sampleNotes[i].customTags - tagName
                                        }
                                        sampleNotes[i] = sampleNotes[i].copyWith(customTags = updatedTags, lastInteractedAt = System.currentTimeMillis())
                                    }
                                }
                            },
                            onRenameTag = { oldName, newName ->
                                val index = customUserTags.indexOf(oldName)
                                if (index != -1) {
                                    customUserTags[index] = newName
                                }
                                for (i in sampleNotes.indices) {
                                    if (sampleNotes[i].customTags.contains(oldName)) {
                                        val updatedTags = sampleNotes[i].customTags - oldName + newName
                                        sampleNotes[i] = sampleNotes[i].copyWith(customTags = updatedTags, lastInteractedAt = System.currentTimeMillis())
                                    }
                                }
                            },
                            onDeleteTag = { tagName ->
                                customUserTags.remove(tagName)
                                for (i in sampleNotes.indices) {
                                    if (sampleNotes[i].customTags.contains(tagName)) {
                                        val updatedTags = sampleNotes[i].customTags - tagName
                                        sampleNotes[i] = sampleNotes[i].copyWith(customTags = updatedTags, lastInteractedAt = System.currentTimeMillis())
                                    }
                                }
                            },
                            onTrashClick = { showTrashScreen = true },
                            onSettingsClick = { showSettingsScreen = true },
                            defaultViewIsGrid = defaultViewIsGrid,
                            autoRenameVoiceNotes = autoRenameVoiceNotes,
                            onPickImage = { pickImageLauncher.launch("image/*") },
                            onTakePhoto = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    showCameraScreen = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            onDraw = { showDrawingScreen = true }
                        )
                    } else {
                        BackHandler {
                            selectedNoteId = null
                            isCreatingNewNote = null
                        }
                        if (noteToDisplay is VoiceNote) {
                            VoiceNotePlaybackScreen(
                                note = noteToDisplay,
                                onBack = {
                                    selectedNoteId = null
                                    isCreatingNewNote = null
                                },
                                onUpdate = { updatedNote ->
                                    val index = sampleNotes.indexOfFirst { it.id == updatedNote.id }
                                    if (index != -1) {
                                        sampleNotes[index] = updatedNote.copyWith(lastInteractedAt = System.currentTimeMillis())
                                    }
                                },
                                onDelete = {
                                    val noteToTrash = sampleNotes.find { it.id == noteToDisplay.id }
                                    if (noteToTrash != null) {
                                        trashNotes.add(noteToTrash.copyWith(deletedAt = System.currentTimeMillis()))
                                    }
                                    sampleNotes.removeAll { it.id == noteToDisplay.id }
                                    selectedNoteId = null
                                    isCreatingNewNote = null
                                }
                            )
                        } else {
                            NoteDetailScreen(
                                note = noteToDisplay,
                                isNewNote = (isCreatingNewNote != null),
                                customUserTags = customUserTags,
                                onBack = { 
                                    selectedNoteId = null
                                    isCreatingNewNote = null
                                },
                                onSave = {
                                    if (isCreatingNewNote != null) {
                                        val isNotEmpty = isCreatingNewNote!!.title.isNotBlank() || 
                                            (isCreatingNewNote as? TextNote)?.content?.isNotBlank() == true
                                        if (isNotEmpty) {
                                            sampleNotes.add(0, isCreatingNewNote!!.copyWith(lastInteractedAt = System.currentTimeMillis()))
                                        }
                                    }
                                    selectedNoteId = null
                                    isCreatingNewNote = null
                                },
                                onUpdate = { updatedNote ->
                                    if (isCreatingNewNote != null) {
                                        isCreatingNewNote = updatedNote.copyWith(lastInteractedAt = System.currentTimeMillis())
                                    } else {
                                        val index = sampleNotes.indexOfFirst { it.id == updatedNote.id }
                                        if (index != -1) {
                                            sampleNotes[index] = updatedNote.copyWith(lastInteractedAt = System.currentTimeMillis())
                                        }
                                    }
                                },
                                onDelete = {
                                    val noteToTrash = sampleNotes.find { it.id == noteToDisplay.id }
                                    if (noteToTrash != null) {
                                        trashNotes.add(noteToTrash.copyWith(deletedAt = System.currentTimeMillis()))
                                    }
                                    sampleNotes.removeAll { it.id == noteToDisplay.id }
                                    selectedNoteId = null
                                    isCreatingNewNote = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getLocalizedTagName(name: String): String {
    return when (name) {
        "Recent" -> stringResource(R.string.tag_recent)
        "All" -> stringResource(R.string.tag_all)
        "Starred" -> stringResource(R.string.tag_starred)
        "Voices" -> stringResource(R.string.tag_voices)
        "Tasks" -> stringResource(R.string.tag_tasks)
        else -> name
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    notes: List<Note>, 
    trashCount: Int, 
    customUserTags: List<String>,
    onNoteClick: (Note) -> Unit, 
    onNewNote: (Note) -> Unit,
    onSaveVoiceNote: (Note) -> Unit,
    onDeleteNotes: (Set<String>) -> Unit,
    onTagToggle: (String, Set<String>, Boolean) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit,
    defaultViewIsGrid: Boolean,
    autoRenameVoiceNotes: Boolean,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onDraw: () -> Unit
) {
    val context = LocalContext.current
    val systemTags = listOf(
        Tag("Recent", null),
        Tag("All", notes.size),
        Tag("Starred", notes.count { it.isStarred }, hasStar = true),
        Tag("Voices", notes.count { it is VoiceNote }),
        Tag("Tasks", notes.count { it is ChecklistNote })
    )
    val dynamicTags = customUserTags.map { tagName -> 
        Tag(tagName, notes.count { it.customTags.contains(tagName) }, isCustom = true) 
    }
    val tags = systemTags + dynamicTags
    var searchFilter by remember { mutableStateOf(SearchFilter()) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showAddImageModal by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }
    var syncEnabled by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(defaultViewIsGrid) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
    var showTagModal by remember { mutableStateOf(false) }
    var showManageTagsModal by remember { mutableStateOf(false) }
    var showVoiceRecordingModal by remember { mutableStateOf(false) }
    
    // Helper to get searchQuery from filter
    val searchQuery = searchFilter.query
    val selectedTag = searchFilter.selectedTag
    val hasActiveFilters = searchFilter.hasActiveFilters
    
    // Pre-calculate displayNotes so it's available for the Header Row and Grid
    val displayNotes = remember(notes, searchFilter) {
        notes.filter { it.deletedAt == null }.applyFilter(searchFilter)
    }
    
    val rotation by animateFloatAsState(
        targetValue = if (showAddMenu) 135f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    var fabOffset by remember { mutableStateOf(Offset.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val gridState = rememberLazyStaggeredGridState()
    var isTagsVisible by remember { mutableStateOf(true) }
    var lastScrollOffset by remember { mutableStateOf(0) }
    var lastScrollIndex by remember { mutableStateOf(0) }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index == 0 && offset == 0) {
                    isTagsVisible = true
                } else {
                    if (index > lastScrollIndex) {
                        isTagsVisible = false
                    } else if (index < lastScrollIndex) {
                        isTagsVisible = true
                    } else {
                        val delta = offset - lastScrollOffset
                        if (delta > 10) {
                            isTagsVisible = false
                        } else if (delta < -10) {
                            isTagsVisible = true
                        }
                    }
                }
                lastScrollIndex = index
                lastScrollOffset = offset
            }
    }

    if (showVoiceRecordingModal) {
        BackHandler { showVoiceRecordingModal = false }
    } else if (showManageTagsModal) {
        BackHandler {
            showManageTagsModal = false
            if (!customUserTags.contains(selectedTag) && selectedTag !in listOf("Recent", "All", "Starred", "Voices", "Tasks")) {
                searchFilter = searchFilter.copy(selectedTag = "Recent")
            }
        }
    } else if (showTagModal) {
        BackHandler { showTagModal = false }
    } else if (showAddImageModal) {
        BackHandler { showAddImageModal = false }
    } else if (showAddMenu) {
        BackHandler { showAddMenu = false }
    } else if (showProfileModal) {
        BackHandler { showProfileModal = false }
    } else if (selectedNoteIds.isNotEmpty()) {
        BackHandler { selectedNoteIds = emptySet() }
    } else if (hasActiveFilters) {
        BackHandler { searchFilter = SearchFilter() }
    }

    Scaffold(
        containerColor = Color(0xFF050505),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onGloballyPositioned { parentCoordinates = it }
        ) {
            // Blurred background layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (showAddImageModal || showProfileModal || showTagModal || showManageTagsModal || showVoiceRecordingModal) Modifier.blur(8.dp) else Modifier)
            ) {
                // Top App Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(4.dp, Color(0xFFfce4a1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFfce4a1), CircleShape))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Pulse Notes", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showProfileModal = true }, modifier = Modifier.offset(x = 12.dp)) {
                        Icon(Icons.Filled.GridView, contentDescription = "Menu", tint = Color.Gray, modifier = Modifier.size(24.dp))
                    }
                }

                // Search Bar and FAB / Contextual Action Bar
                AnimatedContent(
                    targetState = selectedNoteIds.isNotEmpty(),
                    transitionSpec = {
                        if (targetState) {
                            (slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                        } else {
                            (slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                        }
                    },
                    label = "SearchBarTransition"
                ) { isSelected ->
                    if (isSelected) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(56.dp)
                                .background(Color(0xFF2C2C2C), RoundedCornerShape(28.dp))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Close, 
                                contentDescription = "Clear Selection", 
                                tint = Color.White,
                                modifier = Modifier.size(24.dp).bounceClickable { selectedNoteIds = emptySet() }
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "${selectedNoteIds.size} Selected", 
                                color = Color.White, 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier.size(40.dp).bounceClickable { showTagModal = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.LocalOffer, contentDescription = "Tag", tint = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.size(40.dp).bounceClickable {
                                    onDeleteNotes(selectedNoteIds)
                                    selectedNoteIds = emptySet()
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var isSearchFocused by remember { mutableStateOf(false) }

                            BasicTextField(
                                value = searchFilter.query,
                                onValueChange = { searchFilter = searchFilter.copy(query = it) },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .onFocusChanged { isSearchFocused = it.isFocused },
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                            .border(1.dp, if (isSearchFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent, CircleShape)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (searchFilter.query.isEmpty()) {
                                                    Text(stringResource(R.string.search_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 16.sp)
                                                }
                                                innerTextField()
                                            }
                                            if (searchFilter.query.isNotEmpty()) {
                                                Icon(
                                                    Icons.Filled.Close, 
                                                    contentDescription = "Clear", 
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp).bounceClickable { searchFilter = searchFilter.copy(query = "") }
                                                )
                                            } else if (hasActiveFilters) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("!", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            if (fabSize == IntSize.Zero) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .onGloballyPositioned { coordinates ->
                                            parentCoordinates?.let { parent ->
                                                fabOffset = parent.localPositionOf(coordinates, Offset.Zero)
                                                fabSize = coordinates.size
                                            }
                                        }
                                        .clip(MaterialTheme.shapes.large)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .onGloballyPositioned { coordinates ->
                                            parentCoordinates?.let { parent ->
                                                fabOffset = parent.localPositionOf(coordinates, Offset.Zero)
                                                fabSize = coordinates.size
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Tags Row
                AnimatedVisibility(
                    visible = isTagsVisible,
                    enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(tags) { tag ->
                            val isSelected = tag.name == selectedTag
                            Box {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else if (tag.isCustom) MaterialTheme.colorScheme.secondaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer
                                        )
                                        .bounceClickable { searchFilter = searchFilter.copy(selectedTag = tag.name) }
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (tag.hasStar) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = getLocalizedTagName(tag.name),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (tag.count != null && tag.count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(20.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(tag.count.toString(), color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { showTagModal = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Tag", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                            }
                        }
                        
                        item {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { showManageTagsModal = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit Tags", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Enhanced Filter Chips Row
                AnimatedVisibility(
                    visible = isTagsVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Starred filter
                        item {
                            FilterChip(
                                selected = searchFilter.showStarredOnly,
                                onClick = { searchFilter = searchFilter.copy(showStarredOnly = !searchFilter.showStarredOnly) },
                                label = { Text("Starred") },
                                leadingIcon = if (searchFilter.showStarredOnly) {
                                    { Icon(Icons.Filled.Star, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        
                        // Reminder filter
                        item {
                            FilterChip(
                                selected = searchFilter.hasReminder,
                                onClick = { searchFilter = searchFilter.copy(hasReminder = !searchFilter.hasReminder) },
                                label = { Text("Reminder") },
                                leadingIcon = if (searchFilter.hasReminder) {
                                    { Icon(Icons.Filled.Alarm, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                        
                        // Type filter chips
                        NoteType.entries.forEach { type ->
                            item {
                                val isSelected = searchFilter.selectedType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        searchFilter = searchFilter.copy(
                                            selectedType = if (isSelected) null else type
                                        )
                                    },
                                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                )
                            }
                        }
                        
                        // Clear filters button
                        item {
                            AnimatedVisibility(visible = hasActiveFilters) {
                                TextButton(
                                    onClick = { searchFilter = SearchFilter() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Clear All")
                                }
                            }
                        }
                    }
                }

                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val noteCountText = if (hasActiveFilters) "${displayNotes.size} of ${notes.filter { it.deletedAt == null }.size}" else "${notes.filter { it.deletedAt == null }.size}"
                    Text("$noteCountText items • $trashCount in trash", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    
                    // Sort dropdown
                    var showSortMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort", tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        searchFilter = searchFilter.copy(sortBy = option)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (searchFilter.sortBy == option) {
                                        { Icon(Icons.Filled.Check, null, modifier = Modifier.size(20.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                    
                    Icon(
                        Icons.Filled.List, 
                        contentDescription = "List", 
                        tint = if (isGridView) Color.Gray else Color.White, 
                        modifier = Modifier.size(22.dp).bounceClickable { isGridView = false }
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        Icons.Filled.GridView, 
                        contentDescription = "Grid", 
                        tint = if (isGridView) Color.White else Color.Gray, 
                        modifier = Modifier.size(20.dp).bounceClickable { isGridView = true }
                    )
                }

                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(if (isGridView) 2 else 1),
                    verticalItemSpacing = 16.dp,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayNotes, key = { it.id }) { note ->
                        val isSelected = selectedNoteIds.contains(note.id)
                        
                        val index = displayNotes.indexOf(note)
                        val delay = (index * 40).coerceAtMost(300)
                        var isVisible by remember(note.id) { mutableStateOf(false) }
                        LaunchedEffect(note.id) {
                            kotlinx.coroutines.delay(delay.toLong())
                            isVisible = true
                        }
                        
                        val cardAlpha by animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0f,
                            animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)),
                            label = "alpha"
                        )
                        val cardTranslationY by animateFloatAsState(
                            targetValue = if (isVisible) 0f else 32f,
                            animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)),
                            label = "translateY"
                        )
                        
                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = cardAlpha
                                translationY = cardTranslationY * density
                            }
                        ) {
                            NoteCard(
                                note = note, 
                                isGridView = isGridView, 
                                isSelected = isSelected,
                                onClick = { 
                                    if (selectedNoteIds.isNotEmpty()) {
                                        if (isSelected) selectedNoteIds = selectedNoteIds - note.id
                                        else selectedNoteIds = selectedNoteIds + note.id
                                    } else {
                                        onNoteClick(note) 
                                    }
                                },
                                onLongClick = {
                                    if (isSelected) selectedNoteIds = selectedNoteIds - note.id
                                    else selectedNoteIds = selectedNoteIds + note.id
                                }
                            )
                        }
                    }
                }
            }            // Scrim Overlay Layer (dimmed background)
            AnimatedVisibility(
                visible = showAddMenu,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showAddMenu = false }
                )
            }

            // Staggered Pills (cascade downward from right under the FAB position)
            if (showAddMenu && fabSize != IntSize.Zero) {
                val fabBottomOffsetDp = with(LocalDensity.current) { (fabOffset.y + fabSize.height).toDp() }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = fabBottomOffsetDp + 12.dp, end = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data class FabItem(val icon: ImageVector, val label: String, val delay: Int, val action: () -> Unit)
                    val items = listOf(
                        FabItem(Icons.Outlined.Edit, "Text", 0) {
                            showAddMenu = false
                            onNewNote(TextNote(id = java.util.UUID.randomUUID().toString(), title = "", content = "", colorString = "#ffa3a5"))
                        },
                        FabItem(Icons.Outlined.CheckBox, "List", 50) {
                            showAddMenu = false
                            onNewNote(ChecklistNote(id = java.util.UUID.randomUUID().toString(), title = "", items = listOf(ChecklistItem("", false)), colorString = "#cdabfe"))
                        },
                        FabItem(Icons.Outlined.Mic, "Audio", 100) {
                            showAddMenu = false
                            showVoiceRecordingModal = true
                        },
                        FabItem(Icons.Outlined.Brush, "Drawing", 150) {
                            showAddMenu = false
                            onDraw()
                        },
                        FabItem(Icons.Outlined.Image, "Image", 200) {
                            showAddMenu = false
                            showAddImageModal = true
                        }
                    )

                    items.forEach { item ->
                        val visible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(item.delay.toLong())
                            visible.value = true
                        }
                        AnimatedVisibility(
                            visible = visible.value,
                            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = tween(300, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
                            ),
                            exit = fadeOut(animationSpec = tween(100))
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 6.dp,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .clickable { item.action() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                                ) {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // The Floating Action Button (drawn on top of the Scrim at all times, hidden during selection mode)
            AnimatedVisibility(
                visible = selectedNoteIds.isEmpty() && fabSize != IntSize.Zero,
                enter = scaleIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(200)),
                exit = scaleOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(150))
            ) {
                val xOffsetDp = with(LocalDensity.current) { fabOffset.x.toDp() }
                val yOffsetDp = with(LocalDensity.current) { fabOffset.y.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = xOffsetDp, y = yOffsetDp)
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .bounceClickable { showAddMenu = !showAddMenu },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer(rotationZ = rotation)
                    )
                }
            }
            
            // Overlay Layer for Profile Modal
            AnimatedVisibility(
                visible = showProfileModal,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically(initialOffsetY = { -it }, animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))),
                exit = fadeOut(animationSpec = tween(durationMillis = 250)) + slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Dimmed background
                    Box(modifier = Modifier.fillMaxSize().background(Color(0x88000000)).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showProfileModal = false })

                    // Top-aligned Modal
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.extraLarge)
                                .padding(horizontal = 24.dp, vertical = 32.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("Hi,\nDennis essie", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 32.sp)
                                // Profile picture placeholder
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Account Info Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("jamessmith@gmail.com", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Drop", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                
                                // Manage Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                                        .bounceClickable {
                                            Toast.makeText(context, "Account settings coming soon!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Manage", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 15.sp)
                                }

                                Spacer(Modifier.height(8.dp))

                                // Add / Logout Row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                                            .bounceClickable {
                                                Toast.makeText(context, "Switching accounts coming soon!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Add", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 15.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                                            .bounceClickable {
                                                Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Logout", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 15.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Rows
                            // Backup
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                    .bounceClickable {
                                        Toast.makeText(context, "Backup initialized... saved to secure cloud!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.CloudQueue, contentDescription = "Backup", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Backup", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                                Spacer(Modifier.width(16.dp))
                                Text("•", color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(16.dp))
                                Icon(Icons.Filled.Check, contentDescription = "Check", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            }

                            Spacer(Modifier.height(12.dp))

                            // Sync
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                    .bounceClickable {
                                        syncEnabled = !syncEnabled
                                        if (syncEnabled) {
                                            Toast.makeText(context, "Sync enabled!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Sync disabled", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Sync, contentDescription = "Sync", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Sync", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                
                                val switchBgColor by animateColorAsState(
                                    targetValue = if (syncEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "switchBg"
                                )
                                val thumbOffset by animateDpAsState(
                                    targetValue = if (syncEnabled) 22.dp else 0.dp,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                                    label = "switchThumb"
                                )

                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(26.dp)
                                        .background(switchBgColor, CircleShape)
                                        .padding(3.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = thumbOffset)
                                            .size(20.dp)
                                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Settings
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                    .bounceClickable { 
                                        onSettingsClick()
                                        showProfileModal = false
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                            }

                            Spacer(Modifier.height(12.dp))

                            // Trash
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                    .bounceClickable { 
                                        onTrashClick()
                                        showProfileModal = false
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Trash", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Trash", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 16.sp)
                                Spacer(Modifier.width(16.dp))
                                Text("•", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.width(16.dp))
                                Text("$trashCount items", color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), fontSize = 14.sp)
                            }

                            Spacer(Modifier.height(32.dp))

                            // Footer
                            Text("Terms & Conditions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("User Policies", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Drop", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (showTagModal) {
                var tagSearchText by remember { mutableStateOf("") }
                val focusRequester = remember { FocusRequester() }

                // Dimmed background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .pointerInput(Unit) { detectTapGestures(onTap = { showTagModal = false }) }
                )

                // Bottom sheet dialog
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .padding(24.dp)
                    ) {
                        // Drag handle line
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(32.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { showTagModal = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = tagSearchText,
                                onValueChange = { tagSearchText = it },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (tagSearchText.isEmpty()) {
                                        Text("Enter tag name", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        if (tagSearchText.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        onTagToggle(tagSearchText, selectedNoteIds, true)
                                        selectedNoteIds = emptySet()
                                        showTagModal = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.onPrimaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Create", tint = MaterialTheme.colorScheme.primaryContainer)
                                }
                                Spacer(Modifier.width(16.dp))
                                Text("Create \"$tagSearchText\"", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (tagSearchText.isEmpty()) "Your Tags" else "Filtered Tags", 
                            color = MaterialTheme.colorScheme.primary, 
                            style = MaterialTheme.typography.titleSmall, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        val filteredTags = customUserTags.filter { it.contains(tagSearchText, ignoreCase = true) }
                        if (filteredTags.isNotEmpty()) {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.heightIn(max = 240.dp)
                            ) {
                                items(filteredTags) { tag ->
                                    val isChecked = selectedNoteIds.isNotEmpty() && selectedNoteIds.all { id -> notes.find { it.id == id }?.customTags?.contains(tag) == true }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable {
                                                onTagToggle(tag, selectedNoteIds, !isChecked)
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tag, 
                                            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, 
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No matching tags",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                            )
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }

            if (showManageTagsModal) {
                var editingTag by remember { mutableStateOf<String?>(null) }
                val focusRequester = remember { FocusRequester() }

                // Dimmed background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .pointerInput(Unit) { detectTapGestures(onTap = { showManageTagsModal = false }) }
                )

                // Bottom sheet dialog
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .padding(24.dp)
                    ) {
                        // Drag handle line
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(32.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { showManageTagsModal = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Manage Tags", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        if (customUserTags.isNotEmpty()) {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                items(customUserTags) { tag ->
                                    val isEditingThis = editingTag == tag
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isEditingThis) {
                                            var tempName by remember { mutableStateOf(tag) }
                                            BasicTextField(
                                                value = tempName,
                                                onValueChange = { tempName = it },
                                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp),
                                                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            IconButton(
                                                onClick = {
                                                    if (tempName.isNotBlank() && tempName != tag) {
                                                        onRenameTag(tag, tempName)
                                                    }
                                                    editingTag = null
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = "Save",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Text(tag, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                            Spacer(Modifier.width(16.dp))
                                            IconButton(onClick = { editingTag = tag }) {
                                                Icon(
                                                    Icons.Outlined.Edit,
                                                    contentDescription = "Edit Tag",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                onDeleteTag(tag)
                                                if (isEditingThis) editingTag = null
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Delete Tag",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "You haven't created any custom tags yet.", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 24.dp)
                            )
                        }
                    }
                }
                
                LaunchedEffect(editingTag) {
                    if (editingTag != null) {
                        focusRequester.requestFocus()
                    }
                }
            }

            // Overlay Layer for Voice Recording
            if (showVoiceRecordingModal) {
                VoiceRecordingOverlay(
                    notes = notes,
                    autoRenameVoiceNotes = autoRenameVoiceNotes,
                    onDismiss = { showVoiceRecordingModal = false },
                    onSave = { note -> 
                        onSaveVoiceNote(note)
                        showVoiceRecordingModal = false 
                    }
                )
            }

            // Overlay Layer for Image Modal
            AnimatedVisibility(
                visible = showAddImageModal,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                // Dimmed background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showAddImageModal = false }
                )
            }

            AnimatedVisibility(
                visible = showAddImageModal,
                enter = fadeIn(animationSpec = tween(durationMillis = 250)) + 
                        scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(animationSpec = tween(durationMillis = 150)) + 
                       scaleOut(targetScale = 0.95f, animationSpec = tween(durationMillis = 150))
            ) {
                // Center Modal
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .width(320.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge)
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Add image", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Icon(
                                Icons.Filled.Close, 
                                contentDescription = "Close", 
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                                modifier = Modifier.size(24.dp).bounceClickable { showAddImageModal = false }
                            )
                        }

                        // Take photo button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .bounceClickable { 
                                    showAddImageModal = false
                                    onTakePhoto()
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = "Take photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Take photo", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Choose image button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .bounceClickable { 
                                    showAddImageModal = false
                                    onPickImage()
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = "Choose image", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Choose image", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(note: Note, isGridView: Boolean, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val cardColor = try {
        Color(note.colorString.toColorInt())
    } catch (_: Exception) {
        Color.LightGray
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.large) else Modifier)
            .bounceCombinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Column {
                if (note is VoiceNote) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF111111),
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        WaveformIcon()
                    }
                    if (note.subtitle != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(note.subtitle, color = Color(0xDD000000), fontSize = 16.sp)
                    }
                    val showLarge = note.showLargeWaveform || !isGridView
                    if (showLarge) {
                        Spacer(Modifier.height(32.dp))
                        LargeWaveform()
                    }
                } else {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(bottom = 12.dp),
                        lineHeight = 24.sp
                    )
                    when (note) {
                        is TextNote -> {
                            val bitmap = rememberImageBitmapFromUri(note.imageUri)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            val richAnnotatedString = buildRichTextString(note.content, note.richTextSpans)
                            Text(
                                text = richAnnotatedString,
                                fontSize = 16.sp,
                                color = Color(0xDD000000),
                                lineHeight = 22.sp,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        is ChecklistNote -> {
                            note.items.take(4).forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        fontSize = 16.sp,
                                        color = if (item.isChecked) Color(0x88000000) else Color(0xDD000000),
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (note.items.size > 4) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "+${note.items.size - 4} more items", 
                                    fontSize = 13.sp, 
                                    color = Color.Gray, 
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                        else -> {}
                    }
                }
                
                if (note.reminderTime != null) {
                    Spacer(Modifier.height(12.dp))
                    val isExpired = note.reminderTime!! <= System.currentTimeMillis()
                    val badgeColor = if (isExpired) Color(0xFFFF6B6B) else Color(0xFF8DF0B0)
                    val reminderDateStr = remember(note.reminderTime) {
                        val sdf = java.text.SimpleDateFormat("MMM d · h:mm a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(note.reminderTime!!))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), MaterialTheme.shapes.small)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = "Reminder Set",
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = reminderDateStr,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp).size(28.dp).background(Color.White, CircleShape)
                )
            } else if (note.isStarred) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Starred",
                    tint = Color.Black,
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    index: Int,
    item: ChecklistItem,
    focusedItemIndex: Int,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onFocusCleared: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = if (item.isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp).bounceClickable {
                onCheckedChange(!item.isChecked)
            }
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = if (item.isChecked) Color(0x88000000) else Color(0xDD000000),
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Delete",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp).bounceClickable {
                onDelete()
            }
        )
    }

    LaunchedEffect(focusedItemIndex) {
        if (focusedItemIndex == index) {
            focusRequester.requestFocus()
            onFocusCleared()
        }
    }
}

@Composable
fun NoteDetailScreen(
    note: Note, 
    isNewNote: Boolean,
    customUserTags: List<String>,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onUpdate: (Note) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showReminderDialog by remember { mutableStateOf(false) }
    var focusedItemIndex by remember { mutableStateOf(if (isNewNote) 0 else -1) }
    val palette = listOf("#ffa3a5", "#cdabfe", "#fce4a1", "#8df0b0", "#ffffff", "#aaaaaa")

    val bgColor = try {
        Color(note.colorString.toColorInt())
    } catch (_: Exception) {
        Color.LightGray
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // Top Action Bar (Black Background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Arrow Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .bounceClickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Style Picker Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF333322))
                    .bounceClickable { 
                        val currentIndex = palette.indexOf(note.colorString)
                        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % palette.size else 0
                        onUpdate(note.copyWith(colorString = palette[nextIndex]))
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 'A' circle
                Box(
                    modifier = Modifier.size(28.dp).background(Color(0xFFfce4a1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                // Pink/Green pill
                Column(
                    modifier = Modifier.width(10.dp).height(24.dp).clip(RoundedCornerShape(5.dp))
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFffa3a5)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF8df0b0)))
                }
            }

            Spacer(Modifier.width(12.dp))

            // Alarm icon
            val hasReminder = note.reminderTime != null
            val alarmColor = if (hasReminder) Color(0xFF8DF0B0) else Color.LightGray
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1e1e1e), CircleShape)
                    .then(if (hasReminder) Modifier.border(2.dp, Color(0xFF8DF0B0), CircleShape) else Modifier)
                    .bounceClickable { showReminderDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Alarm, contentDescription = "Alarm", tint = alarmColor, modifier = Modifier.size(22.dp))
            }
            
            // Trash icon
            if (!isNewNote) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFF1e1e1e), CircleShape).bounceClickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Trash", tint = Color.LightGray, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            // Star icon
            val starColor = if (note.isStarred) Color(0xFFfce4a1) else Color.LightGray
            val starIcon = if (note.isStarred) Icons.Filled.Star else Icons.Filled.StarBorder
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF1e1e1e), CircleShape).bounceClickable { 
                    onUpdate(note.copyWith(isStarred = !note.isStarred))
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(starIcon, contentDescription = "Star", tint = starColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            // Check icon
            Box(
                modifier = Modifier.size(40.dp).background(Color.White, CircleShape).bounceClickable { onSave() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
        ) {
            val isSketchNoteGlobal = note is TextNote && note.imageUri?.contains("note_drawings") == true

            if (!isSketchNoteGlobal) {
                BasicTextField(
                    value = note.title,
                    onValueChange = { onUpdate(note.copyWith(title = it.replace("\n", ""))) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color(0xFF111111)
                    ),
                    decorationBox = { innerTextField ->
                        if (note.title.isEmpty()) {
                            Text("Title", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Color(0x66111111))
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }

            // Content based on type
            when (note) {
                is TextNote -> {
                    val isSketchNote = note.imageUri?.contains("note_drawings") == true

                    if (isSketchNote) {
                        // Sketch note: show drawing only, no title/content fields
                        val bitmap = rememberImageBitmapFromUri(note.imageUri)
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Sketch Drawing",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.large),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                    } else {
                        // Regular text note with optional photo
                        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            val bitmap = rememberImageBitmapFromUri(note.imageUri)
                            if (bitmap != null) {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(bottom = 16.dp)) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.large),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    // Clear image button in top right
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .clickable { onUpdate(note.copy(imageUri = null)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            RichTextEditor(
                                text = note.content,
                                spans = note.richTextSpans,
                                onTextChange = { newText, newSpans ->
                                    onUpdate(note.copyWithRichText(newText, newSpans))
                                },
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                placeholder = "Start typing..."
                            )
                        }
                    }
                }
                is ChecklistNote -> {
                    Column {
                        note.items.forEachIndexed { index, item ->
                            ChecklistItemRow(
                                index = index,
                                item = item,
                                focusedItemIndex = focusedItemIndex,
                                onCheckedChange = { isChecked ->
                                    val newItems = note.items.toMutableList()
                                    newItems[index] = item.copy(isChecked = isChecked)
                                    onUpdate(note.copy(items = newItems))
                                },
                                onTextChange = { newText ->
                                    val newItems = note.items.toMutableList()
                                    newItems[index] = item.copy(text = newText)
                                    onUpdate(note.copy(items = newItems))
                                },
                                onDelete = {
                                    val newItems = note.items.toMutableList()
                                    newItems.removeAt(index)
                                    onUpdate(note.copy(items = newItems))
                                },
                                onFocusCleared = {
                                    focusedItemIndex = -1
                                }
                            )
                        }

                        // Add new item row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable {
                                    val newItems = note.items.toMutableList()
                                    newItems.add(ChecklistItem("", false))
                                    focusedItemIndex = newItems.lastIndex
                                    onUpdate(note.copy(items = newItems))
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Item",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Add Item", color = Color.Gray, fontSize = 18.sp)
                        }
                    }
                }
                is VoiceNote -> {
                     if (note.subtitle != null) {
                         Text(note.subtitle, color = Color(0xDD000000), fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                     }
                     LargeWaveform()
                }
            }
        }

        // Footer Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Dashed line
            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                drawLine(
                    color = Color(0x44000000),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TAGS:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xAA000000))
                    Spacer(Modifier.height(8.dp))
                    if (note.customTags.isEmpty()) {
                        Text("No tags added", fontStyle = FontStyle.Italic, fontSize = 15.sp, color = Color(0x99000000))
                    } else {
                        Text(note.customTags.joinToString(", "), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xDD000000))
                    }
                }
                
                var showTagDropdown by remember { mutableStateOf(false) }
                Box {
                    Text(
                        text = "+ Add Tags",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { showTagDropdown = true }
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = showTagDropdown,
                        onDismissRequest = { showTagDropdown = false }
                    ) {
                        if (customUserTags.isEmpty()) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("No tags created yet") }, 
                                onClick = { showTagDropdown = false }
                            )
                        } else {
                            customUserTags.forEach { tag ->
                                val hasTag = note.customTags.contains(tag)
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(tag) },
                                    trailingIcon = { 
                                        if (hasTag) Icon(Icons.Filled.Check, contentDescription = "Added", tint = Color.Black) 
                                    },
                                    onClick = { 
                                        val newTags = if (hasTag) note.customTags - tag else note.customTags + tag
                                        val updatedNote = note.copyWith(customTags = newTags)
                                        onUpdate(updatedNote)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp)) // Padding for bottom edge
        }
    }

    if (showReminderDialog) {
        SetReminderDialog(
            note = note,
            onDismiss = { showReminderDialog = false },
            onSetReminder = { absoluteTimeMs ->
                onUpdate(note.copyWith(reminderTime = absoluteTimeMs))
                scheduleAlarm(context, note, absoluteTimeMs)
                Toast.makeText(context, "Reminder set!", Toast.LENGTH_SHORT).show()
                showReminderDialog = false
            },
            onCancelReminder = {
                onUpdate(note.copyWith(clearReminder = true))
                cancelAlarm(context, note)
                Toast.makeText(context, "Reminder cancelled!", Toast.LENGTH_SHORT).show()
                showReminderDialog = false
            }
        )
    }
}

@Composable
fun WaveformIcon(modifier: Modifier = Modifier, color: Color = Color(0xAA000000)) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(2.dp, 8.dp).background(color, CircleShape))
        Box(modifier = Modifier.size(2.dp, 16.dp).background(color, CircleShape))
        Box(modifier = Modifier.size(2.dp, 10.dp).background(color, CircleShape))
        Box(modifier = Modifier.size(2.dp, 6.dp).background(color, CircleShape))
    }
}

@Composable
fun LargeWaveform() {
    Row(
        modifier = Modifier.fillMaxWidth().height(28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val heights = listOf(4, 6, 8, 5, 12, 16, 20, 10, 6, 8, 12, 18, 14, 8, 6, 4)
        heights.forEach { h ->
            Box(modifier = Modifier.width(4.dp).height(h.dp).background(Color(0x33000000), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)))
        }
    }
}

@Composable
fun MenuRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
    }
}

fun getNextVoiceNoteTitle(notes: List<Note>): String {
    val prefix = "New Recording "
    var maxNum = 0
    for (note in notes) {
        if (note is VoiceNote && note.title.startsWith(prefix, ignoreCase = true)) {
            val numPart = note.title.substring(prefix.length).trim()
            val num = numPart.toIntOrNull()
            if (num != null && num > maxNum) {
                maxNum = num
            }
        }
    }
    val nextNum = maxNum + 1
    return String.format(java.util.Locale.US, "New Recording %03d", nextNum)
}

@Composable
fun VoiceRecordingOverlay(
    notes: List<Note>,
    autoRenameVoiceNotes: Boolean,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }
    
    var showRenameDialog by remember { mutableStateOf(false) }
    var enteredTitle by remember { mutableStateOf("") }
    var defaultTitle by remember { mutableStateOf("") }
    var savedSubtitle by remember { mutableStateOf("") }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = java.io.File(context.cacheDir, "audiorecord_${System.currentTimeMillis()}.m4a")
            audioFilePath = file.absolutePath
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                recorder?.stop()
            } catch (e: Exception) {}
            recorder?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .pointerInput(Unit) {} // Block taps
    ) {
        if (!isRecording) {
            // Initial Screen (Record respectfully)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Black)
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Record respectfully\naround others",
                    color = Color.Black,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 36.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .size(100.dp)
                    .background(Color(0xFFE86A58), CircleShape)
                    .clickable { 
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            val file = java.io.File(context.cacheDir, "audiorecord_${System.currentTimeMillis()}.m4a")
                            audioFilePath = file.absolutePath
                            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                MediaRecorder(context)
                            } else {
                                @Suppress("DEPRECATION")
                                MediaRecorder()
                            }.apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(file.absolutePath)
                                prepare()
                                start()
                            }
                            isRecording = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(28.dp).background(Color(0xFF4D1C17), CircleShape))
            }
        } else {
            // Active Recording Screen
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedDate = remember {
                        val sdf = java.text.SimpleDateFormat("MMM dd 'at' h:mm a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date())
                    }
                    Text(text = formattedDate, color = Color.Gray, fontSize = 16.sp)
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline, 
                        contentDescription = "Trash", 
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).clickable { onDismiss() }
                    )
                }

                // Waveform Canvas and Timer State
                var tick by remember { mutableStateOf(0f) }
                var elapsedMillis by remember { mutableStateOf(0L) }
                var isPaused by remember { mutableStateOf(false) }
                
                LaunchedEffect(isPaused) {
                    if (!isPaused) {
                        val startTime = System.currentTimeMillis() - elapsedMillis
                        while (true) {
                            androidx.compose.runtime.withFrameMillis { time ->
                                tick = time / 100f
                                elapsedMillis = System.currentTimeMillis() - startTime
                            }
                        }
                    }
                }
                
                val totalSeconds = elapsedMillis / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val tenths = (elapsedMillis % 1000) / 100
                val timeString = String.format("%02d:%02d.%d", minutes, seconds, tenths)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        val centerY = size.height / 2
                        val pillWidth = 4.dp.toPx()
                        val spacing = 8.dp.toPx()
                        val numPills = (size.width / (pillWidth + spacing)).toInt()
                        
                        for (i in 0 until numPills) {
                            val x = i * (pillWidth + spacing)
                            val baseHeight = 8.dp.toPx()
                            val amplitude = if (isPaused) 0f else (kotlin.math.sin(tick + i * 0.5f) * kotlin.math.cos(tick * 0.3f - i * 0.2f)).toFloat()
                            val height = baseHeight + (kotlin.math.abs(amplitude) * 60.dp.toPx())
                            
                            drawRoundRect(
                                color = Color(0xFFCCCCCC),
                                topLeft = Offset(x, centerY - height / 2),
                                size = androidx.compose.ui.geometry.Size(pillWidth, height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillWidth / 2, pillWidth / 2)
                            )
                        }
                    }
                }

                // Bottom Controls Area
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // View Toggle
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.background(Color.White, CircleShape).padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Icon(Icons.Filled.ClearAll, contentDescription = "Waveform", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Icon(Icons.Filled.Subject, contentDescription = "Transcript", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))

                    // Timer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Blinking red dot or solid grey dot when paused
                        val alpha = if (isPaused) 1f else (kotlin.math.sin(tick * 0.5f) + 1f) / 2f
                        val dotColor = if (isPaused) Color.Gray else Color(0xFFFF5252)
                        Box(modifier = Modifier.size(12.dp).background(dotColor.copy(alpha = alpha), CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(timeString, color = Color.Black, fontSize = 42.sp, fontWeight = FontWeight.Light)
                    }

                    Spacer(Modifier.height(32.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Pause / Resume
                        if (isPaused) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(32.dp))
                                    .clickable { 
                                        isPaused = false 
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) recorder?.resume()
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.FiberManualRecord, contentDescription = "Resume", tint = Color.Black, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Resume", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(32.dp))
                                    .clickable { 
                                        isPaused = true 
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) recorder?.pause()
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = Color.Black, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Pause", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        Spacer(Modifier.width(16.dp))

                        // Stop
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .background(Color(0xFFF0B3AF), RoundedCornerShape(32.dp))
                                .clickable { 
                                    try { recorder?.stop() } catch (e: Exception) {}
                                    recorder?.release()
                                    recorder = null
                                    val defaultName = getNextVoiceNoteTitle(notes)
                                    if (autoRenameVoiceNotes) {
                                        defaultTitle = defaultName
                                        enteredTitle = defaultName
                                        savedSubtitle = timeString
                                        showRenameDialog = true
                                        isPaused = true
                                    } else {
                                        val newNote = VoiceNote(
                                            id = java.util.UUID.randomUUID().toString(),
                                            title = defaultName,
                                            subtitle = timeString,
                                            showLargeWaveform = true,
                                            colorString = "#fce4a1",
                                            audioFilePath = audioFilePath
                                        )
                                        onSave(newNote)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = Color(0xFF4D1C17), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Stop", color = Color(0xFF4D1C17), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // Rename Dialog Overlay
        if (showRenameDialog) {
            // Dimmed background that blocks clicks
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000))
                    .pointerInput(Unit) {} // Block taps behind the modal
                    .systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .background(Color.White, RoundedCornerShape(28.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Rename Recording",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Choose a name for your voice note ($savedSubtitle)",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(20.dp))

                    var isTextFieldFocused by remember { mutableStateOf(false) }
                    BasicTextField(
                        value = enteredTitle,
                        onValueChange = { enteredTitle = it },
                        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .onFocusChanged { isTextFieldFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF2F2F2), RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isTextFieldFocused) Color.Black else Color.Transparent, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (enteredTitle.isEmpty()) {
                                    Text("Recording name", color = Color.Gray, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Discard Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                                .clickable {
                                    // Delete the recording file
                                    audioFilePath?.let { path ->
                                        try {
                                            java.io.File(path).delete()
                                        } catch (e: Exception) {}
                                    }
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Discard", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        // Save Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(Color(0xFFE86A58), RoundedCornerShape(24.dp))
                                .clickable {
                                    val finalTitle = enteredTitle.trim().ifEmpty { defaultTitle }
                                    val newNote = VoiceNote(
                                        id = java.util.UUID.randomUUID().toString(),
                                        title = finalTitle,
                                        subtitle = savedSubtitle,
                                        showLargeWaveform = true,
                                        colorString = "#fce4a1",
                                        audioFilePath = audioFilePath
                                    )
                                    onSave(newNote)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Save", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun VoiceNotePlaybackScreen(
    note: VoiceNote,
    onBack: () -> Unit,
    onUpdate: (Note) -> Unit,
    onDelete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showReminderDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableStateOf(0f) } // 0f to 1f
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var totalDuration by remember { mutableStateOf(1) } // millis, avoid /0
    var hasAudio by remember { mutableStateOf(false) }
    
    // Initialize MediaPlayer once
    DisposableEffect(note.audioFilePath) {
        val filePath = note.audioFilePath
        if (filePath != null && java.io.File(filePath).exists()) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(filePath)
                    prepare()
                }
                totalDuration = mp.duration.coerceAtLeast(1)
                mediaPlayer = mp
                hasAudio = true
                mp.setOnCompletionListener {
                    isPlaying = false
                    playbackPosition = 1f
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hasAudio = false
            }
        }
        onDispose {
            try { mediaPlayer?.release() } catch (_: Exception) {}
            mediaPlayer = null
        }
    }

    // Playback position tracker — only runs while playing
    LaunchedEffect(isPlaying) {
        if (isPlaying && mediaPlayer != null) {
            val mp = mediaPlayer!!
            if (!mp.isPlaying) {
                mp.start()
            }
            while (isPlaying) {
                kotlinx.coroutines.delay(50)
                try {
                    if (mp.isPlaying) {
                        playbackPosition = (mp.currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                    }
                } catch (_: Exception) { break }
            }
        } else if (!isPlaying && mediaPlayer != null) {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                }
            } catch (_: Exception) {}
        }
    }

    val palette = listOf("#ffa3a5", "#cdabfe", "#fce4a1", "#8df0b0", "#ffffff", "#aaaaaa")
    val bgColor = try {
        Color(note.colorString.toColorInt())
    } catch (_: Exception) {
        Color.LightGray
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Top Action Bar (Black Background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Arrow Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Style Picker Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF333322))
                    .clickable { 
                        val currentIndex = palette.indexOf(note.colorString)
                        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % palette.size else 0
                        onUpdate(note.copy(colorString = palette[nextIndex]))
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 'A' circle
                Box(
                    modifier = Modifier.size(28.dp).background(Color(0xFFfce4a1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                // Pink/Green pill
                Column(
                    modifier = Modifier.width(10.dp).height(24.dp).clip(RoundedCornerShape(5.dp))
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFffa3a5)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF8df0b0)))
                }
            }

            Spacer(Modifier.width(12.dp))

            // Alarm icon
            val hasReminder = note.reminderTime != null
            val alarmColor = if (hasReminder) Color(0xFF8DF0B0) else Color.LightGray
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1e1e1e), CircleShape)
                    .then(if (hasReminder) Modifier.border(2.dp, Color(0xFF8DF0B0), CircleShape) else Modifier)
                    .clickable { showReminderDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Alarm, contentDescription = "Alarm", tint = alarmColor, modifier = Modifier.size(22.dp))
            }
            
            // Trash icon
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF1e1e1e), CircleShape).clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Trash", tint = Color.LightGray, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.weight(1f))

            // Star icon
            val starColor = if (note.isStarred) Color(0xFFfce4a1) else Color.LightGray
            val starIcon = if (note.isStarred) Icons.Filled.Star else Icons.Filled.StarBorder
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF1e1e1e), CircleShape).clickable { 
                    onUpdate(note.copy(isStarred = !note.isStarred))
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(starIcon, contentDescription = "Star", tint = starColor, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Waveform Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height / 2
                val pillWidth = 4.dp.toPx()
                val spacing = 8.dp.toPx()
                val numPills = (size.width / (pillWidth + spacing)).toInt()

                // Playhead line
                val playheadX = size.width / 2f
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(playheadX, centerY - 80.dp.toPx()),
                    end = Offset(playheadX, centerY + 80.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Simulated waveform dots shifting left based on playbackPosition
                val shift = playbackPosition * size.width
                
                for (i in 0 until (numPills * 2)) {
                    val x = (i * (pillWidth + spacing)) - shift + (size.width / 2f)
                    
                    if (x > 0 && x < size.width) {
                        val isPlayed = x < playheadX
                        drawRoundRect(
                            color = if (isPlayed) Color(0xFFAAAAAA) else Color(0xFFDDDDDD),
                            topLeft = Offset(x, centerY - pillWidth / 2),
                            size = androidx.compose.ui.geometry.Size(pillWidth, pillWidth),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillWidth / 2, pillWidth / 2)
                        )
                    }
                }
            }
        }

        // Controls Area
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrubber
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                PlaybackScrubber(
                    position = playbackPosition,
                    onPositionChange = { newPos -> 
                        playbackPosition = newPos 
                        mediaPlayer?.seekTo((newPos * totalDuration).toInt())
                    }
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val curSecs = (playbackPosition * totalDuration / 1000).toInt()
                    val remSecs = ((1f - playbackPosition) * totalDuration / 1000).toInt()
                    val curMin = curSecs / 60
                    val curS = curSecs % 60
                    val remMin = remSecs / 60
                    val remS = remSecs % 60
                    Text(String.format("%02d:%02d", curMin, curS), color = Color.Gray, fontSize = 14.sp)
                    Text(String.format("-%02d:%02d", remMin, remS), color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Playback Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE0E0E0), CircleShape)
                        .clickable { 
                            val newPos = (playbackPosition - 0.2f).coerceAtLeast(0f)
                            playbackPosition = newPos
                            mediaPlayer?.seekTo((newPos * totalDuration).toInt())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Replay10, contentDescription = "Rewind", tint = Color.Black)
                }

                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .width(160.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(40.dp))
                        .clickable { 
                            if (!isPlaying && playbackPosition >= 0.99f) {
                                playbackPosition = 0f
                                mediaPlayer?.seekTo(0)
                            }
                            isPlaying = !isPlaying 
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isPlaying) "Pause" else "Play", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE0E0E0), CircleShape)
                        .clickable { 
                            val newPos = (playbackPosition + 0.2f).coerceAtMost(1f)
                            playbackPosition = newPos
                            mediaPlayer?.seekTo((newPos * totalDuration).toInt())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Forward10, contentDescription = "Forward", tint = Color.Black)
                }
            }

            Spacer(Modifier.height(32.dp))
            
            // Bluetooth connection status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Bluetooth, contentDescription = "Bluetooth", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("oraimo SpaceBuds Neo", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showReminderDialog) {
        SetReminderDialog(
            note = note,
            onDismiss = { showReminderDialog = false },
            onSetReminder = { durationMs ->
                onUpdate(note.copyWith(reminderTime = System.currentTimeMillis() + durationMs))
                scheduleAlarm(context, note, durationMs)
                Toast.makeText(context, "Reminder set!", Toast.LENGTH_SHORT).show()
                showReminderDialog = false
            },
            onCancelReminder = {
                onUpdate(note.copyWith(clearReminder = true))
                cancelAlarm(context, note)
                Toast.makeText(context, "Reminder cancelled!", Toast.LENGTH_SHORT).show()
                showReminderDialog = false
            }
        )
    }
}

@Composable
fun PlaybackScrubber(
    position: Float,
    onPositionChange: (Float) -> Unit
) {
    var width by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (width > 0) onPositionChange((offset.x / width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    if (width > 0) onPositionChange((change.position.x / width).coerceIn(0f, 1f))
                }
            }
            .onSizeChanged { width = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            val trackHeight = 2.dp.toPx()
            val playedWidth = position * size.width
            
            // Inactive track
            drawLine(
                color = Color(0xFFDDDDDD),
                start = Offset(playedWidth, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = trackHeight,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            // Wavy active track
            if (playedWidth > 0f) {
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(0f, centerY)
                
                val waveLength = 32.dp.toPx()
                val amplitude = 5.dp.toPx()
                
                var currentX = 0f
                var isUp = true
                while (currentX < playedWidth) {
                    val nextX = (currentX + waveLength / 2).coerceAtMost(playedWidth)
                    val midX = currentX + (nextX - currentX) / 2
                    val cpY = if (isUp) centerY - amplitude else centerY + amplitude
                    
                    path.quadraticTo(midX, cpY, nextX, centerY)
                    currentX = nextX
                    isUp = !isUp
                }
                
                drawPath(
                    path = path,
                    color = Color.Black,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = trackHeight,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
            
            // Thumb
            drawLine(
                color = Color.Black,
                start = Offset(playedWidth, centerY - 10.dp.toPx()),
                end = Offset(playedWidth, centerY + 10.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun DrawingScreen(
    onBack: () -> Unit,
    onSave: (android.net.Uri) -> Unit
) {
    val context = LocalContext.current
    val lines = remember { mutableStateListOf<DrawingLine>() }
    val redoStack = remember { mutableStateListOf<DrawingLine>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedStrokeWidth by remember { mutableStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }

    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }

    // The actual draw color depends on eraser mode
    val drawColor = if (isEraser) Color.White else selectedColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = "Sketch",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Undo
            IconButton(
                onClick = {
                    if (lines.isNotEmpty()) {
                        redoStack.add(lines.removeLast())
                    }
                },
                enabled = lines.isNotEmpty()
            ) {
                Icon(
                    Icons.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (lines.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Redo
            IconButton(
                onClick = {
                    if (redoStack.isNotEmpty()) {
                        lines.add(redoStack.removeLast())
                    }
                },
                enabled = redoStack.isNotEmpty()
            ) {
                Icon(
                    Icons.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Clear All
            IconButton(
                onClick = { lines.clear(); redoStack.clear(); currentPoints.clear() },
                enabled = lines.isNotEmpty()
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Clear Canvas",
                    tint = if (lines.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            Spacer(Modifier.width(4.dp))

            // Save
            IconButton(
                onClick = {
                    if (lines.isNotEmpty()) {
                        val uri = saveDrawingToDisk(context, lines.toList(), canvasWidth, canvasHeight)
                        if (uri != null) {
                            onSave(uri)
                        }
                    } else {
                        onBack()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Color.White)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge)
                .onSizeChanged { size ->
                    canvasWidth = size.width
                    canvasHeight = size.height
                }
                .pointerInput(drawColor, selectedStrokeWidth) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                lines.add(
                                    DrawingLine(
                                        points = currentPoints.toList(),
                                        color = drawColor,
                                        strokeWidth = if (isEraser) selectedStrokeWidth * 3f else selectedStrokeWidth
                                    )
                                )
                                redoStack.clear() // New stroke clears redo history
                                currentPoints.clear()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw existing lines
                lines.forEach { line ->
                    if (line.points.size > 1) {
                        val path = Path().apply {
                            moveTo(line.points[0].x, line.points[0].y)
                            for (i in 1 until line.points.size) {
                                lineTo(line.points[i].x, line.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = line.color,
                            style = Stroke(
                                width = line.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
                // Draw active drawing line
                if (currentPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = drawColor,
                        style = Stroke(
                            width = if (isEraser) selectedStrokeWidth * 3f else selectedStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        // Bottom Toolbar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tool selection row: Pen / Eraser
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pen tool
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (!isEraser) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .clickable { isEraser = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Pen",
                            tint = if (!isEraser) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Pen",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (!isEraser) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (!isEraser) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Eraser tool
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isEraser) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .clickable { isEraser = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Circle,
                            contentDescription = "Eraser",
                            tint = if (isEraser) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Eraser",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isEraser) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isEraser) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Colors row (only visible when pen tool is active)
            AnimatedVisibility(visible = !isEraser) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val colors = listOf(
                        Color.Black,
                        Color(0xFF424242),
                        Color(0xFFFF5252),
                        Color(0xFFFF6D00),
                        Color(0xFFFFC000),
                        Color(0xFF20C060),
                        Color(0xFF4080FF),
                        Color(0xFF7C4DFF),
                        Color(0xFFE040FB)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colors.forEach { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 34.dp else 28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = if (color == Color.Black || color == Color(0xFF424242)) Color.White else Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Stroke size row
            val sizes = listOf(4f, 8f, 16f, 24f)
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                sizes.forEach { size ->
                    val isSelected = selectedStrokeWidth == size
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent
                            )
                            .clickable { selectedStrokeWidth = size },
                        contentAlignment = Alignment.Center
                    ) {
                        val dotSize = (size / 24f * 18f).coerceAtLeast(4f).dp
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraScreen(
    onPhotoCaptured: (android.net.Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCapture = remember { ImageCapture.Builder().setFlashMode(flashMode).build() }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }
    
    LaunchedEffect(lensFacing, cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
            
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .bounceClickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .bounceClickable {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val flashIcon = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> Icons.Filled.FlashOn
                    ImageCapture.FLASH_MODE_AUTO -> Icons.Filled.FlashAuto
                    else -> Icons.Filled.FlashOff
                }
                Icon(flashIcon, contentDescription = "Flash", tint = Color.White)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.4f))
                .navigationBarsPadding()
                .padding(bottom = 32.dp, top = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp)
                    .size(48.dp)
                    .bounceClickable {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .align(Alignment.Center)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .bounceClickable {
                        val storagePath = File(context.filesDir, "note_images")
                        storagePath.mkdirs()
                        val file = File(storagePath, "image_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        
                        imageCapture.takePicture(
                            outputOptions,
                            cameraExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val uri = android.net.Uri.fromFile(file)
                                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                                    handler.post {
                                        onPhotoCaptured(uri)
                                    }
                                }
                                
                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
            )
        }
    }
}

