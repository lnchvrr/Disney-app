package fr.isen.chevrier.disney_app

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text("Disney App", color = Color.White)
        },
        actions = {

            TextButton(onClick = onLoginClick) {
                Text("Login", color = Color.White)
            }

            TextButton(onClick = onRegisterClick) {
                Text("Register", color = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.dark_blue)
        )
    )
}