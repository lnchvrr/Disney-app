package fr.isen.chevrier.disney_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fr.isen.chevrier.disney_app.ui.theme.DisneyappTheme
import fr.isen.chevrier.disney_app.screens.HomeScreen

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            val currentUser: FirebaseUser? = auth.currentUser

            DisneyappTheme {

                Surface(modifier = Modifier.fillMaxSize()) {

                    HomeScreen(currentUser = currentUser)

                }
            }
        }
    }
}

