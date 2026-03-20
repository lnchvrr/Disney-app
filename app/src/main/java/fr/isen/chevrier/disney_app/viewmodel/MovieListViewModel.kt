package fr.isen.chevrier.disney_app.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.chevrier.disney_app.data.MovieRepository
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieSellOffer
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.Universe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieListViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _moviesState = mutableStateOf<UiState<List<Movie>>>(UiState.Loading)
    val moviesState: State<UiState<List<Movie>>> get() = _moviesState

    private val _universes = MutableStateFlow<List<Universe>>(emptyList())
    val universes: StateFlow<List<Universe>> = _universes.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _visibleMovies = MutableStateFlow<List<Movie>>(emptyList())
    val visibleMovies: StateFlow<List<Movie>> = _visibleMovies.asStateFlow()

    private val _selectedUniverseId = MutableStateFlow<String?>(null)
    val selectedUniverseId: StateFlow<String?> = _selectedUniverseId.asStateFlow()

    private val _activeGenres = MutableStateFlow<List<String>>(emptyList())
    val activeGenres: StateFlow<List<String>> = _activeGenres.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userStatuses = MutableStateFlow<Map<String, MovieStatusSelection>>(emptyMap())
    val userStatuses: StateFlow<Map<String, MovieStatusSelection>> = _userStatuses.asStateFlow()

    private val _sellersByMovie = MutableStateFlow<Map<String, List<MovieSellOffer>>>(emptyMap())
    val sellersByMovie: StateFlow<Map<String, List<MovieSellOffer>>> = _sellersByMovie.asStateFlow()

    private val _universesById = MutableStateFlow<Map<String, Universe>>(emptyMap())
    val universesById: StateFlow<Map<String, Universe>> = _universesById.asStateFlow()

    private val _categoriesById = MutableStateFlow<Map<String, Category>>(emptyMap())
    val categoriesById: StateFlow<Map<String, Category>> = _categoriesById.asStateFlow()

    private var hasLoadedUniverses = false
    private var loadedStatusesForUserId: String? = null

    private var allCategoriesRaw: List<Category> = emptyList()
    private var latestMoviesFull: List<Movie> = emptyList()
    private var allMoviesRaw: List<Movie> = emptyList()
    private var allMoviesById: Map<String, Movie> = emptyMap()

    init {
        loadUniversesOnce()
        loadCategoriesOnce()
        subscribeMovies()
    }

    fun setSelectedUniverse(universeId: String?) {
        if (_selectedUniverseId.value == universeId) return
        _selectedUniverseId.value = universeId
        _activeGenres.value = emptyList()
        applyMovieFiltersFromLatest()
    }

    fun onFilmsTabOpenedFromBottomNavigation() {
        _selectedUniverseId.value = null
        _activeGenres.value = emptyList()
        _searchQuery.value = ""
        applyMovieFiltersFromLatest()
    }

    fun toggleGenreFilter(categoryId: String) {
        val set = _activeGenres.value.toMutableSet()
        if (!set.add(categoryId)) set.remove(categoryId)
        _activeGenres.value = set.toList()
        updateVisibleMovies()
    }

    fun clearGenreFilters() {
        _activeGenres.value = emptyList()
        updateVisibleMovies()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateVisibleMovies()
    }

    fun loadUserStatuses(userId: String?) {
        if (userId.isNullOrBlank()) {
            loadedStatusesForUserId = null
            _userStatuses.value = emptyMap()
            return
        }
        if (userId == loadedStatusesForUserId) return
        loadedStatusesForUserId = userId
        repository.fetchUserMovieStatuses(userId) { result ->
            result.onSuccess { map ->
                _userStatuses.value = map
            }.onFailure {
                _userStatuses.value = emptyMap()
            }
        }
    }

    fun updateStatus(movieId: String, status: MovieStatusSelection?, userId: String?) {
        val selection = status?.let { s ->
            if (!s.ownsMovie && s.wantToSell) s.copy(wantToSell = false) else s
        } ?: status

        if (userId.isNullOrBlank()) return

        val previous = _userStatuses.value.toMutableMap()
        val updated = _userStatuses.value.toMutableMap()
        if (selection == null || selection.isEmpty) updated.remove(movieId)
        else updated[movieId] = selection
        _userStatuses.value = updated

        repository.setUserMovieStatus(userId, movieId, selection) { result ->
            result.onFailure { _userStatuses.value = previous }
        }
    }

    fun loadSellersForMovie(movieId: String) {
        if (_sellersByMovie.value.containsKey(movieId)) return
        repository.fetchUsersWantToSellMovie(movieId) { result ->
            val sellers = result.getOrDefault(emptyList())
            _sellersByMovie.value = _sellersByMovie.value.toMutableMap().apply { this[movieId] = sellers }
        }
    }

    private fun loadUniversesOnce() {
        if (hasLoadedUniverses) return
        hasLoadedUniverses = true
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    repository.fetchUniversesSuspend()
                }
                _universes.value = list
                _universesById.value = list.associateBy { it.id }
            } catch (e: Throwable) {
                _universes.value = emptyList()
                _universesById.value = emptyMap()
            }
        }
    }

    private fun loadCategoriesOnce() {
        viewModelScope.launch {
            try {
                val catList = withContext(Dispatchers.IO) { repository.fetchCategoriesSuspend() }
                allCategoriesRaw = catList
            } catch (e: Throwable) {
                allCategoriesRaw = emptyList()
            }
            refreshCategoriesForCurrentUniverse()
        }
    }

    private fun refreshCategoriesForCurrentUniverse() {
        val universeId = _selectedUniverseId.value
        val categoriesForUniverse = if (universeId == null) allCategoriesRaw
        else allCategoriesRaw.filter { it.universeId == universeId }
        _categories.value = categoriesForUniverse
        _categoriesById.value = categoriesForUniverse.associateBy { it.id }
    }

    private fun subscribeMovies() {
        viewModelScope.launch {
            repository.observeMovies()
                .catch { e ->
                    _moviesState.value = UiState.Error(e.message ?: "Erreur chargement films")
                }
                .collect { movieList ->
                    val enriched = repository.enrichMoviesWithPosters(movieList)
                    latestMoviesFull = enriched
                    allMoviesById = enriched.associateBy { it.id }
                    applyMovieFiltersFromLatest()
                }
        }
    }

    private fun applyMovieFiltersFromLatest() {
        val universeId = _selectedUniverseId.value
        val filtered = if (universeId == null) latestMoviesFull
        else latestMoviesFull.filter { it.universeId == universeId }

        allMoviesRaw = filtered
        refreshCategoriesForCurrentUniverse()

        _moviesState.value = if (allMoviesRaw.isEmpty()) UiState.Empty else UiState.Success(allMoviesRaw)
        updateVisibleMovies()
    }

    private fun updateVisibleMovies() {
        val genres = _activeGenres.value
        val byGenre = when {
            genres.isEmpty() -> allMoviesRaw
            else -> allMoviesRaw.filter { m -> m.categoryId != null && m.categoryId in genres }
        }
        val q = _searchQuery.value.trim().lowercase()
        _visibleMovies.value = if (q.isBlank()) byGenre
        else byGenre.filter { it.title.lowercase().contains(q) }
    }

    fun getMovieById(movieId: String): Movie? {
        allMoviesById[movieId]?.let { return it }
        allMoviesRaw.firstOrNull { it.id == movieId }?.let { return it }
        latestMoviesFull.firstOrNull { it.id == movieId }?.let { return it }
        return _visibleMovies.value.firstOrNull { it.id == movieId }
    }
}
