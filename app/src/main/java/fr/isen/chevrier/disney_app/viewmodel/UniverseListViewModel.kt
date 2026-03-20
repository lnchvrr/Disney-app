package fr.isen.chevrier.disney_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.chevrier.disney_app.data.MovieRepository
import fr.isen.chevrier.disney_app.model.Universe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UniverseListViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Universe>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Universe>>> = _uiState.asStateFlow()

    private var loadInFlight = false

    init {
        loadUniverses()
    }

    fun loadUniverses(force: Boolean = false) {
        if (loadInFlight) return
        if (!force && _uiState.value is UiState.Success) return
        loadInFlight = true
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    repository.fetchUniversesSuspend()
                }
                _uiState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            } catch (e: Throwable) {
                _uiState.value = UiState.Error(e.message ?: "Erreur chargement univers")
            } finally {
                loadInFlight = false
            }
        }
    }
}
