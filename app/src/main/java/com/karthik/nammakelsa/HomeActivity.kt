package com.karthik.nammakelsa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class HomeActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(
                Intent(
                    this,
                    RoleSelectionActivity::class.java
                )
            )
            finish()
            return
        }

        val requestedTab =
            intent.getStringExtra("openTab") ?: "home"

        val userId = currentUser.uid
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("workers")
            .document(userId)
            .get()
            .addOnSuccessListener { workerDoc ->

                if (workerDoc.exists()) {

                    setContent {
                        NammaKelsaTheme {
                            MainScreen(
                                role = "worker",
                                openTab = requestedTab
                            )
                        }
                    }

                } else {

                    firestore.collection("hirers")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { hirerDoc ->

                            if (hirerDoc.exists()) {

                                setContent {
                                    NammaKelsaTheme {
                                        MainScreen(
                                            role = "hirer",
                                            openTab = requestedTab
                                        )
                                    }
                                }

                            } else {
                                auth.signOut()

                                startActivity(
                                    Intent(
                                        this,
                                        RoleSelectionActivity::class.java
                                    )
                                )

                                finish()
                            }
                        }
                        .addOnFailureListener {
                            auth.signOut()

                            startActivity(
                                Intent(
                                    this,
                                    RoleSelectionActivity::class.java
                                )
                            )

                            finish()
                        }
                }
            }
            .addOnFailureListener {
                auth.signOut()

                startActivity(
                    Intent(
                        this,
                        RoleSelectionActivity::class.java
                    )
                )

                finish()
            }
    }

    override fun onStart() {
        super.onStart()

        auth.currentUser?.uid?.let { uid ->
            ChatRepository.setUserOnline(uid)
        }
    }

    override fun onStop() {
        super.onStop()

        auth.currentUser?.uid?.let { uid ->
            ChatRepository.setUserOffline(uid)
        }
    }
}