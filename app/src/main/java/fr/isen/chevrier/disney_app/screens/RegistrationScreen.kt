package fr.isen.chevrier.disney_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import fr.isen.chevrier.disney_app.R

private val RegFieldShape = RoundedCornerShape(16.dp)

@Composable
fun RegistrationScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val bg = colorResource(R.color.dark_blue)
    val primary = MaterialTheme.colorScheme.primary
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White.copy(alpha = 0.92f),
        focusedBorderColor = primary,
        unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
        focusedContainerColor = Color.White.copy(alpha = 0.08f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
        focusedLabelColor = Color.White.copy(alpha = 0.85f),
        unfocusedLabelColor = Color.White.copy(alpha = 0.55f),
        cursorColor = primary,
        focusedLeadingIconColor = Color.White.copy(alpha = 0.85f),
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.55f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Créer un compte",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Rejoignez la communauté et synchronisez votre collection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RegFieldShape,
                label = { Text("Pseudo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                colors = fieldColors
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RegFieldShape,
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                colors = fieldColors
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RegFieldShape,
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RegFieldShape,
                label = { Text("Confirmer le mot de passe") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    when {
                        username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "Veuillez remplir tous les champs"
                        }
                        username.length < 3 -> {
                            errorMessage = "Le pseudo doit contenir au moins 3 caractères"
                        }
                        password != confirmPassword -> {
                            errorMessage = "Les mots de passe ne correspondent pas"
                        }
                        password.length < 8 -> {
                            errorMessage = "Le mot de passe doit contenir au moins 8 caractères"
                        }
                        else -> {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build()
                                        user?.updateProfile(profileUpdates)
                                        errorMessage = ""
                                        onRegisterSuccess()
                                    } else {
                                        errorMessage = task.exception?.message ?: "Inscription impossible"
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "S'inscrire",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            TextButton(onClick = onLoginClick) {
                Text(
                    text = "Déjà un compte ? Se connecter",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
