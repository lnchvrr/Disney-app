package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
<<<<<<< HEAD
import androidx.compose.ui.res.painterResource
import fr.isen.chevrier.disney_app.R
=======
import coil.request.ImageRequest
import fr.isen.chevrier.disney_app.R
import fr.isen.chevrier.disney_app.model.OwnershipStatus
>>>>>>> 21dc465 (feat: finalize app (fully functional, only minor design adjustments remaining))
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieSellOffer
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AssistChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.text.style.TextAlign
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.clip

@Composable
fun MovieDetailContent(
    movie: Movie,
    universeName: String,
    categoryName: String?,
    sellers: List<MovieSellOffer>,
    currentStatus: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    statusEnabled: Boolean = true
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
<<<<<<< HEAD
            if (!movie.posterUrl.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = "Affiche du film ${movie.title}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.movie_poster_placeholder),
                        error = painterResource(R.drawable.movie_poster_placeholder)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
=======
            val context = LocalContext.current
            val posterUrlToShow = movie.posterUrl?.takeIf { it.isNotBlank() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(posterUrlToShow ?: R.drawable.universe_default)
                        .placeholder(R.drawable.universe_default)
                        .error(R.drawable.universe_default)
                        .build(),
                    contentDescription = "Affiche du film ${movie.title}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
>>>>>>> 21dc465 (feat: finalize app (fully functional, only minor design adjustments remaining))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TextOnCard
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (universeName.isNotBlank()) universeName else "Univers inconnu",
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
                    text = movie.releaseDate ?: "Date inconnue",
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
                    onStatusSelected = onStatusSelected,
                    enabled = statusEnabled
                )
            }

            if (sellers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Utilisateurs qui veulent s'en séparer",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextOnCard
                )
                sellers.forEach { offer ->
                    val label = offer.userDisplayName?.takeIf { it.isNotBlank() }
                        ?: offer.userEmail?.takeIf { it.isNotBlank() }
                        ?: offer.userId
                    Text(
                        text = "• $label",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnCard.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    universeName: String,
    categoryName: String?,
    currentStatus: MovieStatusSelection?,
    statusEnabled: Boolean,
    onBack: () -> Unit,
    onStatusSelected: (MovieStatusSelection?) -> Unit
) {
    val selection = currentStatus ?: MovieStatusSelection()
    val posterUrlToShow = movie.posterUrl?.takeIf { it.isNotBlank() }
    val year = extractYear(movie.releaseDate)
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = movie.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour au catalogue"
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section informations
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhiteStrong),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Informations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextOnCard
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(posterUrlToShow ?: R.drawable.universe_default)
                                .placeholder(R.drawable.universe_default)
                                .error(R.drawable.universe_default)
                                .build(),
                            contentDescription = "Affiche du film ${movie.title}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (!posterUrlToShow.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    if (!year.isNullOrBlank()) {
                        DetailInfoRow(label = "Année", value = year)
                    }
                    if (universeName.isNotBlank()) {
                        DetailInfoRow(label = "Univers", value = universeName)
                    }
                    categoryName?.takeIf { it.isNotBlank() }?.let {
                        DetailInfoRow(label = "Catégorie", value = it)
                    }
                }
            }

            // Section actions
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Votre statut",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextOnCard
                    )
                    Text(
                        text = "Indiquez si vous avez vu le film, souhaitez le voir, ou possédez un support physique.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnCard.copy(alpha = 0.75f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryToggleButton(
                            label = "Vu",
                            selected = selection.watch == WatchStatus.WATCHED,
                            enabled = statusEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val updated = selection.copy(watch = if (selection.watch == WatchStatus.WATCHED) null else WatchStatus.WATCHED)
                                onStatusSelected(updated.takeIf { !it.isEmpty })
                            }
                        )
                        PrimaryToggleButton(
                            label = "À voir",
                            selected = selection.watch == WatchStatus.WANT_TO_WATCH,
                            enabled = statusEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val updated = selection.copy(
                                    watch = if (selection.watch == WatchStatus.WANT_TO_WATCH) null else WatchStatus.WANT_TO_WATCH
                                )
                                onStatusSelected(updated.takeIf { !it.isEmpty })
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryOutlinedButton(
                            label = "DVD",
                            selected = selection.ownership == OwnershipStatus.OWN_DVD,
                            enabled = statusEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val newOwnership =
                                    if (selection.ownership == OwnershipStatus.OWN_DVD) null else OwnershipStatus.OWN_DVD
                                val updated = selection.copy(
                                    ownership = newOwnership,
                                    wantToSell = if (newOwnership == null) false else selection.wantToSell
                                )
                                onStatusSelected(updated.takeIf { !it.isEmpty })
                            }
                        )
                        SecondaryOutlinedButton(
                            label = "Blu-ray",
                            selected = selection.ownership == OwnershipStatus.OWN_BLURAY,
                            enabled = statusEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val newOwnership =
                                    if (selection.ownership == OwnershipStatus.OWN_BLURAY) null else OwnershipStatus.OWN_BLURAY
                                val updated = selection.copy(
                                    ownership = newOwnership,
                                    wantToSell = if (newOwnership == null) false else selection.wantToSell
                                )
                                onStatusSelected(updated.takeIf { !it.isEmpty })
                            }
                        )
                    }

                    val canSell = selection.ownsMovie
                    AssistChip(
                        onClick = {
                            if (!statusEnabled || !canSell) return@AssistChip
                            val updated = selection.copy(wantToSell = !selection.wantToSell)
                            onStatusSelected(updated.takeIf { !it.isEmpty })
                        },
                        label = { Text(if (selection.wantToSell) "À vendre" else "Marquer à vendre") },
                        enabled = statusEnabled && canSell,
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = if (selection.wantToSell) AccentBlueLight.copy(alpha = 0.45f) else TextOnCard.copy(
                                alpha = 0.12f
                            ),
                            labelColor = TextOnCard
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextOnCard.copy(alpha = 0.65f),
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnCard,
            modifier = Modifier.weight(0.65f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PrimaryToggleButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                TextOnCard
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = TextOnCard.copy(alpha = 0.5f)
        ),
        modifier = modifier.heightIn(min = 48.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 4.dp else 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryOutlinedButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) AccentBlueLight.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = TextOnCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (selected) AccentBlueLight else TextOnCard.copy(alpha = 0.4f)
        )
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun extractYear(releaseDate: String): String? {
    val s = releaseDate.trim()
    if (s.length < 4) return null
    val year = s.take(4)
    return year.takeIf { it.all { c -> c.isDigit() } }
}

