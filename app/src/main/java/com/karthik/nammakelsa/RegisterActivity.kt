package com.karthik.nammakelsa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NammaKelsaTheme {
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var selectedRole by remember {
        mutableStateOf("worker")
    }

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var skill by remember { mutableStateOf("") }
    var dailyCharge by remember { mutableStateOf("") }

    var whatsappSameAsPhone by remember {
        mutableStateOf(false)
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(
                id = R.drawable.namma_kelsa_logo
            ),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            FilterChip(
                selected = selectedRole == "worker",
                onClick = {
                    selectedRole = "worker"
                },
                label = {
                    Text("Worker")
                },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedRole == "hirer",
                onClick = {
                    selectedRole = "hirer"
                },
                label = {
                    Text("Hirer")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text("Full Name")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it
            },
            label = {
                Text("Location")
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedRole == "worker") {

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = skill,
                onValueChange = {
                    skill = it
                },
                label = {
                    Text("Skill (e.g. Mason, Painter, Electrician)")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dailyCharge,
                onValueChange = {
                    dailyCharge =
                        it.filter { c -> c.isDigit() }
                },
                label = {
                    Text("Daily Charge (₹)")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                val filtered =
                    it.filter { c -> c.isDigit() }

                phone = filtered

                if (whatsappSameAsPhone) {
                    whatsapp = filtered
                }
            },
            label = {
                Text("Phone Number")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            Checkbox(
                checked = whatsappSameAsPhone,
                onCheckedChange = { checked ->
                    whatsappSameAsPhone = checked

                    if (checked) {
                        whatsapp = phone
                    } else {
                        whatsapp = ""
                    }
                }
            )

            Text("WhatsApp same as phone number")
        }

        if (!whatsappSameAsPhone) {

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = whatsapp,
                onValueChange = {
                    whatsapp =
                        it.filter { c -> c.isDigit() }
                },
                label = {
                    Text("WhatsApp Number")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it.trim()
            },
            label = {
                Text("Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            visualTransformation =
                if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        imageVector =
                            if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {

                if (
                    name.isBlank() ||
                    location.isBlank() ||
                    phone.isBlank() ||
                    whatsapp.isBlank() ||
                    email.isBlank() ||
                    password.length < 6
                ) {
                    Toast.makeText(
                        context,
                        "Fill all fields correctly",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (
                    selectedRole == "worker" &&
                    (skill.isBlank() || dailyCharge.isBlank())
                ) {
                    Toast.makeText(
                        context,
                        "Add skill and daily charge",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                isLoading = true

                auth.createUserWithEmailAndPassword(
                    email,
                    password
                )
                    .addOnSuccessListener { result ->

                        val userId =
                            result.user?.uid
                                ?: run {
                                    isLoading = false
                                    return@addOnSuccessListener
                                }

                        val collection =
                            if (selectedRole == "worker")
                                "workers"
                            else
                                "hirers"

                        val userData =
                            mutableMapOf<String, Any>(
                                "userId" to userId,
                                "name" to name,
                                "location" to location,
                                "phoneNumber" to phone,
                                "whatsappNumber" to whatsapp,
                                "email" to email,
                                "role" to selectedRole
                            )

                        if (selectedRole == "worker") {
                            userData["availability"] =
                                "Available"

                            userData["skillsList"] =
                                listOf(
                                    mapOf(
                                        "skill" to skill,
                                        "charge" to dailyCharge
                                    )
                                )
                        }

                        firestore.collection(collection)
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {

                                isLoading = false

                                context.startActivity(
                                    Intent(
                                        context,
                                        HomeActivity::class.java
                                    ).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                )
                            }
                    }
                    .addOnFailureListener { e ->

                        isLoading = false

                        val message =
                            if (
                                e is FirebaseAuthUserCollisionException
                            ) {
                                "Email already registered. Please login."
                            } else {
                                "Registration failed: ${e.message}"
                            }

                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        LoginActivity::class.java
                    )
                )
            }
        ) {
            Text("Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}