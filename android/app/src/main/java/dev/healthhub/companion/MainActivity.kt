package dev.healthhub.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.healthhub.companion.ui.home.HomeScreen
import dev.healthhub.companion.ui.mood.MoodScreen
import dev.healthhub.companion.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CompanionApplication).container
        setContent {
            HealthHubApp(container)
        }
    }
}

@Composable
fun HealthHubApp(container: dev.healthhub.companion.di.AppContainer) {
    val navController = rememberNavController()
    val items = listOf("home" to "Today", "mood" to "Mood", "settings" to "Settings")
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { (route, label) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = { navController.navigate(route) },
                        icon = { Text(label.first().toString()) },
                        label = { Text(label) })
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(container) }
            composable("mood") { MoodScreen(container) }
            composable("settings") { SettingsScreen(container) }
        }
    }
}
