package fr.isen.chevrier.disney_app.ui.universe

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import fr.isen.chevrier.disney_app.model.Universe
import fr.isen.chevrier.disney_app.ui.common.SearchFilterRow
import fr.isen.chevrier.disney_app.ui.common.rememberSafePainterResource
import fr.isen.chevrier.disney_app.viewmodel.UiState
import fr.isen.chevrier.disney_app.viewmodel.UniverseListViewModel

@Composable
fun UniverseListScreen(
    viewModel: UniverseListViewModel,
    onUniverseSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        is UiState.Success -> {
            var universeSearch by remember { mutableStateOf("") }
            val filteredUniverses = remember(state.data, universeSearch) {
                if (universeSearch.isBlank()) state.data
                else state.data.filter {
                    it.name.contains(universeSearch, ignoreCase = true) ||
                        it.id.contains(universeSearch, ignoreCase = true)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                SearchFilterRow(
                    searchQuery = universeSearch,
                    onSearchChange = { universeSearch = it },
                    placeholder = "Rechercher un univers…",
                    showFilter = false,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 0.dp,
                        vertical = 8.dp
                    )
                ) {
                    if (filteredUniverses.isEmpty()) {
                        item {
                            Text(
                                text = if (universeSearch.isNotBlank()) {
                                    "Aucun univers ne correspond à « $universeSearch »."
                                } else {
                                    "Aucun univers à afficher."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 24.dp)
                            )
                        }
                    } else {
                        items(filteredUniverses) { universe ->
                            UniverseBannerCard(
                                universe = universe,
                                onClick = { onUniverseSelected(universe.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UniverseBannerCard(
    universe: Universe,
    onClick: () -> Unit
) {
    val displayName = universe.name.trim().ifBlank {
        universe.id.trim().ifBlank { "Univers" }
    }
    val talkBackLabel = "Ouvrir l'univers $displayName"
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.98f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            )
            .clearAndSetSemantics {
                contentDescription = talkBackLabel
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageRes = UniverseImageMapper.universeImageRes(universe.id, universe.name)
            Image(
                painter = rememberSafePainterResource(resId = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.82f)
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
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Découvrir les films",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
