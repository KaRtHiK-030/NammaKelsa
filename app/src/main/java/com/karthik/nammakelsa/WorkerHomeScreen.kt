package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.karthik.nammakelsa.ui.theme.SuccessGreen

@Composable
fun WorkerHomeScreen() {

    val context = LocalContext.current
    val activity = context as? Activity
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            activity?.finish()
        }
        return
    }

    val userId = currentUser.uid
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("Worker") }
    var availability by remember { mutableStateOf("Available") }
    var pendingRequests by remember { mutableStateOf(0) }
    var averageRating by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("workers")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                name = doc.getString("name") ?: "Worker"
                availability = doc.getString("availability") ?: "Available"
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    DisposableEffect(Unit) {

        val requestListener: ListenerRegistration =
            db.collection("requests")
                .whereEqualTo("workerId", userId)
                .whereEqualTo("status", "Pending")
                .addSnapshotListener { snap, _ ->
                    pendingRequests = snap?.size() ?: 0
                }

        val reviewListener: ListenerRegistration =
            db.collection("reviews")
                .whereEqualTo("workerId", userId)
                .addSnapshotListener { snap, _ ->

                    val reviews =
                        snap?.toObjects(Review::class.java)
                            ?: emptyList()

                    averageRating =
                        if (reviews.isNotEmpty()) {
                            reviews.map { it.rating }
                                .average()
                                .toFloat()
                        } else {
                            0f
                        }
                }

        onDispose {
            requestListener.remove()
            reviewListener.remove()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush()),
        contentPadding = PaddingValues(20.dp)
    ) {

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {

            item {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Current Status",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = availability,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when (availability) {
                                    "Offline" -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> SuccessGreen
                                }
                            )
                        }

                        Switch(
                            checked = availability == "Available",
                            onCheckedChange = { checked ->

                                val newStatus =
                                    if (checked) "Available"
                                    else "Offline"

                                availability = newStatus

                                db.collection("workers")
                                    .document(userId)
                                    .set(
                                        mapOf("availability" to newStatus),
                                        SetOptions.merge()
                                    )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                context.startActivity(
                                    Intent(
                                        context,
                                        HomeActivity::class.java
                                    ).apply {
                                        putExtra("role", "worker")
                                        putExtra("openTab", "requests")
                                    }
                                )
                            },
                        title = "New Requests",
                        value = pendingRequests.toString(),
                        icon = Icons.Default.Notifications,
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                context.startActivity(
                                    Intent(
                                        context,
                                        WorkerReviewsActivity::class.java
                                    )
                                )
                            },
                        title = "Avg Rating",
                        value =
                            if (averageRating > 0f)
                                "%.1f".format(averageRating)
                            else "--",
                        icon = Icons.Default.Star,
                        color = Color(0xFFFFC107)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Pro Tip",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Keep your availability updated to receive more work requests."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}