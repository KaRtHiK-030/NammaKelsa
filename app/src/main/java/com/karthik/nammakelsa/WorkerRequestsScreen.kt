package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerRequestsScreen() {

    val context = LocalContext.current

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
                "workerId",
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

            text = "Work Requests",

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

                        // OPEN HIRER PROFILE
                        .clickable {

                            val intent =
                                Intent(
                                    context,
                                    HirerDetailActivity::class.java
                                )

                            intent.putExtra(
                                "name",
                                request.hirerName
                            )

                            intent.putExtra(
                                "imageUrl",
                                request.hirerImage
                            )

                            intent.putExtra(
                                "location",
                                request.hirerLocation
                            )

                            intent.putExtra(
                                "phone",
                                request.hirerPhone
                            )

                            intent.putExtra(
                                "whatsapp",
                                request.hirerWhatsapp
                            )

                            context.startActivity(intent)
                        }
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        // HIRER PROFILE SECTION
                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            AsyncImage(

                                model =
                                    request.hirerImage,

                                contentDescription = null,

                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape),

                                contentScale =
                                    ContentScale.Crop
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(14.dp)
                            )

                            Column {

                                Text(
                                    text =
                                        request.hirerName,

                                    style = MaterialTheme
                                        .typography
                                        .titleLarge
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        "📍 ${request.hirerLocation}"
                                )

                                Text(
                                    text =
                                        "📞 ${request.hirerPhone}"
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )

                        Text(
                            text =
                                request.workDetails
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Text(
                            text =
                                "Status: ${request.status}"
                        )

                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )

                        // ONLY SHOW BUTTONS IF PENDING
                        if (request.status == "Pending") {

                            Row(

                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                // ACCEPT
                                Button(

                                    onClick = {

                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("requests")
                                            .document(
                                                request.requestId
                                            )

                                            .update(
                                                "status",
                                                "Accepted"
                                            )
                                    }
                                ) {

                                    Text("Accept")
                                }

                                // REJECT
                                OutlinedButton(

                                    onClick = {

                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("requests")
                                            .document(
                                                request.requestId
                                            )

                                            .update(
                                                "status",
                                                "Rejected"
                                            )
                                    }
                                ) {

                                    Text("Reject")
                                }

                                // DELETE REQUEST
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
    }
}