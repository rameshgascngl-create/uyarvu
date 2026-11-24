package org.uyarvu.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.uyarvu.app.ui.UyarvuViewModel
import org.uyarvu.app.ui.screens.GrowScreen
import org.uyarvu.app.ui.screens.GuideScreen
import org.uyarvu.app.ui.screens.OnboardingScreen
import org.uyarvu.app.ui.screens.StudyScreen
import org.uyarvu.app.ui.screens.TodayScreen
import org.uyarvu.app.ui.theme.UyarvuTheme

enum class UyarvuDestination(val route: String, val labelRes: Int, val icon: ImageVector) {
    TODAY("today", R.string.nav_today, Icons.Filled.CalendarToday),
    STUDY("study", R.string.nav_study, Icons.Filled.MenuBook),
    GROW("grow", R.string.nav_grow, Icons.Filled.TrendingUp),
    GUIDE("guide", R.string.nav_guide, Icons.Filled.Explore)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UyarvuTheme {
                UyarvuRootScreen()
            }
        }
    }
}

@Composable
fun UyarvuRootScreen(viewModel: UyarvuViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val profile = state.profile
    if (profile == null || !profile.onboardingCompleted) {
        OnboardingScreen(onComplete = viewModel::completeOnboarding)
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: UyarvuDestination.TODAY.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                UyarvuDestination.entries.forEach { dest ->
                    val label = stringResource(dest.labelRes)
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = dest.icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = UyarvuDestination.TODAY.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(UyarvuDestination.TODAY.route) {
                TodayScreen(
                    state = state,
                    onNavigateToStudy = {
                        navController.navigate(UyarvuDestination.STUDY.route) {
                            launchSingleTop = true
                        }
                    },
                    onToggleTask = viewModel::toggleTaskCompleted
                )
            }
            composable(UyarvuDestination.STUDY.route) {
                StudyScreen(
                    state = state,
                    onAddCourse = viewModel::addCourse,
                    onLogAttendance = viewModel::logAttendance,
                    onAddExam = viewModel::addExam,
                    onAddTask = viewModel::addTask,
                    onToggleTask = viewModel::toggleTaskCompleted
                )
            }
            composable(UyarvuDestination.GROW.route) {
                GrowScreen(state = state)
            }
            composable(UyarvuDestination.GUIDE.route) {
                GuideScreen(
                    state = state,
                    onSwitchLanguage = viewModel::switchLanguage,
                    onAdvanceSemester = viewModel::advanceSemester,
                    onExportJson = viewModel::exportJsonBackup,
                    onDeleteAllData = viewModel::deleteAllLocalData
                )
            }
        }
    }
}
