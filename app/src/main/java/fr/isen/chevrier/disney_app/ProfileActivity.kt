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
import androidx.compose.material3.Surface
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
import fr.isen.chevrier.disney_app.R

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var name by remember { mutableStateOf("Mickey Mouse") }
    var email by remember { mutableStateOf("mickey@disney.com") }
    var password by remember { mutableStateOf("123456Mi#") }

    var editedName by remember { mutableStateOf(name) }
    var editedEmail by remember { mutableStateOf(email) }
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
        Text(
            text = "My profile",
            color = Color.White,
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (selectedImageUri != null) {
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

        //Button(
        //    onClick = {
        //        imagePickerLauncher.launch("image/*")
        //    },
        //    modifier = Modifier
        //        .fillMaxWidth()
        //        .padding(bottom = 24.dp),
        //    colors = ButtonDefaults.buttonColors(
        //        containerColor = colorResource(id = R.color.light_blue_1)
        //    ),
        //    shape = RoundedCornerShape(20.dp)
        //) {
        /*    Text(
                text = "Choose a photo",
                color = Color.White
            )
        }*/

        if (!isEditingName) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name: $name",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        editedName = name
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
                            name = editedName
                            isEditingName = false
                            message = "Name updated"
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
                        editedName = name
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
                    text = "Email: $email",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        editedEmail = email
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
                            email = editedEmail
                            isEditingEmail = false
                            message = "Email updated"
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
                        editedEmail = email
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
                            editedPassword.isBlank() || confirmEditedPassword.isBlank() -> {
                                message = "Please fill in both password fields"
                            }

                            editedPassword != confirmEditedPassword -> {
                                message = "Passwords do not match"
                            }

                            editedPassword.length < 8 -> {
                                message = "Password must contain at least 8 characters"
                            }

                            else -> {
                                password = editedPassword
                                isEditingPassword = false
                                editedPassword = ""
                                confirmEditedPassword = ""
                                message = "Password updated"
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

        Button(
            onClick = onLogoutClick,
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
    }
}