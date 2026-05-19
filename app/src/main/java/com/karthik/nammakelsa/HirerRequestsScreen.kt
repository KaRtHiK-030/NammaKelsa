package com.karthik.nammakelsa

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.karthik.nammakelsa.ui.theme.ErrorRed
import com.karthik.nammakelsa.ui.theme.SuccessGreen

@Composable
fun HirerRequestsScreen() {

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
    val firestore = FirebaseFirestore.getInstance()

    var requests by remember {
        mutableStateOf(listOf<Request>())
    }

    var workerNames by remember {
        mutableStateOf(mapOf<String, String>())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    DisposableEffect(Unit) {

        val listener: ListenerRegistration =
            firestore.collection("requests")
                .whereEqualTo("hirerId", userId)
                .addSnapshotListener { snapshot, _ ->

                    if (snapshot == null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    requests =
                        snapshot.toObjects(Request::class.java)

                    isLoading = false

                    val ids =
                        requests.map { it.workerId }
                            .distinct()

                    ids.forEach { workerId ->

                        if (
                            workerId.isNotBlank() &&
                            !workerNames.containsKey(workerId)
                        ) {
                            firestore.collection("workers")
                                .document(workerId)
                                .get()
                                .addOnSuccessListener { doc ->
                                    val workerName =
                                        doc.getString("name")
                                            ?: "Worker"

                                    workerNames =
                                        workerNames +
                                                (workerId to workerName)
                                }
                        }
                    }
                }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
    ) {

        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 4.dp
            )
        ) {
            Text(
                text = "My Requests",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "${requests.size} request" +
                            if (requests.size != 1) "s" else "",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            requests.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            null,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "No requests yet",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "Send a request from a worker profile",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        HirerRequestCard(
                            request = request,
                            workerName =
                                workerNames[request.workerId]
                                    ?: "Worker",
                            onDelete = {
                                firestore.collection("requests")
                                    .document(request.requestId)
                                    .delete()
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HirerRequestCard(
    request: Request,
    workerName: String,
    onDelete: () -> Unit
) {
    val statusColor =
        when (request.status) {
            "Accepted" -> SuccessGreen
            "Declined" -> ErrorRed
            else -> MaterialTheme.colorScheme.primary
        }

    val statusBg =
        when (request.status) {
            "Accepted" ->
                MaterialTheme.colorScheme.secondaryContainer

            "Declined" ->
                MaterialTheme.colorScheme.errorContainer

            else ->
                MaterialTheme.colorScheme.primaryContainer
        }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                InitialAvatar(
                    name = workerName,
                    size = 48.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = workerName,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Worker",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = request.status.ifBlank { "Pending" },
                        style =
                            MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "Work Details",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                request.workDetails.ifBlank {
                    "No details provided."
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor =
                            MaterialTheme.colorScheme.error
                    )
            ) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Withdraw Request")
            }
        }
    }
}