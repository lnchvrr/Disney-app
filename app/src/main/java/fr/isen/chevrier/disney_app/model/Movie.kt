package fr.isen.chevrier.disney_app.model

data class Movie(
<<<<<<< HEAD
    val id: String,
    val title: String,
    val posterUrl: String? = null,
    val releaseDate: String? = null,
    val universeId: String? = null,
    val categoryId: String? = null
=======
    val id: String = "",
    val title: String = "",
    val releaseDate: String = "",
    val universeId: String = "",
    val categoryId: String? = null,
    val posterUrl: String? = null,
    val posterPath: String? = null,
    /** Champs bruts Firebase (affichage détail / debug). */
    val rawFields: Map<String, String> = emptyMap()
>>>>>>> 21dc465 (feat: finalize app (fully functional, only minor design adjustments remaining))
)

