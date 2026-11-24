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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uyarvu.app.R
import org.uyarvu.app.ui.UyarvuUiState

@Composable
fun GuideScreen(
    state: UyarvuUiState,
    onSwitchLanguage: (String) -> Unit,
    onAdvanceSemester: () -> Unit,
    onExportJson: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.profile_delete_all)) },
            text = { Text(if (state.profile?.preferredLanguage == "ta") "அனைத்துக் கல்விப் பதிவுகளும் நிரந்தரமாக நீக்கப்படும்." else "All local academic records will be permanently deleted.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDeleteAllData() }) { Text(if (state.profile?.preferredLanguage == "ta") "நீக்கு" else "Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(if (state.profile?.preferredLanguage == "ta") "ரத்து செய்" else "Cancel") } }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.nav_guide), style = MaterialTheme.typography.headlineSmall)

        SectionCard(title = "How to Study Effectively (Active Recall)") {
            Text(
                text = "Test yourself without looking at notes first. Close the textbook, write or speak key definitions, then check gaps.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionCard(title = "Preparing for Examinations & Improving Attendance") {
            Text(
                text = "Spread revision across 7–14 days instead of last-minute cramming. Track each subject's attendance early; if below your college's threshold, attend consecutive classes or speak with your advisor promptly.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionCard(title = "Approaching Professors (Template)") {
            Text(
                text = "Respected Sir/Madam, I am [Name] from [Department/Year]. Could I meet you for 5 minutes during office hours to clarify [Topic]? Thank you.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionCard(title = "Language / மொழி") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onSwitchLanguage("en") },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("English")
                }
                OutlinedButton(
                    onClick = { onSwitchLanguage("ta") },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("தமிழ்")
                }
            }
        }

        SectionCard(title = stringResource(R.string.profile_title)) {
            Text(stringResource(R.string.privacy_offline_badge), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = onAdvanceSemester,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            ) {
                Text(stringResource(R.string.profile_next_sem))
            }
            OutlinedButton(
                onClick = onExportJson,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            ) {
                Text(if (state.profile?.preferredLanguage == "ta") "சுயவிவரச் சுருக்கம் (காப்புப்பிரதி அல்ல)" else "Preview profile summary (not a backup)")
            }
            state.lastExportJson?.let { json ->
                Text(json, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            ) {
                Text(stringResource(R.string.profile_delete_all))
            }
        }
    }
}
