package com.karthik.nammakelsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class WorkerReviewsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NammaKelsaTheme {
                WorkerReviewsScreen()
            }
        }
    }
}

@Composable
fun WorkerReviewsScreen() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val workerId = currentUser.uid

    var reviews by remember {
        mutableStateOf<List<Review>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var replyingReviewId by remember {
        mutableStateOf("")
    }

    var replyText by remember {
        mutableStateOf("")
    }

    fun loadReviews() {
        db.collection("reviews")
            .whereEqualTo("workerId", workerId)
            .get()
            .addOnSuccessListener { snap ->

                reviews =
                    snap.documents.map { doc ->
                        Review(
                            reviewId =
                                doc.getString("reviewId")
                                    ?: doc.id,
                            workerId =
                                doc.getString("workerId")
                                    ?: "",
                            userId =
                                doc.getString("userId")
                                    ?: "",
                            reviewerName =
                                doc.getString("reviewerName")
                                    ?: "Anonymous",
                            rating =
                                (doc.getDouble("rating")
                                    ?: 0.0).toFloat(),
                            comment =
                                doc.getString("comment")
                                    ?: "",
                            timestamp =
                                doc.getLong("timestamp")
                                    ?: 0L,
                            reply =
                                doc.getString("reply")
                                    ?: "",
                            reactionCount =
                                (doc.getLong("reactionCount")
                                    ?: 0L).toInt(),
                            reactedUsers =
                                doc.get("reactedUsers")
                                        as? List<String>
                                    ?: emptyList()
                        )
                    }

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        loadReviews()
    }

    if (replyingReviewId.isNotBlank()) {
        AlertDialog(
            onDismissRequest = {
                replyingReviewId = ""
            },
            title = {
                Text("Reply to Review")
            },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = {
                        replyText = it
                    },
                    label = {
                        Text("Your reply")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("reviews")
                            .document(replyingReviewId)
                            .update(
                                "reply",
                                replyText
                            )
                            .addOnSuccessListener {
                                replyingReviewId = ""
                                replyText = ""
                                loadReviews()
                            }
                    }
                ) {
                    Text("Reply")
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
            "My Reviews",
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

        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No reviews yet")
            }
            return@Column
        }

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(reviews) { review ->

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier =
                            Modifier.padding(16.dp)
                    ) {

                        Text(
                            review.reviewerName,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                null
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(4.dp)
                            )

                            Text(
                                review.rating.toString()
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(review.comment)

                        if (review.reply.isNotBlank()) {
                            Spacer(
                                modifier =
                                    Modifier.height(10.dp)
                            )

                            Card {
                                Text(
                                    "Your Reply: ${review.reply}",
                                    modifier =
                                        Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Row {

                            IconButton(
                                onClick = {
                                    replyingReviewId =
                                        review.reviewId

                                    replyText =
                                        review.reply
                                }
                            ) {
                                Icon(
                                    Icons.Default.Reply,
                                    null
                                )
                            }

                            IconButton(
                                onClick = {

                                    if (
                                        !review.reactedUsers.contains(
                                            workerId
                                        )
                                    ) {
                                        db.collection("reviews")
                                            .document(
                                                review.reviewId
                                            )
                                            .update(
                                                mapOf(
                                                    "reactionCount" to
                                                            FieldValue.increment(
                                                                1
                                                            ),
                                                    "reactedUsers" to
                                                            FieldValue.arrayUnion(
                                                                workerId
                                                            )
                                                )
                                            )
                                            .addOnSuccessListener {
                                                loadReviews()
                                            }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    null
                                )
                            }

                            Text(
                                "${review.reactionCount}",
                                modifier =
                                    Modifier.padding(
                                        top = 14.dp
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}