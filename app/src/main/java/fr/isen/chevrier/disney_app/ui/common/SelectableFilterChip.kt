package fr.isen.chevrier.disney_app.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val UnselectedBg = Color(0xFF252530)
private val UnselectedBorder = Color.White.copy(alpha = 0.14f)
private val UnselectedLabel = Color(0xFFC5C5D0)

/**
 * Chip filtre : non sélectionné = fond sombre + bordure ; sélectionné = accent + texte blanc.
 */
@Composable
fun SelectableFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = false,
    maxLines: Int = 2
) {
    val primary = MaterialTheme.colorScheme.primary
    val mod = if (fillMaxWidth) modifier.fillMaxWidth() else modifier

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                color = if (selected) Color.White else UnselectedLabel
            )
        },
        modifier = mod,
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primary,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            containerColor = UnselectedBg,
            labelColor = UnselectedLabel
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color.Transparent else UnselectedBorder,
            selectedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        )
    )
}
