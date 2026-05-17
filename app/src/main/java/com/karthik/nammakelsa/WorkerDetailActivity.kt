package com.karthik.nammakelsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karthik.nammakelsa.ui.theme.NammaKelsaTheme

class WorkerDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workerId = intent.getStringExtra("workerId") ?: ""
        val name     = intent.getStringExtra("name")     ?: ""
        val skill    = intent.getStringExtra("skill")    ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val charge   = intent.getStringExtra("charge")   ?: ""
        val phone    = intent.getStringExtra("phone")    ?: ""
        val whatsapp = intent.getStringExtra("whatsapp") ?: ""

        setContent {
            NammaKelsaTheme {
                WorkerDetailScreen(workerId, name, skill, location, charge, phone, whatsapp)
            }
        }
    }
}

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
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isHirer     by remember { mutableStateOf(false) }
    var rating      by remember { mutableStateOf(0f) }
    var comment     by remember { mutableStateOf("") }
    var workDetails by remember { mutableStateOf("") }
    var reviews     by remember { mutableStateOf(listOf<Review>()) }
    var averageRating by remember { mutableStateOf(0f) }

    // CHECK IF CURRENT USER IS A HIRER
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("hirers")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { isHirer = it.exists() }
    }

    // LOAD REVIEWS
    fun loadReviews() {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .whereEqualTo("workerId", workerId)
            .get()
            .addOnSuccessListener { snap ->
                reviews = snap.toObjects(Review::class.java)
                averageRating = if (reviews.isNotEmpty())
                    reviews.map { it.rating }.average().toFloat() else 0f
            }
    }

    LaunchedEffect(Unit) { loadReviews() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {

            // ── INITIAL AVATAR ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(180.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NAME
            Text(text = name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            // AVG RATING
            Text(text = "⭐ ${"%.1f".format(averageRating)}/5", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(20.dp))

            // ── DETAILS CARD ───────────────────────────────────────────────
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = "🛠 Skill: $skill")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "📍 Location: $location")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "₹ $charge/day")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "📞 $phone")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── ACTION BUTTONS ─────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$phone")
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Call") }

                    FilledTonalButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsapp")))
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("WhatsApp") }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = {
                            FirebaseFirestore.getInstance().collection("favorites")
                                .add(hashMapOf("userId" to currentUserId, "workerId" to workerId))
                            Toast.makeText(context, "Worker Saved ❤️", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("❤️ Save") }

                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(context, ChatActivity::class.java)
                            intent.putExtra("receiverId", workerId)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("💬 Chat") }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── SEND REQUEST CARD ──────────────────────────────────────────
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Send Work Request", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = workDetails,
                        onValueChange = { workDetails = it },
                        label = { Text("Work Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            FirebaseFirestore.getInstance()
                                .collection("hirers").document(currentUserId).get()
                                .addOnSuccessListener { hirerDoc ->
                                    val requestId = FirebaseFirestore.getInstance().collection("requests").document().id
                                    FirebaseFirestore.getInstance().collection("requests")
                                        .document(requestId)
                                        .set(hashMapOf(
                                            "requestId"      to requestId,
                                            "workerId"       to workerId,
                                            "hirerId"        to currentUserId,
                                            "hirerName"      to (hirerDoc.getString("name")          ?: ""),
                                            "hirerImage"     to (hirerDoc.getString("imageUrl")       ?: ""),
                                            "hirerLocation"  to (hirerDoc.getString("location")       ?: ""),
                                            "hirerPhone"     to (hirerDoc.getString("phoneNumber")    ?: ""),
                                            "hirerWhatsapp"  to (hirerDoc.getString("whatsappNumber") ?: ""),
                                            "workDetails"    to workDetails,
                                            "status"         to "Pending"
                                        ))
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Request Sent ✅", Toast.LENGTH_LONG).show()
                                            workDetails = ""
                                        }
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Send Request") }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── REVIEW FORM — only visible to hirers ───────────────────────
            if (isHirer) {
                ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Rate Worker", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))

                        // STAR ROW
                        Row {
                            for (i in 1..5) {
                                IconButton(onClick = { rating = i.toFloat() }) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Write Review") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (rating > 0 && comment.isNotBlank()) {
                                    FirebaseFirestore.getInstance()
                                        .collection("hirers").document(currentUserId).get()
                                        .addOnSuccessListener { hirerDoc ->
                                            val hirerName = hirerDoc.getString("name") ?: "Anonymous"
                                            val reviewId  = FirebaseFirestore.getInstance().collection("reviews").document().id
                                            val review    = Review(
                                                reviewId     = reviewId,
                                                workerId     = workerId,
                                                userId       = currentUserId,
                                                reviewerName = hirerName,
                                                rating       = rating,
                                                comment      = comment
                                            )
                                            FirebaseFirestore.getInstance()
                                                .collection("reviews").document(reviewId).set(review)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Review Added ⭐", Toast.LENGTH_LONG).show()
                                                    comment = ""; rating = 0f
                                                    loadReviews()
                                                }
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Submit Review") }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // ── REVIEWS LIST ───────────────────────────────────────────────
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Reviews (${reviews.size})", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (reviews.isEmpty()) {
                        Text(
                            text = "No reviews yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    reviews.forEach { review ->
                        ElevatedCard(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // ── REVIEWER INITIAL AVATAR ────────────────
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = review.reviewerName
                                                .firstOrNull()
                                                ?.uppercaseChar()
                                                ?.toString() ?: "?",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // REVIEW CONTENT
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = review.reviewerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "⭐ ${"%.1f".format(review.rating)}")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
                                }

                                // DELETE — only hirer who wrote it can see this
                                if (isHirer && review.userId == currentUserId) {
                                    IconButton(
                                        onClick = {
                                            FirebaseFirestore.getInstance()
                                                .collection("reviews").document(review.reviewId).delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Review Deleted", Toast.LENGTH_SHORT).show()
                                                    loadReviews()
                                                }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete review",
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