package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieSellOffer
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard

@Composable
fun MovieDetailContent(
    movie: Movie,
    universeName: String,
    categoryName: String?,
    currentStatus: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    sellOffers: List<MovieSellOffer> = emptyList(),
    isLoadingSellOffers: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhiteStrong),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhiteStrong)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = movie.posterUrl ?: fr.isen.chevrier.disney_app.R.drawable.universe_default,
                    contentDescription = "Affiche du film ${movie.title}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TextOnCard
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = universeName,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnCard.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnCard.copy(alpha = 0.7f)
                )
                Text(
                    text = movie.releaseDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnCard.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            categoryName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnCard.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                StatusRow(
                    currentStatus = currentStatus,
                    onStatusSelected = onStatusSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SellOffersSection(
                sellOffers = sellOffers,
                isLoading = isLoadingSellOffers
            )
        }
    }
}

@Composable
private fun SellOffersSection(
    sellOffers: List<MovieSellOffer>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Utilisateurs qui le possèdent et veulent s’en débarrasser",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextOnCard
        )

        when {
            isLoading -> {
                Text(
                    text = "Chargement...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnCard.copy(alpha = 0.8f)
                )
            }

            sellOffers.isEmpty() -> {
                Text(
                    text = "Aucun utilisateur n’a indiqué vouloir s’en séparer pour le moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnCard.copy(alpha = 0.8f)
                )
            }

            else -> {
                sellOffers.forEach { offer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = offer.userName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = TextOnCard
                            )

                            Text(
                                text = when (offer.ownership) {
                                    OwnershipStatus.OWN_DVD -> "DVD"
                                    OwnershipStatus.OWN_BLURAY -> "Blu-ray"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnCard.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDetailScreen(
    movie: Movie,
    universeName: String,
    categoryName: String?,
    currentStatus: MovieStatusSelection?,
    statusEnabled: Boolean,
    onBack: () -> Unit,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    sellOffers: List<MovieSellOffer> = emptyList(),
    isLoadingSellOffers: Boolean = false
) {
    MovieDetailContent(
        movie = movie,
        universeName = universeName,
        categoryName = categoryName,
        currentStatus = currentStatus,
        onStatusSelected = onStatusSelected,
        sellOffers = sellOffers,
        isLoadingSellOffers = isLoadingSellOffers
    )
}