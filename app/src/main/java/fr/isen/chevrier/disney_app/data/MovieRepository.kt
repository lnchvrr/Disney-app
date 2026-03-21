package fr.isen.chevrier.disney_app.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieSellOffer
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.Universe
import fr.isen.chevrier.disney_app.model.WatchStatus
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MovieRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    fun fetchUniverses(onResult: (Result<List<Universe>>) -> Unit) {
        val tag = "MovieRepository"

        db.reference.child("universes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(tag, "fetchUniverses exists=${snapshot.exists()} children=${snapshot.childrenCount}")

                    val universes = snapshot.children.mapNotNull { child ->
                        val id = child.child("id").getValue(String::class.java) ?: child.key
                        val name = child.child("name").getValue(String::class.java)
                            ?: child.getValue(String::class.java)

                        if (id.isNullOrBlank() || name.isNullOrBlank()) {
                            return@mapNotNull null
                        }

                        val imageUrl = child.child("imageUrl").getValue(String::class.java)

                        Universe(
                            id = id,
                            name = name,
                            imageUrl = imageUrl
                        )
                    }

                    Log.d(tag, "fetchUniverses mapped=${universes.size}")
                    onResult(Result.success(universes))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchCategories(onResult: (Result<List<Category>>) -> Unit) {
        val tag = "MovieRepository"

        db.reference.child("franchises")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(tag, "fetchCategories/franchises exists=${snapshot.exists()} children=${snapshot.childrenCount}")

                    val categories = snapshot.children.mapNotNull { child ->
                        val id = child.child("id").getValue(String::class.java) ?: child.key
                        val universeId =
                            child.child("universeId").getValue(String::class.java)
                                ?: child.child("universe_id").getValue(String::class.java)
                        val name = child.child("name").getValue(String::class.java)

                        if (id.isNullOrBlank() || universeId.isNullOrBlank() || name.isNullOrBlank()) {
                            return@mapNotNull null
                        }

                        Category(
                            id = id,
                            universeId = universeId,
                            name = name
                        )
                    }

                    Log.d(tag, "fetchCategories mapped=${categories.size}")
                    onResult(Result.success(categories))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchMovies(onResult: (Result<List<Movie>>) -> Unit) {
        val tag = "MovieRepository"

        db.reference.child("movies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(tag, "fetchMovies exists=${snapshot.exists()} children=${snapshot.childrenCount}")

                    val movies = snapshot.children.mapNotNull { child ->
                        val id = child.child("id").getValue(String::class.java) ?: child.key
                        val title = child.child("title").getValue(String::class.java)

                        val releaseDate =
                            child.child("releaseDate").getValue(String::class.java)
                                ?: child.child("release_year").getValue(String::class.java)
                                ?: child.child("release_year\n    ").getValue(String::class.java)

                        val universeId =
                            child.child("universeId").getValue(String::class.java)
                                ?: child.child("universe_id").getValue(String::class.java)

                        val categoryId =
                            child.child("categoryId").getValue(String::class.java)
                                ?: child.child("franchise_id").getValue(String::class.java)

                        val posterUrl = child.child("posterUrl").getValue(String::class.java)

                        if (id.isNullOrBlank() || title.isNullOrBlank() || releaseDate.isNullOrBlank() || universeId.isNullOrBlank()) {
                            Log.w(
                                tag,
                                "Skipping movie key=${child.key} id=$id title=$title releaseDate=$releaseDate universeId=$universeId categoryId=$categoryId"
                            )
                            return@mapNotNull null
                        }

                        Movie(
                            id = id,
                            title = title,
                            releaseDate = releaseDate,
                            universeId = universeId,
                            categoryId = categoryId,
                            posterUrl = posterUrl
                        )
                    }

                    Log.d(tag, "fetchMovies mapped=${movies.size}")
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
                        val selection = parseMovieStatus(child)

                        if (selection != null && !selection.isEmpty) {
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
        userName: String? = null,
        onComplete: (Result<Unit>) -> Unit
    ) {
        val ref = db.reference.child("user_movie_statuses").child(userId).child(movieId)
        val normalized = status?.normalized

        if (normalized == null || normalized.isEmpty) {
            ref.removeValue()
                .addOnSuccessListener { onComplete(Result.success(Unit)) }
                .addOnFailureListener { onComplete(Result.failure(it)) }
            return
        }

        val updates = mutableMapOf<String, Any?>(
            "watch" to normalized.watch?.name,
            "ownership" to normalized.ownership?.name,
            "wantToSell" to normalized.wantToSell,
            "status" to null
        )

        if (!userName.isNullOrBlank()) {
            updates["userName"] = userName
        }

        ref.updateChildren(updates)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    fun fetchMovieSellOffers(
        movieId: String,
        onResult: (Result<List<MovieSellOffer>>) -> Unit
    ) {
        db.reference.child("user_movie_statuses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offers = mutableListOf<MovieSellOffer>()

                    snapshot.children.forEach { userNode ->
                        val userId = userNode.key ?: return@forEach
                        val movieNode = userNode.child(movieId)
                        if (!movieNode.exists()) return@forEach

                        val selection = parseMovieStatus(movieNode) ?: return@forEach
                        val ownership = selection.ownership ?: return@forEach

                        if (!selection.wantToSell) return@forEach

                        val savedUserName = movieNode.child("userName").getValue(String::class.java)
                        val fallbackName =
                            userNode.child("userName").getValue(String::class.java)
                                ?: userNode.child("displayName").getValue(String::class.java)
                                ?: userId

                        offers += MovieSellOffer(
                            userId = userId,
                            userName = savedUserName?.takeIf { it.isNotBlank() } ?: fallbackName,
                            ownership = ownership
                        )
                    }

                    onResult(
                        Result.success(
                            offers.sortedBy { it.userName.lowercase() }
                        )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    private fun parseMovieStatus(node: DataSnapshot): MovieStatusSelection? {
        val watchStr = node.child("watch").getValue(String::class.java)
        val ownershipStr = node.child("ownership").getValue(String::class.java)
        val wantToSell = node.child("wantToSell").getValue(Boolean::class.java) ?: false

        val watch = watchStr?.let {
            runCatching { WatchStatus.valueOf(it) }.getOrNull()
        }

        val ownership = ownershipStr?.let {
            runCatching { OwnershipStatus.valueOf(it) }.getOrNull()
        }

        val legacy = node.child("status").getValue(String::class.java)

        val legacyWatch = legacy?.let {
            runCatching { WatchStatus.valueOf(it) }.getOrNull()
        }

        val legacyOwnership = legacy?.let {
            when (it) {
                "OWN_DVD" -> OwnershipStatus.OWN_DVD
                "OWN_BLURAY" -> OwnershipStatus.OWN_BLURAY
                else -> null
            }
        }

        val legacyWantToSell = legacy == "WANT_TO_SELL"

        val finalOwnership = ownership ?: legacyOwnership
        val selection = MovieStatusSelection(
            watch = watch ?: legacyWatch,
            ownership = finalOwnership,
            wantToSell = (wantToSell || legacyWantToSell) && finalOwnership != null
        ).normalized

        return selection.takeUnless { it.isEmpty }
    }

    suspend fun fetchUniversesSuspend(): List<Universe> =
        suspendCancellableCoroutine { cont ->
            fetchUniverses { result ->
                if (cont.isActive) cont.resume(result.getOrElse { emptyList() })
            }
        }

    suspend fun fetchCategoriesSuspend(): List<Category> =
        suspendCancellableCoroutine { cont ->
            fetchCategories { result ->
                if (cont.isActive) cont.resume(result.getOrElse { emptyList() })
            }
        }

    suspend fun fetchMoviesSuspend(): List<Movie> =
        suspendCancellableCoroutine { cont ->
            fetchMovies { result ->
                if (cont.isActive) cont.resume(result.getOrElse { emptyList() })
            }
        }

    suspend fun fetchUserMovieStatusesSuspend(userId: String): Map<String, MovieStatusSelection> =
        suspendCancellableCoroutine { cont ->
            fetchUserMovieStatuses(userId) { result ->
                if (cont.isActive) cont.resume(result.getOrElse { emptyMap() })
            }
        }

    suspend fun fetchMovieSellOffersSuspend(movieId: String): List<MovieSellOffer> =
        suspendCancellableCoroutine { cont ->
            fetchMovieSellOffers(movieId) { result ->
                if (cont.isActive) cont.resume(result.getOrElse { emptyList() })
            }
        }
}