package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.CommunityRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CommunityViewModel
import com.example.ui.viewmodel.CommunityViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local Persistent Room database and repository
        val database = AppDatabase.getDatabase(this)
        val repository = CommunityRepository(
            reportDao = database.reportDao(),
            proposalDao = database.proposalDao(),
            notificationDao = database.notificationDao()
        )

        // Setup the ViewModel via Factory
        val factory = CommunityViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[CommunityViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: CommunityViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val isAuthMode by viewModel.isAuthorityMode.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isAuthMode) MaterialTheme.colorScheme.secondaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { viewModel.setAuthorityMode(!isAuthMode) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("authority_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isAuthMode) Icons.Filled.Group else Icons.Filled.Person,
                            contentDescription = "Cambiar de Modo",
                            tint = if (isAuthMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAuthMode) "Autoridad" else "Vecino",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAuthMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isAuthMode) Icons.Filled.Build else Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isAuthMode) "Gestión Local" else "Mi Comunidad",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                actions = {
                    // Quick notification badge top icon
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("top_notifications_badge")
                    ) {
                        IconButton(onClick = { viewModel.navigateTo("NOTIFICATIONS") }) {
                            Icon(
                                imageVector = if (currentScreen == "NOTIFICATIONS") Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Seguimiento de notificaciones"
                            )
                        }
                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadNotificationsCount.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Respect proper insets of the navigation bar
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Dashboard Tab
                NavigationBarItem(
                    selected = currentScreen == "HOME",
                    onClick = { viewModel.navigateTo("HOME") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "HOME") Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Reportes"
                        )
                    },
                    label = { Text("Casos", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("tab_home")
                )

                // Explore Maps Tab
                NavigationBarItem(
                    selected = currentScreen == "EXPLORE",
                    onClick = { viewModel.navigateTo("EXPLORE") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "EXPLORE") Icons.Filled.LocationSearching else Icons.Outlined.LocationSearching,
                            contentDescription = "Ver cercanos"
                        )
                    },
                    label = { Text("Cercanos", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("tab_explore")
                )

                // Create Report Floating style Tab
                NavigationBarItem(
                    selected = currentScreen == "CREATE",
                    onClick = { viewModel.navigateTo("CREATE") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "CREATE") Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                            contentDescription = "Reportar incidente"
                        )
                    },
                    label = { Text("Reportar", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("tab_create")
                )

                // Group/Consultations Decisions Tab
                NavigationBarItem(
                    selected = currentScreen == "PROPOSALS",
                    onClick = { viewModel.navigateTo("PROPOSALS") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "PROPOSALS") Icons.Filled.AssignmentTurnedIn else Icons.Outlined.AssignmentTurnedIn,
                            contentDescription = "Votaciones"
                        )
                    },
                    label = { Text("Votaciones", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("tab_proposals")
                )

                // Ayuntamiento Admin Tab
                NavigationBarItem(
                    selected = currentScreen == "ADMIN",
                    onClick = { viewModel.navigateTo("ADMIN") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "ADMIN") Icons.Filled.AccountBalance else Icons.Outlined.AccountBalance,
                            contentDescription = "Portal de autoridades"
                        )
                    },
                    label = { Text("Gobierno", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("tab_admin")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            // Smooth horizontal animated transitions between tabs
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transitions"
            ) { screen ->
                when (screen) {
                    "HOME" -> HomeScreen(viewModel = viewModel)
                    "EXPLORE" -> ExploreScreen(viewModel = viewModel)
                    "CREATE" -> CreateReportScreen(viewModel = viewModel)
                    "PROPOSALS" -> ProposalsScreen(viewModel = viewModel)
                    "NOTIFICATIONS" -> NotificationsScreen(viewModel = viewModel)
                    "ADMIN" -> AdminScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
