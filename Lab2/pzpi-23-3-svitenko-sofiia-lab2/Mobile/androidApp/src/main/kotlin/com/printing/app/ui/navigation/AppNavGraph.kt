package com.printing.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.printing.app.R
import com.printing.app.data.local.TokenStore
import com.printing.app.data.remote.*
import com.printing.app.data.repository.*
import com.printing.app.ui.auth.LoginScreen
import com.printing.app.ui.auth.LoginViewModel
import com.printing.app.ui.jobs.*
import com.printing.app.ui.map.*

private const val ROUTE_LOGIN = "login"
private const val ROUTE_JOBS = "jobs"
private const val ROUTE_NEW_JOB = "new_job"
private const val ROUTE_MAP = "map"

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val client = remember { buildHttpClient(tokenStore) }

    val authApi = remember { AuthApi(client) }
    val printJobApi = remember { PrintJobApi(client, context) }
    val printerApi = remember { PrinterApi(client) }

    val authRepo = remember { AuthRepository(authApi, tokenStore) }
    val jobRepo = remember { PrintJobRepository(printJobApi, context) }
    val printerRepo = remember { PrinterRepository(printerApi) }

    val loginVm = remember { LoginViewModel(authRepo) }
    val jobVm = remember { JobViewModel(jobRepo) }
    val mapVm = remember { MapViewModel(printerRepo) }

    val navController = rememberNavController()
    val startDest = if (authRepo.isLoggedIn()) ROUTE_JOBS else ROUTE_LOGIN

    val bottomNavItems = listOf(
        BottomNavItem(ROUTE_JOBS, stringResource(R.string.my_jobs), Icons.Filled.Home),
        BottomNavItem(ROUTE_NEW_JOB, stringResource(R.string.new_job), Icons.Filled.Add),
        BottomNavItem(ROUTE_MAP, stringResource(R.string.printers), Icons.Filled.LocationOn)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(ROUTE_JOBS, ROUTE_NEW_JOB, ROUTE_MAP)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = {
                        navController.navigate(ROUTE_JOBS) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(ROUTE_JOBS) {
                JobListScreen(
                    viewModel = jobVm,
                    userId = authRepo.getUserId(),
                    onLogout = {
                        authRepo.logout()
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(ROUTE_NEW_JOB) {
                NewJobScreen(
                    viewModel = jobVm,
                    userId = authRepo.getUserId(),
                    onBack = { navController.popBackStack() },
                    onSubmitSuccess = {
                        navController.navigate(ROUTE_JOBS) {
                            popUpTo(ROUTE_JOBS) { inclusive = false }
                        }
                    }
                )
            }
            composable(ROUTE_MAP) {
                PrinterMapScreen(viewModel = mapVm)
            }
        }
    }
}
