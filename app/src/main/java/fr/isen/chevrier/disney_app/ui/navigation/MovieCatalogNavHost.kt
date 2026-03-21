package fr.isen.chevrier.disney_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.ui.movies.MovieDetailScreen
import fr.isen.chevrier.disney_app.ui.movies.MovieGridScreen
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import fr.isen.chevrier.disney_app.viewmodel.ProfileViewModel

@Composable
fun MovieCatalogNavHost(
    viewModel: MovieListViewModel,
    profileViewModel: ProfileViewModel,
    currentUser: FirebaseUser?,
    onExitToUnivers: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val catalogNavController = rememberNavController()

    NavHost(
        navController = catalogNavController,
        startDestination = "grid",
        modifier = modifier
    ) {
        composable("grid") {
            MovieGridScreen(
                viewModel = viewModel,
                profileViewModel = profileViewModel,
                currentUser = currentUser,
                onMovieClick = { movieId ->
                    catalogNavController.navigate("detail/$movieId")
                }
            )
        }

        composable(
            route = "detail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            val movie = viewModel.getMovieById(movieId)

            MovieDetailRoute(
                movieId = movieId,
                movie = movie,
                viewModel = viewModel,
                profileViewModel = profileViewModel,
                currentUser = currentUser,
                onBack = { catalogNavController.popBackStack() },
                onExitToUnivers = onExitToUnivers
            )
        }
    }
}

@Composable
private fun MovieDetailRoute(
    movieId: String,
    movie: Movie?,
    viewModel: MovieListViewModel,
    profileViewModel: ProfileViewModel,
    currentUser: FirebaseUser?,
    onBack: () -> Unit,
    onExitToUnivers: () -> Unit
) {
    val userStatuses = viewModel.userStatuses
    val universesById = viewModel.universesById
    val categoriesById = viewModel.categoriesById

    if (movie == null) {
        // fallback UI minimal
        MovieDetailScreen(
            movie = Movie(id = movieId, title = "Chargement...", releaseDate = "", universeId = "", categoryId = null),
            universeName = "",
            categoryName = null,
            currentStatus = null,
            statusEnabled = false,
            onBack = onBack,
            onStatusSelected = {}
        )
        return
    }

    val universeName = universesById[movie.universeId]?.name.orEmpty()
    val categoryName = movie.categoryId?.let { categoriesById[it]?.name }
    val statusEnabled = currentUser != null
    val currentStatus = userStatuses[movieId]

    MovieDetailScreen(
        movie = movie,
        universeName = universeName,
        categoryName = categoryName,
        currentStatus = currentStatus,
        statusEnabled = statusEnabled,
        onBack = onBack,
        onStatusSelected = { newStatus ->
            // Optimistic update for profile stats
            profileViewModel.applyStatusUpdateLocal(movieId = movieId, newStatus = newStatus, movieForCache = movie)

            // Persist for the catalog UI
            viewModel.updateStatus(
                movieId = movieId,
                status = newStatus,
                userId = currentUser?.uid,
                userName = currentUser?.displayName
            )
        }
    )
}

