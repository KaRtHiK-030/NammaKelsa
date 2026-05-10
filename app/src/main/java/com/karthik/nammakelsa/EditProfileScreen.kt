package com.karthik.nammakelsa

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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

    val userId = FirebaseAuth
        .getInstance()
        .currentUser
        ?.uid ?: ""

    val collectionName =
        if (role == "worker") "workers"
        else "hirers"

    var name by remember {
        mutableStateOf("")
    }

    var skill by remember {
        mutableStateOf("")
    }

    var location by remember {
        mutableStateOf("")
    }

    var charge by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var whatsapp by remember {
        mutableStateOf("")
    }

    // LOAD EXISTING DATA
    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection(collectionName)
            .document(userId)
            .get()

            .addOnSuccessListener { document ->

                name = document.getString("name") ?: ""
                location = document.getString("location") ?: ""
                phone = document.getString("phoneNumber") ?: ""
                whatsapp = document.getString("whatsappNumber") ?: ""

                if (role == "worker") {

                    skill = document.getString("skill") ?: ""
                    charge = document.getString("chargePerDay") ?: ""
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 120.dp
            )
    ) {

        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        // NAME
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },

            label = {
                Text("Name")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // WORKER ONLY
        if (role == "worker") {

            OutlinedTextField(
                value = skill,
                onValueChange = {
                    skill = it
                },

                label = {
                    Text("Skill")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // LOCATION
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

        Spacer(modifier = Modifier.height(10.dp))

        // PHONE
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
            },

            label = {
                Text("Phone Number")
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // WHATSAPP
        OutlinedTextField(
            value = whatsapp,
            onValueChange = {
                whatsapp = it
            },

            label = {
                Text("WhatsApp Number")
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // CHARGE ONLY FOR WORKER
        if (role == "worker") {

            OutlinedTextField(
                value = charge,
                onValueChange = {
                    charge = it
                },

                label = {
                    Text("Charge Per Day")
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SAVE BUTTON
        Button(
            onClick = {

                val updatedData =
                    if (role == "worker") {

                        mapOf(
                            "userId" to userId,
                            "role" to role,
                            "name" to name,
                            "skill" to skill,
                            "location" to location,
                            "chargePerDay" to charge,
                            "phoneNumber" to phone,
                            "whatsappNumber" to whatsapp
                        )

                    } else {

                        mapOf(
                            "userId" to userId,
                            "role" to role,
                            "name" to name,
                            "location" to location,
                            "phoneNumber" to phone,
                            "whatsappNumber" to whatsapp
                        )
                    }

                FirebaseFirestore
                    .getInstance()
                    .collection(collectionName)
                    .document(userId)
                    .set(
                        updatedData,
                        SetOptions.merge()
                    )

                    .addOnSuccessListener {

                        Toast.makeText(
                            context,
                            "Profile Updated ✅",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    .addOnFailureListener {

                        Toast.makeText(
                            context,
                            "Error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Save Changes")
        }
    }
}