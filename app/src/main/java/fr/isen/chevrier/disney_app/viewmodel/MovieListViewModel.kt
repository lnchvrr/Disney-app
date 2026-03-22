package fr.isen.chevrier.disney_app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.chevrier.disney_app.data.MovieRepository
import fr.isen.chevrier.disney_app.data.TmdbService
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieSellOffer
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.Universe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch


enum class MovieDateSortOrder {
    ASCENDING,
    DESCENDING
}
class MovieListViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    var moviesState: UiState<List<Movie>> by mutableStateOf(UiState.Loading)
        private set

    var categories: List<Category> by mutableStateOf(emptyList())
        private set

    var universesById: Map<String, Universe> by mutableStateOf(emptyMap())
        private set

    var categoriesById: Map<String, Category> by mutableStateOf(emptyMap())
        private set

    var selectedUniverseId: String? by mutableStateOf(null)
        private set

    var selectedCategoryId: String? by mutableStateOf(null)
        private set

    var searchQuery: String by mutableStateOf("")
        private set

    var dateSortOrder: MovieDateSortOrder by mutableStateOf(MovieDateSortOrder.ASCENDING)
        private set

    var userStatuses: Map<String, MovieStatusSelection> by mutableStateOf(emptyMap())
        private set

    var movieSellOffers: List<MovieSellOffer> by mutableStateOf(emptyList())
        private set

    var movieSellOffersLoading: Boolean by mutableStateOf(false)
        private set

    var movieSellOffersError: String? by mutableStateOf(null)
        private set

    private var currentSellOffersMovieId: String? = null
    private var allMovies: List<Movie> = emptyList()

    init {
        loadData()
    }

    fun setSelectedUniverse(universeId: String?) {
        selectedUniverseId = universeId
        selectedCategoryId = null
        updateCategoriesForSelectedUniverse()
        applyFilters()
    }

    fun setCategoryFilter(categoryId: String?) {
        selectedCategoryId = categoryId
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun updateDateSortOrder(order: MovieDateSortOrder) {
        dateSortOrder = order
        applyFilters()
    }

    fun toggleGenreFilter(categoryId: String) {
        selectedCategoryId = if (selectedCategoryId == categoryId) null else categoryId
        applyFilters()
    }

    fun clearGenreFilters() {
        selectedCategoryId = null
        applyFilters()
    }

    val activeGenres: List<String>
        get() = selectedCategoryId?.let(::listOf) ?: emptyList()

    val visibleMovies: List<Movie>
        get() = when (val state = moviesState) {
            is UiState.Success -> state.data
            else -> emptyList()
        }

    fun getMovieById(movieId: String): Movie? {
        return allMovies.firstOrNull { it.id == movieId }
    }

    fun loadUserStatuses(userId: String?) {
        if (userId.isNullOrBlank()) {
            userStatuses = emptyMap()
            return
        }

        repository.fetchUserMovieStatuses(userId) { result ->
            userStatuses = result.getOrElse { emptyMap() }
                .mapValues { (_, value) -> value.normalized }
                .filterValues { !it.isEmpty }
        }
    }

    fun updateStatus(
        movieId: String,
        status: MovieStatusSelection?,
        userId: String? = null,
        userName: String? = null
    ) {
        val normalized = status?.normalized?.takeUnless { it.isEmpty }

        val updatedLocal = userStatuses.toMutableMap()
        if (normalized == null) {
            updatedLocal.remove(movieId)
        } else {
            updatedLocal[movieId] = normalized
        }
        userStatuses = updatedLocal

        if (userId.isNullOrBlank()) return

        repository.setUserMovieStatus(
            userId = userId,
            movieId = movieId,
            status = normalized,
            userName = userName
        ) { result ->
            result.onSuccess {
                if (currentSellOffersMovieId == movieId) {
                    loadMovieSellOffers(movieId)
                }
            }
            result.onFailure {
                loadUserStatuses(userId)
                if (currentSellOffersMovieId == movieId) {
                    loadMovieSellOffers(movieId)
                }
            }
        }
    }

    fun loadMovieSellOffers(movieId: String) {
        currentSellOffersMovieId = movieId
        movieSellOffersLoading = true
        movieSellOffersError = null

        repository.fetchMovieSellOffers(movieId) { result ->
            movieSellOffersLoading = false
            result
                .onSuccess { offers ->
                    movieSellOffers = offers
                    movieSellOffersError = null
                }
                .onFailure { error ->
                    movieSellOffers = emptyList()
                    movieSellOffersError = error.message ?: "Impossible de charger les vendeurs."
                }
        }
    }

    fun clearMovieSellOffers() {
        currentSellOffersMovieId = null
        movieSellOffers = emptyList()
        movieSellOffersLoading = false
        movieSellOffersError = null
    }

    fun loadData() {
        moviesState = UiState.Loading

        repository.fetchMovies { moviesResult ->
            moviesResult
                .onSuccess { movies ->
                    viewModelScope.launch {
                        Log.d("MovieListViewModel", "Movies loaded from Firebase: ${movies.size}")

                        val enrichedMovies = movies.map { movie ->
                            async {
                                if (!movie.posterUrl.isNullOrBlank()) {
                                    movie
                                } else {
                                    val tmdbPoster = runCatching {
                                        TmdbService.fetchMoviePoster(movie.title)
                                    }.getOrNull()

                                    movie.copy(posterUrl = tmdbPoster)
                                }
                            }
                        }.awaitAll()

                        allMovies = enrichedMovies
                        Log.d(
                            "MovieListViewModel",
                            "Movies after TMDB enrichment: ${allMovies.count { !it.posterUrl.isNullOrBlank() }}"
                        )
                        applyFilters()
                    }
                }
                .onFailure { error ->
                    Log.e("MovieListViewModel", "Error loading movies", error)
                    moviesState = UiState.Error(error.message ?: "Erreur lors du chargement des films")
                }
        }

        repository.fetchUniverses { universesResult ->
            val universes = universesResult.getOrElse { emptyList() }
            universesById = universes.associateBy { it.id }
        }

        repository.fetchCategories { categoriesResult ->
            val loadedCategories = categoriesResult.getOrElse { emptyList() }
            categoriesById = loadedCategories.associateBy { it.id }
            updateCategoriesForSelectedUniverse(loadedCategories)
        }
    }

    private fun updateCategoriesForSelectedUniverse(
        source: List<Category> = categoriesById.values.toList()
    ) {
        categories = selectedUniverseId?.let { universeId ->
            source.filter { it.universeId == universeId }
        } ?: source
    }

    private fun applyFilters() {
        var filtered = allMovies

        selectedUniverseId?.let { universeId ->
            filtered = filtered.filter { it.universeId == universeId }
        }

        selectedCategoryId?.let { categoryId ->
            filtered = filtered.filter { it.categoryId == categoryId }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            filtered = filtered.filter { it.title.lowercase().contains(q) }
        }

        filtered = when (dateSortOrder) {
            MovieDateSortOrder.ASCENDING -> {
                filtered.sortedBy { movie ->
                    movie.releaseDate.trim().toIntOrNull() ?: Int.MAX_VALUE
                }
            }
            MovieDateSortOrder.DESCENDING -> {
                filtered.sortedByDescending { movie ->
                    movie.releaseDate.trim().toIntOrNull() ?: Int.MIN_VALUE
                }
            }
        }

        moviesState = if (filtered.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(filtered)
        }
    }
}