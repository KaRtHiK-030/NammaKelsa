<div align="center">

<h1>🚀 NammaKelsa</h1>

<p><strong>Connecting Workers & Hirers on One Smart Platform</strong></p>

<p><em>NammaKelsa is a modern Android application built with Kotlin and Jetpack Compose that bridges the gap between skilled workers and hirers through a seamless, Firebase-powered digital experience.</em></p>

<br/>

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
<img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" />
<img src="https://img.shields.io/badge/Architecture-MVVM-FF6F00?style=for-the-badge" />
<img src="https://img.shields.io/badge/Status-Active-00C853?style=for-the-badge" />
<img src="https://img.shields.io/badge/Min%20SDK-26-blue?style=for-the-badge" />

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Dependencies](#-dependencies)
- [Installation & Setup](#️-installation--setup)
- [Firebase Configuration](#-firebase-configuration)
- [Firestore Collection Schema](#️-firestore-collection-schema)
- [Screenshots](#-screenshots)
- [Future Enhancements](#-future-enhancements)
- [Contributing](#-contributing)
- [License](#-license)
- [Author](#-author)

---

## 📱 Overview

**NammaKelsa** (meaning *"Our Work"* in Kannada) is a two-sided marketplace app that empowers:

- **Workers** — to showcase their skills, manage availability, and connect with hirers
- **Hirers** — to discover, contact, and review trusted local professionals

The app provides a unified platform with real-time chat, smart search, a request management workflow, and a transparent review system — all backed by Firebase.

---

## ✨ Features

### 👷 Worker Module
- Secure worker registration and login via Firebase Auth
- Profile creation with photo, skills, experience, and availability
- Manage and update skill listings
- View incoming hire requests with accept/reject actions
- Access all received reviews and ratings

### 🧑‍💼 Hirer Module
- Hirer authentication (Email/Password via Firebase)
- Browse and search workers by skill or name
- View detailed worker profiles, ratings, and reviews
- Save favorite workers for quick access
- Send work requests directly from the app

### 💬 Real-Time Chat System
- Bidirectional messaging between worker and hirer
- Firebase Realtime Database-powered instant message delivery
- Per-conversation chat rooms with read/unread state

### ⭐ Reviews & Ratings
- Star-based rating system (1–5) submitted after job completion
- Hirer-authored written reviews attached to worker profiles
- Aggregated rating score visible on worker cards and detail pages

### 📩 Request Management
- Full request lifecycle: Pending → Accepted / Rejected
- Status tracking visible to both parties in real time
- Firestore-backed request records with timestamps

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| UI Toolkit | Jetpack Compose (Material 3) |
| Architecture | MVVM + Repository Pattern |
| Authentication | Firebase Authentication |
| Cloud Database | Firebase Cloud Firestore |
| Realtime Sync | Firebase Realtime Database |
| Media Storage | Firebase Storage |
| Async / Concurrency | Kotlin Coroutines + Flow |
| Image Loading | Coil (Compose extension) |
| Navigation | Jetpack Navigation Compose |
| Build System | Gradle (Kotlin DSL) |
| IDE | Android Studio Hedgehog+ |

---

## 🏗️ Architecture

NammaKelsa follows the **MVVM (Model–View–ViewModel)** pattern recommended by Google, combined with the **Repository Pattern** for clean data access separation.

```
UI Layer (Compose Screens)
        │
        ▼
ViewModel (StateFlow / LiveData)
        │
        ▼
Repository (single source of truth)
        │
    ┌───┴────────────────┐
    ▼                    ▼
Firebase Firestore   Firebase Realtime DB
Firebase Auth        Firebase Storage
```

**Data flow:** Screens observe `StateFlow` from ViewModels. ViewModels delegate all data operations to Repositories, which abstract Firebase SDK calls. This keeps UI code clean and testable.

---

## 📂 Project Structure

```
NammaKelsa/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/karthik/nammakelsa/
│   │       │   │
│   │       │   ├── MainActivity.kt               # App entry point, NavHost setup
│   │       │   │
│   │       │   ├── ui/
│   │       │   │   ├── theme/
│   │       │   │   │   ├── Color.kt
│   │       │   │   │   ├── Theme.kt
│   │       │   │   │   └── Type.kt
│   │       │   │   │
│   │       │   │   ├── auth/                     # Authentication screens
│   │       │   │   │   ├── LoginScreen.kt
│   │       │   │   │   ├── RegisterScreen.kt
│   │       │   │   │   └── AuthViewModel.kt
│   │       │   │   │
│   │       │   │   ├── worker/                   # Worker-facing screens
│   │       │   │   │   ├── WorkerHomeScreen.kt
│   │       │   │   │   ├── WorkerProfileScreen.kt
│   │       │   │   │   ├── WorkerEditProfileScreen.kt
│   │       │   │   │   ├── WorkerRequestsScreen.kt
│   │       │   │   │   └── WorkerViewModel.kt
│   │       │   │   │
│   │       │   │   ├── hirer/                    # Hirer-facing screens
│   │       │   │   │   ├── HirerHomeScreen.kt
│   │       │   │   │   ├── WorkerSearchScreen.kt
│   │       │   │   │   ├── WorkerDetailScreen.kt
│   │       │   │   │   ├── FavoriteWorkersScreen.kt
│   │       │   │   │   └── HirerViewModel.kt
│   │       │   │   │
│   │       │   │   ├── chat/                     # Real-time chat
│   │       │   │   │   ├── ChatListScreen.kt
│   │       │   │   │   ├── ChatScreen.kt
│   │       │   │   │   └── ChatViewModel.kt
│   │       │   │   │
│   │       │   │   ├── request/                  # Request management
│   │       │   │   │   ├── SendRequestScreen.kt
│   │       │   │   │   ├── RequestDetailScreen.kt
│   │       │   │   │   └── RequestViewModel.kt
│   │       │   │   │
│   │       │   │   └── review/                   # Reviews & ratings
│   │       │   │       ├── WriteReviewScreen.kt
│   │       │   │       ├── ReviewListScreen.kt
│   │       │   │       └── ReviewViewModel.kt
│   │       │   │
│   │       │   ├── data/
│   │       │   │   ├── model/                    # Data models / entities
│   │       │   │   │   ├── User.kt
│   │       │   │   │   ├── Worker.kt
│   │       │   │   │   ├── Hirer.kt
│   │       │   │   │   ├── ChatMessage.kt
│   │       │   │   │   ├── Request.kt
│   │       │   │   │   └── Review.kt
│   │       │   │   │
│   │       │   │   └── repository/               # Firebase data access layer
│   │       │   │       ├── AuthRepository.kt
│   │       │   │       ├── WorkerRepository.kt
│   │       │   │       ├── HirerRepository.kt
│   │       │   │       ├── ChatRepository.kt
│   │       │   │       ├── RequestRepository.kt
│   │       │   │       └── ReviewRepository.kt
│   │       │   │
│   │       │   └── utils/                        # Helpers & constants
│   │       │       ├── Constants.kt
│   │       │       ├── Extensions.kt
│   │       │       └── NavRoutes.kt
│   │       │
│   │       └── res/
│   │           ├── drawable/
│   │           ├── mipmap/
│   │           └── values/
│   │               ├── strings.xml
│   │               └── colors.xml
│   │
│   ├── google-services.json                      # ← Add your own (not committed)
│   └── build.gradle.kts                          # App-level Gradle config
│
├── gradle/
│   └── libs.versions.toml                        # Centralized dependency versions
├── build.gradle.kts                              # Project-level Gradle config
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## ✅ Prerequisites

Before you begin, ensure you have the following installed:

| Tool | Minimum Version | Download |
|---|---|---|
| Android Studio | Hedgehog (2023.1.1) | [Download](https://developer.android.com/studio) |
| JDK | 17 | Bundled with Android Studio |
| Android SDK | API 26 (min) / API 34 (target) | Via SDK Manager |
| Kotlin | 1.9.x | Bundled with Android Studio |
| Gradle | 8.2+ | Auto-downloaded via wrapper |
| Git | Latest | [Download](https://git-scm.com) |
| Firebase Account | — | [console.firebase.google.com](https://console.firebase.google.com) |

---

## 📦 Dependencies

Below is a reference for all major libraries used. Versions are managed centrally in `gradle/libs.versions.toml`.

### Core Android
```toml
[versions]
kotlin                 = "1.9.22"
agp                    = "8.2.2"
core-ktx               = "1.12.0"
lifecycle              = "2.7.0"
activity-compose       = "1.8.2"

[libraries]
androidx-core-ktx        = { module = "androidx.core:core-ktx",                          version.ref = "core-ktx" }
lifecycle-runtime-ktx    = { module = "androidx.lifecycle:lifecycle-runtime-ktx",        version.ref = "lifecycle" }
lifecycle-viewmodel      = { module = "androidx.lifecycle:lifecycle-viewmodel-compose",  version.ref = "lifecycle" }
activity-compose         = { module = "androidx.activity:activity-compose",              version.ref = "activity-compose" }
```

### Jetpack Compose (BOM)
```toml
[versions]
compose-bom = "2024.02.00"

[libraries]
compose-bom             = { module = "androidx.compose:compose-bom",           version.ref = "compose-bom" }
compose-ui              = { module = "androidx.compose.ui:ui" }
compose-ui-graphics     = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling      = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3       = { module = "androidx.compose.material3:material3" }
compose-icons-extended  = { module = "androidx.compose.material:material-icons-extended" }
```

### Navigation
```toml
[versions]
navigation-compose = "2.7.7"

[libraries]
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
```

### Firebase (BOM)
```toml
[versions]
firebase-bom = "32.7.2"

[libraries]
firebase-bom            = { module = "com.google.firebase:firebase-bom",       version.ref = "firebase-bom" }
firebase-auth           = { module = "com.google.firebase:firebase-auth-ktx" }
firebase-firestore      = { module = "com.google.firebase:firebase-firestore-ktx" }
firebase-database       = { module = "com.google.firebase:firebase-database-ktx" }
firebase-storage        = { module = "com.google.firebase:firebase-storage-ktx" }
```

### Coroutines
```toml
[versions]
coroutines = "1.7.3"

[libraries]
coroutines-core    = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core",    version.ref = "coroutines" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
```

### Image Loading
```toml
[versions]
coil = "2.6.0"

[libraries]
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
```

### `app/build.gradle.kts` — Dependencies Block
```kotlin
dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.activity.compose)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons.extended)

    // Navigation
    implementation(libs.navigation.compose)

    // Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Image Loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## ⚙️ Installation & Setup

### Step 1 — Clone the Repository
```bash
git clone https://github.com/KaRtHiK-030/NammaKelsa.git
cd NammaKelsa
```

### Step 2 — Open in Android Studio
1. Launch **Android Studio**
2. Select **File → Open** and choose the `NammaKelsa/` folder
3. Wait for the IDE to index the project

### Step 3 — Configure Firebase
> See [Firebase Configuration](#-firebase-configuration) below for full instructions.

Place your `google-services.json` inside the `app/` directory:
```
NammaKelsa/
└── app/
    └── google-services.json    ← here
```

### Step 4 — Sync Gradle
Click **"Sync Now"** in the notification bar, or run:
```bash
./gradlew build
```

### Step 5 — Run the App
- Connect a physical Android device (API 26+) via USB with Developer Options enabled, **or**
- Start an Android Virtual Device (AVD) from **Device Manager → Create Device**
- Press **Run ▶** (Shift + F10)

---

## 🔥 Firebase Configuration

### 1. Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** → enter project name → follow the wizard
3. Add an **Android app** with package name `com.karthik.nammakelsa`
4. Download `google-services.json` and place it in `app/`

### 2. Enable Firebase Services

Navigate to the Firebase Console and enable each service:

| Service | Console Path | Notes |
|---|---|---|
| Authentication | Build → Authentication → Sign-in method | Enable **Email/Password** |
| Cloud Firestore | Build → Firestore Database | Create in **production mode**, set rules below |
| Realtime Database | Build → Realtime Database | Start in **test mode** initially |
| Storage | Build → Storage | Default bucket |

### 3. Firestore Security Rules (Recommended)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can read/write their own document
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Workers collection — any authenticated user can read
    match /workers/{workerId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == workerId;
    }

    // Requests — sender or receiver can read/write
    match /requests/{requestId} {
      allow read, write: if request.auth != null &&
        (request.auth.uid == resource.data.hirerId ||
         request.auth.uid == resource.data.workerId);
    }

    // Reviews — authenticated users can read; hirers can write
    match /reviews/{reviewId} {
      allow read: if request.auth != null;
      allow create: if request.auth.uid == request.resource.data.hirerId;
    }
  }
}
```

### 4. Realtime Database Rules (Chat)
```json
{
  "rules": {
    "chats": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

---

## 🗄️ Firestore Collection Schema

NammaKelsa uses **Cloud Firestore** for all persistent data (profiles, requests, reviews, favorites) and **Firebase Realtime Database** exclusively for chat messages. Below is the complete schema for every collection.

> **Document ID convention:** Unless stated otherwise, the document ID equals the Firebase Auth `uid` of the owning user, enabling direct lookups without extra queries.

---

### `users` collection
Stores the base account record created on registration for both roles.

```
users/
└── {uid}                         ← document ID = Firebase Auth UID
    ├── uid            : String   # mirrors the document ID
    ├── email          : String   # registered email address
    ├── role           : String   # "worker" | "hirer"
    ├── displayName    : String   # full name
    ├── photoUrl       : String   # Firebase Storage download URL (nullable)
    ├── phone          : String   # contact number (nullable)
    └── createdAt      : Timestamp
```

---

### `workers` collection
Extended profile for users whose `role == "worker"`. Document ID mirrors the Auth UID.

```
workers/
└── {workerId}                         ← document ID = Firebase Auth UID
    ├── workerId       : String        # mirrors document ID
    ├── name           : String        # display name
    ├── email          : String
    ├── phone          : String
    ├── photoUrl       : String        # profile picture URL
    ├── bio            : String        # short self-description
    ├── skills         : Array<String> # e.g. ["Plumbing", "Electrical"]
    ├── experience     : String        # e.g. "3 years"
    ├── location       : String        # city / area
    ├── isAvailable    : Boolean       # availability toggle
    ├── averageRating  : Number        # recomputed on each new review (0.0–5.0)
    ├── totalReviews   : Number        # count of reviews received
    └── createdAt      : Timestamp
```

---

### `hirers` collection
Extended profile for users whose `role == "hirer"`.

```
hirers/
└── {hirerId}                          ← document ID = Firebase Auth UID
    ├── hirerId        : String
    ├── name           : String
    ├── email          : String
    ├── phone          : String
    ├── photoUrl       : String
    ├── companyName    : String        # optional
    ├── location       : String
    └── createdAt      : Timestamp
```

---

### `requests` collection
Tracks every hire request sent by a hirer to a worker.

```
requests/
└── {requestId}                        ← auto-generated document ID
    ├── requestId      : String        # mirrors document ID
    ├── hirerId        : String        # UID of the hirer who sent the request
    ├── hirerName      : String        # denormalised for display
    ├── workerId       : String        # UID of the targeted worker
    ├── workerName     : String        # denormalised for display
    ├── jobTitle       : String        # e.g. "Fix kitchen sink"
    ├── jobDescription : String        # detailed description
    ├── status         : String        # "pending" | "accepted" | "rejected"
    ├── createdAt      : Timestamp
    └── updatedAt      : Timestamp     # updated on status change
```

**Indexes required:**
- `workerId ASC, createdAt DESC` — load all requests for a worker
- `hirerId ASC, createdAt DESC` — load all requests sent by a hirer

---

### `reviews` collection
Reviews written by hirers after a job is completed.

```
reviews/
└── {reviewId}                         ← auto-generated document ID
    ├── reviewId       : String
    ├── workerId       : String        # worker being reviewed
    ├── hirerId        : String        # hirer who wrote the review
    ├── hirerName      : String        # denormalised
    ├── hirerPhotoUrl  : String        # denormalised
    ├── rating         : Number        # 1 – 5
    ├── comment        : String        # written feedback
    ├── jobTitle       : String        # context for the review
    └── createdAt      : Timestamp
```

**Index required:**
- `workerId ASC, createdAt DESC` — fetch all reviews for a worker in chronological order

---

### `favorites` subcollection
Stored as a subcollection under each hirer document to keep favorites private per user.

```
hirers/
└── {hirerId}/
    └── favorites/
        └── {workerId}                 ← document ID = worker's UID
            ├── workerId   : String
            ├── workerName : String    # denormalised for list display
            ├── photoUrl   : String    # denormalised
            ├── skills     : Array<String>
            └── savedAt    : Timestamp
```

---

### Realtime Database — `chats` node
Chat messages are stored in Firebase **Realtime Database** (not Firestore) for low-latency streaming. The chat room ID is a deterministic composite of both UIDs, sorted alphabetically to ensure uniqueness regardless of who initiates.

```
chats/
└── {chatId}                           ← "{smallerUid}_{largerUid}" (sorted)
    └── messages/
        └── {messageId}                ← push() auto ID
            ├── senderId   : String    # UID of the sender
            ├── senderName : String    # denormalised
            ├── message    : String    # message body
            ├── timestamp  : Long      # Unix ms (ServerValue.TIMESTAMP)
            └── isRead     : Boolean
```

**Chat ID generation (Kotlin):**
```kotlin
fun chatId(uid1: String, uid2: String): String =
    listOf(uid1, uid2).sorted().joinToString("_")
```

---

### Schema Relationships

```
users ──────────────────────────────────────────────┐
  │                                                  │
  ├──► workers/{workerId}                            │
  │         └── (averageRating ← aggregated         │
  │              from reviews collection)            │
  │                                                  │
  ├──► hirers/{hirerId}                              │
  │         └── favorites/{workerId}  (subcollection)│
  │                                                  │
  ├──► requests/{requestId}                          │
  │         (hirerId + workerId foreign keys)        │
  │                                                  │
  └──► reviews/{reviewId}                            │
            (hirerId + workerId foreign keys)        │
                                                     │
Realtime DB:  chats/{uid1_uid2}/messages  ◄──────────┘
```

---

## 📸 Screenshots

<div align="center">

<table>
  <tr>
    <td align="center"><b>Splash / Login</b></td>
    <td align="center"><b>Worker Home</b></td>
    <td align="center"><b>Hirer Search</b></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/d601de8e-3708-431f-b676-be60fbf518fd" width="220"/></td>
    <td><img src="https://github.com/user-attachments/assets/ac19b690-8cf0-4281-9f46-3eb5cd21031a" width="220"/></td>
    <td><img src="https://github.com/user-attachments/assets/62e66910-6179-4c21-8ee2-72edfa9609a7" width="220"/></td>
  </tr>
  <tr>
    <td align="center"><b>Worker Profile</b></td>
    <td align="center"><b>Chat</b></td>
    <td align="center"><b>Request Management</b></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/27f52b86-5248-413d-ba8b-4fa5cfc1db32" width="220"/></td>
    <td><img src="https://github.com/user-attachments/assets/b427a371-677d-4765-98f7-b2232e713188" width="220"/></td>
    <td><img src="https://github.com/user-attachments/assets/382eaca8-4ddc-4db3-b0d8-d84fe54202c2" width="220"/></td>
  </tr>
  <tr>
    <td align="center"><b>Favorites</b></td>
    <td align="center"><b>Reviews</b></td>
    <td align="center"><b>Hirer Dashboard</b></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/aa5117bb-082a-4374-a187-09fbdad74bfb" width="220"/></td>
    <td><img src="https://github.com/user-attachments/assets/d69409e9-f19e-4ebf-a20b-dbc45f7c6ef8" width="220"/></td>
    <td><img src="https://github.com/user-attachments/assets/b3a15543-dcce-4330-b92d-9e326d4632c0" width="220"/></td>
  </tr>
</table>

</div>

---

## 📈 Future Enhancements

| Feature | Description |
|---|---|
| 🔔 Push Notifications | FCM-based alerts for new messages, requests, and reviews |
| 💳 Payment Gateway | In-app payment support via Razorpay / Stripe |
| 📍 Location-Based Search | Discover nearby workers using GPS and Geofirestore |
| 🛡️ Admin Dashboard | Web-based dashboard for user moderation and analytics |
| 🌐 Multi-language Support | Kannada, Hindi, Tamil, and English localisation |
| 🤖 AI Recommendations | Skill-matched worker suggestions using ML Kit |
| ✅ Worker Verification | Government ID-based background check badge |
| 📊 Analytics | Firebase Analytics + Crashlytics integration |

---

## 🤝 Contributing

Contributions are welcome and appreciated!

1. **Fork** the repository
2. **Create** a feature branch
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit** your changes with a descriptive message
   ```bash
   git commit -m "feat: add location-based worker search"
   ```
4. **Push** to your branch
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Open** a Pull Request against `main`

Please follow the existing code style (Kotlin conventions, Compose best practices) and include a brief description of your changes in the PR.

---

## 📄 License

This project is developed for educational and learning purposes. All rights reserved by the author.

---

## 👨‍💻 Author

<div align="center">

### Karthik Naik
**BE in Computer Science Engineering**

Android Developer · Firebase Enthusiast · Kotlin Learner

[![GitHub](https://img.shields.io/badge/GitHub-KaRtHiK--030-181717?style=for-the-badge&logo=github)](https://github.com/KaRtHiK-030)

</div>

---

<div align="center">
  <sub>If you found this project helpful, please consider giving it a ⭐ on GitHub!</sub>
</div>