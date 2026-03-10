package fr.isen.chevrier.disney_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.screens.HomeScreen
import fr.isen.chevrier.disney_app.ui.theme.DisneyappTheme
import fr.isen.chevrier.disney_app.screens.LoginScreen
import fr.isen.chevrier.disney_app.screens.RegistrationScreen

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        setContent {
            val navController = rememberNavController()
            val currentUser: FirebaseUser? = auth.currentUser

            DisneyappTheme {

                Scaffold() { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                currentUser = currentUser,
                                onLoginClick = {
                                    navController.navigate("login")
                                },
                                onRegisterClick = {
                                    navController.navigate("register")
                                },
                                onProfileClick = {
                                    navController.navigate("profile")
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onBack = {
                                    navController.navigate("home")
                                },
                                onRegisterClick = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = {
                                    navController.navigate("profile")
                                }
                            )
                        }

                        composable("register") {
                            RegistrationScreen(
                                onBack = {
                                    navController.navigate("home")
                                },
                                onLoginClick = {
                                    navController.navigate("login")
                                },
                                onRegisterSuccess = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DisneyappTheme {
        Greeting("Android")
    }
}