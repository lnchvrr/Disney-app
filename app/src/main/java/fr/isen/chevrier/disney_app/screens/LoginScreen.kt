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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onRegisterClick: () -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dark_blue)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Login",
                color = Color.White,
                fontSize = 28.sp
            )


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.White) },

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
                modifier = Modifier.padding(bottom = 30.dp)
            )


            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White) },
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
                modifier = Modifier.padding(bottom = 30.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }

            Button(
                onClick = {

                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                    } else {

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {
                                    errorMessage = ""
                                    println("Login success")
                                } else {
                                    errorMessage = task.exception?.message ?: "Login failed"
                                }

                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue_1)
                ),

                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Connection",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = Color.White
                )

                TextButton(
                    onClick = { onRegisterClick() }
                ) {
                    Text(
                        text = "Sign up!",
                        color = colorResource(id = R.color.light_blue_1)
                    )
                }
            }
        }
    }
}