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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import fr.isen.chevrier.disney_app.R

private val DarkFieldShape = RoundedCornerShape(16.dp)

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }
    val canSubmit = email.isNotBlank() && password.isNotBlank() && !isLoading

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
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.55f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error
    )

    fun submit() {
        if (isLoading) return
        keyboardController?.hide()
        val emailBlank = email.isBlank()
        val passwordBlank = password.isBlank()
        emailError = if (emailBlank) "Remplissez tous les champs" else null
        passwordError = if (passwordBlank) "Remplissez tous les champs" else null
        if (emailBlank || passwordBlank) return
        emailError = null
        passwordError = null
        isLoading = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    passwordError = null
                    onLoginSuccess()
                } else {
                    passwordError = task.exception?.message ?: "Échec de la connexion"
                }
            }
    }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Connexion",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Accédez à votre catalogue et à vos statuts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null && it.isNotBlank()) emailError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { keyEvent ->
                        val keyCode = keyEvent.nativeKeyEvent.keyCode
                        if (keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                            keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
                        ) {
                            passwordFocusRequester.requestFocus()
                            true
                        } else {
                            false
                        }
                    },
                singleLine = true,
                shape = DarkFieldShape,
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = {
                    emailError?.let { msg ->
                        if (msg.isNotBlank()) Text(text = msg, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = fieldColors
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null && it.isNotBlank()) passwordError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester)
                    .onKeyEvent { keyEvent ->
                        val keyCode = keyEvent.nativeKeyEvent.keyCode
                        if (keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                            keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
                        ) {
                            submit()
                            true
                        } else {
                            false
                        }
                    },
                singleLine = true,
                shape = DarkFieldShape,
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let { msg ->
                        if (msg.isNotBlank()) Text(text = msg, color = MaterialTheme.colorScheme.error)
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = fieldColors
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = canSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.2f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
                Text(
                    text = "Se connecter",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            TextButton(
                onClick = onRegisterClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Créer un compte",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
