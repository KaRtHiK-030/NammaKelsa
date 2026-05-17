package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme
import com.karthik.nammakelsa.ui.theme.StarYellow
import com.karthik.nammakelsa.ui.theme.WhatsAppGreen
import kotlinx.coroutines.launch

class WorkerDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workerId = intent.getStringExtra("workerId") ?: ""
        val name     = intent.getStringExtra("name") ?: ""
        val skill    = intent.getStringExtra("skill") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val charge   = intent.getStringExtra("charge") ?: ""
        val phone    = intent.getStringExtra("phone") ?: ""
        val whatsapp = intent.getStringExtra("whatsapp") ?: ""

        setContent {
            NammaKelsaTheme {
                WorkerDetailScreen(workerId, name, skill, location, charge, phone, whatsapp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    workerId: String,
    name: String,
    skill: String,
    location: String,
    charge: String,
    phone: String,
    whatsapp: String
) {
    val context       = LocalContext.current
    val activity      = context as? android.app.Activity
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db            = FirebaseFirestore.getInstance()

    var isHirer       by remember { mutableStateOf(false) }
    var rating        by remember { mutableFloatStateOf(0f) }
    var comment       by remember { mutableStateOf("") }
    var workDetails   by remember { mutableStateOf("") }
    var workDetailsError by remember { mutableStateOf<String?>(null) }
    var ratingError   by remember { mutableStateOf<String?>(null) }
    var commentError  by remember { mutableStateOf<String?>(null) }
    var isSaved       by remember { mutableStateOf(false) }
    var reviews       by remember { mutableStateOf(listOf<Review>()) }
    var averageRating by remember { mutableFloatStateOf(0f) }
    var pendingDeleteReviewId by remember { mutableStateOf<String?>(null) }
    var hasExistingRequest by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val errReqDetails  = stringResource(R.string.err_work_details_required)
    val errReqShort    = stringResource(R.string.err_work_details_short)
    val errRatingReq   = stringResource(R.string.err_review_rating_required)
    val errCommentReq  = stringResource(R.string.err_review_comment_required)
    val msgRequestSent = stringResource(R.string.msg_request_sent)
    val msgReviewAdded = stringResource(R.string.msg_review_added)
    val msgReviewDeleted = stringResource(R.string.msg_review_deleted)
    val msgWorkerSaved = stringResource(R.string.msg_worker_saved)
    val msgWorkerRemoved = stringResource(R.string.msg_worker_removed)

    // Determine role (hirer = can review and send requests)
    LaunchedEffect(currentUserId) {
        if (currentUserId.isBlank()) return@LaunchedEffect
        db.collection("hirers").document(currentUserId).get()
            .addOnSuccessListener { isHirer = it.exists() }

        // saved?
        db.collection("hirers").document(currentUserId)
            .collection("favorites").document(workerId).get()
            .addOnSuccessListener { isSaved = it.exists() }

        // existing pending request?
        db.collection("requests")
            .whereEqualTo("hirerId", currentUserId)
            .whereEqualTo("workerId", workerId)
            .whereEqualTo("status", RequestStatus.PENDING.value)
            .get()
            .addOnSuccessListener { hasExistingRequest = !it.isEmpty }
    }

    // Reviews snapshot
    DisposableEffect(workerId) {
        val reg = db.collection("reviews")
            .whereEqualTo("workerId", workerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                reviews = snap.toObjects(Review::class.java)
                averageRating = if (reviews.isNotEmpty())
                    reviews.map { it.rating }.average().toFloat() else 0f
            }
        onDispose { reg.remove() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(name.ifBlank { stringResource(R.string.app_name) }) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(brandBackground())
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                if (reviews.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(averageRating) + " (${reviews.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        DetailRow(icon = Icons.Default.Build, label = stringResource(R.string.profile_primary_skill), value = skill)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(icon = Icons.Default.LocationOn, label = stringResource(R.string.profile_location), value = location)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(icon = Icons.Default.CurrencyRupee, label = stringResource(R.string.rate_label), value = "₹$charge/day")
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(icon = Icons.Default.Phone, label = stringResource(R.string.profile_phone), value = phone.ifBlank { "—" })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(
                            onClick = { context.dialPhone(phone) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_call))
                        }
                        Button(
                            onClick = { context.openWhatsApp(whatsapp) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_whatsapp))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Save / Unsave (only useful for hirers)
                        if (isHirer) {
                            FilledTonalButton(
                                onClick = {
                                    if (currentUserId.isBlank()) return@FilledTonalButton
                                    val favRef = db.collection("hirers").document(currentUserId)
                                        .collection("favorites").document(workerId)
                                    if (isSaved) {
                                        favRef.delete().addOnSuccessListener {
                                            isSaved = false
                                            scope.launch { snackbar.showSnackbar(msgWorkerRemoved) }
                                        }
                                    } else {
                                        favRef.set(
                                            Favorite(userId = currentUserId, workerId = workerId)
                                        ).addOnSuccessListener {
                                            isSaved = true
                                            scope.launch { snackbar.showSnackbar(msgWorkerSaved) }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isSaved) MaterialTheme.colorScheme.error else LocalContentColor.current
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(if (isSaved) R.string.action_saved else R.string.action_save_worker))
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                val intent = Intent(context, ChatActivity::class.java)
                                    .putExtra("receiverId", workerId)
                                    .putExtra("receiverName", name)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_chat))
                        }
                    }
                }

                if (isHirer) {
                    Spacer(modifier = Modifier.height(24.dp))

                    ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(stringResource(R.string.title_send_work_request), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.hint_work_details_help),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = workDetails,
                                onValueChange = {
                                    workDetails = it
                                    workDetailsError = when {
                                        it.isBlank() -> errReqDetails
                                        it.length < 10 -> errReqShort
                                        else -> null
                                    }
                                },
                                label = { Text(stringResource(R.string.hint_work_details)) },
                                isError = workDetailsError != null,
                                supportingText = workDetailsError?.let { { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val ok = when {
                                        workDetails.isBlank() -> { workDetailsError = errReqDetails; false }
                                        workDetails.length < 10 -> { workDetailsError = errReqShort; false }
                                        else -> { workDetailsError = null; true }
                                    }
                                    if (!ok) return@Button
                                    if (hasExistingRequest) {
                                        scope.launch { snackbar.showSnackbar("You already have a pending request with this worker.") }
                                        return@Button
                                    }
                                    db.collection("hirers").document(currentUserId).get()
                                        .addOnSuccessListener { hirerDoc ->
                                            val requestRef = db.collection("requests").document()
                                            val payload = Request(
                                                requestId      = requestRef.id,
                                                workerId       = workerId,
                                                hirerId        = currentUserId,
                                                hirerName      = hirerDoc.getString("name") ?: "",
                                                hirerImage     = hirerDoc.getString("imageUrl") ?: "",
                                                hirerLocation  = hirerDoc.getString("location") ?: "",
                                                hirerPhone     = hirerDoc.getString("phoneNumber") ?: "",
                                                hirerWhatsapp  = hirerDoc.getString("whatsappNumber") ?: "",
                                                workDetails    = workDetails.trim(),
                                                status         = RequestStatus.PENDING.value
                                            )
                                            requestRef.set(payload)
                                                .addOnSuccessListener {
                                                    workDetails = ""
                                                    hasExistingRequest = true
                                                    scope.launch { snackbar.showSnackbar(msgRequestSent) }
                                                }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !hasExistingRequest
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (hasExistingRequest) "Request already pending"
                                    else stringResource(R.string.action_send_request)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Review form
                    ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(stringResource(R.string.title_rate_worker), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                for (i in 1..5) {
                                    IconButton(onClick = {
                                        rating = i.toFloat()
                                        if (ratingError != null) ratingError = null
                                    }) {
                                        Icon(
                                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Rate $i star${if (i == 1) "" else "s"}",
                                            tint = StarYellow
                                        )
                                    }
                                }
                            }
                            ratingError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = comment,
                                onValueChange = {
                                    comment = it
                                    if (it.isBlank()) commentError = errCommentReq else commentError = null
                                },
                                label = { Text(stringResource(R.string.hint_review)) },
                                isError = commentError != null,
                                supportingText = commentError?.let { { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val rOk = if (rating <= 0f) { ratingError = errRatingReq; false } else { ratingError = null; true }
                                    val cOk = if (comment.isBlank()) { commentError = errCommentReq; false } else { commentError = null; true }
                                    if (!rOk || !cOk) return@Button

                                    db.collection("hirers").document(currentUserId).get()
                                        .addOnSuccessListener { hirerDoc ->
                                            val reviewerName = hirerDoc.getString("name") ?: "Anonymous"
                                            // Deterministic id => same hirer can update their existing review.
                                            val reviewId = "${workerId}_${currentUserId}"
                                            val review = Review(
                                                reviewId     = reviewId,
                                                workerId     = workerId,
                                                userId       = currentUserId,
                                                reviewerName = reviewerName,
                                                rating       = rating,
                                                comment      = comment.trim()
                                            )
                                            db.collection("reviews").document(reviewId)
                                                .set(review)
                                                .addOnSuccessListener {
                                                    rating = 0f; comment = ""
                                                    scope.launch { snackbar.showSnackbar(msgReviewAdded) }
                                                    persistAggregateRating(db, workerId)
                                                }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_submit_review))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(id = R.string.reviews_count, reviews.size),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (reviews.isEmpty()) {
                            Text(
                                text = stringResource(R.string.reviews_none),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        reviews.forEach { review ->
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = review.reviewerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(review.reviewerName, fontWeight = FontWeight.SemiBold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("%.1f".format(review.rating), style = MaterialTheme.typography.labelMedium)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = formatRelative(review.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isHirer && review.userId == currentUserId) {
                                        IconButton(onClick = { pendingDeleteReviewId = review.reviewId }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.confirm_delete_review_title),
                                                tint = MaterialTheme.colorScheme.error
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
    }

    pendingDeleteReviewId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteReviewId = null },
            title = { Text(stringResource(R.string.confirm_delete_review_title)) },
            text  = { Text(stringResource(R.string.confirm_delete_review_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("reviews").document(id).delete()
                            .addOnSuccessListener {
                                scope.launch { snackbar.showSnackbar(msgReviewDeleted) }
                                persistAggregateRating(db, workerId)
                            }
                        pendingDeleteReviewId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteReviewId = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Recomputes worker.averageRating + worker.totalReviews from the reviews collection.
 * Lightweight: a single aggregate query then one doc update.
 */
private fun persistAggregateRating(db: FirebaseFirestore, workerId: String) {
    if (workerId.isBlank()) return
    db.collection("reviews").whereEqualTo("workerId", workerId).get()
        .addOnSuccessListener { snap ->
            val all = snap.toObjects(Review::class.java)
            val avg = if (all.isEmpty()) 0.0 else all.map { it.rating.toDouble() }.average()
            db.collection("workers").document(workerId)
                .update(
                    mapOf(
                        "averageRating" to avg,
                        "totalReviews"  to all.size.toLong(),
                        "updatedAt"     to FieldValue.serverTimestamp()
                    )
                )
        }
}
