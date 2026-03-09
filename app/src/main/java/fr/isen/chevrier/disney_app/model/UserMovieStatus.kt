package fr.isen.chevrier.disney_app.model

data class UserMovieStatus(
    val userId: String,
    val movieId: String,
    val status: MovieStatus
)

