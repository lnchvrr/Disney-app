package fr.isen.chevrier.disney_app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.R
import fr.isen.chevrier.disney_app.viewmodel.MovieListViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import fr.isen.chevrier.disney_app.data.MockMovieData
import fr.isen.chevrier.disney_app.model.Movie
import fr.isen.chevrier.disney_app.model.WatchStatus
import fr.isen.chevrier.disney_app.ui.movies.MovieDetailContent

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    currentUser: FirebaseUser?,
    movieListViewModel: MovieListViewModel
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val firebaseName = currentUser?.displayName ?: "No name"
    val firebaseEmail = currentUser?.email ?: "No email"

    var editedName by remember(firebaseName) { mutableStateOf(firebaseName) }
    var editedEmail by remember(firebaseEmail) { mutableStateOf(firebaseEmail) }
    var oldEditedPassword by remember { mutableStateOf("") }
    var editedPassword by remember { mutableStateOf("") }
    var confirmEditedPassword by remember { mutableStateOf("") }

    var isEditingName by remember { mutableStateOf(false) }
    var isEditingEmail by remember { mutableStateOf(false) }
    var isEditingPassword by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(currentUser?.uid) {
        movieListViewModel.loadUserStatuses(currentUser?.uid)
    }

    // Il faudra le changer quand on lèvera les mocks
    val watchedMovies = MockMovieData.movies.filter { movie ->
        movieListViewModel.userStatuses[movie.id]?.watch == WatchStatus.WATCHED
    }

    // Il faudra le changer quand on lèvera les mocks
    val wantToWatchMovies = MockMovieData.movies.filter { movie ->
        movieListViewModel.userStatuses[movie.id]?.watch == WatchStatus.WANT_TO_WATCH
    }

    val moviesForSelectedTab = if (selectedTabIndex == 0) {
        watchedMovies
    } else {
        wantToWatchMovies
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dark_blue))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentUser != null) {
            Text(
                text = "My profile",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val profilePhoto = selectedImageUri ?: currentUser.photoUrl

            Box (modifier = Modifier.padding(bottom = 24.dp))
            {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (profilePhoto != null) {
                        AsyncImage(
                            model = profilePhoto,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Default profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                            .background(
                                color = colorResource(id = R.color.light_blue_1),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Change profile photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }


            if (!isEditingName) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Name: $firebaseName",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            editedName = firebaseName
                            isEditingName = true
                            message = ""
                        }
                    ) {
                        Text(
                            text = "Edit",
                            color = colorResource(id = R.color.light_blue_1)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                    },
                    label = {
                        Text("Name", color = Color.White)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.light_blue_1),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        focusedTextColor = colorResource(id = R.color.white),
                        unfocusedTextColor = colorResource(id = R.color.white),
                        focusedLabelColor = colorResource(id = R.color.light_blue_1),
                        unfocusedLabelColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.light_blue_1)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (editedName.isBlank()) {
                                message = "Name cannot be empty"
                            } else {
                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(editedName)
                                    .build()

                                currentUser?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            isEditingName = false
                                            message = "Name updated"
                                        } else {
                                            message = task.exception?.message ?: "Failed to update name"
                                        }
                                    }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.light_blue_1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = {
                            editedName = firebaseName
                            isEditingName = false
                            message = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_200)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                        )
                    }
                }
            }

            if (!isEditingEmail) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Email: $firebaseEmail",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            editedEmail = firebaseEmail
                            isEditingEmail = true
                            message = ""
                        }
                    ) {
                        Text(
                            text = "Edit",
                            color = colorResource(id = R.color.light_blue_1)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = {
                        editedEmail = it
                    },
                    label = {
                        Text("Email", color = Color.White)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.light_blue_1),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        focusedTextColor = colorResource(id = R.color.white),
                        unfocusedTextColor = colorResource(id = R.color.white),
                        focusedLabelColor = colorResource(id = R.color.light_blue_1),
                        unfocusedLabelColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.light_blue_1)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (editedEmail.isBlank()) {
                                message = "Email cannot be empty"
                            } else {
                                currentUser?.updateEmail(editedEmail)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            isEditingEmail = false
                                            message = "Email updated"
                                        } else {
                                            message = task.exception?.message ?: "Failed to update email"
                                        }
                                    }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.light_blue_1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = {
                            editedEmail = firebaseEmail
                            isEditingEmail = false
                            message = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_200)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White
                        )
                    }
                }
            }

            if (!isEditingPassword) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password: ******",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            oldEditedPassword = ""
                            editedPassword = ""
                            confirmEditedPassword = ""
                            isEditingPassword = true
                            message = ""
                        }
                    ) {
                        Text(
                            text = "Edit",
                            color = colorResource(id = R.color.light_blue_1)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = oldEditedPassword,
                    onValueChange = {
                        oldEditedPassword = it
                    },
                    label = {
                        Text("Old password", color = Color.White)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.light_blue_1),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        focusedTextColor = colorResource(id = R.color.white),
                        unfocusedTextColor = colorResource(id = R.color.white),
                        focusedLabelColor = colorResource(id = R.color.light_blue_1),
                        unfocusedLabelColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.light_blue_1)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = editedPassword,
                    onValueChange = {
                        editedPassword = it
                    },
                    label = {
                        Text("New password", color = Color.White)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.light_blue_1),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        focusedTextColor = colorResource(id = R.color.white),
                        unfocusedTextColor = colorResource(id = R.color.white),
                        focusedLabelColor = colorResource(id = R.color.light_blue_1),
                        unfocusedLabelColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.light_blue_1)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = confirmEditedPassword,
                    onValueChange = {
                        confirmEditedPassword = it
                    },
                    label = {
                        Text("Confirm password", color = Color.White)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.light_blue_1),
                        unfocusedBorderColor = colorResource(id = R.color.white),
                        focusedTextColor = colorResource(id = R.color.white),
                        unfocusedTextColor = colorResource(id = R.color.white),
                        focusedLabelColor = colorResource(id = R.color.light_blue_1),
                        unfocusedLabelColor = colorResource(id = R.color.white),
                        cursorColor = colorResource(id = R.color.light_blue_1)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            when {
                                oldEditedPassword.isBlank() || editedPassword.isBlank() || confirmEditedPassword.isBlank() -> {
                                    message = "Please fill in both password fields"
                                }

                                editedPassword != confirmEditedPassword -> {
                                    message = "Passwords do not match"
                                }

                                editedPassword.length < 8 -> {
                                    message = "Password must contain at least 8 characters"
                                }

                                else -> {
                                    val email = currentUser?.email

                                    if (email == null) {
                                        message = "User email not found"
                                    } else {
                                        val credential = com.google.firebase.auth.EmailAuthProvider
                                            .getCredential(email, oldEditedPassword)

                                        currentUser.reauthenticate(credential)
                                            .addOnCompleteListener { reauthTask ->
                                                if (reauthTask.isSuccessful) {
                                                    currentUser.updatePassword(editedPassword)
                                                        .addOnCompleteListener { updateTask ->
                                                            if (updateTask.isSuccessful) {
                                                                isEditingPassword = false
                                                                oldEditedPassword = ""
                                                                editedPassword = ""
                                                                confirmEditedPassword = ""
                                                                message = "Password updated"
                                                            } else {
                                                                message =
                                                                    updateTask.exception?.message
                                                                        ?: "Failed to update password"
                                                            }
                                                        }
                                                } else {
                                                    message = reauthTask.exception?.message
                                                        ?: "Old password is incorrect"
                                                }
                                            }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.light_blue_1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = {
                            oldEditedPassword = ""
                            editedPassword = ""
                            confirmEditedPassword = ""
                            isEditingPassword = false
                            message = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_200)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White
                        )
                    }
                }
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
            }

            Text(
                text = "My movies",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 8.dp)
            )


            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Vu") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("À voir") }
                )
            }



            MovieStatusList(
                modifier = Modifier.padding(top = 12.dp),
                movies = moviesForSelectedTab,
                emptyText = if (selectedTabIndex == 0) {
                    "Aucun film marqué comme vu pour ce profil"
                } else {
                    "Aucun film marqué comme à voir pour ce profil"
                },
                onMovieClick = { movie ->
                    selectedMovie = movie
                }
            )

            selectedMovie?.let { movie ->
                AlertDialog(
                    onDismissRequest = { selectedMovie = null },
                    confirmButton = {
                        TextButton(onClick = { selectedMovie = null }) {
                            Text("Fermer")
                        }
                    },
                    text = {
                        MovieDetailContent(
                            movie = movie,
                            universeName = MockMovieData.universes.firstOrNull { it.id == movie.universeId }?.name.orEmpty(),
                            categoryName = movie.categoryId?.let { categoryId ->
                                MockMovieData.categories.firstOrNull { it.id == categoryId }?.name
                            },
                            currentStatus = movieListViewModel.userStatuses[movie.id],
                            canManageStatuses = currentUser != null,
                            onStatusSelected = { status ->
                                movieListViewModel.updateStatus(movie.id, status)
                            }
                        )
                    }
                )
            }

            Button(
                onClick = {
                    onLogoutClick()
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue_1)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Logout",
                    color = Color.White
                )
            }


        } else {
            Text(
                text = "My profile",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "You are not connected",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun MovieStatusList(
    movies: List<Movie>,
    emptyText: String,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) {
        Text(
            text = emptyText,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        movies.forEach { movie ->
            MovieStatusCard(
                movie = movie,
                onClick = { onMovieClick(movie) }
            )
        }
    }
}

@Composable
private fun MovieStatusCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = movie.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(
                text = movie.releaseDate,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}