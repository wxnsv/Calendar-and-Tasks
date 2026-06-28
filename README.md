# Calendar App — Google Calendar & Tasks Client

**Calendar App** is a minimalist, responsive, and functional personal organizer for Android. The application is a lightweight client for **Google Calendar** and **Google Tasks**, designed to streamline your daily schedule and eliminate unnecessary distractions.

The project is built using modern Android development practices, showcasing a clean MVVM architecture and a fully open-source codebase.

---

## 🚀 Core Features

* **Dual-View Interface:** Easily switch between two display paradigms: a continuous unified stream of all upcoming events and tasks (Unified List stream) or a precise split screen layout (Split layout).
* **Smart Split Synchronization:** In Split mode, the top half displays a semi-static calendar grid while the bottom hosts a scrollable agenda feed. Scrolling through the list dynamically realigns and updates the calendar grid focus based on currently visible items.
* **Essential Task Management:** Create, edit, and sync tasks directly via the official Google Tasks API. Seamlessly assign tasks to specific pre-existing task lists during the creation flow.
* **Clean Event Scheduling:** Quickly add and edit events in Google Calendar. Supports all essential customization elements: title, date, time, description, and event color-coding.

---

## 🛠 Technical Architecture & Stack

The application follows modern Android development standards, separating reactive UI states from reliable asynchronous business logic. The entire codebase is structured around the **MVVM** pattern.

### Dependency Stack:

* **UI & Interface Components:**
  * *Jetpack Compose (Material 3 & Material)* — for building a fully declarative user interface.
  * *Kizitonwose Compose Calendar* — a highly flexible calendar grid component.
  * Traditional XML components (*ConstraintLayout, RecyclerView, SwipeRefreshLayout*) for optimal combining of approaches.
  * *Jetpack Core SplashScreen API* — for a smooth and clean application launch.
* **Asynchronous Flow & Threading:**
  * *Kotlin Coroutines & Flow* (including extensions for Android and Play Services).
  * *Channels* are utilized to create a predictable, event-driven, and loosely coupled navigation processing architecture.
* **Networking & API Integration:**
  * *Retrofit & OkHttp* (with configured Logging Interceptors) for reliable remote infrastructure interactions.
  * *Moshi* — for fast and safe JSON serialization/deserialization.
* **Google Cloud Ecosystem APIs:**
  * *Google API Services Calendar* & *Google API Services Tasks*.
  * *Google API Client Android*.
  * *AndroidX Credentials Manager* & *Play Services Auth* for secure user authentication via Google Accounts.
* **Local Storage & Background Processing:**
  * *Room Database* — for local structured caching and offline-first support.
  * *Jetpack DataStore (Preferences)* — for lightweight user preference persistence.
  * *AndroidX WorkManager* — for guaranteed execution of background data sync tasks even when the app is closed.
* **Dependency Injection (DI):**
  * *Koin DI* (featuring native `koin-androidx-compose` and `koin-androidx-workmanager` integrations).
* **Image Processing:**
  * *Coil* — for asynchronous, coroutine-backed loading of user avatars and other graphical resources.
* **Testing Environment:**
  * *JUnit Jupiter (JUnit 5)* — a modern architecture platform for unit testing.
  * *MockK* — a powerful mocking library built specifically for Kotlin.
  * *Kotlinx Coroutines Test framework* — for deterministic testing of asynchronous execution flows.

---

## 🔗 Links & Resources

* **Project Source Code:** [GitHub Repository](https://github.com/wxnsv/calendar-and-tasks)
* **Privacy Policy:** [Privacy Policy](https://sites.google.com/view/calendarandtasks)

---
📦 *Developed in 2026 using Kotlin and Jetpack Compose.*
