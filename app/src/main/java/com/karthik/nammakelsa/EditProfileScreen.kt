package com.karthik.nammakelsa

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun EditProfileScreen(role: String) {

    val context = LocalContext.current
    val activity = context as? Activity
    val db = FirebaseFirestore.getInstance()
    val currentUser =
        FirebaseAuth.getInstance().currentUser ?: return

    val userId = currentUser.uid

    val collection =
        if (role == "worker")
            "workers"
        else
            "hirers"

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }

    var whatsappSameAsPhone by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var saving by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        db.collection(collection)
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->

                name =
                    doc.getString("name") ?: ""

                location =
                    doc.getString("location") ?: ""

                phone =
                    doc.getString("phoneNumber") ?: ""

                whatsapp =
                    doc.getString("whatsappNumber") ?: ""

                whatsappSameAsPhone =
                    phone.isNotBlank() &&
                            phone == whatsapp

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBgBrush()),
            contentAlignment =
                androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp)
    ) {

        Text(
            text = "Edit Profile",
            style =
                MaterialTheme.typography
                    .headlineMedium
        )

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
            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = whatsappSameAsPhone,
                onCheckedChange = { checked ->
                    whatsappSameAsPhone = checked

                    if (checked) {
                        whatsapp = phone
                    }
                }
            )

            Text(
                "WhatsApp same as phone"
            )
        }

        if (!whatsappSameAsPhone) {

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = whatsapp,
                onValueChange = {
                    whatsapp =
                        it.filter { c ->
                            c.isDigit()
                        }
                },
                label = {
                    Text("WhatsApp Number")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType =
                        KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {

                if (
                    name.isBlank() ||
                    location.isBlank() ||
                    phone.isBlank() ||
                    whatsapp.isBlank()
                ) {
                    Toast.makeText(
                        context,
                        "Fill all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                saving = true

                db.collection(collection)
                    .document(userId)
                    .set(
                        mapOf(
                            "name" to name,
                            "location" to location,
                            "phoneNumber" to phone,
                            "whatsappNumber" to whatsapp
                        ),
                        SetOptions.merge()
                    )
                    .addOnSuccessListener {
                        saving = false

                        Toast.makeText(
                            context,
                            "Profile updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        activity?.finish()
                    }
                    .addOnFailureListener {
                        saving = false

                        Toast.makeText(
                            context,
                            "Update failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Save Changes")
            }
        }
    }
}