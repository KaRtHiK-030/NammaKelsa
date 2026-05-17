package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Worker dashboard. Replaces the previous "list of other workers" home with a real
 * incoming-work-focused screen: greeting, availability toggle, pending count,
 * recent accepted requests, and a CTA to manage skills.
 */
@Composable
fun WorkerHomeScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available") }
    var pendingCount by remember { mutableIntStateOf(0) }
    var acceptedCount by remember { mutableIntStateOf(0) }
    var totalReviews by remember { mutableLongStateOf(0L) }
    var avgRating by remember { mutableDoubleStateOf(0.0) }
    var recent by remember { mutableStateOf(listOf<Request>()) }

    DisposableEffect(userId) {
        if (userId.isBlank()) return@DisposableEffect onDispose {}
        val regs = mutableListOf<ListenerRegistration>()

        regs += db.collection("workers").document(userId)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                name = snap.getString("name") ?: ""
                availability = snap.getString("availability") ?: "Available"
                avgRating = snap.getDouble("averageRating") ?: 0.0
                totalReviews = snap.getLong("totalReviews") ?: 0L
            }

        regs += db.collection("requests")
            .whereEqualTo("workerId", userId)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                val items = snap.toObjects(Request::class.java)
                pendingCount = items.count { it.status == RequestStatus.PENDING.value }
                acceptedCount = items.count { it.status == RequestStatus.ACCEPTED.value }
                recent = items.sortedByDescending { it.createdAt }.take(3)
            }

        onDispose { regs.forEach { it.remove() } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScrollIfNeeded()
            .padding(16.dp)
    ) {

        // Greeting
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hello, ${name.ifBlank { "Worker" }} 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (availability) {
                        "Busy"    -> stringResource(R.string.avail_currently_busy)
                        "Offline" -> stringResource(R.string.avail_offline)
                        else      -> stringResource(R.string.avail_for_work)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stat cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashStat(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.HourglassTop,
                value = pendingCount.toString(),
                label = "Pending"
            )
            DashStat(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                value = acceptedCount.toString(),
                label = "Accepted"
            )
            DashStat(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Star,
                value = if (totalReviews > 0L) "%.1f".format(avgRating) else "—",
                label = "Rating"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Availability toggle card
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.profile_change_availability),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvailabilityChip(
                        modifier = Modifier.weight(1f),
                        selected = availability == "Available",
                        label = stringResource(R.string.avail_available),
                        onClick = {
                            availability = "Available"
                            db.collection("workers").document(userId).update("availability", "Available")
                        }
                    )
                    AvailabilityChip(
                        modifier = Modifier.weight(1f),
                        selected = availability == "Busy",
                        label = stringResource(R.string.avail_busy),
                        onClick = {
                            availability = "Busy"
                            db.collection("workers").document(userId).update("availability", "Busy")
                        }
                    )
                    AvailabilityChip(
                        modifier = Modifier.weight(1f),
                        selected = availability == "Offline",
                        label = stringResource(R.string.avail_offline),
                        onClick = {
                            availability = "Offline"
                            db.collection("workers").document(userId).update("availability", "Offline")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = {
                    context.startActivity(Intent(context, AddSkillActivity::class.java))
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_add_new_skill))
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(context, ProfileActivity::class.java).putExtra("role", "worker")
                    )
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_edit))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent requests preview
        Text(
            text = "Recent requests",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (recent.isEmpty()) {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.empty_requests_worker),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.empty_requests_worker_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            recent.forEach { req ->
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = req.hirerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.hirerName.ifBlank { "Unknown hirer" }, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = req.workDetails.take(60),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        StatusBadge(status = RequestStatus.from(req.status))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun AvailabilityChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label) },
        modifier = modifier
    )
}

@Composable
private fun DashStat(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusBadge(status: RequestStatus) {
    val color = when (status) {
        RequestStatus.ACCEPTED  -> MaterialTheme.colorScheme.tertiary
        RequestStatus.DECLINED  -> MaterialTheme.colorScheme.error
        RequestStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        else                    -> MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Modifier.verticalScrollIfNeeded(): Modifier {
    val scroll = androidx.compose.foundation.rememberScrollState()
    return this.verticalScroll(scroll)
}
