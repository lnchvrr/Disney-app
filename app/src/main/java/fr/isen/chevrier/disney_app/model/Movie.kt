package fr.isen.chevrier.disney_app.model

data class Movie(
    val id: String,
    val title: String,
    val releaseDate: String,
    val universeId: String,
    val categoryId: String? = null,
    val posterUrl: String? = null
)

