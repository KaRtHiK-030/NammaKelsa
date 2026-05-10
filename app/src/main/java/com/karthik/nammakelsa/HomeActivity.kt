package com.karthik.nammakelsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GET ROLE
        val role =
            intent.getStringExtra("role")
                ?: "worker"

        setContent {

            NammaKelsaTheme {

                MainScreen(role)
            }
        }
    }
}