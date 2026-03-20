package fr.isen.chevrier.disney_app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.model.OwnershipStatus
import fr.isen.chevrier.disney_app.ui.profile.ProfileTabFilterSheet
import fr.isen.chevrier.disney_app.ui.theme.AccentBlueLight
import fr.isen.chevrier.disney_app.ui.theme.CardWhite
import fr.isen.chevrier.disney_app.ui.theme.CardWhiteStrong
import fr.isen.chevrier.disney_app.ui.theme.TextOnCard
import fr.isen.chevrier.disney_app.viewmodel.OwnedMovieItem
import fr.isen.chevrier.disney_app.viewmodel.ProfileViewModel

private enum class ProfileMovieTab {
    OWNED, SEEN, WISH, SELL
}

@Composable
fun ProfileScreen(
    currentUser: FirebaseUser?,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLogoutClick: () -> Unit,
    profileViewModel: ProfileViewModel
) {
    LaunchedEffect(currentUser?.uid) {
        profileViewModel.loadProfile(currentUser)
    }
    val state by profileViewModel.uiState.collectAsState()

    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) localAvatarUri = uri
    }

    var showEditProfile by remember { mutableStateOf(false) }
    var editedName by remember(state.displayName) { mutableStateOf(state.displayName) }
    var editedEmail by remember(state.email) { mutableStateOf(state.email) }
    var editedPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(state.displayName, state.email) {
        editedName = state.displayName
        editedEmail = state.email
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabKeys = remember {
        listOf(
            ProfileMovieTab.OWNED to "Possédés",
            ProfileMovieTab.SEEN to "Déjà vus",
            ProfileMovieTab.WISH to "À voir",
            ProfileMovieTab.SELL to "À vendre"
        )
    }

    val moviesForTab = when (tabKeys[selectedTab].first) {
        ProfileMovieTab.OWNED -> state.ownedMovies
        ProfileMovieTab.SEEN -> state.seenMovies
        ProfileMovieTab.WISH -> state.wishlistMovies
        ProfileMovieTab.SELL -> state.sellingMovies
    }

    var showProfileTabFilter by remember { mutableStateOf(false) }

    ProfileTabFilterSheet(
        visible = showProfileTabFilter,
        tabLabels = tabKeys.map { it.second },
        selectedTabIndex = selectedTab,
        onSelectTab = { selectedTab = it },
        onDismiss = { showProfileTabFilter = false }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentUser == null) {
            item {
                ProfileGuestAuthCard(
                    onLoginClick = onLoginClick,
                    onRegisterClick = onRegisterClick
                )
            }
        } else {
            item {
                ProfileAvatarHeader(
                    displayName = state.displayName.ifBlank { "Utilisateur" },
                    email = state.email,
                    localAvatarUri = localAvatarUri,
                    onAvatarClick = {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            item {
                StatsRowCard(
                    watched = state.watchedCount,
                    toWatch = state.toWatchCount,
                    dvd = state.dvdCount,
                    bluray = state.blurayCount
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showEditProfile = !showEditProfile },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            "Modifier le profil",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    OutlinedButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            "Déconnexion",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = showEditProfile) {
                    EditProfileExpandableCard(
                        editedName = editedName,
                        onNameChange = { editedName = it },
                        editedEmail = editedEmail,
                        onEmailChange = { editedEmail = it },
                        editedPassword = editedPassword,
                        onPasswordChange = { editedPassword = it },
                        confirmPassword = confirmPassword,
                        onConfirmChange = { confirmPassword = it },
                        currentUser = currentUser,
                        onSaveProfile = {
                            profileViewModel.saveDisplayName(currentUser, editedName)
                            profileViewModel.saveEmail(currentUser, editedEmail)
                        },
                        onSavePassword = {
                            profileViewModel.savePassword(currentUser, editedPassword, confirmPassword)
                            editedPassword = ""
                            confirmPassword = ""
                        }
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ma collection",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showProfileTabFilter = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filtrer par onglet",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tabKeys.forEachIndexed { index, (_, label) ->
                                FilterChip(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    label = {
                                        Text(
                                            label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (state.isLoadingOwnedMovies) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            } else if (moviesForTab.isEmpty()) {
                item {
                    EmptyMoviesCard(
                        message = when (tabKeys[selectedTab].first) {
                            ProfileMovieTab.OWNED -> "Aucun film possédé pour le moment."
                            ProfileMovieTab.SEEN -> "Aucun film marqué comme vu."
                            ProfileMovieTab.WISH -> "Votre liste « à voir » est vide."
                            ProfileMovieTab.SELL -> "Aucune offre de vente en cours."
                        }
                    )
                }
            } else {
                items(moviesForTab.chunked(2), key = { chunk -> chunk.joinToString { it.movie.id } }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { item ->
                            ProfileMovieMiniCard(
                                item = item,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            state.message?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentBlueLight,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProfileGuestAuthCard(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connectez-vous",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Synchronisez votre collection, vos statuts et votre profil sur tous vos appareils.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "Connexion",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Créer un compte",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatarHeader(
    displayName: String,
    email: String,
    localAvatarUri: Uri?,
    onAvatarClick: () -> Unit
) {
    val ringGradient = Brush.linearGradient(
        colors = listOf(
            AccentBlueLight.copy(alpha = 0.75f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        )
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(ringGradient)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(CardWhiteStrong)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                if (localAvatarUri != null) {
                    AsyncImage(
                        model = localAvatarUri,
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AccentBlueLight
                    )
                }
            }
        }
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = email.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatsRowCard(
    watched: Int,
    toWatch: Int,
    dvd: Int,
    bluray: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Votre activité",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard(
                    value = watched,
                    label = "Vu",
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    value = toWatch,
                    label = "À voir",
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    value = dvd,
                    label = "DVD",
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    value = bluray,
                    label = "Blu-ray",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EditProfileExpandableCard(
    editedName: String,
    onNameChange: (String) -> Unit,
    editedEmail: String,
    onEmailChange: (String) -> Unit,
    editedPassword: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmChange: (String) -> Unit,
    currentUser: FirebaseUser?,
    onSaveProfile: () -> Unit,
    onSavePassword: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Informations & sécurité",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextOnCard
                )
            )
            OutlinedTextField(
                value = editedName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nom") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                colors = profileFieldColors()
            )
            OutlinedTextField(
                value = editedEmail,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("E-mail") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                colors = profileFieldColors()
            )
            Button(
                onClick = onSaveProfile,
                enabled = currentUser != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enregistrer les infos")
            }
            OutlinedTextField(
                value = editedPassword,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nouveau mot de passe") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                colors = profileFieldColors()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmer le mot de passe") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                colors = profileFieldColors()
            )
            Button(
                onClick = onSavePassword,
                enabled = currentUser != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Mettre à jour le mot de passe")
            }
        }
    }
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextOnCard,
    unfocusedTextColor = TextOnCard,
    focusedLabelColor = TextOnCard.copy(alpha = 0.8f),
    unfocusedLabelColor = TextOnCard.copy(alpha = 0.6f),
    cursorColor = AccentBlueLight,
    focusedBorderColor = AccentBlueLight,
    unfocusedBorderColor = TextOnCard.copy(alpha = 0.35f)
)

@Composable
private fun EmptyMoviesCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextOnCard.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Composable
private fun ProfileMovieMiniCard(
    item: OwnedMovieItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.25f))
            ) {
                if (!item.movie.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.movie.posterUrl,
                        contentDescription = item.movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.movie.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnCard
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val ownershipLabel = when (item.status.ownership) {
                OwnershipStatus.OWN_DVD -> "DVD"
                OwnershipStatus.OWN_BLURAY -> "Blu-ray"
                null -> ""
            }
            if (ownershipLabel.isNotBlank()) {
                Text(
                    text = ownershipLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOnCard.copy(alpha = 0.65f)
                )
            }
            if (item.status.wantToSell) {
                Text(
                    text = "À vendre",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentBlueLight
                )
            }
        }
    }
}
