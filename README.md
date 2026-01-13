# UST Assignment - Android App

Android application with OAuth 2.0 authentication, mDNS device discovery, and public IP information.

## Features

### Part 1: Login
- Google Sign-In using Firebase Authentication (OAuth 2.0)
- Token caching for silent authentication
- Force logout when network is unavailable during silent auth

### Part 2: Home Screen - Device Discovery
- Discovers network devices using mDNS protocol
- Displays devices in RecyclerView with IP address and name
- Stores devices in SQLite, updates status on rediscovery

### Part 3: Detail Screen
- Shows device information
- Fetches public IP from `api.ipify.org`
- Displays geo info from `ipinfo.io`

## Tech Stack

- **Language**: Kotlin
- **UI**: XML
- **Architecture**: MVVM
- **DI**: Hilt
- **Network**: Retrofit
- **Database**: Room
- **Auth**: Firebase + Google Sign-In
- **Discovery**: Android NSD

---

## Setup Instructions

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd ustproj
```

### Step 2: Add Your SHA-1 Fingerprint

Firebase config is included. You need to add your SHA-1 to the Firebase project.

Get your SHA-1:

**Windows:**
```cmd
"C:\Program Files\Android\Android Studio\jbr\bin\keytool" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Mac/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Send your SHA-1 to the project owner to add it to Firebase.

### Step 3: Build and Run

1. Open project in Android Studio
2. Wait for Gradle sync
3. Run on device/emulator

---

## Alternative: Create Your Own Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project
3. Add Android app with package name: `com.ustdemo.assignment`
4. Add your SHA-1 fingerprint
5. Enable **Google Sign-In** in Authentication
6. Download `google-services.json` to `app/` folder
7. Copy Web Client ID to `app/src/main/res/values/strings.xml`

---

## Project Structure

```
app/src/main/java/com/ustdemo/assignment/
├── data/
│   ├── local/          # Room DB, Entity, DAO
│   ├── remote/         # Retrofit APIs, Models
│   └── repository/     # Repositories
├── di/                 # Hilt modules
├── discovery/          # mDNS discovery
├── ui/                 # Fragments, ViewModels, Adapters
└── util/               # Utilities
```

---

## Troubleshooting

**Google Sign-In fails:** Verify your SHA-1 is added to Firebase project.

**No devices found:** Ensure WiFi is connected and you have mDNS-compatible devices (Smart TV, Chromecast, Printer, etc.)
