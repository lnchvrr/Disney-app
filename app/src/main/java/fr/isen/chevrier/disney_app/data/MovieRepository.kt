package fr.isen.chevrier.disney_app.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.Universe
import fr.isen.chevrier.disney_app.model.WatchStatus

/**
 * Repository centralisant l'accès à Firebase Realtime Database pour :
 * - universes
 * - categories
 * - movies
 * - user_movie_statuses
 *
 * Structure de base de données :
 * - /universes/{universeId}
 * - /categories/{categoryId}
 * - /movies/{movieId}
 * - /user_movie_statuses/{userId}/{movieId}/watch = "WATCHED" | "WANT_TO_WATCH"
 * - /user_movie_statuses/{userId}/{movieId}/ownership = "OWN_DVD" | "WANT_TO_SELL"
 *
 * Rétro-compat : si /status existe (ancien format), on le mappe vers le bon groupe.
 */
class MovieRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    fun fetchUniverses(onResult: (Result<List<Universe>>) -> Unit) {
        val tag = "MovieRepository"
        db.reference.child("universes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(tag, "fetchUniverses: path=/universes exists=${snapshot.exists()} children=${snapshot.childrenCount}")
                    val universes = snapshot.children.mapNotNull { child ->
                        val rawName = child.child("name").getValue(String::class.java)
                            ?: child.getValue(String::class.java)
                        val id = child.child("id").getValue(String::class.java) ?: child.key
                        if (id == null || rawName == null) {
                            Log.w(tag, "fetchUniverses: skipping child key=${child.key} id=$id name=$rawName")
                            return@mapNotNull null
                        }
                        val imageUrl = child.child("imageUrl").getValue(String::class.java)
                        Universe(id = id, name = rawName, imageUrl = imageUrl)
                    }
                    Log.d(tag, "fetchUniverses: mapped universes count=${universes.size}")
                    onResult(Result.success(universes))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(tag, "fetchUniverses: cancelled ${error.toException()}")
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchCategories(onResult: (Result<List<Category>>) -> Unit) {
        db.reference.child("categories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categories = snapshot.children.mapNotNull { child ->
                        val id = child.child("id").getValue(String::class.java) ?: child.key
                        val universeId = child.child("universeId").getValue(String::class.java)
                        val name = child.child("name").getValue(String::class.java)
                        if (id == null || universeId == null || name == null) return@mapNotNull null
                        Category(id = id, universeId = universeId, name = name)
                    }
                    onResult(Result.success(categories))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchMovies(onResult: (Result<List<Movie>>) -> Unit) {
        db.reference.child("movies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val movies = snapshot.children.mapNotNull { child ->
                        val id = child.child("id").getValue(String::class.java) ?: child.key
                        val title = child.child("title").getValue(String::class.java)
                        val releaseDate = child.child("releaseDate").getValue(String::class.java)
                        val universeId = child.child("universeId").getValue(String::class.java)
                        if (id == null || title == null || releaseDate == null || universeId == null) {
                            return@mapNotNull null
                        }
                        val categoryId = child.child("categoryId").getValue(String::class.java)
                        val posterUrl = child.child("posterUrl").getValue(String::class.java)
                        Movie(
                            id = id,
                            title = title,
                            releaseDate = releaseDate,
                            universeId = universeId,
                            categoryId = categoryId,
                            posterUrl = posterUrl
                        )
                    }
                    onResult(Result.success(movies))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchUserMovieStatuses(
        userId: String,
        onResult: (Result<Map<String, MovieStatusSelection>>) -> Unit
    ) {
        db.reference.child("user_movie_statuses").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val statuses = mutableMapOf<String, MovieStatusSelection>()
                    snapshot.children.forEach { child ->
                        val movieId = child.key ?: return@forEach

                        val watchStr = child.child("watch").getValue(String::class.java)
                        val ownershipStr = child.child("ownership").getValue(String::class.java)

                        val watch = watchStr?.let { runCatching { WatchStatus.valueOf(it) }.getOrNull() }
                        val ownership = ownershipStr?.let { runCatching { OwnershipStatus.valueOf(it) }.getOrNull() }

                        // Ancien format : /status
                        val legacy = child.child("status").getValue(String::class.java)
                        val legacyWatch = legacy?.let { runCatching { WatchStatus.valueOf(it) }.getOrNull() }
                        val legacyOwnership = legacy?.let { runCatching { OwnershipStatus.valueOf(it) }.getOrNull() }

                        val selection = MovieStatusSelection(
                            watch = watch ?: legacyWatch,
                            ownership = ownership ?: legacyOwnership
                        )

                        if (!selection.isEmpty) {
                            statuses[movieId] = selection
                        }
                    }
                    onResult(Result.success(statuses))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun setUserMovieStatus(
        userId: String,
        movieId: String,
        status: MovieStatusSelection?,
        onComplete: (Result<Unit>) -> Unit
    ) {
        val ref = db.reference.child("user_movie_statuses").child(userId).child(movieId)
        val selection = status

        if (selection == null || selection.isEmpty) {
            ref.removeValue()
                .addOnSuccessListener { onComplete(Result.success(Unit)) }
                .addOnFailureListener { onComplete(Result.failure(it)) }
            return
        }

        val updates = mutableMapOf<String, Any?>(
            "watch" to selection.watch?.name,
            "ownership" to selection.ownership?.name
        )

        // Nettoyage éventuel de l'ancien champ
        updates["status"] = null

        ref.updateChildren(updates)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }
}

