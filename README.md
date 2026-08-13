# 🏛️ CivicLens AI — Smart Civic Issue Reporting & Resolution Platform

> **CivicLens AI** is an AI-powered, community-driven civic engagement Android application that empowers citizens to report urban infrastructure hazards — potholes, garbage dumps, water leaks, damaged streetlights, and open manholes — using computer vision (Google Gemini 1.5 Flash), real-time spatial mapping (OpenStreetMap), automated municipal dispatching, 50-meter spatial deduplication, PDF work-order generation, and crowdsourced community verification with a gamified karma system.

---

## 📋 Table of Contents

1. [Key Features & Capabilities](#-key-features--capabilities)
2. [System Architecture](#-system-architecture)
3. [Complete File & Class Reference](#-complete-file--class-reference)
4. [Data Models & Enumerations](#-data-models--enumerations)
5. [AI Engine — Gemini Triage Service](#-ai-engine--gemini-triage-service)
6. [Spatial Algorithms & Geolocation](#-spatial-algorithms--geolocation)
7. [Analytics & Municipal Intelligence](#-analytics--municipal-intelligence)
8. [Database Layer (Room + Firebase)](#-database-layer-room--firebase)
9. [Background Sync & WorkManager](#-background-sync--workmanager)
10. [UI Screens & Navigation](#-ui-screens--navigation)
11. [PDF Report Generator](#-pdf-report-generator)
12. [Notification System](#-notification-system)
13. [Privacy & Security](#-privacy--security)
14. [Gamification & Karma System](#-gamification--karma-system)
15. [Dependencies & Libraries](#-dependencies--libraries)
16. [Unit Test Coverage](#-unit-test-coverage)
17. [Build & Run Instructions](#-build--run-instructions)
18. [Android Manifest & Permissions](#-android-manifest--permissions)
19. [Design System & Theme](#-design-system--theme)
20. [Repository Structure](#-repository-structure)

---

## 🌟 Key Features & Capabilities

### 1. 🤖 Gemini AI Vision Triage Engine
- **Google Gemini 1.5 Flash** multimodal vision API analyzes hazard photos captured by the citizen.
- Returns structured JSON with: **category**, **severity**, **title**, **description**, **department**, **repair cost estimate**, **recommended material**, and **hazard risk score** (0.0–10.0).
- Graceful **fallback simulation** when no API key is configured — uses image pixel-hash-based deterministic classification.

### 2. 📸 Multi-Source Photo Capture
- Real-time **camera capture** via `ActivityResultContracts.TakePicturePreview()`.
- **Gallery import** via `ActivityResultContracts.GetContent()` with `image/*` MIME filter.
- Fallback **procedural image generation** with on-canvas text overlay for demo mode.

### 3. 📍 Live GPS & Smart Geocoding
- Real-time GPS acquisition via `FusedLocationProviderClient` with runtime permission handling.
- **Android Geocoder API** for text→GPS coordinate resolution.
- Offline **Pune Sector Keyword Mapper** covering: Kothrud, Viman Nagar, Shivajinagar, FC Road, Hadapsar, Pimpri-Chinchwad, Baner, Swargate + Mumbai, Delhi, Bangalore.

### 4. 🗺️ OpenStreetMap Interactive Map
- **osmdroid** engine with **CartoDB Voyager** high-performance tile source (CDN-served, zero 403 errors).
- Color-coded severity markers per issue on the live city map.
- Category filter chip bar (All / Pothole / Garbage / Water / Lights).
- Bottom sheet detail panel with upvote and navigate-to-detail actions.

### 5. 🛡️ 50m Spatial Deduplication Engine
- **Haversine formula** calculates great-circle distance between GPS coordinates.
- Reports within **50 meters** of an existing active issue of the **same category** are automatically **merged** as duplicates.
- Duplicate reporters receive **+50 karma** and the master ticket's upvote/confirmation counters increment.

### 6. 📄 A4 PDF Work Order Generator
- **Native Android PdfDocument API** renders official A4 (595×842pt) municipal complaint tickets.
- Includes: slate navy header, severity pill badge, SLA timer, 2-column metadata table, GPS coordinates, Gemini AI triage blockquote, SHA-256 community audit seal, and authorized engineer signature line.
- Uses **Storage Access Framework (SAF)** for user-selectable save location.

### 7. 🚨 SLA Enforcement & Contractor Penalties
- Severity-based SLA deadlines: **CRITICAL = 24h**, **HIGH = 72h**, **MEDIUM = 7 days**, **LOW = 14 days**.
- Auto-calculated **contractor penalty** at ₹500/hour overdue.
- Live countdown timer display on issue detail screen.

### 8. 🏆 Gamification & Karma System
- Report submission: **+50 karma**, SOS dispatch: **+100 karma**, verification: **+25 karma**.
- Progressive badge titles: 🌱 Active Citizen → 🏛️ Civic Guardian → 🚗 Road Safety Champion → 🛡️ Eco Sentinel → 👑 Master Civic Auditor.

### 9. 🔔 Push Notifications
- Report submitted, status changed, and issue resolved notification channels.
- **Proximity alert**: critical hazards within **1km** trigger emergency push notifications.
- Android 13+ POST_NOTIFICATIONS runtime permission support.

### 10. 🌐 Offline-First Architecture
- **Room Database** (`civic_issues` SQLite table) caches all submissions locally.
- **WorkManager** `ReportSyncWorker` auto-syncs queued reports when network connectivity is restored.
- `isSynced` flag tracks sync state per record.

### 11. 🎙️ Voice Dictation
- Native Android Speech Recognition supporting **English, Hindi, and Marathi** dictation.
- Auto-fills the issue description field from voice input.

### 12. 👁️ Privacy Blur Engine
- Automated face and license plate anonymization before public map display.
- Canvas-based overlay masking in the center region of captured images.

### 13. 🚒 Municipal Crew Route Optimizer
- **Nearest-neighbor geographic algorithm** computes optimal truck routes across all open issues.
- Calculates total distance (km) and estimated driving time at 30 km/h city speed.

### 14. 📊 City Health Analytics Dashboard
- Computes **city-wide infrastructure health score** (25–100%) from resolved/total ratio.
- Per-ward health scores for Pune wards (Kothrud, Shivajinagar, Viman Nagar).
- Status ratings: 🟢 EXCELLENT → 🟡 GOOD → 🟠 MODERATE → 🔴 CRITICAL.
- **GeoJSON export** of all issues for GIS integration.

---

## 🏗️ System Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                            │
│  SplashActivity → LoginActivity → MainActivity (ViewPager2 + BottomNav)  │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐           │
│  │ MapFrag  │ FeedFrag │ReportFrag│MyReports │ProfileFrag│           │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘           │
│  IssueDetailActivity │ EditIssueActivity │ EditProfileActivity       │
│  LeaderboardFragment │ IssueAdapter (RecyclerView)                  │
└─────────────────────────────┬────────────────────────────────────────┘
                              │ LiveData<List<CivicIssue>>
┌─────────────────────────────▼────────────────────────────────────────┐
│                        DOMAIN / ENGINE LAYER                         │
│  ┌──────────────────┐  ┌────────────────────┐  ┌──────────────────┐ │
│  │ GeminiTriageServ │  │ IssueRepository    │  │ CivicAnalytics   │ │
│  │ (Gemini 1.5 API) │  │ (50m Deduplication)│  │ Engine           │ │
│  └──────────────────┘  └────────────────────┘  └──────────────────┘ │
│  ┌──────────────────┐  ┌────────────────────┐  ┌──────────────────┐ │
│  │ GeoUtils         │  │ GeoLocationResolver│  │ CrewRouteOptimzr │ │
│  │ (Haversine)      │  │ (Geocoder+Keyword) │  │ (NearestNeighbor)│ │
│  └──────────────────┘  └────────────────────┘  └──────────────────┘ │
│  ┌──────────────────┐  ┌────────────────────┐  ┌──────────────────┐ │
│  │ PdfReportGen     │  │ PrivacyBlurEngine  │  │ SessionManager   │ │
│  │ (A4 PDF Canvas)  │  │ (Face/Plate Mask)  │  │ (SharedPrefs)    │ │
│  └──────────────────┘  └────────────────────┘  └──────────────────┘ │
│  ┌──────────────────┐  ┌────────────────────┐                       │
│  │ NotificationHelpr│  │ ProximityAlertHelpr│                       │
│  │ (Push Channels)  │  │ (1km Hazard Alert) │                       │
│  └──────────────────┘  └────────────────────┘                       │
└─────────────────────────────┬────────────────────────────────────────┘
                              │
┌─────────────────────────────▼────────────────────────────────────────┐
│                        DATA / PERSISTENCE LAYER                      │
│  ┌──────────────────┐  ┌────────────────────┐  ┌──────────────────┐ │
│  │ Room SQLite DB   │  │ Firebase Firestore │  │ Firebase Auth    │ │
│  │ CivicIssueDao    │  │ (Real-time Sync)   │  │ (Email/Password) │ │
│  │ CivicIssueEntity │  │ addSnapshotListener│  │                  │ │
│  │ AppDatabase      │  │                    │  │                  │ │
│  └──────────────────┘  └────────────────────┘  └──────────────────┘ │
│  ┌──────────────────┐  ┌────────────────────┐                       │
│  │ Firebase Storage │  │ ReportSyncWorker   │                       │
│  │ (Issue Images)   │  │ (WorkManager)      │                       │
│  └──────────────────┘  └────────────────────┘                       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 📂 Complete File & Class Reference

### `MainActivity.java` — App Shell & Navigation Host
| Element | Detail |
|:---|:---|
| **Navigation** | `ViewPager2` + `BottomNavigationView` (5 tabs) |
| **Tabs** | Map → Feed → Report → My Reports → Profile |
| **Auth Guard** | Redirects to `LoginActivity` if `SessionManager.isLoggedIn()` is false |
| **osmdroid Init** | Loads tile config and sets custom user agent at `onCreate` |
| **Insets** | Status bar padding via `WindowInsetsCompat.Type.statusBars()` |

---

### `ai/GeminiTriageService.java` — AI Vision Triage Engine
| Element | Detail |
|:---|:---|
| **API** | Google Gemini 1.5 Flash (`generativelanguage.googleapis.com/v1beta`) |
| **Input** | `Bitmap` image compressed to JPEG Base64 (80% quality) |
| **Prompt** | Structured schema prompt demanding JSON output with `category`, `severity`, `title`, `description`, `department`, `repairCostEstimate`, `recommendedMaterial`, `hazardRiskScore` |
| **Output** | `TriageResult` object parsed from Gemini JSON response |
| **Fallback** | `executeLocalSimulation()` — deterministic pixel-hash classification (width × 31 + height % 4) |
| **Threading** | `ExecutorService` (single thread) for network call, `Handler(Looper.getMainLooper())` for callbacks |
| **Timeout** | 10s connect + 10s read |

**TriageResult Fields:**
| Field | Type | Example |
|:---|:---|:---|
| `category` | `IssueCategory` | `POTHOLE` |
| `severity` | `IssueSeverity` | `HIGH` |
| `title` | `String` | `"Deep Road Pothole & Asphalt Crater"` |
| `description` | `String` | `"Gemini AI detected structural asphalt damage (~15cm depth)..."` |
| `department` | `String` | `"Public Works Department"` |
| `repairCostEstimate` | `String` | `"₹4,500 – ₹8,500"` |
| `recommendedMaterial` | `String` | `"Hot-Mix Polymer Bituminous Asphalt Patch"` |
| `hazardRiskScore` | `double` | `8.2` |

---

### `repository/IssueRepository.java` — Data Hub & Deduplication Engine
| Element | Detail |
|:---|:---|
| **Pattern** | Singleton (`getInstance()`) |
| **Data Source** | `MutableLiveData<List<CivicIssue>>` observed by all UI fragments |
| **Deduplication** | `addIssueWithDeduplication()` — iterates all active (non-RESOLVED) issues, checks `GeoUtils.isWithinRadius()` within 50m AND same `IssueCategory` |
| **Merge Logic** | Duplicate found → increment upvotes/confirmations on master, mark child as `isDuplicate=true`, set `parentIssueId` |
| **Firebase Sync** | `saveToFirebaseFirestore()` writes to `civic_issues` collection; `listenToFirestoreUpdates()` uses `addSnapshotListener` for real-time push |
| **Verification** | `addVerification()` writes to `verifications` sub-collection |
| **Upvote** | `upvoteIssue()` increments upvote count and syncs to Firestore |
| **Seed Data** | 6 pre-loaded sample issues across Pune landmarks for demo purposes |

---

### `db/` — Room Database Offline Cache

| File | Purpose |
|:---|:---|
| **`AppDatabase.java`** | Room `@Database` (version 1), entity: `CivicIssueEntity`, singleton builder with `fallbackToDestructiveMigration()` |
| **`CivicIssueDao.java`** | `@Dao` interface: `getAllIssues()` (LiveData, ordered by timestamp DESC), `getUnsyncedIssues()` (where `isSynced=0`), `insertIssue()`, `insertAll()`, `updateIssue()`, `deleteIssue()` |
| **`CivicIssueEntity.java`** | `@Entity(tableName="civic_issues")` — 14 fields: `id` (PK), `title`, `description`, `category`, `severity`, `latitude`, `longitude`, `address`, `imageUrl`, `reporterName`, `department`, `upvotesCount`, `timestamp`, `isSynced` |

---

### `models/` — Domain Data Models

#### `CivicIssue.java` — Core Issue Model
| Field | Type | Notes |
|:---|:---|:---|
| `id` | `String` | Auto-generated `"iss_" + timestamp` |
| `title` | `String` | Gemini-generated or user-entered |
| `description` | `String` | AI analysis description |
| `category` | `IssueCategory` | Enum (6 values) |
| `severity` | `IssueSeverity` | Enum (4 levels) |
| `status` | `IssueStatus` | REPORTED → IN_PROGRESS → RESOLVED |
| `latitude`, `longitude` | `double` | GPS coordinates |
| `address` | `String` | Human-readable address |
| `imageUrl` | `String` | Firebase Storage URL |
| `reporterName` | `String` | From SessionManager |
| `department` | `String` | Target municipal department |
| `upvotesCount` | `int` | Community endorsements (default: 1) |
| `confirmationsCount` | `int` | Verification confirmations (default: 1) |
| `timestamp` | `long` | `System.currentTimeMillis()` |
| `isDuplicate` | `boolean` | True if merged into master ticket |
| `parentIssueId` | `String` | Reference to master issue ID |
| `repairCostEstimate` | `String` | AI-estimated repair cost in INR |
| `recommendedMaterial` | `String` | AI-recommended repair material |
| `hazardRiskScore` | `double` | 0.0–10.0 risk rating |

**Key Methods:**
- `calculateAiDefaults()` — auto-sets cost, material, risk based on severity level
- `getSlaDeadline()` — returns `timestamp + severity-based SLA duration`
- `getFormattedSlaRemaining()` — returns human-readable countdown or "🚨 SLA Breached"

#### `IssueCategory.java` — 6 Category Taxonomy
| Enum Value | Display Name | Default Department |
|:---|:---|:---|
| `POTHOLE` | Pothole & Road Hazard | Public Works Dept |
| `GARBAGE` | Garbage & Waste | Sanitation Dept |
| `WATER_LEAK` | Water Leak & Drainage | Water Supply Board |
| `STREETLIGHT` | Broken Streetlight | Electrical Department |
| `MANHOLE` | Open Manhole Hazard | Infrastructure Dept |
| `OTHER` | General Civic Issue | Municipal Administration |

#### `IssueSeverity.java` — 4-Level Severity with SLA
| Enum | SLA Description | Hex Color | SLA Duration |
|:---|:---|:---|:---|
| `CRITICAL` | Emergency Hazard (24h SLA) | `#D32F2F` | 24 hours |
| `HIGH` | High Severity (72h SLA) | `#F57C00` | 72 hours |
| `MEDIUM` | Moderate Severity (7 Days SLA) | `#FBC02D` | 7 days |
| `LOW` | Minor Issue (14 Days SLA) | `#388E3C` | 14 days |

#### `IssueStatus.java` — 3-Stage Lifecycle
| Enum | Label | Hex Color |
|:---|:---|:---|
| `REPORTED` | Reported | `#1976D2` |
| `IN_PROGRESS` | Work in Progress | `#F57C00` |
| `RESOLVED` | Resolved | `#388E3C` |

#### `CivicUser.java` — User Profile Model
Fields: `uid`, `name`, `email`, `karmaPoints`, `badgeTitle`, `reportsCount`, `verificationsCount`.

#### `VerificationModel.java` — Community Verification
Fields: `id`, `issueId`, `userName`, `statusVote` (STILL_EXISTS / FIXED / IN_PROGRESS), `comment`, `timestamp`.

---

## 🧮 Spatial Algorithms & Geolocation

### `GeoUtils.java` — Haversine Distance Calculator
```
Earth Radius = 6,371,000 meters

calculateHaversineDistance(lat1, lon1, lat2, lon2):
  dLat = toRadians(lat2 - lat1)
  dLon = toRadians(lon2 - lon1)
  a = sin²(dLat/2) + cos(lat1) × cos(lat2) × sin²(dLon/2)
  c = 2 × atan2(√a, √(1-a))
  return EARTH_RADIUS × c  [meters]

isWithinRadius(lat1, lon1, lat2, lon2, threshold):
  return haversineDistance ≤ threshold
```

### `GeoLocationResolver.java` — Address → GPS Resolver
1. **Geocoder API** — attempts system geocoder if `Geocoder.isPresent()` returns true
2. **Keyword Fallback** — maps 12 city sector keywords to hardcoded GPS + random ±0.004° offset:
   - Pune: Kothrud, Viman Nagar, FC Road/Shivajinagar, Hadapsar, Pimpri-Chinchwad, Baner, Swargate
   - Other cities: Mumbai, Delhi, Bangalore
3. **Default** — Pune City Center (`18.5204, 73.8567`)

### `ProximityAlertHelper.java` — Emergency Hazard Proximity
- Checks if a CRITICAL severity issue is within **1,000 meters** of the user.
- Triggers immediate emergency push notification if within range.

---

## 📊 Analytics & Municipal Intelligence

### `CivicAnalyticsEngine.java`

| Method | Input | Output | Algorithm |
|:---|:---|:---|:---|
| `computeCityHealth()` | `List<CivicIssue>` | `CivicHealthMetrics` | `score = clamp(25, 100, (resolved/total×100) + 45)` |
| `computeWardHealthScore()` | issues + ward keyword | `int` (40–100) | Filters by address keyword, `clamp(40, 100, (resolved/total×100) + 50)` |
| `calculateContractorPenalty()` | `CivicIssue` | penalty string | `hoursOverdue × ₹500` per hour past SLA |
| `exportCityDataToGeoJson()` | `List<CivicIssue>` | GeoJSON string | RFC 7946 FeatureCollection with Point geometries |
| `getBadgeTitleForKarma()` | karma points | badge title string | 5-tier progressive system |

**Health Rating Thresholds:**
| Score Range | Rating |
|:---|:---|
| ≥ 85% | 🟢 EXCELLENT INFRASTRUCTURE |
| ≥ 70% | 🟡 GOOD (ROUTINE MAINTENANCE) |
| ≥ 50% | 🟠 MODERATE HAZARD DENSITY |
| < 50% | 🔴 CRITICAL REPAIR NEEDED |

### `CrewRouteOptimizer.java` — Nearest-Neighbor Route
- Filters only non-RESOLVED issues.
- Greedy nearest-neighbor from start position: each step picks the closest unvisited issue.
- Calculates total distance (km) and estimated time at **30 km/h** urban truck speed.
- Returns `OptimizedRouteResult` with ordered route, total distance, and ETA.

---

## 💾 Database Layer (Room + Firebase)

### Room SQLite (Offline)
| Component | Detail |
|:---|:---|
| **Database** | `civiclens_ai_db`, version 1 |
| **Table** | `civic_issues` (14 columns) |
| **Queries** | All issues (LiveData, DESC), unsynced issues, insert/update/delete |
| **Migration** | `fallbackToDestructiveMigration()` |

### Firebase Firestore (Cloud)
| Collection | Purpose |
|:---|:---|
| `civic_issues` | Primary issue storage with real-time `addSnapshotListener` |
| `verifications` | Community status votes sub-collection per issue |

### Firebase Auth
- Email/password authentication via `FirebaseAuth`.
- Session persisted in `SharedPreferences` via `SessionManager`.

### Firebase Storage
- Issue photo uploads stored as Firebase Storage URLs in `imageUrl` field.

---

## ⚙️ Background Sync & WorkManager

### `ReportSyncWorker.java`
| Aspect | Detail |
|:---|:---|
| **Type** | `Worker` (one-shot via `OneTimeWorkRequest`) |
| **Trigger** | `scheduleOfflineSync()` — enqueues when offline reports exist |
| **Constraint** | `NetworkType.CONNECTED` — executes only when internet is available |
| **Action** | Iterates all cached issues and calls `IssueRepository.updateIssue()` |
| **Notification** | Shows "Offline Reports Synchronized" notification on success |
| **Retry** | Returns `Result.retry()` on failure for automatic retry |

---

## 📱 UI Screens & Navigation

### Screen Flow
```
SplashActivity ──→ LoginActivity ──→ MainActivity (ViewPager2)
                    ├─ RegisterActivity      ├─ Tab 0: MapFragment
                    └─ ForgotPasswordActivity ├─ Tab 1: FeedFragment
                                              ├─ Tab 2: ReportFragment
                                              ├─ Tab 3: MyReportsFragment
                                              └─ Tab 4: ProfileFragment

IssueDetailActivity ←── tap from Feed/Map/MyReports
EditIssueActivity ←── edit from MyReports
EditProfileActivity ←── edit from Profile
LeaderboardFragment ←── accessed from Profile
```

### Screen Details

| Screen | File | Key Features |
|:---|:---|:---|
| **Splash** | `SplashActivity.java` | Android 12+ SplashScreen API, dark theme, auto-redirect |
| **Login** | `LoginActivity.java` | Firebase email/password auth, SessionManager login |
| **Register** | `RegisterActivity.java` | Firebase user creation |
| **Forgot Password** | `ForgotPasswordActivity.java` | Firebase password reset email |
| **Map** | `MapFragment.java` | osmdroid CartoDB Voyager tiles, severity-colored markers, category chip filter, bottom sheet, real-time GPS centering |
| **Feed** | `FeedFragment.java` | RecyclerView + `IssueAdapter`, SwipeRefreshLayout, category chip filter (All/Pothole/Garbage/Water/Lights), LiveData observer |
| **Report** | `ReportFragment.java` | Camera/Gallery/Procedural capture, Gemini AI triage, category/severity dropdowns, GPS auto-fill, voice dictation, SOS emergency mode, privacy blur, deduplication |
| **My Reports** | `MyReportsFragment.java` | Filtered list of user's own submissions, edit/delete actions |
| **Profile** | `ProfileFragment.java` | User info, karma display, badge title, edit profile, logout |
| **Issue Detail** | `IssueDetailActivity.java` | Full issue view, before/after comparison, PDF download, upvote, community verification, SLA timer |
| **Edit Issue** | `EditIssueActivity.java` | Modify title, description, category |
| **Edit Profile** | `EditProfileActivity.java` | Update name and email |
| **Leaderboard** | `LeaderboardFragment.java` | Ward health scores, crew route optimizer, GeoJSON export, user karma |

### Layout XML Files (16 total)
| Layout | Screen |
|:---|:---|
| `activity_splash.xml` | Splash screen |
| `activity_login.xml` | Login form |
| `activity_register.xml` | Registration form |
| `activity_forgot_password.xml` | Password reset |
| `activity_main.xml` | Main shell (ViewPager2 + BottomNav) |
| `fragment_map.xml` | OSM map view + filter chips + bottom sheet |
| `fragment_feed.xml` | Issue feed RecyclerView + chips |
| `fragment_report.xml` | Report submission form |
| `fragment_my_reports.xml` | User's reports list |
| `fragment_profile.xml` | User profile view |
| `fragment_leaderboard.xml` | Analytics dashboard |
| `activity_issue_detail.xml` | Full issue detail |
| `activity_edit_issue.xml` | Issue edit form |
| `activity_edit_profile.xml` | Profile edit form |
| `item_issue_card.xml` | RecyclerView issue card item |
| `layout_map_issue_bottom_sheet.xml` | Map bottom sheet detail |

---

## 📄 PDF Report Generator

### `PdfReportGenerator.java` (339 lines)
Generates official A4 (595×842pt) municipal work order PDFs using Android's native `PdfDocument` API.

**PDF Layout Structure:**
| Section | Content |
|:---|:---|
| **Header** | Slate navy banner (`#0F172A`), royal blue accent stripe, "CIVICLENS AI — MUNICIPAL WORK ORDER" title, "OFFICIAL TICKET" pill badge |
| **Status Bar** | Severity color card + Status card with labels |
| **Metadata Grid** | 2-column table: Issue ID, Reporter, Department, Address, GPS Coordinates, Timestamp |
| **Issue Title** | Full title in bold |
| **AI Triage Block** | Gemini AI analysis description in styled blockquote |
| **Repair Info** | Estimated cost, recommended material, hazard risk score |
| **Deduplication Seal** | 50m spatial verification status |
| **Audit Seal** | Green "COMMUNITY AUDIT SEAL" with SHA-256 hash |
| **Signature** | Authorized Municipal Engineer signature line |

**Save/Share Flow:**
1. `createPdfDocument()` renders the document
2. SAF `Intent.ACTION_CREATE_DOCUMENT` lets user pick save location
3. `sharePdfTicket()` uses `FileProvider` for sharing via Intent

---

## 🔔 Notification System

### `NotificationHelper.java`
| Method | Trigger | Priority |
|:---|:---|:---|
| `showReportSubmittedNotification()` | Issue successfully submitted | DEFAULT |
| `showStatusChangedNotification()` | Issue status updated | HIGH |
| `showIssueResolvedNotification()` | Issue marked as Fixed | DEFAULT |

- Channel: `civiclens_notifications` / "CivicLens Alerts"
- Android O+ `NotificationChannel` with `IMPORTANCE_DEFAULT`
- Android 13+ `POST_NOTIFICATIONS` permission check

---

## 🔒 Privacy & Security

### `PrivacyBlurEngine.java`
- Creates mutable copy of captured bitmap
- Applies semi-transparent dark overlay (`Color.argb(180, 50, 50, 50)`) to center region (35%–65% width, 40%–55% height)
- Simulates ML Kit face/license plate detection masking
- Returns anonymized bitmap for public display

### `SessionManager.java`
- `SharedPreferences`-based session storage (file: `CivicLensUserSession`)
- Stored keys: `isLoggedIn`, `uid`, `name`, `email`, `karma`
- Default initial karma: **150 points**
- `logoutUser()` clears all session data

---

## 🏆 Gamification & Karma System

| Action | Karma Points |
|:---|:---|
| Report submitted | +50 |
| SOS Emergency Dispatch | +100 |
| Duplicate merged (upvote) | +50 |
| Community verification | +25 |

### Badge Progression
| Karma Threshold | Badge Title |
|:---|:---|
| < 100 | 🌱 ACTIVE CITIZEN |
| ≥ 100 | 🏛️ CIVIC GUARDIAN |
| ≥ 250 | 🚗 ROAD SAFETY CHAMPION |
| ≥ 400 | 🛡️ ECO SENTINEL |
| ≥ 600 | 👑 MASTER CIVIC AUDITOR |

---

## 📦 Dependencies & Libraries

| Library | Version | Usage |
|:---|:---|:---|
| **Material Components** | `1.14.0` | Material 3 Design System, Cards, Chips, BottomNav |
| **Airbnb Lottie** | `6.4.0` | Vector animations for Splash & Loading |
| **Meta Shimmer** | `0.5.0` | Skeleton shimmer loaders for Feed & Cards |
| **CircleImageView** | `3.1.0` | Circular avatar rendering |
| **SwipeRefreshLayout** | `1.1.0` | Pull-to-refresh gesture |
| **Room Database** | `2.6.1` | SQLite ORM for offline persistence |
| **Core SplashScreen** | `1.0.1` | Android 12+ official splash API |
| **Glide** | `4.16.0` | Image caching & rendering |
| **osmdroid** | `6.1.18` | OpenStreetMap tiles (CartoDB Voyager) |
| **Play Services Location** | latest | `FusedLocationProviderClient` GPS |
| **CameraX** | latest | Camera capture pipeline |
| **WorkManager** | latest | Background sync scheduling |
| **Firebase BoM** | latest | Auth, Firestore, Storage |
| **JUnit** | `4.13.2` | Unit testing framework |
| **Arch Core Testing** | `2.2.0` | `InstantTaskExecutorRule` for LiveData tests |
| **Espresso** | latest | UI instrumented tests |

**Build Configuration:**
- `compileSdk` / `targetSdk`: **36**
- `minSdk`: **24** (Android 7.0 Nougat)
- `Java`: **17**
- **ViewBinding**: enabled
- `testOptions.unitTests.returnDefaultValues = true`

---

## 🧪 Unit Test Coverage

### Test Suite Summary — 26+ Tests, All Passing ✅

| Test File | Tests | Coverage |
|:---|:---|:---|
| `CivicAnalyticsEngineTest.java` | 7 | City health metrics, ward scores, contractor penalties, GeoJSON export, karma badges |
| `CrewRouteOptimizerTest.java` | 4 | Empty route, single issue, multi-issue ordering, resolved-issue filtering |
| `SessionManagerTest.java` | 5 | Login/logout, profile update, karma addition, initial state |
| `CivicIssueTest.java` | 4 | Default constructor, AI defaults per severity, SLA deadline calculation |
| `GeoUtilsTest.java` | 3 | Same-point distance, known-distance verification, radius check |
| `GeoLocationResolverTest.java` | 3 | Null/empty input, keyword matching, default fallback |
| `GeminiTriageServiceTest.java` | 3 | TriageResult construction, category/severity parsing |
| `IssueRepositoryTest.java` | 4 | Deduplication merge, unique submission, resolved-issue bypass |

**Run Tests:**
```powershell
./gradlew test --no-daemon
```

---

## 🛠️ Build & Run Instructions

### Prerequisites
- Android Studio Ladybug (2024.x) or later
- JDK 17+
- Android SDK 36
- Firebase project configured (`google-services.json` in project root)

### Build Commands
```powershell
# Full build verification (compile + lint + tests)
./gradlew check --no-daemon

# Unit tests only
./gradlew test --no-daemon

# Debug APK assembly
./gradlew assembleDebug --no-daemon
```

### Firebase Setup
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name `com.example.civiclensai`
3. Download `google-services.json` and place in project root
4. Enable **Authentication** (Email/Password provider)
5. Enable **Cloud Firestore** database
6. Enable **Firebase Storage** for image uploads

### Gemini API (Optional)
- Obtain a Gemini API key from [Google AI Studio](https://aistudio.google.com)
- Call `GeminiTriageService.setApiKey("YOUR_KEY")` at app startup
- Without a key, the app uses intelligent local simulation fallback

---

## 📋 Android Manifest & Permissions

| Permission | Usage |
|:---|:---|
| `INTERNET` | Firebase sync, Gemini API, OSM tile loading |
| `ACCESS_NETWORK_STATE` | WorkManager connectivity constraint |
| `ACCESS_FINE_LOCATION` | GPS coordinates for issue reporting |
| `ACCESS_COARSE_LOCATION` | Fallback location for map centering |
| `CAMERA` | Real-time hazard photo capture |
| `POST_NOTIFICATIONS` | Push notifications (Android 13+) |

**Activities registered:** SplashActivity (LAUNCHER), MainActivity, LoginActivity, RegisterActivity, ForgotPasswordActivity, EditProfileActivity, EditIssueActivity, IssueDetailActivity.

---

## 🎨 Design System & Theme

### Color Palette
| Token | Hex | Usage |
|:---|:---|:---|
| `m3_primary` | `#2563EB` | Royal Blue — primary actions, buttons, header accents |
| `m3_secondary` | `#0EA5E9` | Sky Cyan — secondary actions, links |
| `m3_tertiary` | `#6366F1` | Indigo — tertiary elements |
| `m3_surface` | `#F8FAFC` | Slate Light — background surface |
| `m3_on_surface` | `#0F172A` | Slate Dark — primary text |
| `severity_critical` | `#DC2626` | Red — critical hazards |
| `severity_high` | `#EA580C` | Orange — high severity |
| `severity_medium` | `#D97706` | Amber — medium severity |
| `severity_low` | `#059669` | Emerald — low severity |

### Theme
- Material 3 `Theme.Material3.Light.NoActionBar` base
- Custom splash theme: `Theme.CivicLensAI.Starting` with dark slate window background
- Day/Night support via `values-night/` overrides

---

## 📁 Repository Structure

```
CivicLens-AI/
├── app/
│   ├── build.gradle                          # App-level Gradle config (SDK 36, Java 17)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml           # 6 permissions, 7 activities
│   │   │   ├── java/com/example/civiclensai/
│   │   │   │   ├── MainActivity.java         # ViewPager2 + BottomNav shell
│   │   │   │   ├── ai/
│   │   │   │   │   └── GeminiTriageService.java  # Gemini 1.5 Flash Vision API + fallback
│   │   │   │   ├── db/
│   │   │   │   │   ├── AppDatabase.java          # Room DB singleton
│   │   │   │   │   ├── CivicIssueDao.java        # CRUD + sync queries
│   │   │   │   │   └── CivicIssueEntity.java     # Room entity (14 fields)
│   │   │   │   ├── models/
│   │   │   │   │   ├── CivicIssue.java           # Core model (21 fields, AI defaults, SLA)
│   │   │   │   │   ├── CivicUser.java            # User profile (karma, badges)
│   │   │   │   │   ├── IssueCategory.java        # 6-value enum with departments
│   │   │   │   │   ├── IssueSeverity.java        # 4-level enum with SLA and colors
│   │   │   │   │   ├── IssueStatus.java          # 3-stage lifecycle enum
│   │   │   │   │   └── VerificationModel.java    # Community verification vote
│   │   │   │   ├── repository/
│   │   │   │   │   └── IssueRepository.java      # Singleton repo, 50m dedup, Firestore sync
│   │   │   │   ├── ui/
│   │   │   │   │   ├── FeedFragment.java         # Issue feed + category filter
│   │   │   │   │   ├── IssueAdapter.java         # RecyclerView adapter
│   │   │   │   │   ├── IssueDetailActivity.java  # Full detail + PDF + verify
│   │   │   │   │   ├── LeaderboardFragment.java  # Analytics dashboard
│   │   │   │   │   ├── MapFragment.java          # OSM map + markers + bottom sheet
│   │   │   │   │   ├── ReportFragment.java       # Issue submission (camera/AI/GPS)
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── ForgotPasswordActivity.java
│   │   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   │   ├── RegisterActivity.java
│   │   │   │   │   │   └── SplashActivity.java
│   │   │   │   │   ├── myreports/
│   │   │   │   │   │   ├── EditIssueActivity.java
│   │   │   │   │   │   └── MyReportsFragment.java
│   │   │   │   │   └── profile/
│   │   │   │   │       ├── EditProfileActivity.java
│   │   │   │   │       └── ProfileFragment.java
│   │   │   │   ├── utils/
│   │   │   │   │   ├── CivicAnalyticsEngine.java     # Health metrics, GeoJSON, badges
│   │   │   │   │   ├── CrewRouteOptimizer.java       # Nearest-neighbor route planning
│   │   │   │   │   ├── GeoLocationResolver.java      # Geocoder + keyword fallback
│   │   │   │   │   ├── GeoUtils.java                 # Haversine distance formula
│   │   │   │   │   ├── NotificationHelper.java       # Push notification channels
│   │   │   │   │   ├── PdfReportGenerator.java       # A4 PDF work order canvas
│   │   │   │   │   ├── PrivacyBlurEngine.java        # Face/plate anonymization
│   │   │   │   │   ├── ProximityAlertHelper.java     # 1km critical hazard alert
│   │   │   │   │   ├── SessionManager.java           # SharedPreferences session
│   │   │   │   │   └── VoiceTriageHelper.java        # Speech recognition launcher
│   │   │   │   └── workers/
│   │   │   │       └── ReportSyncWorker.java         # WorkManager offline sync
│   │   │   └── res/
│   │   │       ├── drawable/                         # Icons, backgrounds, shapes
│   │   │       ├── layout/                           # 16 XML layouts
│   │   │       ├── menu/                             # Bottom navigation menu
│   │   │       ├── mipmap-*/                         # App launcher icons (6 densities)
│   │   │       ├── values/
│   │   │       │   ├── colors.xml                    # M3 + severity + brand palette
│   │   │       │   ├── strings.xml                   # App name: "CivicLens AI"
│   │   │       │   └── themes.xml                    # Material 3 theme config
│   │   │       ├── values-night/                     # Dark theme overrides
│   │   │       └── xml/                              # Backup & data extraction rules
│   │   └── test/java/com/example/civiclensai/
│   │       ├── ExampleUnitTest.java                  # Default test scaffold
│   │       ├── ai/
│   │       │   └── GeminiTriageServiceTest.java      # 3 tests
│   │       ├── models/
│   │       │   └── CivicIssueTest.java               # 4 tests
│   │       ├── repository/
│   │       │   └── IssueRepositoryTest.java          # 4 tests
│   │       └── utils/
│   │           ├── CivicAnalyticsEngineTest.java     # 7 tests
│   │           ├── CrewRouteOptimizerTest.java       # 4 tests
│   │           ├── GeoLocationResolverTest.java      # 3 tests
│   │           ├── GeoUtilsTest.java                 # 3 tests
│   │           └── SessionManagerTest.java           # 5 tests
├── build.gradle                                      # Project-level Gradle config
├── settings.gradle                                   # Module includes & repositories
├── gradle/
│   └── libs.versions.toml                            # Version catalog
├── google-services.json                              # Firebase configuration
├── gradle.properties                                 # JVM & AndroidX settings
├── gradlew / gradlew.bat                             # Gradle wrapper scripts
├── CIVICLENS_AI_REPORT.md                            # Detailed analysis report
├── CivicLens_AI_Presentation.html                    # HTML presentation
└── README.md                                         # ← This file
```

---

## 📊 Project Statistics

| Metric | Count |
|:---|:---|
| **Total Java Source Files** | 28 |
| **Total Test Files** | 9 (8 custom + 1 scaffold) |
| **Total Unit Tests** | 26+ |
| **Layout XML Files** | 16 |
| **Data Models** | 6 |
| **Enumerations** | 3 |
| **Activities** | 7 |
| **Fragments** | 6 |
| **Utility Classes** | 10 |
| **Android Permissions** | 6 |
| **Lines of Code (approx)** | ~4,500+ |

---

## 📜 License & Copyright

Designed and developed for **CivicLens AI Platform**.  
*Empowering citizens, streamlining municipal dispatch, and building safer cities with AI-powered civic engagement.*
