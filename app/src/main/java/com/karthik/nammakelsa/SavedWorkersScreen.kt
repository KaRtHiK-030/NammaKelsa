package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class SavedWorker(
    val workerId: String = "",
    val name: String = "",
    val location: String = ""
)

@Composable
fun SavedWorkersScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val hirerId = currentUser.uid

    var savedWorkers by remember {
        mutableStateOf<List<SavedWorker>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        db.collection("savedWorkers")
            .whereEqualTo("hirerId", hirerId)
            .addSnapshotListener { snap, _ ->

                if (snap == null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snap.documents.isEmpty()) {
                    savedWorkers = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }

                val tempList =
                    mutableListOf<SavedWorker>()

                snap.documents.forEach { doc ->

                    val workerId =
                        doc.getString("workerId") ?: ""

                    db.collection("workers")
                        .document(workerId)
                        .get()
                        .addOnSuccessListener { workerDoc ->

                            if (workerDoc.exists()) {

                                tempList.removeAll {
                                    it.workerId == workerId
                                }

                                tempList.add(
                                    SavedWorker(
                                        workerId = workerId,
                                        name =
                                            workerDoc.getString("name")
                                                ?: "Worker",
                                        location =
                                            workerDoc.getString("location")
                                                ?: ""
                                    )
                                )

                                savedWorkers = tempList
                                isLoading = false
                            }
                        }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
            .padding(16.dp)
    ) {

        Text(
            "Saved Workers",
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

        if (savedWorkers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved workers")
            }
            return@Column
        }

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(savedWorkers) { worker ->

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                worker.name,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(worker.location)
                        }

                        IconButton(
                            onClick = {
                                db.collection("savedWorkers")
                                    .document(
                                        "${hirerId}_${worker.workerId}"
                                    )
                                    .delete()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null
                            )
                        }
                    }
                }
            }
        }
    }
}