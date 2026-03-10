package fr.isen.chevrier.disney_app.viewmodel

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String? = null) : UiState<Nothing>()
}

