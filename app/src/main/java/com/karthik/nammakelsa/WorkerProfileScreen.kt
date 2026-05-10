package com.karthik.nammakelsa

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerProfileScreen() {

    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    val userId = FirebaseAuth
        .getInstance()
        .currentUser
        ?.uid ?: ""

    var name by remember {
        mutableStateOf("")
    }

    var location by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var whatsapp by remember {
        mutableStateOf("")
    }

    var imageUrl by remember {
        mutableStateOf("")
    }

    var skillsList by remember {
        mutableStateOf(listOf<Map<String, String>>())
    }

    var availability by remember {
        mutableStateOf("Available")
    }

    val listState = rememberLazyListState()

    // LOAD PROFILE
    fun loadProfile() {

        db.collection("workers")
            .document(userId)
            .get()

            .addOnSuccessListener { document ->

                name =
                    document.getString("name") ?: ""

                location =
                    document.getString("location") ?: ""

                phone =
                    document.getString("phoneNumber") ?: ""

                whatsapp =
                    document.getString("whatsappNumber") ?: ""

                imageUrl =
                    document.getString("imageUrl") ?: ""

                availability =
                    document.getString("availability")
                        ?: "Available"

                skillsList =
                    document.get("skillsList")
                            as? List<Map<String, String>>
                        ?: emptyList()
            }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    LazyColumn(

        state = listState,

        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),

        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 220.dp
        ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // PROFILE SECTION
        item {

            ElevatedCard(

                shape = RoundedCornerShape(24.dp),

                modifier = Modifier.fillMaxWidth()
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Image(
                        painter =
                            rememberAsyncImagePainter(

                                if (imageUrl.isNotEmpty())
                                    imageUrl
                                else
                                    "https://i.imgur.com/8Km9tLL.png"
                            ),

                        contentDescription = null,

                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),

                        contentScale = ContentScale.Crop
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = name,

                        style = MaterialTheme
                            .typography
                            .headlineSmall
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "📍 $location"
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "📞 $phone"
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "💬 $whatsapp"
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(

                        text = when (availability) {

                            "Busy" -> "🔴 Busy"

                            "Offline" -> "⚫ Offline"

                            else -> "🟢 Available"
                        },

                        style = MaterialTheme
                            .typography
                            .titleMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "My Skills",

                style = MaterialTheme
                    .typography
                    .headlineSmall
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )
        }

        // SKILLS LIST
        itemsIndexed(skillsList) { index, skill ->

            ElevatedCard(

                shape = RoundedCornerShape(18.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            text =
                                "🛠 ${skill["skill"]}",

                            style = MaterialTheme
                                .typography
                                .titleLarge
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "₹ ${skill["charge"]}/day"
                        )
                    }

                    // DELETE BUTTON
                    IconButton(

                        onClick = {

                            val updatedList =
                                skillsList.toMutableList()

                            updatedList.removeAt(index)

                            db.collection("workers")
                                .document(userId)
                                .update(
                                    "skillsList",
                                    updatedList
                                )

                                .addOnSuccessListener {

                                    skillsList = updatedList
                                }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,

                            contentDescription = null
                        )
                    }
                }
            }
        }

        // ACTION BUTTONS
        item {

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(

                onClick = {

                    val intent = Intent(
                        context,
                        ProfileActivity::class.java
                    )

                    intent.putExtra(
                        "role",
                        "worker"
                    )

                    context.startActivity(intent)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                Text("Edit Profile")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(

                onClick = {

                    val intent = Intent(
                        context,
                        AddSkillActivity::class.java
                    )

                    context.startActivity(intent)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                Text("Add New Skill")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Change Availability",

                style = MaterialTheme
                    .typography
                    .titleMedium
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                FilledTonalButton(
                    onClick = {

                        availability = "Available"

                        db.collection("workers")
                            .document(userId)
                            .update(
                                "availability",
                                "Available"
                            )
                    }
                ) {

                    Text("🟢")
                }

                FilledTonalButton(
                    onClick = {

                        availability = "Busy"

                        db.collection("workers")
                            .document(userId)
                            .update(
                                "availability",
                                "Busy"
                            )
                    }
                ) {

                    Text("🔴")
                }

                FilledTonalButton(
                    onClick = {

                        availability = "Offline"

                        db.collection("workers")
                            .document(userId)
                            .update(
                                "availability",
                                "Offline"
                            )
                    }
                ) {

                    Text("⚫")
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // LOGOUT BUTTON
            OutlinedButton(

                onClick = {

                    FirebaseAuth
                        .getInstance()
                        .signOut()

                    val intent = Intent(
                        context,
                        RoleSelectionActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    context.startActivity(intent)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                Text("Logout")
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // DELETE ACCOUNT BUTTON
            Button(

                onClick = {

                    val user =
                        FirebaseAuth
                            .getInstance()
                            .currentUser

                    // DELETE FIRESTORE DATA
                    db.collection("workers")
                        .document(userId)
                        .delete()

                        .addOnSuccessListener {

                            // DELETE AUTH ACCOUNT
                            user?.delete()

                                ?.addOnSuccessListener {

                                    Toast.makeText(
                                        context,
                                        "Account Deleted",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val intent = Intent(
                                        context,
                                        RoleSelectionActivity::class.java
                                    )

                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                                    context.startActivity(intent)
                                }
                        }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {

                Text("Delete Account")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}