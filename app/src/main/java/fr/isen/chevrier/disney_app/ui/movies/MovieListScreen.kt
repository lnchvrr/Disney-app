package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.data.MockMovieData
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatus
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.UiState

@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    currentUser: FirebaseUser?,
    onBack: () -> Unit
) {
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            viewModel.loadUserStatuses(userId)
        }
    }

    val universesById = remember { MockMovieData.universes.associateBy { it.id } }
    val categoriesById = remember { MockMovieData.categories.associateBy { it.id } }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        TopBar(onBack = onBack)
        Spacer(modifier = Modifier.height(12.dp))
        SearchBar(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        CategoryFilters(
            categories = viewModel.categories,
            selectedCategoryId = viewModel.selectedCategoryId,
            onCategorySelected = { id -> viewModel.setCategoryFilter(id) }
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (val state = viewModel.moviesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Impossible de charger les films",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun film trouvé",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is UiState.Success -> {
                val baseList = state.data
                val filteredByCategory = viewModel.selectedCategoryId?.let { catId ->
                    baseList.filter { it.categoryId == catId }
                } ?: baseList
                val filteredBySearch = if (viewModel.searchQuery.isBlank()) {
                    filteredByCategory
                } else {
                    val q = viewModel.searchQuery.trim().lowercase()
                    filteredByCategory.filter {
                        it.title.lowercase().contains(q)
                    }
                }

                if (filteredBySearch.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun film ne correspond à votre recherche",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBySearch) { movie ->
                            MovieCard(
                                movie = movie,
                                universeName = universesById[movie.universeId]?.name.orEmpty(),
                                categoryName = movie.categoryId?.let { categoriesById[it]?.name },
                                status = viewModel.userStatuses[movie.id],
                                onStatusSelected = { newStatus ->
                                    viewModel.updateStatus(
                                        movieId = movie.id,
                                        status = newStatus
                                    )
                                },
                                onClick = {
                                    selectedMovie = movie
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedMovie?.let { movie ->
        AlertDialog(
            onDismissRequest = { selectedMovie = null },
            confirmButton = {},
            text = {
                MovieDetailContent(
                    movie = movie,
                    universeName = universesById[movie.universeId]?.name.orEmpty(),
                    categoryName = movie.categoryId?.let { categoriesById[it]?.name },
                    currentStatus = viewModel.userStatuses[movie.id],
                    onStatusSelected = { status ->
                        viewModel.updateStatus(movie.id, status)
                    }
                )
            }
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "Films",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Rechercher un film") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Recherche"
            )
        },
        singleLine = true
    )
}

@Composable
private fun CategoryFilters(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit
) {
    if (categories.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { onCategorySelected(null) },
            label = { Text("Toutes") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selectedCategoryId == null) AccentBlueLight else CardWhiteStrong
            )
        )
        categories.take(5).forEach { category ->
            val selected = selectedCategoryId == category.id
            AssistChip(
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) AccentBlueLight else CardWhite
                )
            )
        }
    }
}

@Composable
private fun MovieCard(
    movie: Movie,
    universeName: String,
    categoryName: String?,
    status: MovieStatus?,
    onStatusSelected: (MovieStatus?) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextOnCard,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Sortie cinéma : ${movie.releaseDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnCard
            )
            Text(
                text = "Univers : $universeName",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnCard
            )
            categoryName?.let {
                Text(
                    text = "Catégorie : $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnCard
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StatusRow(
                currentStatus = status,
                onStatusSelected = onStatusSelected
            )
        }
    }
}

@Composable
fun StatusRow(
    currentStatus: MovieStatus?,
    onStatusSelected: (MovieStatus?) -> Unit
) {
    Column {
        Text(
            text = "Statut",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusChip(
                label = "Vu",
                status = MovieStatus.WATCHED,
                currentStatus = currentStatus,
                onStatusSelected = onStatusSelected
            )
            StatusChip(
                label = "À voir",
                status = MovieStatus.WANT_TO_WATCH,
                currentStatus = currentStatus,
                onStatusSelected = onStatusSelected
            )
            StatusChip(
                label = "DVD",
                status = MovieStatus.OWN_DVD,
                currentStatus = currentStatus,
                onStatusSelected = onStatusSelected
            )
            StatusChip(
                label = "À vendre",
                status = MovieStatus.WANT_TO_SELL,
                currentStatus = currentStatus,
                onStatusSelected = onStatusSelected
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    status: MovieStatus,
    currentStatus: MovieStatus?,
    onStatusSelected: (MovieStatus?) -> Unit
) {
    val isSelected = currentStatus == status
    AssistChip(
        onClick = {
            onStatusSelected(if (isSelected) null else status)
        },
        label = {
            Text(
                label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextOnCard
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) AccentBlueLight else CardWhiteStrong
        )
    )
}


