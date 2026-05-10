package com.karthik.nammakelsa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HirerRequestsScreen() {

    val userId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var requests by remember {
        mutableStateOf(listOf<Request>())
    }

    // REALTIME REQUEST LISTENER
    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("requests")

            .whereEqualTo(
                "hirerId",
                userId
            )

            .addSnapshotListener { value, _ ->

                if (value != null) {

                    requests =
                        value.toObjects(
                            Request::class.java
                        )
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {

        Text(

            text = "My Requests",

            style = MaterialTheme
                .typography
                .headlineMedium,

            modifier = Modifier.padding(20.dp)
        )

        LazyColumn(

            contentPadding = PaddingValues(
                bottom = 140.dp
            )
        ) {

            items(requests) { request ->

                ElevatedCard(

                    shape = RoundedCornerShape(20.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        // WORK DETAILS
                        Text(
                            text =
                                request.workDetails,

                            style = MaterialTheme
                                .typography
                                .titleMedium
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        // STATUS
                        Text(
                            text =
                                "Status: ${request.status}"
                        )

                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )

                        // DELETE BUTTON
                        IconButton(

                            onClick = {

                                FirebaseFirestore
                                    .getInstance()
                                    .collection("requests")
                                    .document(
                                        request.requestId
                                    )
                                    .delete()
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
        }
    }
}