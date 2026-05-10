package com.karthik.nammakelsa

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun HirerProfileScreen() {

    val context = LocalContext.current

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

    val listState = rememberLazyListState()

    // LOAD PROFILE
    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("hirers")
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
            }
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
            bottom = 120.dp
        ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        item {

            // TITLE
            Text(
                text = "My Profile",

                style = MaterialTheme
                    .typography
                    .headlineMedium
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // PROFILE IMAGE
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
                modifier = Modifier.height(24.dp)
            )

            // PROFILE CARD
            ElevatedCard(

                shape = RoundedCornerShape(20.dp),

                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "👤 Name: $name",

                        style = MaterialTheme
                            .typography
                            .titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text =
                            "📍 Location: $location",

                        style = MaterialTheme
                            .typography
                            .titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text =
                            "📞 Phone: $phone",

                        style = MaterialTheme
                            .typography
                            .titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text =
                            "💬 WhatsApp: $whatsapp",

                        style = MaterialTheme
                            .typography
                            .titleMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // EDIT PROFILE BUTTON
            Button(

                onClick = {

                    val intent = Intent(
                        context,
                        ProfileActivity::class.java
                    )

                    intent.putExtra(
                        "role",
                        "hirer"
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
                modifier = Modifier.height(14.dp)
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

                    Toast.makeText(
                        context,
                        "Logged Out ✅",
                        Toast.LENGTH_LONG
                    ).show()
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
                    FirebaseFirestore
                        .getInstance()
                        .collection("hirers")
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

                colors = ButtonDefaults.buttonColors(),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                Text("Delete Account")
            }
        }
    }
}