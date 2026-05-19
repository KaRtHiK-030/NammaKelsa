package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerRequestsScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val workerId = currentUser.uid

    var requests by remember {
        mutableStateOf<List<Request>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        db.collection("requests")
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snap, _ ->

                requests =
                    snap?.toObjects(Request::class.java)
                        ?: emptyList()

                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .padding(16.dp)
    ) {

        Text(
            text = "Work Requests",
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

        if (requests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No requests yet")
            }
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(
                                Intent(
                                    context,
                                    HirerDetailActivity::class.java
                                ).apply {
                                    putExtra(
                                        "hirerId",
                                        request.hirerId
                                    )
                                }
                            )
                        },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = request.hirerName,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Location: ${request.hirerLocation}"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Phone: ${request.hirerPhone}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Work Message",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = request.workDetails
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Status: ${request.status}"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        when (request.status) {

                            "Pending" -> {
                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                    Button(
                                        onClick = {
                                            db.collection("requests")
                                                .document(request.requestId)
                                                .update(
                                                    "status",
                                                    "Accepted"
                                                )
                                        },
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {
                                        Text("Accept")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            db.collection("requests")
                                                .document(request.requestId)
                                                .update(
                                                    "status",
                                                    "Rejected"
                                                )
                                        },
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {
                                        Text("Reject")
                                    }
                                }
                            }

                            "Accepted" -> {
                                Button(
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                ChatActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    "receiverId",
                                                    request.hirerId
                                                )
                                            }
                                        )
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                ) {
                                    Text("Chat Hirer")
                                }
                            }

                            "Rejected" -> {
                                OutlinedButton(
                                    onClick = {},
                                    modifier =
                                        Modifier.fillMaxWidth()
                                ) {
                                    Text("Rejected")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}