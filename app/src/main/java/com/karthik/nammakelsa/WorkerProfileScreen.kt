package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.karthik.nammakelsa.ui.theme.StarYellow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var skillsList by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var availability by remember { mutableStateOf("Available") }
    var avgRating by remember { mutableDoubleStateOf(0.0) }
    var totalReviews by remember { mutableLongStateOf(0L) }

    var pendingDeleteSkill by remember { mutableStateOf<Pair<Int, Map<String, String>>?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val msgSkillRemoved = stringResource(R.string.msg_skill_removed)
    val msgLoggedOut    = stringResource(R.string.msg_logged_out)
    val msgAccountDeleted = stringResource(R.string.msg_account_deleted)
    val msgRecentLogin = stringResource(R.string.msg_recent_login_required)

    val listState = rememberLazyListState()

    DisposableEffect(userId) {
        if (userId.isBlank()) return@DisposableEffect onDispose {}
        val reg = db.collection("workers").document(userId)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                name = snap.getString("name") ?: ""
                location = snap.getString("location") ?: ""
                phone = snap.getString("phoneNumber") ?: ""
                whatsapp = snap.getString("whatsappNumber") ?: ""
                availability = snap.getString("availability") ?: "Available"
                @Suppress("UNCHECKED_CAST")
                skillsList = (snap.get("skillsList") as? List<Map<String, String>>) ?: emptyList()
                avgRating = snap.getDouble("averageRating") ?: 0.0
                totalReviews = snap.getLong("totalReviews") ?: 0L
            }
        onDispose { reg.remove() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        LazyColumn(
            state = listState,
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
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Text(stringResource(R.string.profile_my), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Identity card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(140.dp)) {
                            Surface(modifier = Modifier.size(140.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (availability) {
                                            "Busy"    -> Icons.Default.DoNotDisturbOn
                                            "Offline" -> Icons.Default.PowerSettingsNew
                                            else      -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = availability,
                                        tint = when (availability) {
                                            "Busy"    -> MaterialTheme.colorScheme.error
                                            "Offline" -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else      -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (availability) {
                                "Busy"    -> stringResource(R.string.avail_currently_busy)
                                "Offline" -> stringResource(R.string.avail_offline)
                                else      -> stringResource(R.string.avail_for_work)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (totalReviews > 0L) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("%.1f (%d)".format(avgRating, totalReviews), style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ContactCell(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.LocationOn,
                                value = location.ifBlank { "—" }
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            ContactCell(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Phone,
                                value = phone.ifBlank { "—" }
                            )
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            ContactCell(
                                modifier = Modifier.weight(1f),
                                icon = Icons.AutoMirrored.Filled.Chat,
                                value = whatsapp.ifBlank { "—" }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Skills header
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.profile_my_skills), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${skillsList.size} skills", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (skillsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.WorkOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(stringResource(R.string.profile_no_skills_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.profile_no_skills_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                itemsIndexed(skillsList) { index, item ->
                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item["skill"] ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text("${item["charge"]}/day", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            IconButton(onClick = { pendingDeleteSkill = index to item }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.confirm_delete_skill_title), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java).putExtra("role", "worker"))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_edit_title), style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = { context.startActivity(Intent(context, AddSkillActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_add_new_skill), style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(20.dp))

                ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.profile_change_availability), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = availability == "Available",
                                onClick = {
                                    availability = "Available"
                                    db.collection("workers").document(userId).update("availability", "Available")
                                },
                                label = { Text(stringResource(R.string.avail_available)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = availability == "Busy",
                                onClick = {
                                    availability = "Busy"
                                    db.collection("workers").document(userId).update("availability", "Busy")
                                },
                                label = { Text(stringResource(R.string.avail_busy)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = availability == "Offline",
                                onClick = {
                                    availability = "Offline"
                                    db.collection("workers").document(userId).update("availability", "Offline")
                                },
                                label = { Text(stringResource(R.string.avail_offline)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_logout), style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_delete_account), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // Confirm: remove skill
    pendingDeleteSkill?.let { (index, item) ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSkill = null },
            title = { Text(stringResource(R.string.confirm_delete_skill_title)) },
            text  = { Text(stringResource(R.string.confirm_delete_skill_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = skillsList.toMutableList().also { it.removeAt(index) }
                        db.collection("workers").document(userId).update("skillsList", updated)
                            .addOnSuccessListener {
                                skillsList = updated
                                scope.launch { snackbar.showSnackbar(msgSkillRemoved) }
                            }
                        pendingDeleteSkill = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSkill = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.confirm_logout_title)) },
            text  = { Text(stringResource(R.string.confirm_logout_body)) },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(context, RoleSelectionActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    scope.launch { snackbar.showSnackbar(msgLoggedOut) }
                }) { Text(stringResource(R.string.action_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete_account_title)) },
            text  = { Text(stringResource(R.string.confirm_delete_account_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        deleteCurrentAccount(
                            role = "worker",
                            onSuccess = {
                                scope.launch { snackbar.showSnackbar(msgAccountDeleted) }
                                val intent = Intent(context, RoleSelectionActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            },
                            onError = { code ->
                                if (code == "RECENT_LOGIN_REQUIRED") {
                                    FirebaseAuth.getInstance().signOut()
                                    scope.launch { snackbar.showSnackbar(msgRecentLogin) }
                                    val intent = Intent(context, RoleSelectionActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                } else {
                                    scope.launch { snackbar.showSnackbar(code) }
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun ContactCell(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
