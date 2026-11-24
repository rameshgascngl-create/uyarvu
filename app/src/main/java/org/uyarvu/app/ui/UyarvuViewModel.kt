package org.uyarvu.app.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.uyarvu.app.UyarvuApplication
import org.uyarvu.app.data.local.entity.AcademicTaskEntity
import org.uyarvu.app.data.local.entity.AttendanceLogEntity
import org.uyarvu.app.data.local.entity.AttendanceStatus
import org.uyarvu.app.data.local.entity.CourseEntity
import org.uyarvu.app.data.local.entity.ExamEntity
import org.uyarvu.app.data.local.entity.ExamType
import org.uyarvu.app.data.local.entity.SessionType
import org.uyarvu.app.data.local.entity.StudentProfileEntity
import org.uyarvu.app.data.local.entity.StudySessionEntity
import org.uyarvu.app.data.local.entity.SyllabusUnitEntity
import org.uyarvu.app.data.local.entity.TaskCategory
import org.uyarvu.app.data.local.entity.TimetableSlotEntity
import org.uyarvu.app.domain.attendance.AttendanceCalculator
import org.uyarvu.app.domain.attendance.AttendanceEvaluation
import org.uyarvu.app.domain.attendance.InstitutionalAttendancePolicy
import org.uyarvu.app.domain.attendance.SubjectAttendanceInput
import org.uyarvu.app.domain.planner.PlannableTask
import org.uyarvu.app.domain.planner.WorkloadBalancer
import org.uyarvu.app.domain.planner.WorkloadPlanResult
import java.time.LocalDate

data class CourseAttendanceSummary(
    val course: CourseEntity,
    val evaluation: AttendanceEvaluation
)

data class UyarvuUiState(
    val isLoading: Boolean = true,
    val profile: StudentProfileEntity? = null,
    val courses: List<CourseEntity> = emptyList(),
    val attendanceSummaries: List<CourseAttendanceSummary> = emptyList(),
    val aggregateAttendance: AttendanceEvaluation? = null,
    val todaySlots: List<TimetableSlotEntity> = emptyList(),
    val exams: List<ExamEntity> = emptyList(),
    val tasks: List<AcademicTaskEntity> = emptyList(),
    val dailyPlan: WorkloadPlanResult? = null,
    val lastExportJson: String? = null
)

class UyarvuViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as UyarvuApplication).database
    private val profileDao = db.studentProfileDao()
    private val courseDao = db.courseDao()
    private val timetableDao = db.timetableDao()
    private val attendanceDao = db.attendanceDao()
    private val examDao = db.examDao()
    private val taskDao = db.academicTaskDao()
    private val syllabusDao = db.syllabusDao()
    private val sessionDao = db.studySessionDao()

    private val availableMinutes = MutableStateFlow(120)
    private val exportedJson = MutableStateFlow<String?>(null)

    private val todayEpochDay: Long
        get() = LocalDate.now().toEpochDay()

    private val todayDayOfWeek: Int
        get() = LocalDate.now().dayOfWeek.value

    val uiState: StateFlow<UyarvuUiState> = combine(
        profileDao.observeProfile(),
        courseDao.observeAllCoursesIncludingHistory(),
        attendanceDao.observeAllLogs(),
        examDao.observeAllExams(),
        taskDao.observeAllTasks(),
        availableMinutes,
        exportedJson
    ) { values ->
        val profile = values[0] as StudentProfileEntity?
        @Suppress("UNCHECKED_CAST")
        val allCourses = values[1] as List<CourseEntity>
        @Suppress("UNCHECKED_CAST")
        val allLogs = values[2] as List<AttendanceLogEntity>
        @Suppress("UNCHECKED_CAST")
        val exams = values[3] as List<ExamEntity>
        @Suppress("UNCHECKED_CAST")
        val tasks = values[4] as List<AcademicTaskEntity>
        val studyMins = values[5] as Int
        val json = values[6] as String?

        val activeSem = profile?.currentSemester ?: 1
        val activeCourses = allCourses.filter { it.semester == activeSem && !it.isArchived }

        val summaries = activeCourses.map { course ->
            val logs = allLogs.filter { it.courseId == course.id }
            val attended = logs.count { it.status == AttendanceStatus.PRESENT }
            val absent = logs.count { it.status == AttendanceStatus.ABSENT }
            val policy = InstitutionalAttendancePolicy(
                regularThresholdPct = course.regularThresholdPct,
                condonationFloorPct = course.condonationFloorPct,
                medicalCondonationFloorPct = course.medicalCondonationFloorPct
            )
            val eval = AttendanceCalculator.evaluateSubject(
                SubjectAttendanceInput(
                    courseId = course.id,
                    attended = attended,
                    absent = absent,
                    remainingPlannedClasses = (course.plannedTheoryClasses - (attended + absent)).coerceAtLeast(0)
                ),
                policy = policy
            )
            CourseAttendanceSummary(course, eval)
        }

        val aggInput = activeCourses.map { course ->
            val logs = allLogs.filter { it.courseId == course.id }
            val attended = logs.count { it.status == AttendanceStatus.PRESENT }
            val absent = logs.count { it.status == AttendanceStatus.ABSENT }
            SubjectAttendanceInput(
                courseId = course.id,
                attended = attended,
                absent = absent,
                remainingPlannedClasses = (course.plannedTheoryClasses - (attended + absent)).coerceAtLeast(0)
            )
        }
        val aggregate = if (aggInput.isEmpty()) null else AttendanceCalculator.evaluateAggregate(aggInput)

        val pendingTasks = tasks.filter { !it.isCompleted }.map { t ->
            PlannableTask(
                id = t.id,
                title = t.titleEn,
                estimatedMinutes = t.estimatedMinutes,
                daysUntilDue = (t.dueDateEpochDay - todayEpochDay).toInt(),
                priorityWeight = t.priority
            )
        }
        val plan = if (pendingTasks.isEmpty()) null else WorkloadBalancer.buildDailyPlan(pendingTasks, studyMins)

        UyarvuUiState(
            isLoading = false,
            profile = profile,
            courses = activeCourses,
            attendanceSummaries = summaries,
            aggregateAttendance = aggregate,
            exams = exams.sortedBy { it.examDateEpochDay },
            tasks = tasks,
            dailyPlan = plan,
            lastExportJson = json
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UyarvuUiState(isLoading = true)
    )

    fun completeOnboarding(profile: StudentProfileEntity) {
        viewModelScope.launch {
            profileDao.upsertProfile(profile)
            applyLocale(profile.preferredLanguage)
        }
    }

    fun switchLanguage(lang: String) {
        viewModelScope.launch {
            val current = profileDao.getProfileOnce()
            if (current != null) {
                profileDao.upsertProfile(current.copy(preferredLanguage = lang))
            }
            applyLocale(lang)
        }
    }

    private fun applyLocale(lang: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
    }

    fun addCourse(code: String, name: String, hasPractical: Boolean = false) {
        viewModelScope.launch {
            val sem = uiState.value.profile?.currentSemester ?: 1
            val id = courseDao.insertCourse(
                CourseEntity(
                    code = code.trim().uppercase(),
                    nameEn = name.trim(),
                    nameTa = name.trim(),
                    semester = sem,
                    hasPractical = hasPractical
                )
            )
            timetableDao.insertSlot(
                TimetableSlotEntity(
                    courseId = id,
                    semester = sem,
                    dayOfWeek = todayDayOfWeek,
                    hourIndex = 1,
                    startTimeLabel = "09:30",
                    endTimeLabel = "10:20",
                    sessionType = SessionType.THEORY
                )
            )
        }
    }

    fun logAttendance(courseId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            // Allow multiple logs per day by incrementing hourIndex if needed for quick logging
            val existingCount = uiState.value.attendanceSummaries
                .find { it.course.id == courseId }?.evaluation?.conductedClasses ?: 0
            attendanceDao.insertLogPreventDuplicate(
                AttendanceLogEntity(
                    courseId = courseId,
                    dateEpochDay = todayEpochDay,
                    hourIndex = existingCount + 1,
                    sessionType = SessionType.THEORY,
                    status = status
                )
            )
        }
    }

    fun addExam(courseId: Long, title: String, daysFromToday: Long, examType: ExamType = ExamType.INTERNAL_ASSESSMENT) {
        viewModelScope.launch {
            examDao.insertExam(
                ExamEntity(
                    courseId = courseId,
                    titleEn = title.trim(),
                    titleTa = title.trim(),
                    examType = examType,
                    examDateEpochDay = todayEpochDay + daysFromToday.coerceAtLeast(0)
                )
            )
        }
    }

    fun addTask(title: String, daysUntilDue: Long, estimatedMinutes: Int = 30, priority: Int = 2) {
        viewModelScope.launch {
            taskDao.insertTask(
                AcademicTaskEntity(
                    titleEn = title.trim(),
                    titleTa = title.trim(),
                    category = TaskCategory.ASSIGNMENT,
                    priority = priority,
                    dueDateEpochDay = todayEpochDay + daysUntilDue,
                    estimatedMinutes = estimatedMinutes
                )
            )
        }
    }

    fun toggleTaskCompleted(task: AcademicTaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(
                task.copy(
                    isCompleted = !task.isCompleted,
                    completedAtEpochDay = if (!task.isCompleted) todayEpochDay else null
                )
            )
        }
    }

    fun addSyllabusUnit(courseId: Long, unitNumber: Int, title: String) {
        viewModelScope.launch {
            syllabusDao.insertUnit(
                SyllabusUnitEntity(
                    courseId = courseId,
                    unitNumber = unitNumber,
                    titleEn = title.trim(),
                    titleTa = title.trim()
                )
            )
        }
    }

    fun setDailyStudyMinutes(minutes: Int) {
        availableMinutes.value = minutes.coerceIn(15, 360)
    }

    fun advanceSemester() {
        viewModelScope.launch {
            val current = profileDao.getProfileOnce() ?: return@launch
            val next = current.currentSemester + 1
            courseDao.archivePriorSemesters(next)
            profileDao.updateSemester(next)
        }
    }

    fun exportJsonBackup() {
        val state = uiState.value
        val summary = """
            {
              "name": "${state.profile?.preferredName ?: ""}",
              "degree": "${state.profile?.degreeDepartment ?: ""}",
              "semester": ${state.profile?.currentSemester ?: 1},
              "coursesCount": ${state.courses.size},
              "examsCount": ${state.exams.size},
              "tasksCount": ${state.tasks.size}
            }
        """.trimIndent()
        exportedJson.value = summary
    }

    fun clearExportPreview() {
        exportedJson.value = null
    }

    fun deleteAllLocalData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.clearAllTables() }
            exportedJson.value = null
        }
    }
}
