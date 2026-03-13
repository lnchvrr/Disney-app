package fr.isen.chevrier.disney_app.data

import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.model.Universe

object MockMovieData {

    val universes: List<Universe> = listOf(
        Universe(
            id = "disney",
            name = "Disney",
            imageUrl = "https://lumiere-a.akamaihd.net/v1/images/disneyplus_disney_2000x3000_e35a5ad3.jpeg"
        ),
        Universe(
            id = "marvel",
            name = "Marvel",
            imageUrl = "https://lumiere-a.akamaihd.net/v1/images/marvel_studios_2000x3000_50f9f246.jpeg"
        ),
        Universe(
            id = "pixar",
            name = "Pixar",
            imageUrl = "https://lumiere-a.akamaihd.net/v1/images/pixar_2000x3000_aa8e7b31.jpeg"
        ),
        Universe(
            id = "starwars",
            name = "Star Wars",
            imageUrl = "https://lumiere-a.akamaihd.net/v1/images/sw_logo_2000x3000_4c4a2f14.jpeg"
        ),
        Universe(
            id = "avatar",
            name = "Avatar",
            imageUrl = "https://lumiere-a.akamaihd.net/v1/images/avatar_2000x3000_5c0fd4b4.jpeg"
        )
    )

    val categories: List<Category> = listOf(
        Category(id = "classic", universeId = "disney", name = "Classiques"),
        Category(id = "princesses", universeId = "disney", name = "Princesses"),
        Category(id = "infinitySaga", universeId = "marvel", name = "Infinity Saga"),
        Category(id = "multiverseSaga", universeId = "marvel", name = "Multiverse Saga"),
        Category(id = "originals", universeId = "pixar", name = "Originaux Pixar"),
        Category(id = "skywalker", universeId = "starwars", name = "Skywalker Saga"),
        Category(id = "pandora", universeId = "avatar", name = "Pandora")
    )

    val movies: List<Movie> = listOf(
        Movie(
            id = "lionKing",
            title = "Le Roi Lion",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/p_the_lion_king_19754_4060d8b3.jpeg",
            releaseDate = "1994-11-23",
            universeId = "disney",
            categoryId = "classic"
        ),
        Movie(
            id = "frozen",
            title = "La Reine des Neiges",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/frozen_2000x3000_bf6c7d89.jpeg",
            releaseDate = "2013-11-27",
            universeId = "disney",
            categoryId = "princesses"
        ),
        Movie(
            id = "endgame",
            title = "Avengers: Endgame",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/avengers-endgame_2000x3000_8f8a9f3d.jpeg",
            releaseDate = "2019-04-24",
            universeId = "marvel",
            categoryId = "infinitySaga"
        ),
        Movie(
            id = "noWayHome",
            title = "Spider-Man: No Way Home",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/spider-man_no_way_home_2000x3000.jpeg",
            releaseDate = "2021-12-15",
            universeId = "marvel",
            categoryId = "multiverseSaga"
        ),
        Movie(
            id = "toyStory",
            title = "Toy Story",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/p_toy_story_19753_4c3d6f57.jpeg",
            releaseDate = "1995-11-22",
            universeId = "pixar",
            categoryId = "originals"
        ),
        Movie(
            id = "empireStrikesBack",
            title = "L'Empire contre-attaque",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/star-wars-episode-v_2000x3000.jpeg",
            releaseDate = "1980-05-21",
            universeId = "starwars",
            categoryId = "skywalker"
        ),
        Movie(
            id = "avatar1",
            title = "Avatar",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/avatar_2000x3000_5c0fd4b4.jpeg",
            releaseDate = "2009-12-16",
            universeId = "avatar",
            categoryId = "pandora"
        ),
        Movie(
            id = "avatar2",
            title = "Avatar: La Voie de l'eau",
            posterUrl = "https://lumiere-a.akamaihd.net/v1/images/avatar-the-way-of-water_2000x3000.jpeg",
            releaseDate = "2022-12-14",
            universeId = "avatar",
            categoryId = "pandora"
        )
    )

    val initialStatuses: Map<String, MovieStatusSelection> = mapOf(
        "endgame" to MovieStatusSelection(watch = WatchStatus.WATCHED),
        "toyStory" to MovieStatusSelection(ownership = OwnershipStatus.OWN_DVD),
        "avatar2" to MovieStatusSelection(watch = WatchStatus.WANT_TO_WATCH)
    )
}

