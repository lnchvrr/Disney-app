package fr.isen.chevrier.disney_app.model

data class MovieSellOffer(
    val movieId: String,
    val userId: String,
    val userDisplayName: String?,
    val userEmail: String?
)
