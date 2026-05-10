package com.karthik.nammakelsa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // GET ROLE
        val role =
            intent.getStringExtra("role")
                ?: "worker"

        // ONLINE STATUS
        val userId =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

        if (userId != null) {

            FirebaseFirestore
                .getInstance()
                .collection("workers")
                .document(userId)

                .update(
                    "online",
                    true
                )
        }

        setContent {

            NammaKelsaTheme {

                AuthScreen(role)
            }
        }
    }

    // OFFLINE STATUS
    override fun onDestroy() {
        super.onDestroy()

        val userId =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

        if (userId != null) {

            FirebaseFirestore
                .getInstance()
                .collection("workers")
                .document(userId)

                .update(
                    "online",
                    false
                )
        }
    }
}

@Composable
fun AuthScreen(role: String) {

    val auth = FirebaseAuth.getInstance()

    val context = LocalContext.current

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(20.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Login / Register",

            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            shape = RoundedCornerShape(20.dp),

            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // EMAIL FIELD
                OutlinedTextField(
                    value = email,

                    onValueChange = {
                        email = it
                    },

                    label = {
                        Text("Email")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PASSWORD FIELD
                OutlinedTextField(
                    value = password,

                    onValueChange = {
                        password = it
                    },

                    label = {
                        Text("Password")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // REGISTER BUTTON
                Button(
                    onClick = {

                        auth.createUserWithEmailAndPassword(
                            email.trim(),
                            password.trim()
                        )

                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    val userId =
                                        auth.currentUser?.uid ?: ""

                                    // SAVE ROLE
                                    FirebaseFirestore
                                        .getInstance()
                                        .collection("users")
                                        .document(userId)
                                        .set(
                                            mapOf(
                                                "role" to role
                                            )
                                        )

                                    Toast.makeText(
                                        context,
                                        "Registered Successfully ✅",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // OPEN PROFILE
                                    val intent = Intent(
                                        context,
                                        ProfileActivity::class.java
                                    )

                                    intent.putExtra(
                                        "role",
                                        role
                                    )

                                    context.startActivity(intent)

                                } else {

                                    Toast.makeText(
                                        context,
                                        task.exception?.message
                                            ?: "Error",

                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Register")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LOGIN BUTTON
                OutlinedButton(
                    onClick = {

                        auth.signInWithEmailAndPassword(
                            email.trim(),
                            password.trim()
                        )

                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    val userId =
                                        auth.currentUser?.uid ?: ""

                                    // GET ROLE
                                    FirebaseFirestore
                                        .getInstance()
                                        .collection("users")
                                        .document(userId)
                                        .get()

                                        .addOnSuccessListener { userDoc ->

                                            val savedRole =
                                                userDoc.getString("role")
                                                    ?: "worker"

                                            val collectionName =
                                                if (savedRole == "worker")
                                                    "workers"
                                                else
                                                    "hirers"

                                            // ONLINE STATUS
                                            FirebaseFirestore
                                                .getInstance()
                                                .collection(collectionName)
                                                .document(userId)

                                                .update(
                                                    "online",
                                                    true
                                                )

                                            // CHECK PROFILE EXISTS
                                            FirebaseFirestore
                                                .getInstance()
                                                .collection(collectionName)
                                                .document(userId)
                                                .get()

                                                .addOnSuccessListener { document ->

                                                    if (document.exists()) {

                                                        // OPEN HOME
                                                        val intent = Intent(
                                                            context,
                                                            HomeActivity::class.java
                                                        )

                                                        intent.putExtra(
                                                            "role",
                                                            savedRole
                                                        )

                                                        context.startActivity(intent)

                                                    } else {

                                                        // OPEN PROFILE CREATION
                                                        val intent = Intent(
                                                            context,
                                                            ProfileActivity::class.java
                                                        )

                                                        intent.putExtra(
                                                            "role",
                                                            savedRole
                                                        )

                                                        context.startActivity(intent)
                                                    }
                                                }
                                        }

                                } else {

                                    Toast.makeText(
                                        context,
                                        task.exception?.message
                                            ?: "Error",

                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Login")
                }
            }
        }
    }
}