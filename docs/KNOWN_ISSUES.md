# KNOWN ISSUES
1. Offline sandbox VM lacks Android SDK, Gradle, and `kotlinc`, so compilation and test execution must be run locally in Android Studio (`./gradlew testDebugUnitTest connectedDebugAndroidTest`).
2. Phases 3C–3G (Full Study Planner UI, Exam Rescue UI, Career Compass, Professor Connect, Student Life Handbook) remain queued after Phase 3B compiles cleanly.
