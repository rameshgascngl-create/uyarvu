package org.uyarvu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uyarvu.app.R
import org.uyarvu.app.data.local.entity.StudentProfileEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (StudentProfileEntity) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var institution by rememberSaveable { mutableStateOf("") }
    var degree by rememberSaveable { mutableStateOf("B.Sc. Computer Science") }
    var semester by rememberSaveable { mutableIntStateOf(1) }
    var language by rememberSaveable { mutableStateOf("en") }
    var workingDays by rememberSaveable { mutableStateOf(setOf("MON", "TUE", "WED", "THU", "FRI")) }

    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.onboard_welcome), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(R.string.onboard_subtitle), style = MaterialTheme.typography.bodyMedium)

        Text(text = stringResource(R.string.onboard_lang_label), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = language == "en",
                onClick = { language = "en" },
                label = { Text("English") },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            )
            FilterChip(
                selected = language == "ta",
                onClick = { language = "ta" },
                label = { Text("தமிழ்") },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.onboard_name_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = institution,
            onValueChange = { institution = it },
            label = { Text(stringResource(R.string.onboard_institution_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = degree,
            onValueChange = { degree = it },
            label = { Text(stringResource(R.string.onboard_degree_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = stringResource(R.string.onboard_days_label), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            allDays.forEach { day ->
                val selected = day in workingDays
                FilterChip(
                    selected = selected,
                    onClick = {
                        workingDays = if (selected && workingDays.size > 1) workingDays - day else workingDays + day
                    },
                    label = { Text(day) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }

        Button(
            onClick = {
                onComplete(
                    StudentProfileEntity(
                        preferredName = name.ifBlank { "Student" },
                        preferredLanguage = language,
                        institutionName = institution.ifBlank { null },
                        degreeDepartment = degree.ifBlank { "Undergraduate" },
                        academicYearLabel = "2026-2027",
                        currentSemester = semester,
                        workingDaysMask = workingDays.joinToString(","),
                        onboardingCompleted = true
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Text(stringResource(R.string.onboard_save_btn))
        }
    }
}
