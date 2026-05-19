package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen() {

    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val userId = currentUser.uid

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var skillsList by remember {
        mutableStateOf<List<Map<String, String>>>(emptyList())
    }

    var availability by remember {
        mutableStateOf("Available")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var deletePassword by remember {
        mutableStateOf("")
    }

    var deleting by remember {
        mutableStateOf(false)
    }

    var newSkill by remember {
        mutableStateOf("")
    }

    var newCharge by remember {
        mutableStateOf("")
    }

    val listState = rememberLazyListState()

    fun loadProfile() {
        db.collection("workers")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                name = document.getString("name") ?: ""
                location = document.getString("location") ?: ""
                phone = document.getString("phoneNumber") ?: ""
                whatsapp =
                    document.getString("whatsappNumber") ?: ""

                availability =
                    document.getString("availability")
                        ?: "Available"

                val rawSkills =
                    document.get("skillsList") as? List<*>

                skillsList =
                    rawSkills?.mapNotNull {
                        it as? Map<String, String>
                    } ?: emptyList()

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun logout() {
        auth.signOut()

        context.startActivity(
            Intent(
                context,
                RoleSelectionActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )

        activity?.finish()
    }

    fun deleteAccount() {

        if (deletePassword.isBlank()) {
            Toast.makeText(
                context,
                "Enter password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val email = currentUser.email ?: return

        deleting = true

        val credential =
            EmailAuthProvider.getCredential(
                email,
                deletePassword
            )

        currentUser.reauthenticate(credential)
            .addOnSuccessListener {

                db.collection("workers")
                    .document(userId)
                    .delete()

                db.collection("presence")
                    .document(userId)
                    .delete()

                currentUser.delete()
                    .addOnSuccessListener {
                        deleting = false
                        logout()
                    }
            }
            .addOnFailureListener {
                deleting = false

                Toast.makeText(
                    context,
                    "Wrong password",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Account")
            },
            text = {
                Column {

                    Text(
                        "Enter password to delete account permanently."
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = {
                            deletePassword = it
                        },
                        visualTransformation =
                            PasswordVisualTransformation(),
                        label = {
                            Text("Password")
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme.colorScheme.error
                    )
                ) {
                    if (deleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBgBrush()),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush()),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 220.dp
        ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        item {
            Text(
                "My Profile",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    InitialAvatar(
                        name = name,
                        size = 140.dp
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        name.ifBlank { "Worker" },
                        style =
                            MaterialTheme.typography
                                .headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        FilterChip(
                            selected =
                                availability == "Available",
                            onClick = {
                                availability = "Available"

                                db.collection("workers")
                                    .document(userId)
                                    .set(
                                        mapOf(
                                            "availability" to
                                                    "Available"
                                        ),
                                        SetOptions.merge()
                                    )
                            },
                            label = {
                                Text("Available")
                            }
                        )

                        FilterChip(
                            selected =
                                availability == "Busy",
                            onClick = {
                                availability = "Busy"

                                db.collection("workers")
                                    .document(userId)
                                    .set(
                                        mapOf(
                                            "availability" to
                                                    "Busy"
                                        ),
                                        SetOptions.merge()
                                    )
                            },
                            label = {
                                Text("Busy")
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    ProfileInfoRow(
                        Icons.Default.LocationOn,
                        "Location",
                        location
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    ProfileInfoRow(
                        Icons.Default.Phone,
                        "Phone",
                        phone
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    ProfileInfoRow(
                        Icons.Default.Chat,
                        "WhatsApp",
                        whatsapp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {

            Button(
                onClick = {
                    context.startActivity(
                        Intent(
                            context,
                            ProfileActivity::class.java
                        ).apply {
                            putExtra("role", "worker")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, null)

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Edit Profile")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {
                    context.startActivity(
                        Intent(
                            context,
                            WorkerReviewsActivity::class.java
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Star, null)

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("View Reviews")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }

        item {

            Text(
                "Add Skill",
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = newSkill,
                onValueChange = {
                    newSkill = it
                },
                label = {
                    Text("Skill")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = newCharge,
                onValueChange = {
                    newCharge =
                        it.filter { c ->
                            c.isDigit()
                        }
                },
                label = {
                    Text("Daily Charge")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {

                    if (
                        newSkill.isBlank() ||
                        newCharge.isBlank()
                    ) {
                        Toast.makeText(
                            context,
                            "Enter skill and charge",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val updated =
                        skillsList.toMutableList()

                    updated.add(
                        mapOf(
                            "skill" to newSkill,
                            "charge" to newCharge
                        )
                    )

                    db.collection("workers")
                        .document(userId)
                        .set(
                            mapOf(
                                "skillsList" to updated
                            ),
                            SetOptions.merge()
                        )
                        .addOnSuccessListener {
                            skillsList = updated
                            newSkill = ""
                            newCharge = ""
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Skill")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }

        item {
            Text(
                "My Skills",
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        itemsIndexed(skillsList) { index, skill ->

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(skill["skill"] ?: "")
                        Text("₹${skill["charge"]}/day")
                    }

                    IconButton(
                        onClick = {

                            val updated =
                                skillsList.toMutableList()

                            updated.removeAt(index)

                            db.collection("workers")
                                .document(userId)
                                .set(
                                    mapOf(
                                        "skillsList" to updated
                                    ),
                                    SetOptions.merge()
                                )
                                .addOnSuccessListener {
                                    skillsList = updated
                                }
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null
                        )
                    }
                }
            }
        }

        item {

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedButton(
                onClick = {
                    logout()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Account")
            }
        }
    }
}