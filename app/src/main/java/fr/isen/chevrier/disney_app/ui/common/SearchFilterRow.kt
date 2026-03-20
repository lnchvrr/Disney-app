package fr.isen.chevrier.disney_app.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchFilterRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    showFilter: Boolean = false,
    onFilterClick: () -> Unit = {},
    searchEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    var focused by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.surfaceContainerHigh
    val bgFocused = MaterialTheme.colorScheme.surfaceContainerHighest
    val bgDisabled = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp, max = 48.dp)
                .onFocusChanged { focused = it.isFocused },
            enabled = searchEnabled,
            singleLine = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (focused) 1f else 0.75f
                    )
                )
            },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = if (searchEnabled) bgFocused else bgDisabled,
                unfocusedContainerColor = if (searchEnabled) bg else bgDisabled,
                disabledContainerColor = bgDisabled,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (showFilter) {
            FilledTonalIconButton(
                onClick = onFilterClick,
                enabled = searchEnabled,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filtres",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
