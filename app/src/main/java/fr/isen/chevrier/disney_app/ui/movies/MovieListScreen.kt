package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.ui.components.BackHeader
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.UiState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import fr.isen.chevrier.disney_app.viewmodel.MovieDateSortOrder

@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    currentUser: FirebaseUser?,
    onBack: () -> Unit
) {
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            viewModel.loadUserStatuses(userId)
        }
    }

    val universesById = viewModel.universesById
    val categoriesById = viewModel.categoriesById
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    val resolvedUserName = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: currentUser?.uid

    LaunchedEffect(selectedMovie?.id) {
        val movieId = selectedMovie?.id
        if (movieId == null) {
            viewModel.clearMovieSellOffers()
        } else {
            viewModel.loadMovieSellOffers(movieId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        BackHeader(
            title = "Films",
            subtitle = "Catalogue",
            onBack = onBack
        )


        SearchBar(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) }
        )


        CategoryFilters(
            categories = viewModel.categories,
            selectedCategoryId = viewModel.selectedCategoryId,
            selectedSortOrder = viewModel.dateSortOrder,
            onCategorySelected = { id -> viewModel.setCategoryFilter(id) },
            onSortOrderSelected = { order -> viewModel.updateDateSortOrder(order) }
        )


        when (val state = viewModel.moviesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun film trouvé",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            is UiState.Success -> {
                val movies = state.data

                if (movies.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                        items(movies) { movie ->
                            MovieCard(
                                movie = movie,
                                universeName = universesById[movie.universeId]?.name.orEmpty(),
                                categoryName = movie.categoryId?.let { categoriesById[it]?.name },
                                status = viewModel.userStatuses[movie.id],
                                statusEnabled = currentUser != null,
                                onStatusSelected = { newStatus ->
                                    viewModel.updateStatus(
                                        movieId = movie.id,
                                        status = newStatus,
                                        userId = currentUser?.uid,
                                        userName = resolvedUserName
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
            onDismissRequest = {
                selectedMovie = null
                viewModel.clearMovieSellOffers()
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedMovie = null
                    viewModel.clearMovieSellOffers()
                }) {
                    Text("Fermer")
                }
            },
            text = {
                MovieDetailContent(
                    movie = movie,
                    universeName = universesById[movie.universeId]?.name.orEmpty(),
                    categoryName = movie.categoryId?.let { categoriesById[it]?.name },
                    currentStatus = viewModel.userStatuses[movie.id],
                    statusEnabled = currentUser != null,
                    onStatusSelected = { status ->
                        viewModel.updateStatus(
                            movieId = movie.id,
                            status = status,
                            userId = currentUser?.uid,
                            userName = resolvedUserName
                        )
                    },
                    sellOffers = viewModel.movieSellOffers,
                    isLoadingSellOffers = viewModel.movieSellOffersLoading
                )
            }
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
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CategoryFilters(
    categories: List<Category>,
    selectedCategoryId: String?,
    selectedSortOrder: MovieDateSortOrder,
    onCategorySelected: (String?) -> Unit,
    onSortOrderSelected: (MovieDateSortOrder) -> Unit
) {
    if (categories.isEmpty()) return

    var sortMenuExpanded by remember { mutableStateOf(false) }

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

        item {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { sortMenuExpanded = true },
                    label = {
                        Text(
                            when (selectedSortOrder) {
                                MovieDateSortOrder.ASCENDING -> "Par date ↑"
                                MovieDateSortOrder.DESCENDING -> "Par date ↓"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Tri par date"
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
                        selected = false,
                        borderColor = Color.White.copy(alpha = 0.6f),
                        selectedBorderColor = Color.Transparent
                    )
                )

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Date croissante") },
                        onClick = {
                            onSortOrderSelected(MovieDateSortOrder.ASCENDING)
                            sortMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Date décroissante") },
                        onClick = {
                            onSortOrderSelected(MovieDateSortOrder.DESCENDING)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
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
    statusEnabled: Boolean,
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
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
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
                    modifier = Modifier.size(width = 84.dp, height = 112.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhiteStrong),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    AsyncImage(
                        model = movie.posterUrl ?: fr.isen.chevrier.disney_app.R.drawable.universe_default,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
                            text = universeName,
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
                            text = movie.releaseDate,
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


            if (statusEnabled) {
                StatusRow(
                    currentStatus = status,
                    onStatusSelected = onStatusSelected
                )
            }
        }
    }
}