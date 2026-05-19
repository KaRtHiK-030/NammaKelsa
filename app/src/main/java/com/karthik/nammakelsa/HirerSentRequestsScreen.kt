package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
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
fun HirerSentRequestsScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val hirerId = currentUser.uid

    var requests by remember {
        mutableStateOf<List<Request>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var editRequest by remember {
        mutableStateOf<Request?>(null)
    }

    var editedMessage by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        db.collection("requests")
            .whereEqualTo("hirerId", hirerId)
            .addSnapshotListener { snap, _ ->
                requests =
                    snap?.toObjects(Request::class.java)
                        ?: emptyList()

                isLoading = false
            }
    }

    if (editRequest != null) {
        AlertDialog(
            onDismissRequest = {
                editRequest = null
            },
            title = {
                Text("Edit Request")
            },
            text = {
                OutlinedTextField(
                    value = editedMessage,
                    onValueChange = {
                        editedMessage = it
                    },
                    label = {
                        Text("Work Details")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("requests")
                            .document(editRequest!!.requestId)
                            .update(
                                "workDetails",
                                editedMessage
                            )

                        editRequest = null
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editRequest = null
                    }
                ) {
                    Text("Cancel")
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
            text = "Sent Requests",
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
                Text("No sent requests")
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
                                    WorkerDetailActivity::class.java
                                ).apply {
                                    putExtra(
                                        "workerId",
                                        request.workerId
                                    )
                                }
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Name: ${request.workerName}",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Skill: ${request.workerSkill}"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Location: ${request.workerLocation}"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Status: ${request.workerAvailability}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Message: ${request.workDetails}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            when (request.status) {

                                "Accepted" -> {
                                    Icon(Icons.Default.CheckCircle, null)
                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )
                                    Text("Accepted")
                                }

                                "Rejected" -> {
                                    Icon(Icons.Default.Cancel, null)
                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )
                                    Text("Rejected")
                                }

                                else -> {
                                    Icon(Icons.Default.HourglassEmpty, null)
                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )
                                    Text("Pending")
                                }
                            }

                            Spacer(
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    editRequest = request
                                    editedMessage =
                                        request.workDetails
                                }
                            ) {
                                Icon(Icons.Default.Edit, null)
                            }

                            IconButton(
                                onClick = {
                                    db.collection("requests")
                                        .document(request.requestId)
                                        .delete()
                                }
                            ) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }
                }
            }
        }
    }
}