package fr.isen.chevrier.disney_app.model

/**
 * Deux groupes de statuts indépendants :
 * - Groupe 1 : progression (Vu / À voir)
 * - Groupe 2 : possession (DVD / À vendre)
 *
 * Un seul choix par groupe, mais les deux groupes peuvent être sélectionnés simultanément.
 */
enum class WatchStatus {
    WATCHED,
    WANT_TO_WATCH
}

enum class OwnershipStatus {
    OWN_DVD,
    WANT_TO_SELL
}

data class MovieStatusSelection(
    val watch: WatchStatus? = null,
    val ownership: OwnershipStatus? = null
) {
    val isEmpty: Boolean get() = watch == null && ownership == null
}

