package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karthik.nammakelsa.ui.theme.StarYellow
import com.karthik.nammakelsa.ui.theme.SuccessGreen
import com.karthik.nammakelsa.ui.theme.WhatsAppGreen

@Composable
fun HirerHomeScreen() {

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var workers by remember { mutableStateOf(listOf<Worker>()) }
    var search by remember { mutableStateOf("") }
    var skillFilter by remember { mutableStateOf("") }
    var locationFilter by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        db.collection("workers")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener {
                workers = it.toObjects(Worker::class.java)
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    val filtered = workers.filter { w ->
        val skillMatch = skillFilter.isBlank() ||
            w.skill.contains(skillFilter, ignoreCase = true) ||
            w.skillsList.any { it["skill"]?.contains(skillFilter, ignoreCase = true) == true }
        val locationMatch = locationFilter.isBlank() ||
            w.location.contains(locationFilter, ignoreCase = true)
        val searchMatch = search.isBlank() ||
            w.name.contains(search, ignoreCase = true) ||
            w.skill.contains(search, ignoreCase = true) ||
            w.location.contains(search, ignoreCase = true) ||
            w.skillsList.any { it["skill"]?.contains(search, ignoreCase = true) == true }
        skillMatch && locationMatch && searchMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brandBackground())
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.title_available_workers),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filtered.size} skilled professionals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(onClick = { showFilter = true }) {
                Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.action_filter))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                    }
                }
            },
            label = { Text(stringResource(R.string.search_workers_hint)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        if (skillFilter.isNotBlank() || locationFilter.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (skillFilter.isNotBlank()) {
                    AssistChip(
                        onClick = { skillFilter = "" },
                        label = { Text(skillFilter) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                if (locationFilter.isNotBlank()) {
                    AssistChip(
                        onClick = { locationFilter = "" },
                        label = { Text(locationFilter) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showFilter) {
            FilterDialog(
                skill = skillFilter,
                location = locationFilter,
                onApply = { s, l ->
                    skillFilter = s
                    locationFilter = l
                    showFilter = false
                },
                onClear = {
                    skillFilter = ""
                    locationFilter = ""
                    showFilter = false
                },
                onDismiss = { showFilter = false }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) { ShimmerCard() }
                }
                filtered.isEmpty() -> EmptyState(
                    icon = Icons.Default.PersonSearch,
                    title = stringResource(R.string.empty_workers),
                    subtitle = stringResource(R.string.search_workers_hint)
                )
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.userId.ifBlank { it.name } }) { worker ->
                        HirerWorkerCard(
                            worker = worker,
                            onOpen = {
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
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HirerWorkerCard(worker: Worker, onOpen: () -> Unit) {
    val context = LocalContext.current
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {

            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = worker.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (worker.totalReviews > 0L) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("%.1f".format(worker.averageRating), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(
                    text = worker.skill,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = worker.location.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "₹${worker.chargePerDay.ifBlank { "—" }}/day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (worker.availability) {
                            "Busy" -> MaterialTheme.colorScheme.errorContainer
                            "Offline" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> SuccessGreen.copy(alpha = 0.9f)
                        },
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = worker.availability,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { context.dialPhone(worker.phoneNumber) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.action_call))
            }
            Button(
                onClick = { context.openWhatsApp(worker.whatsappNumber) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.action_whatsapp))
            }
        }
    }
}

@Composable
private fun FilterDialog(
    skill: String,
    location: String,
    onApply: (String, String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var s by remember { mutableStateOf(skill) }
    var l by remember { mutableStateOf(location) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Workers") },
        text = {
            Column {
                OutlinedTextField(
                    value = s,
                    onValueChange = { s = it },
                    label = { Text("Skill") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = l,
                    onValueChange = { l = it },
                    label = { Text("Location") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(s, l) }) { Text(stringResource(R.string.action_apply)) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onClear) { Text(stringResource(R.string.action_clear)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            }
        }
    )
}
