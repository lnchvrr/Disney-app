package fr.isen.chevrier.disney_app.model

import com.google.firebase.database.PropertyName

/**
 * Aligné sur Firebase Realtime Database : le champ JSON est `universe_id` (snake_case).
 */
data class Category(
    val id: String = "",
    @get:PropertyName("universe_id") val universeId: String = "",
    val name: String = ""
)

