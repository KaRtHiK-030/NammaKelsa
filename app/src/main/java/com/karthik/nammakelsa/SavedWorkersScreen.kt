package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.StarYellow
import kotlinx.coroutines.launch

@Composable
fun SavedWorkersScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var workers by remember { mutableStateOf(listOf<Worker>()) }
    var isLoading by remember { mutableStateOf(true) }
    var pendingRemoval by remember { mutableStateOf<Worker?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val removedMsg = stringResource(R.string.msg_worker_removed)

    fun loadSaved() {
        if (currentUserId.isBlank()) {
            isLoading = false
            return
        }
        // Favorites are stored under hirers/{uid}/favorites/{workerId}
        db.collection("hirers").document(currentUserId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { favoriteSnap ->
                val workerIds = favoriteSnap.documents.mapNotNull { it.id.takeIf { id -> id.isNotBlank() } }
                if (workerIds.isEmpty()) {
                    workers = emptyList()
                    isLoading = false
                    return@addOnSuccessListener
                }
                // Firestore `whereIn` accepts at most 30 IDs per query.
                val chunks = workerIds.chunked(30)
                val collected = mutableListOf<Worker>()
                var done = 0
                chunks.forEach { ids ->
                    db.collection("workers")
                        .whereIn("userId", ids)
                        .get()
                        .addOnSuccessListener { qs ->
                            collected += qs.toObjects(Worker::class.java)
                            done++
                            if (done == chunks.size) {
                                workers = collected.distinctBy { it.userId }
                                isLoading = false
                            }
                        }
                        .addOnFailureListener {
                            done++
                            if (done == chunks.size) isLoading = false
                        }
                }
            }
            .addOnFailureListener { isLoading = false }
    }

    LaunchedEffect(currentUserId) { loadSaved() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brandBackground())
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_saved_workers),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(3) { ShimmerCard() }
                    }
                    workers.isEmpty() -> EmptyState(
                        icon = Icons.Default.FavoriteBorder,
                        title = stringResource(R.string.empty_saved),
                        subtitle = "Tap the heart on a worker to save them here."
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(workers, key = { it.userId }) { worker ->
                            SavedWorkerCard(
                                worker = worker,
                                onClick = {
                                    val intent = Intent(context, WorkerDetailActivity::class.java).apply {
                                        putExtra("workerId", worker.userId)
                                        putExtra("name", worker.name)
                                        putExtra("skill", worker.skill)
                                        putExtra("location", worker.location)
                                        putExtra("charge", worker.chargePerDay)
                                        putExtra("phone", worker.phoneNumber)
                                        putExtra("whatsapp", worker.whatsappNumber)
                                        putExtra("imageUrl", worker.imageUrl)
                                    }
                                    context.startActivity(intent)
                                },
                                onRemove = { pendingRemoval = worker }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingRemoval?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text(stringResource(R.string.confirm_remove_favorite_title)) },
            text  = { Text(stringResource(R.string.confirm_remove_favorite_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        // Remove by deterministic doc id (workerId), NOT by list index.
                        db.collection("hirers").document(currentUserId)
                            .collection("favorites").document(target.userId)
                            .delete()
                            .addOnSuccessListener {
                                workers = workers.filterNot { it.userId == target.userId }
                                scope.launch { snackbar.showSnackbar(removedMsg) }
                            }
                        pendingRemoval = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_remove)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SavedWorkerCard(
    worker: Worker,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = worker.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(worker.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(worker.skill, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = worker.location.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${worker.chargePerDay.ifBlank { "—" }}/day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (worker.totalReviews > 0L) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow)
                        Text(
                            text = "%.1f".format(worker.averageRating),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRemove,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_remove))
            }
        }
    }
}
