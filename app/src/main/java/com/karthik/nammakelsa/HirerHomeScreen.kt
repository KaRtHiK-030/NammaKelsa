package com.karthik.nammakelsa

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class WorkerCardData(
    val workerId: String = "",
    val name: String = "",
    val location: String = "",
    val availability: String = "Available"
)

@Composable
fun HirerHomeScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val hirerId = currentUser.uid

    var workers by remember {
        mutableStateOf<List<WorkerCardData>>(emptyList())
    }

    var hirerName by remember {
        mutableStateOf("Hirer")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var reviewWorkerId by remember {
        mutableStateOf("")
    }

    var rating by remember {
        mutableStateOf("")
    }

    var comment by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        db.collection("hirers")
            .document(hirerId)
            .get()
            .addOnSuccessListener {
                hirerName =
                    it.getString("name") ?: "Hirer"
            }

        db.collection("workers")
            .addSnapshotListener { snap, _ ->

                workers = snap?.documents?.map { doc ->
                    WorkerCardData(
                        workerId = doc.id,
                        name = doc.getString("name") ?: "Worker",
                        location = doc.getString("location") ?: "",
                        availability =
                            doc.getString("availability")
                                ?: "Available"
                    )
                } ?: emptyList()

                isLoading = false
            }
    }

    if (reviewWorkerId.isNotBlank()) {
        AlertDialog(
            onDismissRequest = {
                reviewWorkerId = ""
            },
            title = {
                Text("Rate Worker")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = rating,
                        onValueChange = {
                            rating = it
                        },
                        label = {
                            Text("Rating (1-5)")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = comment,
                        onValueChange = {
                            comment = it
                        },
                        label = {
                            Text("Review")
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {

                        val ratingValue =
                            rating.toFloatOrNull()

                        if (
                            ratingValue == null ||
                            ratingValue < 1f ||
                            ratingValue > 5f
                        ) {
                            Toast.makeText(
                                context,
                                "Invalid rating",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val reviewRef =
                            db.collection("reviews")
                                .document()

                        reviewRef.set(
                            Review(
                                reviewId = reviewRef.id,
                                workerId = reviewWorkerId,
                                userId = hirerId,
                                reviewerName = hirerName,
                                rating = ratingValue,
                                comment = comment,
                                timestamp = System.currentTimeMillis()
                            )
                        )

                        reviewWorkerId = ""
                        rating = ""
                        comment = ""
                    }
                ) {
                    Text("Submit")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .padding(16.dp)
    ) {

        Text(
            "Find Workers",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(workers) { worker ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(
                                Intent(
                                    context,
                                    WorkerDetailActivity::class.java
                                ).apply {
                                    putExtra(
                                        "workerId",
                                        worker.workerId
                                    )
                                }
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            worker.name,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(worker.location)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(worker.availability)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row {

                            IconButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            ChatActivity::class.java
                                        ).apply {
                                            putExtra(
                                                "receiverId",
                                                worker.workerId
                                            )
                                        }
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Chat, null)
                            }

                            IconButton(
                                onClick = {
                                    reviewWorkerId =
                                        worker.workerId
                                }
                            ) {
                                Icon(Icons.Default.Star, null)
                            }

                            IconButton(
                                onClick = {
                                    db.collection("savedWorkers")
                                        .document(
                                            "${hirerId}_${worker.workerId}"
                                        )
                                        .set(
                                            mapOf(
                                                "hirerId" to hirerId,
                                                "workerId" to worker.workerId
                                            )
                                        )
                                }
                            ) {
                                Icon(Icons.Default.Favorite, null)
                            }

                            IconButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            WorkerDetailActivity::class.java
                                        ).apply {
                                            putExtra(
                                                "workerId",
                                                worker.workerId
                                            )
                                        }
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Send, null)
                            }
                        }
                    }
                }
            }
        }
    }
}