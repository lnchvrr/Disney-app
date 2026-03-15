package fr.isen.chevrier.disney_app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.ui.movies.MovieListScreen
import fr.isen.chevrier.disney_app.ui.universe.UniverseListScreen
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.UniverseListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(currentUser: FirebaseUser?,
               onLoginClick: () -> Unit,
               onRegisterClick: () -> Unit,
               onLogoutClick: () -> Unit
) {
    val universeViewModel: UniverseListViewModel = viewModel()
    val movieListViewModel: MovieListViewModel = viewModel()

    var selectedUniverseId by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf(0) } // 0 = Univers, 1 = Films

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            /*topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Disneyapp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            },*/
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Disneyapp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 10.dp, bottom = 0.dp, start = 10.dp)
                    )
                    if (currentTab == 2 && currentUser == null) {
                        Row (
                            modifier = Modifier.padding(top = 10.dp, end = 10.dp)
                        ) {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    onLoginClick()
                                }
                            )

                            Text(
                                text = "Register",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    onRegisterClick()
                                }.padding(start = 20.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = {
                            currentTab = 0
                            selectedUniverseId = null
                        },
                        label = { Text("Univers") },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Univers",
                                tint = if (currentTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = {
                            currentTab = 1
                            // Si aucun univers n'est sélectionné, on affiche tous les films par défaut.
                            if (selectedUniverseId == null) {
                                movieListViewModel.setSelectedUniverse(null)
                            }
                        },
                        label = { Text("Films") },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Movie,
                                contentDescription = "Films",
                                tint = if (currentTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = {
                            currentTab = 2
                            selectedUniverseId = null
                        },
                        label = { Text("My profile") },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "My profile",
                                tint = if (currentTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
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
                            selectedUniverseId = universeId
                            movieListViewModel.setSelectedUniverse(universeId)
                            currentTab = 1
                        }
                    )

                    1 -> MovieListScreen(
                        viewModel = movieListViewModel,
                        currentUser = currentUser,
                        onBack = {
                            selectedUniverseId = null
                            currentTab = 0
                        }
                    )

                    else -> ProfileScreen(
                        currentUser = currentUser,
                        onLogoutClick = {
                            onLogoutClick()
                            currentTab = 2
                        },
                        onLoginClick = onLoginClick,
                        onRegisterClick = onRegisterClick,
                        movieListViewModel = movieListViewModel
                    )
                }
            }
        }
    }
}

