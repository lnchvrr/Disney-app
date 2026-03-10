package fr.isen.chevrier.disney_app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fr.isen.chevrier.disney_app.data.MockMovieData
import fr.isen.chevrier.disney_app.data.MovieRepository
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection

class MovieListViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    var moviesState: UiState<List<Movie>> by mutableStateOf(UiState.Loading)
        private set

    var categories: List<Category> by mutableStateOf(emptyList())
        private set

    var selectedUniverseId: String? by mutableStateOf(null)
        private set

    var selectedCategoryId: String? by mutableStateOf(null)
        private set

    var searchQuery: String by mutableStateOf("")
        private set

    var userStatuses: Map<String, MovieStatusSelection> by mutableStateOf(MockMovieData.initialStatuses)
        private set

    init {
        // Par défaut : tous les films, tous les univers, toutes les catégories.
        loadData()
    }

    fun setSelectedUniverse(universeId: String?) {
        selectedUniverseId = universeId
        selectedCategoryId = null
        loadData()
    }

    fun setCategoryFilter(categoryId: String?) {
        selectedCategoryId = categoryId
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun loadUserStatuses(userId: String) {
        // Version mockée : les statuts initiaux sont chargés depuis MockMovieData.initialStatuses.
        // On ne fait rien ici, car userStatuses est déjà initialisé.
    }

    fun updateStatus(movieId: String, status: MovieStatusSelection?) {
        // Version mockée : met à jour uniquement l'état en mémoire.
        val updated = userStatuses.toMutableMap()
        if (status == null || status.isEmpty) {
            updated.remove(movieId)
        } else {
            updated[movieId] = status
        }
        userStatuses = updated
    }

    private fun loadData() {
        moviesState = UiState.Loading
        val universeId = selectedUniverseId

        categories = if (universeId == null) {
            MockMovieData.categories
        } else {
            MockMovieData.categories.filter { it.universeId == universeId }
        }

        val filtered = if (universeId == null) {
            MockMovieData.movies
        } else {
            MockMovieData.movies.filter { it.universeId == universeId }
        }
        moviesState = if (filtered.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(filtered)
        }
    }
}


