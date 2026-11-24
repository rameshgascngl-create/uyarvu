package org.uyarvu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.uyarvu.app.ui.theme.UyarvuTheme

enum class UyarvuDestination(val route: String, val labelRes: Int) {
    TODAY("today", R.string.nav_today),
    STUDY("study", R.string.nav_study),
    GROW("grow", R.string.nav_grow),
    GUIDE("guide", R.string.nav_guide)
}

class MainActivity : ComponentActivity() {
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
fun UyarvuRootScreen() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: UyarvuDestination.TODAY.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                UyarvuDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(dest.name.take(1)) },
                        label = { Text(stringResource(dest.labelRes)) }
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
            UyarvuDestination.entries.forEach { dest ->
                composable(dest.route) {
                    Phase3APlaceholderShell(title = stringResource(dest.labelRes))
                }
            }
        }
    }
}

@Composable
private fun Phase3APlaceholderShell(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.privacy_offline_badge),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
