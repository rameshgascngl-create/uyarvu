# ARCHITECTURE — UYARVU
- **Pattern:** Offline-first Unidirectional Data Flow (UDF) with Compose UI -> ViewModel -> Repository -> Room (`uyarvu_db`) / DataStore.
- **Domain Purity:** `AttendanceCalculator` and `Sm2Scheduler` are pure Kotlin modules with zero Android framework dependencies for fast JUnit testing.
