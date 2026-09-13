package com.aeroalga.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aeroalga.app.data.repository.NodeRepository
import com.aeroalga.app.ui.screens.analytics.AnalyticsScreen
import com.aeroalga.app.ui.screens.dashboard.DashboardScreen
import com.aeroalga.app.ui.screens.nodes.NodesScreen
import com.aeroalga.app.ui.screens.settings.SettingsScreen
import com.aeroalga.app.ui.theme.BioBackground
import com.aeroalga.app.ui.theme.BioBorder
import com.aeroalga.app.ui.theme.BioLime
import com.aeroalga.app.ui.theme.BioSurface
import com.aeroalga.app.ui.theme.BioTextMuted

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Nodes : Screen("nodes", "Nodes", Icons.Default.DeviceHub)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigationHost(
    repository: NodeRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.Dashboard,
        Screen.Analytics,
        Screen.Nodes,
        Screen.Settings
    )

    Scaffold(
        containerColor = BioBackground,
        bottomBar = {
            NavigationBar(
                containerColor = BioSurface,
                tonalElevation = 4.dp
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BioBackground,
                            selectedTextColor = BioLime,
                            indicatorColor = BioLime,
                            unselectedIconColor = BioTextMuted,
                            unselectedTextColor = BioTextMuted
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(repository)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(repository)
            }
            composable(Screen.Nodes.route) {
                NodesScreen(repository)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(repository)
            }
        }
    }
}
