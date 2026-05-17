package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HirerProfileScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var postedJobs by remember { mutableIntStateOf(0) }
    var hiredWorkers by remember { mutableIntStateOf(0) }
    var reviewsGiven by remember { mutableIntStateOf(0) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val msgLoggedOut = stringResource(R.string.msg_logged_out)
    val msgAccountDeleted = stringResource(R.string.msg_account_deleted)
    val msgRecentLogin = stringResource(R.string.msg_recent_login_required)

    val listState = rememberLazyListState()

    DisposableEffect(userId) {
        if (userId.isBlank()) return@DisposableEffect onDispose {}

        val regProfile = db.collection("hirers").document(userId)
            .addSnapshotListener { document, _ ->
                document ?: return@addSnapshotListener
                name     = document.getString("name") ?: ""
                location = document.getString("location") ?: ""
                phone    = document.getString("phoneNumber") ?: ""
                whatsapp = document.getString("whatsappNumber") ?: ""
            }
        email = FirebaseAuth.getInstance().currentUser?.email ?: ""

        // Live counts
        val regJobs = db.collection("requests").whereEqualTo("hirerId", userId)
            .addSnapshotListener { s, _ -> postedJobs = s?.size() ?: 0 }
        val regHired = db.collection("requests")
            .whereEqualTo("hirerId", userId)
            .whereEqualTo("status", RequestStatus.ACCEPTED.value)
            .addSnapshotListener { s, _ -> hiredWorkers = s?.size() ?: 0 }
        val regReviews = db.collection("reviews").whereEqualTo("userId", userId)
            .addSnapshotListener { s, _ -> reviewsGiven = s?.size() ?: 0 }

        onDispose {
            regProfile.remove(); regJobs.remove(); regHired.remove(); regReviews.remove()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(brandBackground())
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(stringResource(R.string.profile_my), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))

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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.profile_hirer_account), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(20.dp))

                ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.profile_contact_information), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        ProfileInfoRow(Icons.Default.Email, stringResource(R.string.profile_field_email), email.ifBlank { stringResource(R.string.profile_field_not_provided) })
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow(Icons.Default.LocationOn, stringResource(R.string.profile_location), location.ifBlank { stringResource(R.string.profile_field_not_provided) })
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow(Icons.Default.Phone, stringResource(R.string.profile_phone), phone.ifBlank { stringResource(R.string.profile_field_not_provided) })
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoRow(Icons.AutoMirrored.Filled.Chat, stringResource(R.string.profile_whatsapp), whatsapp.ifBlank { stringResource(R.string.profile_field_not_provided) })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.profile_account_statistics), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatisticItem(Icons.Default.WorkOutline, stringResource(R.string.profile_stat_jobs), postedJobs.toString())
                            VerticalDivider(modifier = Modifier.height(60.dp))
                            StatisticItem(Icons.Default.People, stringResource(R.string.profile_stat_hired), hiredWorkers.toString())
                            VerticalDivider(modifier = Modifier.height(60.dp))
                            StatisticItem(Icons.Default.Star, stringResource(R.string.profile_stat_reviews), reviewsGiven.toString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { context.startActivity(Intent(context, ProfileActivity::class.java).putExtra("role", "hirer")) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_edit_title), style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_delete_account), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
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
                            role = "hirer",
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

// ── Shared composables ───────────────────────────────────────────────────────

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
