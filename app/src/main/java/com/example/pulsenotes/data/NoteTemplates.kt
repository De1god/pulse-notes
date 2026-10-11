package com.example.pulsenotes.data

import com.example.pulsenotes.ChecklistItem
import com.example.pulsenotes.ChecklistNote
import com.example.pulsenotes.TextNote
import com.example.pulsenotes.Note
import java.util.UUID

data class NoteTemplate(
    val name: String,
    val icon: String,
    val description: String,
    val createNote: () -> Note
)

object NoteTemplates {
    val all = listOf(
        NoteTemplate(
            name = "Meeting Notes",
            icon = "meeting",
            description = "Structured notes with attendees, agenda, and action items"
        ) {
            TextNote(
                id = UUID.randomUUID().toString(),
                title = "Meeting Notes",
                content = """## Agenda
- Topic 1
- Topic 2
- Topic 3

## Attendees
-

## Action Items
- [ ] 
- [ ] 
- [ ] 

## Notes
""",
                colorString = "#fce4a1"
            )
        },
        NoteTemplate(
            name = "Shopping List",
            icon = "shopping",
            description = "Checklist with common grocery categories"
        ) {
            ChecklistNote(
                id = UUID.randomUUID().toString(),
                title = "Shopping List",
                items = listOf(
                    ChecklistItem("Vegetables", false),
                    ChecklistItem("Fruits", false),
                    ChecklistItem("Dairy", false),
                    ChecklistItem("Bread", false),
                    ChecklistItem("", false)
                ),
                colorString = "#8df0b0"
            )
        },
        NoteTemplate(
            name = "Daily Journal",
            icon = "journal",
            description = "Daily reflection with prompts"
        ) {
            TextNote(
                id = UUID.randomUUID().toString(),
                title = "Journal - ${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
                content = """## Today's Highlights
- 

## What went well?
- 

## What could be better?
- 

## Gratitude
- 

## Tomorrow's Focus
- """,
                colorString = "#ffffff"
            )
        },
        NoteTemplate(
            name = "To-Do List",
            icon = "todo",
            description = "Simple task checklist"
        ) {
            ChecklistNote(
                id = UUID.randomUUID().toString(),
                title = "To-Do List",
                items = listOf(
                    ChecklistItem("", false),
                    ChecklistItem("", false),
                    ChecklistItem("", false),
                    ChecklistItem("", false)
                ),
                colorString = "#cdabfe"
            )
        },
        NoteTemplate(
            name = "Recipe",
            icon = "recipe",
            description = "Recipe with ingredients and instructions"
        ) {
            TextNote(
                id = UUID.randomUUID().toString(),
                title = "Recipe",
                content = """## Ingredients
- 
- 
- 

## Instructions
1. 
2. 
3. 

## Notes
""",
                colorString = "#ffa3a5"
            )
        },
        NoteTemplate(
            name = "Project Plan",
            icon = "project",
            description = "Project overview with goals and timeline"
        ) {
            TextNote(
                id = UUID.randomUUID().toString(),
                title = "Project Plan",
                content = """## Project Name

## Goals
- 
- 

## Timeline
- Start: 
- Milestone 1: 
- Milestone 2: 
- Deadline: 

## Resources
- 

## Status
""",
                colorString = "#fce4a1"
            )
        },
        NoteTemplate(
            name = "Quick Note",
            icon = "note",
            description = "Blank text note"
        ) {
            TextNote(
                id = UUID.randomUUID().toString(),
                title = "",
                content = "",
                colorString = "#ffffff"
            )
        }
    )
}