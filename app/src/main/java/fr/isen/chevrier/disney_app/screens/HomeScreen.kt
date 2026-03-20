package fr.isen.chevrier.disney_app.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.ui.common.AppTopBar
import fr.isen.chevrier.disney_app.ui.navigation.MovieCatalogNavHost
import fr.isen.chevrier.disney_app.ui.universe.UniverseListScreen
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.ProfileViewModel
import fr.isen.chevrier.disney_app.viewmodel.UniverseListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: FirebaseUser?,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val universeViewModel: UniverseListViewModel = viewModel()
    val movieListViewModel: MovieListViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    var currentTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentUser?.uid) {
        profileViewModel.loadProfile(currentUser)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                when (currentTab) {
                    0 -> AppTopBar(title = "Univers")
                    1 -> AppTopBar(
                        title = "Films",
                        showBack = true,
                        onBack = { currentTab = 0 }
                    )
                    else -> AppTopBar(title = "Profil")
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.height(56.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0.dp)
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        label = {
                            Text(
                                "Univers",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Univers",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = itemColors,
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = {
                            currentTab = 1
                            movieListViewModel.onFilmsTabOpenedFromBottomNavigation()
                        },
                        label = {
                            Text(
                                "Films",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Movie,
                                contentDescription = "Films",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = itemColors,
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        label = {
                            Text(
                                "Profil",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Profil",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = itemColors,
                        alwaysShowLabel = true
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    0 -> UniverseListScreen(
                        viewModel = universeViewModel,
                        onUniverseSelected = { universeId ->
                            movieListViewModel.setSelectedUniverse(universeId)
                            currentTab = 1
                        }
                    )

                    1 -> MovieCatalogNavHost(
                        viewModel = movieListViewModel,
                        profileViewModel = profileViewModel,
                        currentUser = currentUser,
                        onExitToUnivers = {
                            currentTab = 0
                        }
                    )

                    else -> ProfileScreen(
                        currentUser = currentUser,
                        onLoginClick = onLoginClick,
                        onRegisterClick = onRegisterClick,
                        onLogoutClick = onLogoutClick,
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
    }
}
