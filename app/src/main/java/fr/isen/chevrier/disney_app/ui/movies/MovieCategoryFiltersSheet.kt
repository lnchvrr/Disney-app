package fr.isen.chevrier.disney_app.ui.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.isen.chevrier.disney_app.model.Category
import fr.isen.chevrier.disney_app.ui.common.SelectableFilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieCategoryFiltersSheet(
    visible: Boolean,
    categories: List<Category>,
    activeGenres: List<String>,
    onDismiss: () -> Unit,
    onToggleGenre: (String) -> Unit,
    onClearGenres: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Catégories",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Choisissez les univers ou genres à afficher.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    SelectableFilterChip(
                        label = "Toutes les catégories",
                        selected = activeGenres.isEmpty(),
                        onClick = { onClearGenres() }
                    )
                }
                items(categories, key = { it.id }) { category ->
                    val selected = category.id in activeGenres
                    SelectableFilterChip(
                        label = category.name,
                        selected = selected,
                        onClick = { onToggleGenre(category.id) },
                        fillMaxWidth = true
                    )
                }
            }
        }
    }
}
