package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HirerProfileScreen() {

    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            activity?.finish()
        }
        return
    }

    val userId = currentUser.uid

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var deletePassword by remember {
        mutableStateOf("")
    }

    var deleting by remember {
        mutableStateOf(false)
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

                firestore.collection("hirers")
                    .document(userId)
                    .delete()

                firestore.collection("presence")
                    .document(userId)
                    .delete()

                firestore.collection("chats")
                    .whereArrayContains(
                        "participants",
                        userId
                    )
                    .get()
                    .addOnSuccessListener { snap ->

                        val batch =
                            firestore.batch()

                        snap.documents.forEach {
                            batch.delete(it.reference)
                        }

                        batch.commit()
                            .addOnSuccessListener {

                                currentUser.delete()
                                    .addOnSuccessListener {
                                        deleting = false
                                        logout()
                                    }
                            }
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
        firestore.collection("hirers")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                location =
                    document.getString("location") ?: ""
                phone =
                    document.getString("phoneNumber") ?: ""
                whatsapp =
                    document.getString("whatsappNumber") ?: ""
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    BackHandler {
        activity?.finish()
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
                        "Enter password to permanently delete account."
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
                        },
                        modifier = Modifier.fillMaxWidth()
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
                OutlinedButton(
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
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .padding(16.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        contentPadding =
            PaddingValues(bottom = 120.dp)
    ) {

        item {
            Text(
                text = "My Profile",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    InitialAvatar(
                        name = name,
                        size = 120.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = name.ifBlank { "Hirer" },
                        style =
                            MaterialTheme.typography
                                .headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value =
                            location.ifBlank { "Not set" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value =
                            phone.ifBlank { "Not set" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp",
                        value =
                            whatsapp.ifBlank { "Not set" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    context.startActivity(
                        Intent(
                            context,
                            ProfileActivity::class.java
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    logout()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Logout")
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Delete Account")
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}