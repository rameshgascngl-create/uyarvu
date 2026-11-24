package org.uyarvu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uyarvu.app.R
import org.uyarvu.app.data.local.entity.AcademicTaskEntity
import org.uyarvu.app.domain.attendance.EligibilityCategory
import org.uyarvu.app.ui.UyarvuUiState
import java.time.LocalDate
import java.util.Locale

@Composable
fun TodayScreen(
    state: UyarvuUiState,
    onNavigateToStudy: () -> Unit,
    onToggleTask: (AcademicTaskEntity) -> Unit
) {
    val name = state.profile?.preferredName ?: "Student"
    val today = LocalDate.now().toEpochDay()
    val pendingTasks = state.tasks.filter { !it.isCompleted }.take(3)
    val completedCount = state.tasks.count { it.isCompleted }
    val nextExam = state.exams.firstOrNull { it.examDateEpochDay >= today }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.home_greeting, name),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "${state.profile?.degreeDepartment ?: ""} · Sem ${state.profile?.currentSemester ?: 1}",
            style = MaterialTheme.typography.bodyMedium
        )

        // 1. Today's Classes
        SectionCard(title = stringResource(R.string.home_q_classes)) {
            if (state.courses.isEmpty()) {
                Text(stringResource(R.string.home_no_classes), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = onNavigateToStudy,
                    modifier = Modifier.padding(top = 8.dp).defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Add Subjects in Study")
                }
            } else {
                state.courses.forEach { course ->
                    Text("• ${course.code} — ${course.nameEn}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 2 & 3. Current Attendance & Eligibility Risk
        SectionCard(title = stringResource(R.string.home_q_attendance)) {
            val agg = state.aggregateAttendance
            if (agg == null || agg.conductedClasses == 0) {
                Text(stringResource(R.string.att_status_none), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = onNavigateToStudy,
                    modifier = Modifier.padding(top = 8.dp).defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Log Attendance")
                }
            } else {
                val pctStr = String.format(Locale.US, "%.1f%%", agg.percentage)
                Text(
                    text = "Overall: $pctStr (${agg.attendedClasses}/${agg.conductedClasses})",
                    style = MaterialTheme.typography.titleMedium
                )
                val riskLabel = when (agg.category) {
                    EligibilityCategory.NORMAL_ELIGIBLE -> "Eligible (≥75%) · Safe allowance: ${agg.safeAbsenceAllowance}"
                    EligibilityCategory.CONDONATION_ELIGIBLE -> "Condonation range · Attend next ${agg.classesNeededForRegular} class(es) for 75%"
                    EligibilityCategory.MEDICAL_CONDONATION_ELIGIBLE -> "Medical condonation range · Attend next ${agg.classesNeededForRegular} class(es)"
                    EligibilityCategory.SEMESTER_REPEAT_RISK -> "Below 50% · Consult your academic advisor"
                    EligibilityCategory.NO_CLASSES_CONDUCTED -> stringResource(R.string.att_status_none)
                }
                Text(riskLabel, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // 4. Approaching Deadlines (Exams + Assignments)
        SectionCard(title = stringResource(R.string.home_q_deadlines)) {
            if (nextExam == null && pendingTasks.isEmpty()) {
                Text("No upcoming exams or pending deadlines recorded.", style = MaterialTheme.typography.bodyMedium)
            } else {
                if (nextExam != null) {
                    val days = (nextExam.examDateEpochDay - today).coerceAtLeast(0)
                    Text("Exam: ${nextExam.titleEn} in $days day(s)", style = MaterialTheme.typography.bodyMedium)
                }
                pendingTasks.forEach { task ->
                    val days = task.dueDateEpochDay - today
                    val dueStr = if (days < 0) "Overdue by ${-days}d" else "Due in ${days}d"
                    Text("• ${task.titleEn} ($dueStr)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 5. Today's Top Priority & Recommended Study Plan
        SectionCard(title = stringResource(R.string.home_q_priority)) {
            if (pendingTasks.isEmpty()) {
                Text("No pending tasks. Add assignments or exam prep in Study.", style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = onNavigateToStudy,
                    modifier = Modifier.padding(top = 8.dp).defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Open Study Planner")
                }
            } else {
                pendingTasks.forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) }
                        )
                        Text(
                            text = "${task.titleEn} (${task.estimatedMinutes}m)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                state.dailyPlan?.blocks?.firstOrNull()?.let { block ->
                    Text(
                        text = "Recommended session: ${block.title} — ${block.allocatedMinutes}m (${block.explanation})",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Text(
                    text = "Completed tasks: $completedCount / ${state.tasks.size}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
