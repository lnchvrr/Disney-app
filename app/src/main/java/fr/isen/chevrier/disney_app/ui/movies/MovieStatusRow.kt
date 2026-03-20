package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard

@Composable
fun StatusRow(
    currentStatus: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    enabled: Boolean = true
) {
    val selection = currentStatus ?: MovieStatusSelection()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Statuts",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        StatusGroupRow(
            title = "Visionnage",
            options = listOf(
                WatchStatus.WATCHED to "Vu",
                WatchStatus.WANT_TO_WATCH to "À voir"
            ),
            selected = selection.watch,
            onSelected = { watch ->
                val updated = selection.copy(watch = if (selection.watch == watch) null else watch)
                onStatusSelected(if (updated.isEmpty) null else updated)
            },
            enabled = enabled
        )

        StatusGroupRow(
            title = "Support",
            options = listOf(
                OwnershipStatus.OWN_DVD to "DVD",
                OwnershipStatus.OWN_BLURAY to "Blu-ray"
            ),
            selected = selection.ownership,
            onSelected = { ownership ->
                val newOwnership = if (selection.ownership == ownership) null else ownership
                val updated = selection.copy(
                    ownership = newOwnership,
                    wantToSell = if (newOwnership == null) false else selection.wantToSell
                )
                onStatusSelected(if (updated.isEmpty) null else updated)
            },
            enabled = enabled
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Veut vendre",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
            val canSell = selection.ownsMovie
            FilterChip(
                selected = selection.wantToSell,
                onClick = {
                    if (canSell && enabled) {
                        onStatusSelected(selection.copy(wantToSell = !selection.wantToSell))
                    }
                },
                enabled = canSell && enabled,
                label = { Text("À vendre", maxLines = 1) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBlueLight,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = CardWhiteStrong,
                    labelColor = TextOnCard
                ),
                shape = RoundedCornerShape(14.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = canSell && enabled,
                    selected = selection.wantToSell,
                    borderColor = Color.White.copy(alpha = 0.6f),
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun <T> StatusGroupRow(
    title: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                val isSelected = selected == value
                FilterChip(
                    selected = isSelected,
                    onClick = { if (enabled) onSelected(value) },
                    enabled = enabled,
                    label = {
                        Text(
                            text = label,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlueLight,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = CardWhite,
                        labelColor = TextOnCard
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.White.copy(alpha = 0.6f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}
