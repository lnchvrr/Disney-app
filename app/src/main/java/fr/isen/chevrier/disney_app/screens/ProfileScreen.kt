package fr.isen.chevrier.disney_app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
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

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onSeeWatchedMovies: () -> Unit,
    onSeeWantToWatchMovies: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    currentUser: FirebaseUser?
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

            val profilePhoto = selectedImageUri ?: currentUser?.photoUrl

            if (profilePhoto != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(bottom = 24.dp)
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
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(bottom = 24.dp)
                )
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My movies",
                    //style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                )

                Button(
                    onClick = { onSeeWantToWatchMovies() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("Movies to watch")
                }

                Button(
                    onClick = { onSeeWatchedMovies() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Movies watched")
                }
            }

            Button(
                onClick = {
                    onLogoutClick()
                },
                modifier = Modifier.fillMaxWidth(),
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