# Pulse Notes 📝

Pulse Notes is a modern, feature-rich note-taking Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

## ✨ Features

- **Rich Text Editing**: Full formatting capabilities including bold, italics, underline, strikethrough, highlights, headings, code blocks, lists, and more.
- **Modern Jetpack Compose UI**: Clean, responsive Material Design 3 interface with dynamic theming and smooth animations.
- **Home Screen Widget**: Quick glance and access to notes directly from the home screen (`PulseNotesWidget`).
- **Quick Settings Tile**: Add notes quickly from Android's quick settings panel (`QuickNoteTileService`).
- **Reminders & Notifications**: Set custom reminders with alert notifications via `ReminderReceiver`.
- **Search & Filter**: Powerful search with tags, categories, and multiple sorting options.
- **Note Templates & Organization**: Ready-to-use templates for checklists, meeting notes, ideas, and daily logs.
- **Trash & Auto-Cleanup**: Soft-delete trash bin with scheduled background cleanup worker.
- **Export & Import**: Backup and restore notes seamlessly.
- **Multi-language Support**: Localized in English, Spanish (Español), French (Français), German (Deutsch), Italian (Italiano), and Portuguese (Português).

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM with Coroutines & StateFlow
- **Background Tasks**: WorkManager
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/De1god/pulse-notes.git
   ```
2. Open the project in **Android Studio** (Koala / Ladybug or newer recommended).
3. Allow Gradle to sync dependencies.
4. Run the app on an Android emulator (API 26+) or a physical device.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
