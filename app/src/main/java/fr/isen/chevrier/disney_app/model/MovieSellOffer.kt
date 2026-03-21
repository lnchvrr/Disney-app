package fr.isen.chevrier.disney_app.model

data class MovieSellOffer(
    val userId: String = "",
    val userName: String = "",
    val ownership: OwnershipStatus = OwnershipStatus.OWN_DVD
)