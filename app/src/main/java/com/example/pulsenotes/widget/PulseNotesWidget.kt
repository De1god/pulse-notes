package com.example.pulsenotes.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.example.pulsenotes.MainActivity
import com.example.pulsenotes.Note
import com.example.pulsenotes.TextNote
import com.example.pulsenotes.loadNotesFromDisk

private val NOTE_ID_KEY = ActionParameters.Key<String>("noteId")

class PulseNotesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val notesData = loadNotesFromDisk(context)
            val notes = notesData?.first?.filter { it.deletedAt == null }?.take(5) ?: emptyList()

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(android.graphics.Color.parseColor("#111111")))
                    .padding(12.dp)
            ) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Pulse Notes",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.parseColor("#fce4a1")),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        modifier = GlanceModifier
                    )
                }

                Spacer(GlanceModifier.height(8.dp))

                if (notes.isEmpty()) {
                    Text(
                        text = "No notes yet. Tap to create one!",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.GRAY),
                            fontSize = 13.sp
                        ),
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                } else {
                    notes.forEachIndexed { index, note ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    val intent = Intent(context, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putExtra("noteId", note.id)
                                    }
                                    context.startActivity(intent)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bulletColor = try {
                                android.graphics.Color.parseColor(note.colorString)
                            } catch (_: Exception) {
                                android.graphics.Color.GRAY
                            }
                            val indicatorSize = 6.dp
                            Box(
                                modifier = GlanceModifier
                                    .width(indicatorSize)
                                    .height(indicatorSize)
                                    .background(ColorProvider(bulletColor)),
                                content = { }
                            )

                            Spacer(GlanceModifier.width(8.dp))

                            val displayText = note.title.ifBlank {
                                when (note) {
                                    is TextNote -> note.content.take(30)
                                    else -> "Untitled"
                                }
                            }.take(35)

                            Text(
                                text = displayText,
                                style = TextStyle(
                                    color = ColorProvider(android.graphics.Color.WHITE),
                                    fontSize = 13.sp
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )

                            if (note.isStarred) {
                                Text(
                                    text = "\u2605",
                                    style = TextStyle(
                                        color = ColorProvider(android.graphics.Color.parseColor("#fce4a1")),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class PulseNotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulseNotesWidget()
}