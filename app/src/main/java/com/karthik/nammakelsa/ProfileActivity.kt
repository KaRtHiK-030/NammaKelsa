package com.karthik.nammakelsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser =
            FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            finish()
            return
        }

        val userId = currentUser.uid
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("workers")
            .document(userId)
            .get()
            .addOnSuccessListener { workerDoc ->

                setContent {
                    NammaKelsaTheme {
                        if (workerDoc.exists()) {
                            EditProfileScreen(
                                role = "worker"
                            )
                        } else {
                            EditProfileScreen(
                                role = "hirer"
                            )
                        }
                    }
                }
            }
            .addOnFailureListener {
                finish()
            }
    }
}