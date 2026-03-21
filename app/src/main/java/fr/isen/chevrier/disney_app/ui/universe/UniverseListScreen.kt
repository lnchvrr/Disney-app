package fr.isen.chevrier.disney_app.ui.universe

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.isen.chevrier.disney_app.R
import fr.isen.chevrier.disney_app.viewmodel.UiState
import fr.isen.chevrier.disney_app.viewmodel.UniverseListViewModel

@Composable
fun UniverseListScreen(
    viewModel: UniverseListViewModel,
    onUniverseSelected: (String) -> Unit
) {
    when (val state = viewModel.uiState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message ?: "Impossible de charger les univers",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        is UiState.Empty -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun univers disponible",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        is UiState.Success -> {
            // On conserve la logique métier (sélection d'un id d'univers),
            // mais l'affichage devient une liste verticale premium et statique.
            UniverseVerticalList(onUniverseSelected = onUniverseSelected)
        }
    }
}

private data class UniversePresentation(
    val id: String,
    val title: String,
    @DrawableRes val imageRes: Int,
    val talkBackLabel: String
)

@Composable
private fun UniverseVerticalList(
    onUniverseSelected: (String) -> Unit
) {
    val universes = listOf(
        UniversePresentation(
            id = "disney",
            title = "Disney",
            imageRes = R.drawable.disney,
            talkBackLabel = "Ouvrir l’univers Disney"
        ),
        UniversePresentation(
            id = "marvel",
            title = "Marvel",
            imageRes = R.drawable.marvel,
            talkBackLabel = "Ouvrir l’univers Marvel"
        ),
        UniversePresentation(
            id = "pixar",
            title = "Pixar",
            imageRes = R.drawable.pixar,
            talkBackLabel = "Ouvrir l’univers Pixar"
        ),
        UniversePresentation(
            id = "star_wars",
            title = "Star Wars",
            imageRes = R.drawable.starwars,
            talkBackLabel = "Ouvrir l’univers Star Wars"
        ),
        UniversePresentation(
            id = "20th_century_studios",
            title = "Avatar",
            imageRes = R.drawable.avatar,
            talkBackLabel = "Ouvrir l’univers Avatar"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 24.dp
        )
    ) {
        items(universes) { universe ->
            UniverseBannerCard(
                universe = universe,
                onClick = { onUniverseSelected(universe.id) }
            )
        }
    }
}

@Composable
private fun UniverseBannerCard(
    universe: UniversePresentation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                // Un seul libellé explicite pour TalkBack
                contentDescription = universe.talkBackLabel
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = universe.imageRes),
                contentDescription = null, // évite la redondance avec le libellé de la carte
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.12f),
                                Color.Black.copy(alpha = 0.28f),
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = universe.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Plongez dans l’univers ${universe.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}

