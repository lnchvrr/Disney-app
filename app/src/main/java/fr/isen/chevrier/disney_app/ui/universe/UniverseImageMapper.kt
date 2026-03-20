package fr.isen.chevrier.disney_app.ui.universe

import fr.isen.chevrier.disney_app.R

/**
 * Associe chaque univers Firebase à un drawable.
 * Le fallback générique [R.drawable.universe_default] est réservé à l’univers **international** (et variantes).
 * Pour les autres ids, on utilise le mapping id → puis le nom affiché.
 */
object UniverseImageMapper {

    /**
     * @param universeName Nom affiché Firebase (secours si l’id n’est pas dans la table).
     */
    fun universeImageRes(universeId: String, universeName: String = ""): Int {
        val id = universeId.trim().lowercase()
        val nameKey = normalize(universeName)

        // Fallback explicite : international uniquement
        if (id == "international" ||
            id.contains("international") ||
            nameKey.contains("international")
        ) {
            return R.drawable.universe_default
        }

        val byId = when (id) {
            "disney" -> R.drawable.disney
            "pixar" -> R.drawable.pixar
            "marvel" -> R.drawable.marvel
            "starwars", "star_wars" -> R.drawable.starwars
            "20th_century_studios", "20thcentury", "century" -> R.drawable.century
            "dimension" -> R.drawable.dimension
            "avatar" -> R.drawable.avatar
            "touchstone", "touch_stone" -> R.drawable.touchstone
            else -> null
        }
        if (byId != null) return byId

        // Secours par nom (sans utiliser universe_default sauf déjà géré)
        return getUniverseImageResId(universeName)
    }

    /** Alias pratique pour l’UI ([painterResource]). */
    fun getUniverseImage(name: String): Int = getUniverseImageResId(name)

    fun getUniverseImageResId(name: String): Int {
        val key = normalize(name)
        if (key.contains("international")) return R.drawable.universe_default
        return when {
            key.contains("touchstone") -> R.drawable.touchstone
            key.contains("marvel") -> R.drawable.marvel
            key.contains("pixar") -> R.drawable.pixar
            key.contains("star") && key.contains("war") -> R.drawable.starwars
            key.contains("starwars") -> R.drawable.starwars
            key.contains("20th") || key.contains("century") -> R.drawable.century
            key.contains("dimension") -> R.drawable.dimension
            key.contains("avatar") -> R.drawable.avatar
            key.contains("disney") -> R.drawable.disney
            else -> R.drawable.disney
        }
    }

    private fun normalize(raw: String): String {
        return raw.trim()
            .lowercase()
            .replace("é", "e")
            .replace("è", "e")
            .replace("ê", "e")
            .replace("à", "a")
            .replace("ù", "u")
    }
}
