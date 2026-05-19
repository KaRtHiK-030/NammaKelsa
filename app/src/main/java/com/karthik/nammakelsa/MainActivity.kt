package com.karthik.nammakelsa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(
                Intent(this, RoleSelectionActivity::class.java)
            )
            finish()
            return
        }

        val userId = currentUser.uid
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("workers")
            .document(userId)
            .get()
            .addOnSuccessListener { workerDoc ->

                if (workerDoc.exists()) {
                    openHome()
                } else {
                    firestore.collection("hirers")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { hirerDoc ->

                            if (hirerDoc.exists()) {
                                openHome()
                            } else {
                                auth.signOut()
                                openRoleSelection()
                            }
                        }
                        .addOnFailureListener {
                            auth.signOut()
                            openRoleSelection()
                        }
                }
            }
            .addOnFailureListener {
                auth.signOut()
                openRoleSelection()
            }
    }

    private fun openHome() {
        startActivity(
            Intent(this, HomeActivity::class.java)
        )
        finish()
    }

    private fun openRoleSelection() {
        startActivity(
            Intent(this, RoleSelectionActivity::class.java)
        )
        finish()
    }
}