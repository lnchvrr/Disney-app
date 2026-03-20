package fr.isen.chevrier.disney_app.model

/**
 * Statuts utilisateur pour un film :
 * - Visionnage : Vu / À voir
 * - Possession : DVD ou Blu-ray (un seul à la fois)
 * - Veut vendre : possible uniquement si le film est possédé (DVD ou Blu-ray)
 */
enum class WatchStatus {
    WATCHED,
    WANT_TO_WATCH
}

enum class OwnershipStatus {
    OWN_DVD,
    OWN_BLURAY
}

data class MovieStatusSelection(
    val watch: WatchStatus? = null,
    val ownership: OwnershipStatus? = null,
    val wantToSell: Boolean = false
) {
    val isEmpty: Boolean get() = watch == null && ownership == null && !wantToSell

    /** true si l'utilisateur possède le film (DVD ou Blu-ray), donc peut cocher "Veut vendre" */
    val ownsMovie: Boolean get() = ownership == OwnershipStatus.OWN_DVD || ownership == OwnershipStatus.OWN_BLURAY
}

