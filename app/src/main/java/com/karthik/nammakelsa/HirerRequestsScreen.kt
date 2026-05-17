package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun HirerRequestsScreen() {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    var requests by remember { mutableStateOf(listOf<Request>()) }
    var isLoading by remember { mutableStateOf(true) }
    var pendingDelete by remember { mutableStateOf<Request?>(null) }

    DisposableEffect(userId) {
        if (userId.isBlank()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
        val reg = db.collection("requests")
            .whereEqualTo("hirerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                requests = snap.toObjects(Request::class.java)
                isLoading = false
            }
        onDispose { reg.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brandBackground())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.title_my_requests),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) { ShimmerCard() }
                }
                requests.isEmpty() -> EmptyState(
                    icon = Icons.Default.Inbox,
                    title = stringResource(R.string.empty_requests_hirer),
                    subtitle = "Send a request from a worker's profile to see it here."
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests, key = { it.requestId }) { request ->
                        HirerRequestCard(request = request, onDelete = { pendingDelete = request })
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_request_title)) },
            text  = { Text(stringResource(R.string.confirm_delete_request_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("requests").document(target.requestId).delete()
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun HirerRequestCard(request: Request, onDelete: () -> Unit) {
    val status = RequestStatus.from(request.status)
    val statusColor = when (status) {
        RequestStatus.ACCEPTED  -> MaterialTheme.colorScheme.tertiary
        RequestStatus.DECLINED  -> MaterialTheme.colorScheme.error
        RequestStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        else                    -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = request.workDetails.ifBlank { "No details provided." },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = status.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatRelative(request.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_delete))
            }
        }
    }
}
