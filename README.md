# Calendar and Tasks — Google Calendar & Tasks Client

**Calendar and Tasks** is a minimalist, responsive, and functional personal organizer for Android. The application is a lightweight client for **Google Calendar** and **Google Tasks**, designed to streamline your daily schedule and eliminate unnecessary distractions.

The project is built using modern Android development methods, uses a clean MVVM & MVI architectures and a fully open codebase.

---

## Core Features

* **Interface with two display modes:** Easy switching between two display modes: a familiar list or combining a calendar and a list.
* **Intelligent split synchronization:** In split mode, a calendar grid is displayed in the upper half, and a scrollable list of events is displayed in the lower half. When scrolling through the list, the focus of the calendar grid is dynamically rearranged and updated based on the currently displayed items.
* **Main Task Management Features:** Create, edit, and sync tasks directly through the official Google Tasks API. During the creation process, easily assign tasks to specific task lists that already exist.
* **Clear planning of events and birthdays:** Quickly add and edit events or birthdays in Google Calendar. Supports all necessary customization elements: name, date, time, description and color tag of the event.

---

## Technical Architecture & Stack

The application follows modern Android development standards, separating reactive UI states from reliable asynchronous business logic. The entire codebase is structured around the **MVVM & MVI** patterns.

### Dependency Stack:

* **UI & Interface Components:**
  * *Jetpack Compose* — for building a fully declarative user interface.
  * *Kizitonwose Compose Calendar* — a highly flexible calendar grid component.
  * *AndroidX Navigation* - for a navigate the fragments.
  * Traditional XML components for optimal combining of approaches.
* **Asynchronous Flow & Threading:**
  * *Kotlin Coroutines & Flow*.
  * *Channels* are utilized to create a predictable, event-driven, and loosely coupled navigation processing architecture.
* **Network & API Integration:**
  * *Retrofit & OkHttp* for reliable remote infrastructure interactions.
* **Google APIs and Auth:**
  * *Google Calendar v3* & *Google Tasks v1*.
  * *AndroidX Credentials Manager* for secure user authentication via Google Accounts.
* **Local Storage & Background Processing:**
  * *Room Database* — for local structured caching entities and offline-first support.
  * *Jetpack DataStore (Preferences)* — for lightweight user preference persistence.
  * *AndroidX WorkManager* — for guaranteed execution of background data sync tasks even when the app is closed.
* **Dependency Injection (DI):**
  * *Koin DI*
* **Image Processing:**
  * *Coil* — for loading of user avatar.
* **Testing Environment:**
  * *JUnit Jupiter (JUnit 5)* — a modern architecture platform for unit testing.
  * *MockK* — a powerful mocking library built specifically for Kotlin.

---

## 🔗 Links & Resources

* **Project Site:** [App site](http://calendarandtasks.xyz/)
* **Project Source Code:** [GitHub Repository](https://github.com/wxnsv/calendar-and-tasks)
* **Privacy Policy:** [Privacy Policy](http://calendarandtasks.xyz/privacy)

---
📦 *Developed in 2026 using Kotlin and Jetpack Compose.*
