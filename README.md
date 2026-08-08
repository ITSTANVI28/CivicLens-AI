# 🏛️ CivicLens AI — Smart Civic Issue Reporting & Resolution Platform

> **CivicLens AI** is an AI-powered, community-driven civic engagement platform that empowers citizens to report urban infrastructure hazards (potholes, garbage dumps, water leaks, damaged streetlights, open manholes) using computer vision, real-time spatial mapping, automated municipal dispatching, and crowdsourced verification.

---

## 🌟 Key Features & Capabilities

### 1. 🚀 Material 3 Animated Splash Screen (`SplashActivity`)
- Official Android 12+ `androidx.core:core-splashscreen` API integration.
- Sleek dark slate (`#0F172A`) launch theme with 3D metallic brand logo entrance animation, tagline, and instant session routing (`MainActivity` vs `LoginActivity`).

### 2. 🤖 Gemini AI Vision Triage Engine (`GeminiTriageService`)
- Multimodal computer vision analysis using Google Gemini 1.5 Flash.
- Automatically analyzes hazard photos to extract:
  - Concise Hazard Title & Summary
  - Issue Category Taxonomy (`POTHOLE`, `GARBAGE`, `WATER_LEAK`, `STREETLIGHT`, `MANHOLE`)
  - Structural Hazard Severity (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`)
  - Target Municipal Department Tagging (`Public Works Dept`, `Water Board`, etc.)

### 3. ⚡ 1-Tap Emergency Hazard SOS Mode
- Single-button emergency dispatch for critical life-threatening hazards (e.g., open manholes, fallen live power lines).
- Instantly captures GPS location and dispatches a **24-Hour Priority SLA Ticket** to Disaster Management (+100 Karma Points).

### 4. 📄 Storage Access Framework (SAF) PDF Ticket Generator (`PdfReportGenerator`)
- Prompts citizens with Android's system file picker (`Intent.ACTION_CREATE_DOCUMENT`) allowing custom folder selection (`Downloads`, `Documents`, `Google Drive`).
- Renders an **Official Municipal Work Order PDF**:
  - Slate Navy Header & Work Order Badge
  - Severity Level Color Pill & SLA Resolution Window
  - 2-Column Incident Metadata Table Grid
  - 50m Spatial Deduplication Verification Seal
  - Gemini AI Triage Blockquote
  - Green **COMMUNITY AUDIT SEAL** (SHA256 Security Hash)
  - Authorized Municipal Engineer Signature Line

### 5. ↔️ Proof-of-Fix Before/After Visual Comparison
- Interactive side-by-side visual comparison card on `IssueDetailActivity`.
- Displays the original citizen reported photo (**BEFORE** red badge) against the municipal crew's resolved result (**AFTER FIXED** green badge) loaded via Glide.

### 6. 📍 Smart Geocoder & Pune Sector Resolver (`GeoLocationResolver`)
- Converts typed address text (e.g. `FC Road, Pune`, `Kothrud`, `Viman Nagar`, `Baner`, `Pimpri-Chinchwad`) to exact GPS coordinates using `android.location.Geocoder`.
- Features an offline Pune sector fallback mapper and centers the live map directly on **Pune City Center (`18.5204, 73.8567`)**.

### 7. 🌐 Offline Room Database Engine (`AppDatabase`)
- Offline-first caching architecture using `androidx.room:room-runtime:2.6.1`.
- Submissions created without active cellular network are queued locally in `civic_issues` table and auto-synced upon reconnecting.

### 8. 🛡️ 50m Spatial Deduplication & Privacy Guard
- **50m Haversine Radius**: Checks active issues within 50 meters. If duplicate, merges submissions into a single master ticket and awards +50 upvotes/karma to reporter.
- **Privacy Blur Engine**: Automatically anonymizes faces and license plates prior to public map display.

---

## 📦 Production Android UI Libraries & Dependencies

| Library | Version | Usage / Purpose |
| :--- | :--- | :--- |
| **Material Components** | `1.14.0` | Google Material 3 Design System, Buttons, Cards, Chips |
| **Airbnb Lottie** | `6.4.0` | Smooth Vector Lottie Animations for Splash & Loading |
| **Meta Shimmer** | `0.5.0` | Skeleton Shimmer Loaders for Feed & Cards |
| **CircleImageView** | `3.1.0` | Circular Avatar Rendering for Profiles |
| **SwipeRefreshLayout**| `1.1.0` | Pull-to-Refresh Gesture for Feed & Issue Lists |
| **Room Database** | `2.6.1` | Local SQLite ORM Engine for Offline Persistence |
| **Core SplashScreen** | `1.0.1` | Android 12+ Official Splash Screen API |
| **Glide** | `4.16.0` | High-Performance Image Caching & Rendering |
| **osmdroid** | `6.1.18` | OpenStreetMap CartoDB Voyager Custom Tiles |

---

## 🏗️ Architecture & Tech Stack

```
 ┌──────────────────────────────────────────────────────────────┐
 │                    Android Native Client                     │
 │  • Java 17 / Android SDK Target 36                          │
 │  • ViewBinding, Material 3 Guidelines, Zero Raw Emojis       │
 │  • osmdroid OpenStreetMap Engine (CartoDB Voyager Tiles)     │
 └──────────────────────────────┬───────────────────────────────┘
                                │
                      HTTPS REST / Native APIs
                                │
 ┌──────────────────────────────▼───────────────────────────────┐
 │                      Core Engine Layer                       │
 │  • Gemini 1.5 Flash Vision Triage API                        │
 │  • Storage Access Framework (SAF) PDF Ticket Generator        │
 │  • 50m Haversine Spatial Deduplication Engine                │
 │  • GeoLocationResolver (Android Geocoder + Pune Sectors)     │
 │  • Room Database (SQLite Offline Cache & Sync Queue)         │
 └──────────────────────────────────────────────────────────────┘
```

---

## 📁 Repository Structure

```
CivicLens-AI/
├── app/
│   ├── src/main/java/com/example/civiclensai/
│   │   ├── ai/               # GeminiTriageService (Vision API)
│   │   ├── db/               # Room DB (AppDatabase, CivicIssueEntity, CivicIssueDao)
│   │   ├── models/           # CivicIssue, IssueCategory, IssueSeverity, VerificationModel
│   │   ├── repository/       # IssueRepository (Haversine Deduplication Engine)
│   │   ├── ui/               # MainActivity, MapFragment, FeedFragment, ReportFragment,
│   │   │   │                 # IssueDetailActivity, LeaderboardFragment
│   │   │   ├── auth/         # SplashActivity, LoginActivity, RegisterActivity
│   │   │   └── profile/      # ProfileFragment, EditProfileActivity
│   │   └── utils/            # GeoLocationResolver, PdfReportGenerator, PrivacyBlurEngine,
│   │                         # NotificationHelper, SessionManager, ProximityAlertHelper
│   └── src/main/res/         # Material 3 Layouts, Drawables, Mipmaps, Values
├── gradle/                   # Gradle wrapper & libs.versions.toml
├── build.gradle              # Project build configuration
└── README.md                 # Master Documentation
```

---

## 🧪 Build & Verification Instructions

### 1. Build Verification
Run the standard Gradle check task from the project root:
```powershell
./gradlew check --no-daemon
```

### 2. Debug APK Assembly
Compile the debug build bundle:
```powershell
./gradlew assembleDebug --no-daemon
```

---

## 📜 License & Copyright

Designed and developed for **CivicLens AI Platform**.  
*Empowering citizens, streamlining municipal dispatch, and building safer cities.*
