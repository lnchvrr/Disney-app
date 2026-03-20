package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import fr.isen.chevrier.disney_app.R
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight

/**
 * @param compact Mode grille 2 colonnes : titre en overlay sur l’affiche, actions en bas.
 */
@Composable
fun MovieCatalogCard(
    movie: Movie,
    status: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    onOpenDetail: () -> Unit,
    statusEnabled: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val selection = status ?: MovieStatusSelection()
    val context = LocalContext.current
    val posterUrl = movie.posterUrl?.takeIf { it.isNotBlank() }
    val iconSize = if (compact) 36.dp else 46.dp
    val iconInner = if (compact) 20.dp else 24.dp
    val corner = if (compact) 12.dp else 16.dp
    val posterInteraction = remember { MutableInteractionSource() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 0.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(corner))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = posterInteraction,
                            indication = ripple(bounded = true),
                            onClick = onOpenDetail
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(posterUrl ?: R.drawable.universe_default)
                            .placeholder(R.drawable.universe_default)
                            .error(R.drawable.universe_default)
                            .build(),
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient bas renforcé pour lisibilité
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.65f),
                                        Color.Black.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                }

                if (!compact) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        StatusIconRow(
                            selection = selection,
                            statusEnabled = statusEnabled,
                            onStatusSelected = onStatusSelected,
                            iconSize = iconSize,
                            iconInner = iconInner
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.9f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 8f
                                )
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        StatusIconRow(
                            selection = selection,
                            statusEnabled = statusEnabled,
                            onStatusSelected = onStatusSelected,
                            iconSize = iconSize,
                            iconInner = iconInner
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIconRow(
    selection: MovieStatusSelection,
    statusEnabled: Boolean,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    iconSize: androidx.compose.ui.unit.Dp,
    iconInner: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        ActionIconButton(
            selected = selection.watch == WatchStatus.WATCHED,
            enabled = statusEnabled,
            onClick = {
                val next = if (selection.watch == WatchStatus.WATCHED) {
                    selection.copy(watch = null)
                } else {
                    selection.copy(watch = WatchStatus.WATCHED)
                }
                onStatusSelected(if (next.isEmpty) null else next)
            },
            imageVector = Icons.Filled.Visibility,
            contentDescription = "Vu",
            iconSize = iconSize,
            iconInner = iconInner
        )
        ActionIconButton(
            selected = selection.watch == WatchStatus.WANT_TO_WATCH,
            enabled = statusEnabled,
            onClick = {
                val next = if (selection.watch == WatchStatus.WANT_TO_WATCH) {
                    selection.copy(watch = null)
                } else {
                    selection.copy(watch = WatchStatus.WANT_TO_WATCH)
                }
                onStatusSelected(if (next.isEmpty) null else next)
            },
            imageVector = Icons.Filled.BookmarkAdd,
            contentDescription = "À voir",
            iconSize = iconSize,
            iconInner = iconInner
        )
        ActionIconButton(
            selected = selection.ownsMovie,
            enabled = statusEnabled,
            onClick = {
                val next = when (selection.ownership) {
                    null -> selection.copy(ownership = OwnershipStatus.OWN_DVD, wantToSell = false)
                    OwnershipStatus.OWN_DVD -> selection.copy(ownership = OwnershipStatus.OWN_BLURAY)
                    OwnershipStatus.OWN_BLURAY -> selection.copy(ownership = null, wantToSell = false)
                }
                onStatusSelected(if (next.isEmpty) null else next)
            },
            imageVector = Icons.Filled.Inventory2,
            contentDescription = "Possédé",
            iconSize = iconSize,
            iconInner = iconInner
        )
        ActionIconButton(
            selected = selection.wantToSell,
            enabled = statusEnabled && selection.ownsMovie,
            onClick = {
                onStatusSelected(selection.copy(wantToSell = !selection.wantToSell))
            },
            imageVector = Icons.Filled.PointOfSale,
            contentDescription = "À vendre",
            iconSize = iconSize,
            iconInner = iconInner
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionIconButton(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    iconSize: androidx.compose.ui.unit.Dp,
    iconInner: androidx.compose.ui.unit.Dp
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
        } else {
            Color.Black.copy(alpha = 0.62f)
        },
        modifier = Modifier.size(iconSize),
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                Color.White.copy(alpha = 0.55f)
            } else {
                Color.White.copy(alpha = 0.2f)
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconInner)
            )
        }
    }
}
