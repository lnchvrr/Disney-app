package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.ui.common.SearchFilterRow
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.ProfileViewModel
import fr.isen.chevrier.disney_app.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieGridScreen(
    viewModel: MovieListViewModel,
    profileViewModel: ProfileViewModel,
    currentUser: FirebaseUser?,
    onMovieClick: (String) -> Unit
) {
    LaunchedEffect(currentUser?.uid) {
        viewModel.loadUserStatuses(currentUser?.uid)
    }

    val moviesState by viewModel.moviesState
    val visibleMovies by viewModel.visibleMovies.collectAsState()
    val activeGenres by viewModel.activeGenres.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userStatuses by viewModel.userStatuses.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    MovieCategoryFiltersSheet(
        visible = showFilterSheet,
        categories = categories,
        activeGenres = activeGenres,
        onDismiss = { showFilterSheet = false },
        onToggleGenre = { viewModel.toggleGenreFilter(it) },
        onClearGenres = { viewModel.clearGenreFilters() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 0.dp)
    ) {
        SearchFilterRow(
            searchQuery = searchQuery,
            onSearchChange = { viewModel.updateSearchQuery(it) },
            placeholder = "Rechercher un film…",
            showFilter = true,
            onFilterClick = { showFilterSheet = true }
        )

        when (val state = moviesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Impossible de charger les films.",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyMoviesState(
                        message = "Aucun film disponible pour le moment."
                    )
                }
            }

            is UiState.Success -> {
                val stateData = state.data
                val filtersDefault = activeGenres.isEmpty() && searchQuery.isBlank()
                val listToShow = if (visibleMovies.isEmpty() && filtersDefault && stateData.isNotEmpty()) {
                    stateData
                } else {
                    visibleMovies
                }
                if (listToShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f, fill = true),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyMoviesState(message = "Aucun film ne correspond à votre sélection.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = listToShow,
                            key = { it.id }
                        ) { movie ->
                            MovieCatalogCard(
                                movie = movie,
                                status = userStatuses[movie.id],
                                onStatusSelected = { newStatus ->
                                    profileViewModel.applyStatusUpdateLocal(movie.id, newStatus, movieForCache = movie)
                                    viewModel.updateStatus(
                                        movieId = movie.id,
                                        status = newStatus,
                                        userId = currentUser?.uid
                                    )
                                },
                                onOpenDetail = { onMovieClick(movie.id) },
                                statusEnabled = currentUser != null,
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMoviesState(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
