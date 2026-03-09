package fr.isen.chevrier.disney_app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fr.isen.chevrier.disney_app.data.MockMovieData
import fr.isen.chevrier.disney_app.data.MovieRepository
import fr.isen.chevrier.disney_app.model.Universe

class UniverseListViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    var uiState: UiState<List<Universe>> by mutableStateOf(UiState.Loading)
        private set

    init {
        Log.d("UniverseListViewModel", "init: loading universes (mock)")
        loadUniverses()
    }

    fun loadUniverses() {
        Log.d("UniverseListViewModel", "loadUniverses: using MockMovieData")
        val universes = MockMovieData.universes
        uiState = if (universes.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(universes)
        }
    }
}


