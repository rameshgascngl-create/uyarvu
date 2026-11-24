package org.uyarvu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uyarvu.app.R
import org.uyarvu.app.ui.UyarvuUiState
import java.util.Locale

@Composable
fun GrowScreen(state: UyarvuUiState) {
    val totalTasks = state.tasks.size
    val completedTasks = state.tasks.count { it.isCompleted }
    val completionFraction = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.nav_grow), style = MaterialTheme.typography.headlineSmall)

        SectionCard(title = "Completed Academic Tasks") {
            Text(
                text = if (totalTasks == 0) "No tasks logged yet." else "$completedTasks of $totalTasks tasks completed",
                style = MaterialTheme.typography.bodyMedium
            )
            if (totalTasks > 0) {
                LinearProgressIndicator(
                    progress = { completionFraction },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
            }
        }

        SectionCard(title = "Attendance Standing") {
            val agg = state.aggregateAttendance
            if (agg == null || agg.conductedClasses == 0) {
                Text("Log attendance in Study to track your real progress.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    text = String.format(Locale.US, "Aggregate: %.1f%% (%d/%d classes)", agg.percentage, agg.attendedClasses, agg.conductedClasses),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = String.format(Locale.US, "Projected semester-end: %.1f%%", agg.projectedSemesterEndPct),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SectionCard(title = "Academic Profile & Semester History") {
            Text("Degree: ${state.profile?.degreeDepartment ?: "Not set"}", style = MaterialTheme.typography.bodyMedium)
            Text("Current Semester: ${state.profile?.currentSemester ?: 1}", style = MaterialTheme.typography.bodyMedium)
            Text("Active Subjects: ${state.courses.size}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
