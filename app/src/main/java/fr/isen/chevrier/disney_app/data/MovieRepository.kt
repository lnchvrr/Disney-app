package fr.isen.chevrier.disney_app.data

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Accès Firebase Realtime Database : universes, categories, movies, user_movie_statuses.
 * Schéma : /universes, /categories, /movies, /user_movie_statuses/{userId}/{movieId}/…
 * Ancien champ [status] mappé vers watch/ownership si présent.
 */
class MovieRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val PATH_MOVIES = "movies"

    /**
     * Cache TMDB par titre normalisé ; valeur vide = aucune affiche (pas de rappel API).
     */
    private val tmdbPosterByTitleKey = ConcurrentHashMap<String, String>()

    private data class FranchiseDto(
        val id: String? = null,
        val name: String? = null,
        val universeId: String? = null,
        val universe_id: String? = null
    )

    private data class MovieDto(
        val id: String? = null,
        val title: String? = null,
        val releaseDate: String? = null,
        val release_date: String? = null,
        val universeId: String? = null,
        val universe_id: String? = null,
        val categoryId: String? = null,
        val category_id: String? = null,
        val franchiseId: String? = null,
        val franchise_id: String? = null,
        val posterUrl: String? = null,
        val posterPath: String? = null
    )

    private fun normalizeKey(key: Any?): String? {
        val s = key?.toString() ?: return null
        val cleaned = s.trim()
            .replace("\n", "")
            .replace("\r", "")
            .trim()
        return cleaned.takeIf { it.isNotBlank() }
    }

    private fun normalizeString(value: Any?): String? {
        val s = value?.toString() ?: return null
        val cleaned = s.trim()
            .replace("\n", "")
            .replace("\r", "")
            .trim()
        return cleaned.takeIf { it.isNotBlank() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun normalizeMapKeys(map: Map<*, *>): Map<String, Any?> {
        return map.entries.mapNotNull { entry ->
            val k = normalizeKey(entry.key) ?: return@mapNotNull null
            k to (entry.value as? Any)
        }.toMap()
    }

    private fun parseMoviesStrict(snapshot: DataSnapshot): List<Movie> {
        if (!snapshot.exists()) return emptyList()
        if (snapshot.childrenCount == 0L) return emptyList()

        val out = ArrayList<Movie>(snapshot.childrenCount.toInt())
        for (child in snapshot.children) {
            mapMovieFromChild(child)?.let { out.add(it) }
        }
        return out
    }

    /** Lit un champ feuille quel que soit le type Firebase (String, Long, Double, Boolean). */
    private fun valueToNormalizedString(value: Any?): String? {
        return when (value) {
            null -> null
            is String -> normalizeString(value)
            is Number -> value.toString().trim().takeIf { it.isNotBlank() }
            is Boolean -> value.toString()
            else -> normalizeString(value.toString())
        }
    }

    private fun readLeafString(child: DataSnapshot, vararg keys: String): String? {
        for (k in keys) {
            val node = child.child(k)
            if (!node.exists()) continue
            valueToNormalizedString(node.value)?.let { return it }
        }
        return null
    }

    /** Tous les champs feuille du snapshot (pour écran détail). */
    private fun snapshotToRawStringMap(child: DataSnapshot): Map<String, String> {
        val out = linkedMapOf<String, String>()
        for (snap in child.children) {
            val k = snap.key ?: continue
            valueToNormalizedString(snap.value)?.let { out[k] = it }
        }
        return out
    }

    private fun rawMapFromNormalizedMap(normalized: Map<String, Any?>): Map<String, String> {
        return normalized.mapNotNull { (k, v) ->
            valueToNormalizedString(v)?.let { k to it }
        }.toMap()
    }

    private fun mapMovieFromChild(child: DataSnapshot): Movie? {
        val id = readLeafString(child, "id") ?: normalizeString(child.key)
        if (id.isNullOrBlank()) return null

        val title = readLeafString(child, "title")
        val releaseDate = readLeafString(child, "releaseDate", "release_year", "release_date")
        val universeId = readLeafString(child, "universeId", "universe_id")
        val categoryId = readLeafString(child, "categoryId", "category_id", "franchise_id", "franchiseId")

        val posterUrlDirect = readLeafString(child, "posterUrl", "poster_url")
        val posterPath = readLeafString(child, "posterPath", "poster_path")
        val posterUrlResolved = posterUrlDirect ?: posterPath?.let { rawPath ->
            if (rawPath.isBlank()) return@let null
            val normalizedPath = if (rawPath.startsWith("/")) rawPath else "/$rawPath"
            TmdbClient.buildPosterUrl(normalizedPath)
        }

        if (!title.isNullOrBlank() && !releaseDate.isNullOrBlank() && !universeId.isNullOrBlank()) {
            return Movie(
                id = id,
                title = title,
                releaseDate = releaseDate,
                universeId = universeId,
                categoryId = categoryId,
                posterUrl = posterUrlResolved,
                posterPath = posterPath,
                rawFields = snapshotToRawStringMap(child)
            )
        }

        val rawMap = child.value as? Map<*, *> ?: return null
        return movieFromRawMap(rawMap, child.key.orEmpty())
    }

    private fun movieFromRawMap(map: Map<*, *>, fallbackKey: String): Movie? {
        val normalized = normalizeMapKeys(map)
        fun str(vararg keys: String): String? {
            for (k in keys) {
                val nk = normalizeKey(k) ?: continue
                val v = normalized[nk] ?: continue
                val s = normalizeString(v)
                if (s != null) return s
            }
            return null
        }

        val id = str("id") ?: fallbackKey
        val title = str("title") ?: return null
        val releaseDate = str("releaseDate", "release_year", "release_date") ?: return null
        val universeId = str("universeId", "universe_id") ?: return null
        val categoryId = str("categoryId", "category_id", "franchise_id", "franchiseId")
        val posterUrlRaw = str("posterUrl", "poster_url")
        val posterPath = str("posterPath", "poster_path")?.takeIf { it.isNotBlank() }
        val posterUrl = when {
            !posterUrlRaw.isNullOrBlank() -> posterUrlRaw
            !posterPath.isNullOrBlank() -> TmdbClient.buildPosterUrl(
                if (posterPath.startsWith("/")) posterPath else "/$posterPath"
            )
            else -> null
        }

        if (id.isBlank() || title.isBlank() || releaseDate.isBlank() || universeId.isBlank()) return null

        return Movie(
            id = id,
            title = title,
            releaseDate = releaseDate,
            universeId = universeId,
            categoryId = categoryId,
            posterUrl = posterUrl,
            posterPath = posterPath,
            rawFields = rawMapFromNormalizedMap(normalized)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun categoriesFromFranchisesNode(node: DataSnapshot): List<Category> {
        val fromChildren = node.children.mapNotNull { child ->
            val mapped = child.getValue(Category::class.java)
            if (mapped != null) {
                val resolvedId = mapped.id.ifBlank { child.key.orEmpty() }
                if (resolvedId.isBlank() || mapped.universeId.isBlank() || mapped.name.isBlank()) return@mapNotNull null
                return@mapNotNull mapped.copy(id = resolvedId)
            }
            val dto = child.getValue(FranchiseDto::class.java) ?: return@mapNotNull null
            val id = dto.id ?: child.key
            val universeId = dto.universeId ?: dto.universe_id
            val name = dto.name
            if (id.isNullOrBlank() || universeId.isNullOrBlank() || name.isNullOrBlank()) return@mapNotNull null
            Category(id = id, universeId = universeId, name = name)
        }
        if (fromChildren.isNotEmpty()) return fromChildren
        val list = node.value as? List<Map<String, Any?>> ?: return emptyList()
        return list.mapIndexedNotNull { index, map ->
            fun str(vararg keys: String): String? {
                for (k in keys) {
                    val v = map[k] as? String
                    if (!v.isNullOrBlank()) return v
                }
                return null
            }
            val id = str("id") ?: index.toString()
            val universeId = str("universeId", "universe_id") ?: return@mapIndexedNotNull null
            val name = str("name") ?: return@mapIndexedNotNull null
            Category(id = id, universeId = universeId, name = name)
        }
    }

    suspend fun fetchUniversesSuspend(): List<Universe> = suspendCancellableCoroutine { cont ->
        fetchUniverses { result ->
            result.onSuccess { list ->
                if (cont.isActive) cont.resume(list)
            }.onFailure { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        }
    }

    suspend fun fetchCategoriesSuspend(): List<Category> = suspendCancellableCoroutine { cont ->
        fetchCategories { result ->
            result.onSuccess { list ->
                if (cont.isActive) cont.resume(list)
            }.onFailure { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        }
    }

    suspend fun fetchMoviesSuspend(): List<Movie> = withContext(Dispatchers.IO) {
        enrichMoviesWithPosters(observeMovies().first())
    }

    /** Complète les affiches via TMDB (cache [tmdbPosterByTitleKey]). */
    suspend fun enrichMoviesWithPosters(movies: List<Movie>): List<Movie> = withContext(Dispatchers.IO) {
        val out = ArrayList<Movie>(movies.size)
        for (movie in movies) {
            out.add(enrichSingleMoviePoster(movie))
        }
        out
    }

    private suspend fun enrichSingleMoviePoster(movie: Movie): Movie {
        val existing = movie.posterUrl?.takeIf { it.isNotBlank() }
        if (existing != null) return movie

        val fromPath = movie.posterPath?.takeIf { it.isNotBlank() }?.let { path ->
            TmdbClient.buildPosterUrl(path)
        }
        if (fromPath != null) return movie.copy(posterUrl = fromPath)

        val key = movie.title.trim().lowercase(Locale.ROOT)
        if (key.isEmpty()) return movie

        if (tmdbPosterByTitleKey.containsKey(key)) {
            val cached = tmdbPosterByTitleKey[key].orEmpty()
            return movie.copy(posterUrl = cached.takeIf { it.isNotBlank() } ?: movie.posterUrl)
        }

        val url = TmdbService.fetchMoviePoster(movie.title).orEmpty()
        tmdbPosterByTitleKey[key] = url
        return movie.copy(posterUrl = url.takeIf { it.isNotBlank() } ?: movie.posterUrl)
    }

    fun observeMovies(): Flow<List<Movie>> = callbackFlow {
        val ref = db.reference.child(PATH_MOVIES)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    trySend(parseMoviesStrict(snapshot))
                } catch (e: Exception) {
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    suspend fun fetchUserMovieStatusesSuspend(userId: String): Map<String, MovieStatusSelection> =
        suspendCancellableCoroutine { cont ->
            fetchUserMovieStatuses(userId) { result ->
                result.onSuccess { map ->
                    if (cont.isActive) cont.resume(map)
                }.onFailure { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
            }
        }

    suspend fun fetchUsersWantToSellMovieSuspend(movieId: String): List<MovieSellOffer> =
        suspendCancellableCoroutine { cont ->
            fetchUsersWantToSellMovie(movieId) { result ->
                result.onSuccess { list ->
                    if (cont.isActive) cont.resume(list)
                }.onFailure { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
            }
        }

    suspend fun setUserMovieStatusSuspend(
        userId: String,
        movieId: String,
        status: MovieStatusSelection?
    ): Unit = suspendCancellableCoroutine { cont ->
        setUserMovieStatus(userId, movieId, status) { result ->
            result.onSuccess {
                if (cont.isActive) cont.resume(Unit)
            }.onFailure { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        }
    }

    fun fetchUniverses(onResult: (Result<List<Universe>>) -> Unit) {
        db.reference.child("universes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val universes = snapshot.children.mapNotNull { child ->
                        val mapped = child.getValue(Universe::class.java)
                        if (mapped == null) {
                            return@mapNotNull null
                        }
                        val resolvedId = when {
                            mapped.id.isNotBlank() -> mapped.id
                            child.child("id").exists() -> child.child("id").getValue(String::class.java).orEmpty()
                            child.child("universeId").exists() -> child.child("universeId").getValue(String::class.java).orEmpty()
                            child.child("universe_id").exists() -> child.child("universe_id").getValue(String::class.java).orEmpty()
                            else -> child.key.orEmpty()
                        }

                        val resolvedName = when {
                            mapped.name.isNotBlank() -> mapped.name
                            child.child("name").exists() -> child.child("name").getValue(String::class.java).orEmpty()
                            child.child("universeName").exists() -> child.child("universeName").getValue(String::class.java).orEmpty()
                            child.child("universe_name").exists() -> child.child("universe_name").getValue(String::class.java).orEmpty()
                            else -> resolvedId
                        }
                        when {
                            resolvedId.isBlank() -> null
                            resolvedName.isBlank() -> null
                            else -> mapped.copy(id = resolvedId)
                        }
                    }

                    onResult(Result.success(universes))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchCategories(onResult: (Result<List<Category>>) -> Unit) {
        db.reference
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoriesNode = snapshot.child("categories")
                    val franchisesNode = snapshot.child("franchises")

                    val categories = when {
                        categoriesNode.exists() && (categoriesNode.childrenCount > 0L || categoriesNode.value is List<*>) -> {
                            val fromChildren = categoriesNode.children.mapNotNull { child ->
                                val mapped = child.getValue(Category::class.java) ?: return@mapNotNull null
                                val resolvedId = mapped.id.ifBlank { child.key.orEmpty() }
                                if (resolvedId.isBlank() || mapped.universeId.isBlank() || mapped.name.isBlank()) return@mapNotNull null
                                mapped.copy(id = resolvedId)
                            }
                            if (fromChildren.isNotEmpty()) fromChildren
                            else {
                                @Suppress("UNCHECKED_CAST")
                                val list = categoriesNode.value as? List<Map<String, Any?>> ?: emptyList()
                                val listParsed = list.mapIndexedNotNull { index, map ->
                                    fun str(vararg keys: String): String? {
                                        for (k in keys) {
                                            val v = map[k] as? String
                                            if (!v.isNullOrBlank()) return v
                                        }
                                        return null
                                    }
                                    val id = str("id") ?: index.toString()
                                    val universeId = str("universeId", "universe_id") ?: return@mapIndexedNotNull null
                                    val name = str("name") ?: return@mapIndexedNotNull null
                                    Category(id = id, universeId = universeId, name = name)
                                }
                                listParsed.ifEmpty { categoriesFromFranchisesNode(franchisesNode) }
                            }
                        }
                        else -> categoriesFromFranchisesNode(franchisesNode)
                    }
                    onResult(Result.success(categories))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(Result.failure(error.toException()))
                }
            })
    }

    fun fetchMovies(onResult: (Result<List<Movie>>) -> Unit) {
<<<<<<< HEAD
        val tag = "MovieRepository"
        db.reference.child("movies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val movies = snapshot.children.mapNotNull { child ->
                        val idRaw = child.child("id").getValue(String::class.java) ?: child.key
                        val titleRaw = child.child("title").getValue(String::class.java)

                        val id = idRaw?.takeIf { it.isNotBlank() }
                        val title = titleRaw?.takeIf { it.isNotBlank() }

                        if (id == null || title == null) {
                            Log.w(tag, "fetchMovies: skipping movie key=${child.key} id=$idRaw title=$titleRaw")
                            return@mapNotNull null
                        }

                        val releaseDate =
                            child.child("releaseDate").getValue(String::class.java)
                                ?: child.child("yearDate").getValue(String::class.java)
                                ?: child.child("release_year").getValue(String::class.java)
                                ?: child.child("year").getValue(String::class.java)

                        val universeId = child.child("universeId").getValue(String::class.java)
                        val categoryId = child.child("categoryId").getValue(String::class.java)

                        val posterRaw =
                            child.child("posterUrl").getValue(String::class.java)
                                ?: child.child("poster_path").getValue(String::class.java)
                                ?: child.child("poster").getValue(String::class.java)
                                ?: child.child("posterPath").getValue(String::class.java)

                        val posterUrl = posterRaw?.let { raw ->
                            val cleaned = raw.trim()
                            if (cleaned.startsWith("http")) {
                                cleaned
                            } else {
                                "https://image.tmdb.org/t/p/w500/${cleaned.trimStart('/')}"
                            }
                        }

                        Log.d("MOVIE_DEBUG", "Movie title: $title")
                        Log.d("MOVIE_DEBUG", "Raw poster value: $posterRaw")
                        Log.d("MOVIE_DEBUG", "Final poster URL: $posterUrl")
                        Log.d("MOVIE_DEBUG", "ReleaseDate: $releaseDate")

                        Movie(
                            id = id,
                            title = title,
                            posterUrl = posterUrl,
                            releaseDate = releaseDate,
                            universeId = universeId,
                            categoryId = categoryId
                        )
=======
        db.reference.child(PATH_MOVIES)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        onResult(Result.success(parseMoviesStrict(snapshot)))
                    } catch (e: Exception) {
                        onResult(Result.failure(e))
>>>>>>> 21dc465 (feat: finalize app (fully functional, only minor design adjustments remaining))
                    }
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
                        val watch = watchStr?.let { runCatching { WatchStatus.valueOf(it) }.getOrNull() }

                        val ownershipStr = child.child("ownership").getValue(String::class.java)
                        val ownership = when {
                            ownershipStr == "OWN_DVD" || ownershipStr == "OWN_BLURAY" ->
                                runCatching { OwnershipStatus.valueOf(ownershipStr!!) }.getOrNull()
                            ownershipStr == "WANT_TO_SELL" -> null
                            else -> ownershipStr?.let { runCatching { OwnershipStatus.valueOf(it) }.getOrNull() }
                        }

                        val wantToSell = child.child("wantToSell").getValue(Boolean::class.javaObjectType) ?: false

                        val legacy = child.child("status").getValue(String::class.java)
                        val legacyWatch = legacy?.let { runCatching { WatchStatus.valueOf(it) }.getOrNull() }
                        val legacyOwnership = legacy?.let {
                            runCatching { OwnershipStatus.valueOf(it) }.getOrNull()
                                ?: if (it == "WANT_TO_SELL") null else null
                        }

                        val selection = MovieStatusSelection(
                            watch = watch ?: legacyWatch,
                            ownership = ownership ?: legacyOwnership,
                            wantToSell = wantToSell && (ownership != null || legacyOwnership != null)
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

    fun fetchUsersWantToSellMovie(
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
                        val wantToSell = movieNode.child("wantToSell").getValue(Boolean::class.javaObjectType) ?: false
                        val ownership = movieNode.child("ownership").getValue(String::class.java)
                        val ownsMovie = ownership == "OWN_DVD" || ownership == "OWN_BLURAY"
                        if (!wantToSell || !ownsMovie) return@forEach

                        val displayName = movieNode.child("displayName").getValue(String::class.java)
                        val email = movieNode.child("email").getValue(String::class.java)
                        offers += MovieSellOffer(
                            movieId = movieId,
                            userId = userId,
                            userDisplayName = displayName,
                            userEmail = email
                        )
                    }
                    onResult(Result.success(offers))
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

        val ownershipValue = selection.ownership?.takeIf {
            it == OwnershipStatus.OWN_DVD || it == OwnershipStatus.OWN_BLURAY
        }?.name
        val wantToSellValue = selection.ownsMovie && selection.wantToSell

        val updates = mutableMapOf<String, Any?>(
            "watch" to selection.watch?.name,
            "ownership" to ownershipValue,
            "wantToSell" to wantToSellValue
        )
        updates["status"] = null

        ref.updateChildren(updates)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

}

