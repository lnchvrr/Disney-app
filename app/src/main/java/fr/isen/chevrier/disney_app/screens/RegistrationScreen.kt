package fr.isen.chevrier.disney_app.screens

import fr.isen.chevrier.disney_app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import fr.isen.chevrier.disney_app.ui.components.BackHeader

@Composable
fun RegistrationScreen(onLoginClick: () -> Unit, onBack: () -> Unit, onRegisterSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    //val currentUser = auth.currentUser

    /*LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onRegisterSuccess()
        }
    }*/

    Box(
        Modifier.background(colorResource(id = R.color.dark_blue)
        ).fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }


        Column(
            modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BackHeader(
                title = "",
                subtitle = "",
                onBack = onBack
            )

            Text(
                text = "Sign up",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
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
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
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
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") } ,
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
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password", color = Color.White) },
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
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "Please fill in all fields"
                        }

                        username.length < 3 -> {
                            errorMessage = "Username must contain at least 3 characters"
                        }

                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }

                        password.length < 8 -> {
                            errorMessage = "Password must contain at least 8 characters"
                        }

                        else -> {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser

                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build()

                                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                            errorMessage = ""
                                            onRegisterSuccess()
                                        } ?: run {
                                            errorMessage = ""
                                            onRegisterSuccess()
                                        }
                                    } else {
                                        errorMessage = task.exception?.message ?: "Registration failed"
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue_1)
                )
            )  {
                Text("Register",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Do you have an account?",
                    color = Color.White
                )

                TextButton(
                    onClick = { onLoginClick() }
                ) {
                    Text(
                        text = "Login",
                        color = colorResource(id = R.color.light_blue_1)
                    )
                }
            }
        }
    }
}