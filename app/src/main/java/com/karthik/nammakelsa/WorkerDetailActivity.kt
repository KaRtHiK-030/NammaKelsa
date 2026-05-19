package com.karthik.nammakelsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class WorkerDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workerId = intent.getStringExtra("workerId") ?: ""

        setContent {
            NammaKelsaTheme {
                WorkerDetailScreen(workerId)
            }
        }
    }
}

@Composable
fun WorkerDetailScreen(workerId: String) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var workerImageUrl by remember { mutableStateOf("") }

    var skills by remember {
        mutableStateOf<List<Map<String, String>>>(emptyList())
    }

    var reviews by remember {
        mutableStateOf<List<Review>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isSaved by remember {
        mutableStateOf(false)
    }

    var showRequestDialog by remember {
        mutableStateOf(false)
    }

    var requestMessage by remember {
        mutableStateOf("")
    }

    var editingReview by remember {
        mutableStateOf<Review?>(null)
    }

    var editedComment by remember {
        mutableStateOf("")
    }

    var editedRating by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(workerId) {
        if (workerId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("workers")
            .document(workerId)
            .get()
            .addOnSuccessListener { doc ->

                name = doc.getString("name") ?: "Worker"
                location = doc.getString("location") ?: ""
                phone = doc.getString("phoneNumber") ?: ""
                whatsapp = doc.getString("whatsappNumber") ?: ""
                availability = doc.getString("availability") ?: "Available"
                workerImageUrl = doc.getString("imageUrl") ?: ""

                val rawSkills = doc.get("skillsList") as? List<*>
                skills =
                    rawSkills?.mapNotNull {
                        it as? Map<String, String>
                    } ?: emptyList()

                if (currentUserId.isNotBlank()) {
                    db.collection("savedWorkers")
                        .document("${currentUserId}_$workerId")
                        .get()
                        .addOnSuccessListener {
                            isSaved = it.exists()
                        }
                }

                isLoading = false
            }
    }

    DisposableEffect(workerId) {
        val listener =
            db.collection("reviews")
                .whereEqualTo("workerId", workerId)
                .addSnapshotListener { snap, _ ->
                    reviews =
                        snap?.toObjects(Review::class.java)
                            ?: emptyList()
                }

        onDispose {
            listener.remove()
        }
    }

    if (editingReview != null) {
        AlertDialog(
            onDismissRequest = {
                editingReview = null
            },
            title = {
                Text("Edit Review")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedComment,
                        onValueChange = {
                            editedComment = it
                        },
                        label = {
                            Text("Comment")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editedRating.toString(),
                        onValueChange = {
                            editedRating =
                                it.toFloatOrNull() ?: 0f
                        },
                        label = {
                            Text("Rating")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("reviews")
                            .document(editingReview!!.reviewId)
                            .update(
                                mapOf(
                                    "comment" to editedComment,
                                    "rating" to editedRating
                                )
                            )

                        editingReview = null
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingReview = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = {
                showRequestDialog = false
            },
            title = {
                Text("Send Work Request")
            },
            text = {
                OutlinedTextField(
                    value = requestMessage,
                    onValueChange = {
                        requestMessage = it
                    },
                    label = {
                        Text("Enter work details")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentUserId.isNotBlank()) {
                            db.collection("hirers")
                                .document(currentUserId)
                                .get()
                                .addOnSuccessListener { userDoc ->

                                    val requestId =
                                        db.collection("requests")
                                            .document()
                                            .id

                                    val primarySkill =
                                        if (skills.isNotEmpty())
                                            skills.first()["skill"] ?: ""
                                        else
                                            ""

                                    val request = Request(
                                        requestId = requestId,
                                        workerId = workerId,
                                        workerName = name,
                                        workerImage = workerImageUrl,
                                        workerSkill = primarySkill,
                                        workerLocation = location,
                                        workerAvailability = availability,
                                        hirerId = currentUserId,
                                        hirerName = userDoc.getString("name") ?: "",
                                        hirerImage = userDoc.getString("imageUrl") ?: "",
                                        hirerLocation = userDoc.getString("location") ?: "",
                                        hirerPhone = userDoc.getString("phoneNumber") ?: "",
                                        hirerWhatsapp = userDoc.getString("whatsappNumber") ?: "",
                                        workDetails = requestMessage,
                                        status = "Pending",
                                        timestamp = System.currentTimeMillis()
                                    )

                                    db.collection("requests")
                                        .document(requestId)
                                        .set(request)

                                    requestMessage = ""
                                    showRequestDialog = false
                                }
                        }
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRequestDialog = false
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
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {

        item {
            InitialAvatar(name = name, size = 140.dp)

            Spacer(Modifier.height(16.dp))

            Text(
                name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(availability)

            Spacer(Modifier.height(20.dp))
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(20.dp)
                ) {
                    ProfileInfoRow(Icons.Default.Phone, "Phone", phone)
                    Spacer(Modifier.height(12.dp))
                    ProfileInfoRow(Icons.Default.Chat, "WhatsApp", whatsapp)
                    Spacer(Modifier.height(12.dp))
                    ProfileInfoRow(Icons.Default.Phone, "Location", location)
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        item {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                ChatActivity::class.java
                            ).apply {
                                putExtra("receiverId", workerId)
                            }
                        )
                    }
                ) {
                    Icon(Icons.Default.Chat, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Chat")
                }

                Button(
                    onClick = {
                        if (phone.isNotBlank()) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:$phone")
                                )
                            )
                        }
                    }
                ) {
                    Icon(Icons.Default.Phone, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Call")
                }

                Button(
                    onClick = {
                        showRequestDialog = true
                    }
                ) {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Request")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (isSaved) Color.Red
                            else MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        if (!isSaved) {
                            db.collection("savedWorkers")
                                .document("${currentUserId}_$workerId")
                                .set(
                                    mapOf(
                                        "hirerId" to currentUserId,
                                        "workerId" to workerId
                                    )
                                )
                            isSaved = true
                        }
                    }
                ) {
                    Icon(Icons.Default.Favorite, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isSaved) "Saved" else "Save")
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        item {
            Text(
                "Skills",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))
        }

        items(skills) { skill ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    Text(skill["skill"] ?: "")
                    Text("₹${skill["charge"]}/day")
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))

            Text(
                "Reviews",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))
        }

        items(reviews) { review ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    Text(
                        review.reviewerName,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null)
                        Spacer(Modifier.width(4.dp))
                        Text(review.rating.toString())
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(review.comment)

                    if (review.userId == currentUserId) {
                        Spacer(Modifier.height(10.dp))

                        Row {
                            IconButton(
                                onClick = {
                                    editingReview = review
                                    editedComment = review.comment
                                    editedRating = review.rating
                                }
                            ) {
                                Icon(Icons.Default.Edit, null)
                            }

                            IconButton(
                                onClick = {
                                    db.collection("reviews")
                                        .document(review.reviewId)
                                        .delete()
                                }
                            ) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }

                    if (review.reply.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))

                        Card {
                            Text(
                                "Worker reply: ${review.reply}",
                                modifier =
                                    Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}