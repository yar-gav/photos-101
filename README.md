# Photos101

An Android app for browsing recent photos and searching the Flickr public photo stream. View photo details, get notified when new photos match your search, and open the app from notifications.

## Features

- **Recent photos** – Browse the latest public photos from Flickr in an infinite-scrolling grid.
- **Search** – Search photos by keyword; results update as you type (debounced).
- **Photo detail** – Tap a photo to see it full-screen with title, owner, and date taken.
- **Active search in top bar** – When you have an active search, it’s shown in the top bar and restored when you return to the app (saved in local storage).
- **Background polling** – When you have an active search, the app schedules a background job every 15 minutes to check for new photos (WorkManager).
- **Notifications** – If new photos are found for your search, a notification is shown. Tapping it opens the app to the photos list with your search restored.
- **App icon & splash** – Custom photo-gallery icon with white background.

## Requirements

- **Android Studio** (tested with Arctic Fox or newer)
- **Android SDK**: minSdk 24, targetSdk 36
- **Flickr API key** – The app uses the [Flickr API](https://www.flickr.com/services/api/); you must provide your own key.

## Setup

### 1. Get a Flickr API key

1. Go to [Flickr App Garden](https://www.flickr.com/services/apps/create/).
2. Sign in and create an app (e.g. “Non-Commercial”).
3. Copy the **Key** value.

### 2. Add your API key in `local.properties`

The app reads the Flickr API key from the project’s `local.properties` file (which is not committed to git).

- If you don’t have a `local.properties` file, copy the example:
  ```bash
  cp local.properties.example local.properties
  ```
- Open `local.properties` and set your key:
  ```properties
  sdk.dir=/path/to/your/Android/sdk
  FLICKR_API_KEY=your_flickr_api_key_here
  ```
  Replace `your_flickr_api_key_here` with your actual Flickr API key.  
  Update `sdk.dir` if needed (Android Studio usually sets this for you).

**Important:** Without a valid `FLICKR_API_KEY` in `local.properties`, the app will build but API requests will fail. Keep your key private and do not commit `local.properties`.

## Run the app

1. Open the project in Android Studio.
2. Sync the Gradle project.
3. Connect a device or start an emulator (API 24+).
4. Run **Run → Run 'app'** (or the green Run button).

## Tech stack

- **Kotlin**, **Jetpack Compose**, **Material 3**
- **MVI**-style UI (ViewModel + sealed state)
- **Koin** for dependency injection
- **Retrofit** + **OkHttp** for the Flickr REST API
- **Coil** for image loading
- **DataStore** for persisting active search (poll state)
- **WorkManager** for 15-minute background polling when there is an active search
- **Navigation Compose** for list ↔ detail

## Project structure (high level)

- `app/src/main/java/.../` – App code
  - `data/` – API (Flickr), DTOs, repository, local storage (poll state)
  - `domain/` – Models, repository interface, use cases
  - `di/` – Koin modules
  - `ui/` – Compose screens (photos list, photo detail), ViewModels, navigation
  - `worker/` – WorkManager worker for polling and notifications
- `app/src/main/res/` – Resources (drawables, mipmap, values, themes)

## License

This project is for demonstration purposes. Use of the Flickr API is subject to [Flickr’s API Terms](https://www.flickr.com/services/api/tos/).
<a href="https://www.flaticon.com/free-icons/picture" title="picture icons">Picture icons created by Hasymi - Flaticon</a>
