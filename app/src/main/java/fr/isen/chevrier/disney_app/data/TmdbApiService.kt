package fr.isen.chevrier.disney_app.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

private const val TMDB_BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjZmY3NGY0YWZmODM1ZjQyZTU0YTVkNWExMTQ4MzZkMSIsIm5iZiI6MTc3MzE1NDQ3Ny42NzEsInN1YiI6IjY5YjAzMGFkZmQxNGMxZWFkM2MxZDM5YyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.RiqC_68IojXrqtIN-BhG-dRS9uX__X618LUUpvhEbkw"

data class TmdbSearchResponse(
    @SerializedName("results") val results: List<TmdbMovieResult> = emptyList()
)

data class TmdbMovieResult(
    @SerializedName("title") val title: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null
)

interface TmdbApiService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "fr-FR"
    ): TmdbSearchResponse
}

object TmdbClient {
    val api: TmdbApiService by lazy {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Authorization", "Bearer $TMDB_BEARER_TOKEN")
                .header("Accept", "application/json")

            chain.proceed(builder.build())
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(35, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(TMDB_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }

    fun buildPosterUrl(posterPath: String?): String? {
        if (posterPath.isNullOrBlank()) return null
        return "$TMDB_IMAGE_BASE_URL$posterPath"
    }
}