package fr.isen.chevrier.disney_app.model

data class Movie(
    val id: String,
    val title: String,
    val posterUrl: String? = null,
    val releaseDate: String? = null,
    val universeId: String? = null,
    val categoryId: String? = null
)

