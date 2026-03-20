package fr.isen.chevrier.disney_app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.isen.chevrier.disney_app.ui.common.SelectableFilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTabFilterSheet(
    visible: Boolean,
    tabLabels: List<String>,
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    onDismiss: () -> Unit
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
                text = "Afficher la liste",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Choisissez quelle collection afficher.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(tabLabels) { index, label ->
                    SelectableFilterChip(
                        label = label,
                        selected = selectedTabIndex == index,
                        onClick = {
                            onSelectTab(index)
                            onDismiss()
                        },
                        fillMaxWidth = true
                    )
                }
            }
        }
    }
}
