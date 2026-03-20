package fr.isen.chevrier.disney_app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "TmdbService"

/**
 * Accès TMDB (search/movie) en suspend — à appeler depuis [Dispatchers.IO] uniquement
 * (cette fonction force déjà [Dispatchers.IO]).
 *
 * Retourne l’URL complète d’affiche `https://image.tmdb.org/t/p/w500{poster_path}` ou null.
 */
object TmdbService {

    suspend fun fetchMoviePoster(title: String): String? = withContext(Dispatchers.IO) {
        val query = title.trim()
        if (query.isBlank()) return@withContext null
        try {
            val body = TmdbClient.api.searchMovies(query = query)
            val posterPath = body.results.firstOrNull()?.posterPath
            TmdbClient.buildPosterUrl(posterPath)
        } catch (_: HttpException) {
            null
        } catch (_: IOException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}
