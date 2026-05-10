package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
fun SavedWorkersScreen() {

    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    val currentUserId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var workers by remember {
        mutableStateOf(listOf<Worker>())
    }

    var favoriteDocs by remember {
        mutableStateOf(listOf<String>())
    }

    // LOAD SAVED WORKERS
    fun loadSavedWorkers() {

        db.collection("favorites")

            .whereEqualTo(
                "userId",
                currentUserId
            )

            .get()

            .addOnSuccessListener { favorites ->

                favoriteDocs =
                    favorites.documents.map {
                        it.id
                    }

                val workerIds =
                    favorites.documents.mapNotNull {

                        it.getString("workerId")
                    }

                if (workerIds.isNotEmpty()) {

                    db.collection("workers")

                        .whereIn(
                            "userId",
                            workerIds
                        )

                        .get()

                        .addOnSuccessListener {

                            workers =
                                it.toObjects(
                                    Worker::class.java
                                )
                        }

                } else {

                    workers = emptyList()
                }
            }
    }

    LaunchedEffect(Unit) {
        loadSavedWorkers()
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
            .padding(16.dp)
    ) {

        Text(
            text = "Saved Workers",

            style = MaterialTheme
                .typography
                .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        LazyColumn(

            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(
                bottom = 160.dp
            )
        ) {

            itemsIndexed(workers) { index, worker ->

                ElevatedCard(

                    shape = RoundedCornerShape(20.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    Column(

                        modifier = Modifier
                            .clickable {

                                val intent = Intent(
                                    context,
                                    WorkerDetailActivity::class.java
                                )

                                intent.putExtra(
                                    "workerId",
                                    worker.userId
                                )

                                intent.putExtra(
                                    "name",
                                    worker.name
                                )

                                intent.putExtra(
                                    "skill",
                                    worker.skill
                                )

                                intent.putExtra(
                                    "location",
                                    worker.location
                                )

                                intent.putExtra(
                                    "charge",
                                    worker.chargePerDay
                                )

                                intent.putExtra(
                                    "phone",
                                    worker.phoneNumber
                                )

                                intent.putExtra(
                                    "whatsapp",
                                    worker.whatsappNumber
                                )

                                intent.putExtra(
                                    "imageUrl",
                                    worker.imageUrl
                                )

                                context.startActivity(intent)
                            }

                            .padding(16.dp)
                    ) {

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            // IMAGE
                            Image(
                                painter =
                                    rememberAsyncImagePainter(
                                        worker.imageUrl
                                    ),

                                contentDescription = null,

                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(
                                        RoundedCornerShape(16.dp)
                                    ),

                                contentScale = ContentScale.Crop
                            )

                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )

                            Column {

                                Text(
                                    text = worker.name,

                                    style = MaterialTheme
                                        .typography
                                        .titleLarge
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text =
                                        "🛠 ${worker.skill}"
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text =
                                        "📍 ${worker.location}"
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text =
                                        "₹ ${worker.chargePerDay}/day"
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        // REMOVE BUTTON
                        OutlinedButton(

                            onClick = {

                                if (index < favoriteDocs.size) {

                                    db.collection("favorites")
                                        .document(
                                            favoriteDocs[index]
                                        )
                                        .delete()

                                    loadSavedWorkers()
                                }
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Delete,

                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text("Remove from Saved")
                        }
                    }
                }
            }
        }
    }
}