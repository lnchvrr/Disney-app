package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.data.MockMovieData
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.UiState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import fr.isen.chevrier.disney_app.R

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MoviesHeader(onBack = onBack)
        Spacer(modifier = Modifier.height(10.dp))
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
            confirmButton = {
                TextButton(onClick = { selectedMovie = null }) {
                    Text("Fermer")
                }
            },
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
private fun MoviesHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "Catalogue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = "Films",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
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
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CategoryFilters(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit
) {
    if (categories.isEmpty()) return

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Toutes") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBlueLight,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = CardWhiteStrong,
                    labelColor = TextOnCard
                ),
                shape = RoundedCornerShape(14.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategoryId == null,
                    borderColor = Color.White.copy(alpha = 0.6f),
                    selectedBorderColor = Color.Transparent
                )
            )
        }
        items(categories) { category ->
            val selected = selectedCategoryId == category.id
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBlueLight,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = CardWhite,
                    labelColor = TextOnCard
                ),
                shape = RoundedCornerShape(14.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = Color.White.copy(alpha = 0.6f),
                    selectedBorderColor = Color.Transparent
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
    status: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .size(width = 84.dp, height = 112.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhiteStrong),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.movie_poster_placeholder),
                        error = painterResource(R.drawable.movie_poster_placeholder)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextOnCard,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (universeName.isNotBlank()) universeName else "Univers inconnu",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextOnCard.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextOnCard.copy(alpha = 0.7f)
                        )
                        Text(
                            text = movie.releaseDate ?: "Date inconnue",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextOnCard.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    categoryName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnCard.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
    currentStatus: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit
) {
    val selection = currentStatus ?: MovieStatusSelection()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Statuts",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        StatusGroupRow(
            title = "Visionnage",
            options = listOf(
                WatchStatus.WATCHED to "Vu",
                WatchStatus.WANT_TO_WATCH to "À voir"
            ),
            selected = selection.watch,
            onSelected = { watch ->
                val updated = selection.copy(watch = if (selection.watch == watch) null else watch)
                onStatusSelected(if (updated.isEmpty) null else updated)
            }
        )

        StatusGroupRow(
            title = "Support",
            options = listOf(
                OwnershipStatus.OWN_DVD to "DVD",
                OwnershipStatus.WANT_TO_SELL to "À vendre"
            ),
            selected = selection.ownership,
            onSelected = { ownership ->
                val updated = selection.copy(ownership = if (selection.ownership == ownership) null else ownership)
                onStatusSelected(if (updated.isEmpty) null else updated)
            }
        )
    }
}

@Composable
private fun <T> StatusGroupRow(
    title: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                val isSelected = selected == value
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelected(value) },
                    label = {
                        Text(
                            text = label,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlueLight,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = CardWhiteStrong,
                        labelColor = TextOnCard
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.White.copy(alpha = 0.6f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}


