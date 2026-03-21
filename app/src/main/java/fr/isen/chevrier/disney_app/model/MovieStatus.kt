package fr.isen.chevrier.disney_app.model

/**
 * Deux groupes de statuts indépendants :
 * - Groupe 1 : progression (Vu / À voir)
 * - Groupe 2 : possession (DVD / Blu-ray)
 * - Groupe 3 : revente (À vendre)
 *
 * On peut être à la fois :
 * - propriétaire d’un DVD ou Blu-ray
 * - et vouloir s’en débarrasser
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
    val normalized: MovieStatusSelection
        get() = if (ownership == null) copy(wantToSell = false) else this

    val isEmpty: Boolean
        get() = watch == null && ownership == null && !wantToSell
}