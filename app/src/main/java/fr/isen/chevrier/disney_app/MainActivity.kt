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
import fr.isen.chevrier.disney_app.screens.HomeScreen
import fr.isen.chevrier.disney_app.ui.theme.DisneyappTheme
import fr.isen.chevrier.disney_app.screens.LoginScreen
import fr.isen.chevrier.disney_app.screens.RegistrationScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            DisneyappTheme {

                Scaffold(
                    topBar = {
                        MainTopBar(
                            onLoginClick = {
                                navController.navigate("login")
                            },
                            onRegisterClick = {
                                navController.navigate("register")
                            }
                        )
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen()
                        }

                        composable("login") {
                            LoginScreen(onRegisterClick = {
                                navController.navigate("register")
                            })
                        }

                        composable("register") {
                            RegistrationScreen(onLoginClick = {
                                navController.navigate("login")
                            })
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