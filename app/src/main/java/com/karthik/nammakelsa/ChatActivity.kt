package com.karthik.nammakelsa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser =
            FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Please login first",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        val receiverId =
            intent.getStringExtra("receiverId") ?: ""

        if (
            receiverId.isBlank() ||
            receiverId == currentUser.uid
        ) {
            Toast.makeText(
                this,
                "Invalid chat user",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        setContent {
            NammaKelsaTheme {
                ChatScreen(receiverId)
            }
        }
    }
}