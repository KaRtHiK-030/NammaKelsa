package com.karthik.nammakelsa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Entry-point activity. Reads the persisted auth + role and routes the user
 * to RoleSelection / MainActivity (auth) / ProfileActivity (profile creation) /
 * HomeActivity. Avoids forcing an authenticated user to pick their role on every cold start.
 */
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        route()
    }

    private fun route() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role")
                if (role.isNullOrBlank()) {
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }
                val collection = if (role == "worker") "workers" else "hirers"
                FirebaseFirestore.getInstance()
                    .collection(collection)
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { profileDoc ->
                        val intent = if (profileDoc.exists()) {
                            Intent(this, HomeActivity::class.java)
                                .putExtra("role", role)
                        } else {
                            Intent(this, ProfileActivity::class.java)
                                .putExtra("role", role)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        startActivity(Intent(this, RoleSelectionActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
                finish()
            }
    }
}
