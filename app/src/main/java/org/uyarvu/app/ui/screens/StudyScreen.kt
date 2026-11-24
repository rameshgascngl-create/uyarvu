package org.uyarvu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uyarvu.app.R
import org.uyarvu.app.data.local.entity.AcademicTaskEntity
import org.uyarvu.app.data.local.entity.AttendanceStatus
import org.uyarvu.app.domain.attendance.EligibilityCategory
import org.uyarvu.app.ui.UyarvuUiState
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyScreen(
    state: UyarvuUiState,
    onAddCourse: (String, String) -> Unit,
    onLogAttendance: (Long, AttendanceStatus) -> Unit,
    onAddExam: (Long, String, Long) -> Unit,
    onAddTask: (String, Long, Int) -> Unit,
    onToggleTask: (AcademicTaskEntity) -> Unit
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    val sections = listOf("Attendance", "Planner & Tasks", "Exams")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.nav_study), style = MaterialTheme.typography.headlineSmall)

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sections.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedSection == idx,
                    onClick = { selectedSection = idx },
                    label = { Text(label) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }

        when (selectedSection) {
            0 -> AttendanceAndSubjectsSection(state, onAddCourse, onLogAttendance)
            1 -> PlannerAndTasksSection(state, onAddTask, onToggleTask)
            else -> ExamsSection(state, onAddExam)
        }
    }
}

@Composable
private fun AttendanceAndSubjectsSection(
    state: UyarvuUiState,
    onAddCourse: (String, String) -> Unit,
    onLogAttendance: (Long, AttendanceStatus) -> Unit
) {
    var code by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }

    SectionCard(title = "Add Subject") {
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Course Code (e.g. CS301)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Subject Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onAddCourse(code.ifBlank { "SUB" }, name)
                    code = ""
                    name = ""
                }
            },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
            Text("Save Subject")
        }
    }

    Text(text = stringResource(R.string.att_title), style = MaterialTheme.typography.titleMedium)
    Text(text = stringResource(R.string.att_disclaimer), style = MaterialTheme.typography.bodySmall)

    if (state.attendanceSummaries.isEmpty()) {
        SectionCard(title = "No subjects yet") {
            Text("Add your first subject above to start logging attendance and recovery targets.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        state.attendanceSummaries.forEach { item ->
            val eval = item.evaluation
            SectionCard(title = "${item.course.code} — ${item.course.nameEn}") {
                val pct = String.format(Locale.US, "%.1f%%", eval.percentage)
                Text("Conducted: ${eval.conductedClasses} · Attended: ${eval.attendedClasses} ($pct)", style = MaterialTheme.typography.bodyMedium)
                val statusText = when (eval.category) {
                    EligibilityCategory.NO_CLASSES_CONDUCTED -> stringResource(R.string.att_status_none)
                    EligibilityCategory.NORMAL_ELIGIBLE -> "Normal eligibility · Safe absence allowance: ${eval.safeAbsenceAllowance}"
                    EligibilityCategory.CONDONATION_ELIGIBLE -> "Condonation zone · Attend next ${eval.classesNeededForRegular} class(es) for 75%"
                    EligibilityCategory.MEDICAL_CONDONATION_ELIGIBLE -> "Medical condonation zone · Attend next ${eval.classesNeededForRegular} class(es)"
                    EligibilityCategory.SEMESTER_REPEAT_RISK -> "Below 50% floor · Attend next ${eval.classesNeededForRegular} class(es)"
                }
                Text(statusText, style = MaterialTheme.typography.bodyMedium)
                if (!eval.isRegularTargetAchievable && eval.conductedClasses > 0) {
                    Text(stringResource(R.string.att_impossible_warning), style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = { onLogAttendance(item.course.id, AttendanceStatus.PRESENT) },
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Present")
                    }
                    OutlinedButton(
                        onClick = { onLogAttendance(item.course.id, AttendanceStatus.ABSENT) },
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Absent")
                    }
                    OutlinedButton(
                        onClick = { onLogAttendance(item.course.id, AttendanceStatus.CANCELLED) },
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Cancelled")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerAndTasksSection(
    state: UyarvuUiState,
    onAddTask: (String, Long, Int) -> Unit,
    onToggleTask: (AcademicTaskEntity) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf("3") }
    var mins by rememberSaveable { mutableStateOf("45") }

    SectionCard(title = "Add Assignment or Study Task") {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = days,
                onValueChange = { days = it },
                label = { Text("Due (days)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = mins,
                onValueChange = { mins = it },
                label = { Text("Minutes") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onAddTask(title, days.toLongOrNull() ?: 3L, mins.toIntOrNull() ?: 30)
                    title = ""
                }
            },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
            Text("Save Task")
        }
    }

    state.dailyPlan?.let { plan ->
        SectionCard(title = "Balanced Daily Study Plan") {
            Text(plan.sleepProtectedNotice, style = MaterialTheme.typography.bodySmall)
            plan.blocks.forEach { block ->
                Text("• ${block.title}: ${block.allocatedMinutes}m (${block.explanation})", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    SectionCard(title = "Tasks (${state.tasks.size})") {
        if (state.tasks.isEmpty()) {
            Text("No tasks yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            state.tasks.forEach { task ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                ) {
                    Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
                    Text("${task.titleEn} (${task.estimatedMinutes}m)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ExamsSection(
    state: UyarvuUiState,
    onAddExam: (Long, String, Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf("10") }

    SectionCard(title = "Schedule Examination") {
        if (state.courses.isEmpty()) {
            Text("Add at least one subject in Attendance first.", style = MaterialTheme.typography.bodyMedium)
        } else {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Exam Title (e.g. Internal Assessment I)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = days,
                onValueChange = { days = it },
                label = { Text("Days from today") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddExam(state.courses.first().id, title, days.toLongOrNull() ?: 7L)
                        title = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Save Exam")
            }
        }
    }

    SectionCard(title = "Scheduled Examinations (${state.exams.size})") {
        if (state.exams.isEmpty()) {
            Text("No examinations scheduled yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            state.exams.forEach { exam ->
                Text("• ${exam.titleEn} (${exam.examType.name})", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
