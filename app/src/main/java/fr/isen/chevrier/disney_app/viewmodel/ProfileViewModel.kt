package fr.isen.chevrier.disney_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import fr.isen.chevrier.disney_app.data.MovieRepository
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.WatchStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OwnedMovieItem(
    val movie: Movie,
    val status: MovieStatusSelection
)

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val ownedMovies: List<OwnedMovieItem> = emptyList(),
    val seenMovies: List<OwnedMovieItem> = emptyList(),
    val wishlistMovies: List<OwnedMovieItem> = emptyList(),
    val sellingMovies: List<OwnedMovieItem> = emptyList(),
    val watchedCount: Int = 0,
    val toWatchCount: Int = 0,
    val dvdCount: Int = 0,
    val blurayCount: Int = 0,
    val isLoadingOwnedMovies: Boolean = false,
    val message: String? = null
)

class ProfileViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadedForUserId: String? = null
    private var moviesById: Map<String, Movie> = emptyMap()
    private var statusesByMovieId: Map<String, MovieStatusSelection> = emptyMap()

    fun loadProfile(user: FirebaseUser?) {
        if (user == null) {
            loadedForUserId = null
            moviesById = emptyMap()
            statusesByMovieId = emptyMap()
            _uiState.value = ProfileUiState(message = "Connectez-vous pour voir votre profil.")
            return
        }
        _uiState.value = _uiState.value.copy(
            displayName = user.displayName.orEmpty(),
            email = user.email.orEmpty(),
            message = null
        )
        if (loadedForUserId == user.uid) return
        loadedForUserId = user.uid
        loadOwnedMovies(user.uid)
    }

    fun saveDisplayName(user: FirebaseUser?, newName: String) {
        if (user == null) return
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Le nom ne peut pas être vide.")
            return
        }
        val updates = UserProfileChangeRequest.Builder().setDisplayName(trimmed).build()
        user.updateProfile(updates)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(displayName = trimmed, message = "Nom mis a jour.")
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(message = it.message ?: "Impossible de mettre a jour le nom.")
            }
    }

    fun saveEmail(user: FirebaseUser?, newEmail: String) {
        if (user == null) return
        val trimmed = newEmail.trim()
        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "L'email ne peut pas etre vide.")
            return
        }
        user.updateEmail(trimmed)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(email = trimmed, message = "Email mis a jour.")
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(message = it.message ?: "Impossible de mettre a jour l'email.")
            }
    }

    fun savePassword(user: FirebaseUser?, password: String, confirm: String) {
        if (user == null) return
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(message = "Le mot de passe doit contenir au moins 8 caracteres.")
            return
        }
        if (password != confirm) {
            _uiState.value = _uiState.value.copy(message = "Les mots de passe ne correspondent pas.")
            return
        }
        user.updatePassword(password)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(message = "Mot de passe mis a jour.")
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(message = it.message ?: "Impossible de mettre a jour le mot de passe.")
            }
    }

    /**
     * Mise à jour optimistic “UI-only” des stats et des sections.
     * La persistance Firebase reste faite via `MovieListViewModel.updateStatus(...)`.
     */
    /**
     * @param movieForCache si le cache profil n’a pas encore ce film (ex. action depuis le catalogue avant ouverture du profil).
     */
    fun applyStatusUpdateLocal(
        movieId: String,
        newStatus: MovieStatusSelection?,
        movieForCache: Movie? = null
    ) {
        if (movieForCache != null) {
            moviesById = moviesById + (movieId to movieForCache)
        }
        if (moviesById[movieId] == null) return

        val mutable = statusesByMovieId.toMutableMap()
        if (newStatus == null || newStatus.isEmpty) mutable.remove(movieId)
        else mutable[movieId] = newStatus
        statusesByMovieId = mutable
        recomputeFromCache()
    }

    private fun loadOwnedMovies(userId: String) {
        _uiState.value = _uiState.value.copy(isLoadingOwnedMovies = true, message = null)
        viewModelScope.launch {
            try {
                val movies = withContext(Dispatchers.IO) { repository.fetchMoviesSuspend() }
                val statuses = withContext(Dispatchers.IO) { repository.fetchUserMovieStatusesSuspend(userId) }
                moviesById = movies.associateBy { it.id }
                statusesByMovieId = statuses
                recomputeFromCache()
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoadingOwnedMovies = false,
                    message = e.message ?: "Impossible de charger vos films ou statuts."
                )
            }
        }
    }

    private fun recomputeFromCache() {
        val statuses = statusesByMovieId

        fun item(movieId: String, status: MovieStatusSelection): OwnedMovieItem? {
            val movie = moviesById[movieId] ?: return null
            return OwnedMovieItem(movie = movie, status = status)
        }

        val owned = statuses.entries.mapNotNull { (movieId, status) ->
            if (!status.ownsMovie) return@mapNotNull null
            item(movieId, status)
        }.sortedBy { it.movie.title }

        val seen = statuses.entries.mapNotNull { (movieId, status) ->
            if (status.watch != WatchStatus.WATCHED) return@mapNotNull null
            item(movieId, status)
        }.sortedBy { it.movie.title }

        val wishlist = statuses.entries.mapNotNull { (movieId, status) ->
            if (status.watch != WatchStatus.WANT_TO_WATCH) return@mapNotNull null
            item(movieId, status)
        }.sortedBy { it.movie.title }

        val selling = statuses.entries.mapNotNull { (movieId, status) ->
            if (!status.wantToSell || !status.ownsMovie) return@mapNotNull null
            item(movieId, status)
        }.sortedBy { it.movie.title }

        val watchedCount = statuses.values.count { it.watch == WatchStatus.WATCHED }
        val toWatchCount = statuses.values.count { it.watch == WatchStatus.WANT_TO_WATCH }
        val dvdCount = statuses.values.count { it.ownership == fr.isen.chevrier.disney_app.model.OwnershipStatus.OWN_DVD }
        val blurayCount = statuses.values.count { it.ownership == fr.isen.chevrier.disney_app.model.OwnershipStatus.OWN_BLURAY }

        _uiState.value = _uiState.value.copy(
            isLoadingOwnedMovies = false,
            ownedMovies = owned,
            seenMovies = seen,
            wishlistMovies = wishlist,
            sellingMovies = selling,
            watchedCount = watchedCount,
            toWatchCount = toWatchCount,
            dvdCount = dvdCount,
            blurayCount = blurayCount
        )
    }
}
