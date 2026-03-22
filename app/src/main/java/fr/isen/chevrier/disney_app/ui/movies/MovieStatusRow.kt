package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.isen.chevrier.disney_app.model.MovieStatusSelection
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard

@Composable
fun StatusRow(
    currentStatus: MovieStatusSelection?,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    enabled: Boolean = true
) {
    val selection = currentStatus?.normalized ?: MovieStatusSelection()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Statuts",
            color = Color.Black,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        StatusGroupRow(
            title = "Visionnage",
            options = listOf(
                WatchStatus.WATCHED to "Vu",
                WatchStatus.WANT_TO_WATCH to "À voir"
            ),
            selected = selection.watch,
            onSelected = { watch ->
                val updated = selection.copy(
                    watch = if (selection.watch == watch) null else watch
                ).normalized

                onStatusSelected(updated.takeUnless { it.isEmpty })
            },
            enabled = enabled
        )

        SupportStatusRow(
            selection = selection,
            onStatusSelected = onStatusSelected,
            enabled = enabled
        )
    }
}

@Composable
private fun SupportStatusRow(
    selection: MovieStatusSelection,
    onStatusSelected: (MovieStatusSelection?) -> Unit,
    enabled: Boolean
) {
    val canShowSellOption =
        selection.ownership == OwnershipStatus.OWN_DVD ||
                selection.ownership == OwnershipStatus.OWN_BLURAY ||
                selection.wantToSell

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Support",
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupportChip(
                label = "DVD",
                selected = selection.ownership == OwnershipStatus.OWN_DVD,
                enabled = enabled,
                onClick = {
                    val nextOwnership =
                        if (selection.ownership == OwnershipStatus.OWN_DVD) null
                        else OwnershipStatus.OWN_DVD

                    val updated = selection.copy(
                        ownership = nextOwnership,
                        wantToSell = if (nextOwnership == null) false else selection.wantToSell
                    ).normalized

                    onStatusSelected(updated.takeUnless { it.isEmpty })
                }
            )

            SupportChip(
                label = "Blu-ray",
                selected = selection.ownership == OwnershipStatus.OWN_BLURAY,
                enabled = enabled,
                onClick = {
                    val nextOwnership =
                        if (selection.ownership == OwnershipStatus.OWN_BLURAY) null
                        else OwnershipStatus.OWN_BLURAY

                    val updated = selection.copy(
                        ownership = nextOwnership,
                        wantToSell = if (nextOwnership == null) false else selection.wantToSell
                    ).normalized

                    onStatusSelected(updated.takeUnless { it.isEmpty })
                }
            )
        }

        if (canShowSellOption) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SupportChip(
                    label = "À vendre",
                    selected = selection.wantToSell,
                    enabled = enabled,
                    onClick = {
                        val updated = selection.copy(
                            wantToSell = !selection.wantToSell && selection.ownership != null
                        ).normalized

                        onStatusSelected(updated.takeUnless { it.isEmpty })
                    }
                )
            }
        }
    }
}

@Composable
private fun SupportChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = { if (enabled) onClick() },
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
            enabled = enabled,
            selected = selected,
            borderColor = Color.Gray.copy(alpha = 0.6f),
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun <T> StatusGroupRow(
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
            color = Color.Black
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
                        enabled = enabled,
                        selected = isSelected,
                        borderColor = Color.Gray.copy(alpha = 0.6f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}