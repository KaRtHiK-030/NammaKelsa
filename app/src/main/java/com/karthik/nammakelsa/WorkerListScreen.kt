package com.karthik.nammakelsa

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerListScreen() {

    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            activity?.finish()
        }
        return
    }

    val currentUserId = currentUser.uid

    var workers by remember {
        mutableStateOf(listOf<Worker>())
    }

    var filteredWorkers by remember {
        mutableStateOf(listOf<Worker>())
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var hasError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { userDoc ->

                val role =
                    userDoc.getString("role") ?: "worker"

                if (role == "worker") {
                    workers = emptyList()
                    filteredWorkers = emptyList()
                    isLoading = false
                    return@addOnSuccessListener
                }

                firestore.collection("workers")
                    .get()
                    .addOnSuccessListener { result ->

                        workers =
                            result.toObjects(
                                Worker::class.java
                            )
                                .sortedByDescending {
                                    it.availability == "Available"
                                }

                        filteredWorkers = workers
                        isLoading = false
                    }
                    .addOnFailureListener {
                        hasError = true
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                hasError = true
                isLoading = false
            }
    }

    LaunchedEffect(searchText, workers) {
        filteredWorkers =
            if (searchText.isBlank()) {
                workers
            } else {
                workers.filter { worker ->
                    worker.name.contains(
                        searchText,
                        ignoreCase = true
                    ) ||
                            worker.skill.contains(
                                searchText,
                                ignoreCase = true
                            ) ||
                            worker.location.contains(
                                searchText,
                                ignoreCase = true
                            )
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
            text = "Workers",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search workers")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            hasError -> {
                ErrorStateScreen(
                    title = "Failed to load workers",
                    message = "Please try again.",
                    actionText = "Retry",
                    onActionClick = {
                        activity?.recreate()
                    }
                )
            }

            filteredWorkers.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text("No workers found")
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp),
                    contentPadding =
                        PaddingValues(bottom = 100.dp)
                ) {

                    items(filteredWorkers) { worker ->

                        GlassCard(
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
                                                worker.userId
                                            )
                                        }
                                    )
                                }
                        ) {

                            Row(
                                modifier =
                                    Modifier.padding(18.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                if (worker.imageUrl.isNotBlank()) {
                                    Image(
                                        painter =
                                            rememberAsyncImagePainter(
                                                worker.imageUrl
                                            ),
                                        contentDescription = null,
                                        modifier =
                                            Modifier
                                                .size(72.dp)
                                                .clip(CircleShape),
                                        contentScale =
                                            ContentScale.Crop
                                    )
                                } else {
                                    InitialAvatar(
                                        name = worker.name,
                                        size = 72.dp
                                    )
                                }

                                Spacer(
                                    modifier =
                                        Modifier.width(16.dp)
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = worker.name,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleLarge
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = worker.skill,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodyMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(4.dp)
                                    )

                                    Text(
                                        text =
                                            worker.location,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(6.dp)
                                    )

                                    Text(
                                        text =
                                            "₹${worker.chargePerDay}/day",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleMedium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(4.dp)
                                    )

                                    Text(
                                        text =
                                            worker.availability,
                                        color =
                                            if (
                                                worker.availability == "Available"
                                            )
                                                MaterialTheme
                                                    .colorScheme
                                                    .primary
                                            else
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
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