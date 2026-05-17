package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@Composable
fun WorkerRequestsScreen() {

    val context  = LocalContext.current
    val db       = FirebaseFirestore.getInstance()
    val workerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var requests  by remember { mutableStateOf(listOf<Request>()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbar  = remember { SnackbarHostState() }
    val scope     = rememberCoroutineScope()
    val msgAccept = stringResource(R.string.msg_request_accepted)
    val msgDecline= stringResource(R.string.msg_request_declined)

    DisposableEffect(workerId) {
        if (workerId.isBlank()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
        val reg = db.collection("requests")
            .whereEqualTo("workerId", workerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                requests = snap.toObjects(Request::class.java)
                isLoading = false
            }
        onDispose { reg.remove() }
    }

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
                text = stringResource(R.string.title_work_requests),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${requests.size} request${if (requests.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(3) { ShimmerCard() }
                    }
                    requests.isEmpty() -> EmptyState(
                        icon = Icons.Default.Inbox,
                        title = stringResource(R.string.empty_requests_worker),
                        subtitle = stringResource(R.string.empty_requests_worker_sub)
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(requests, key = { it.requestId }) { request ->
                            RequestCard(
                                request = request,
                                onAccept = {
                                    db.collection("requests").document(request.requestId)
                                        .update("status", RequestStatus.ACCEPTED.value)
                                        .addOnSuccessListener {
                                            scope.launch { snackbar.showSnackbar(msgAccept) }
                                        }
                                },
                                onDecline = {
                                    db.collection("requests").document(request.requestId)
                                        .update("status", RequestStatus.DECLINED.value)
                                        .addOnSuccessListener {
                                            scope.launch { snackbar.showSnackbar(msgDecline) }
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: Request,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current
    val status = RequestStatus.from(request.status)
    val statusColor = when (status) {
        RequestStatus.ACCEPTED  -> MaterialTheme.colorScheme.tertiary
        RequestStatus.DECLINED  -> MaterialTheme.colorScheme.error
        RequestStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        else                    -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = request.hirerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.hirerName.ifBlank { "Unknown Hirer" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (request.hirerLocation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = request.hirerLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = formatRelative(request.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Work Details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = request.workDetails.ifBlank { "No details provided." },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (request.hirerPhone.isNotBlank()) {
                    OutlinedButton(
                        onClick = { context.dialPhone(request.hirerPhone) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_call))
                    }
                }
                if (request.hirerWhatsapp.isNotBlank()) {
                    OutlinedButton(
                        onClick = { context.openWhatsApp(request.hirerWhatsapp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_whatsapp))
                    }
                }
            }

            if (status == RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept")
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline")
                    }
                }
            }
        }
    }
}
