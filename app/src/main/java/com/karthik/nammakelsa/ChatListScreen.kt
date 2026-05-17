package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.karthik.nammakelsa.ui.theme.SuccessGreen

@Composable
fun ChatListScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var chatUsers by remember { mutableStateOf(listOf<ChatUser>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }

        val listeners = mutableListOf<ListenerRegistration>()

        // 1. Watch our chats only.
        listeners += db.collection("chats")
            .whereArrayContains("users", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener

                val seeds = snap.documents.mapNotNull { doc ->
                    val ids = doc.id.split("_")
                    if (!ids.contains(currentUserId)) return@mapNotNull null
                    val otherId = ids.firstOrNull { it != currentUserId } ?: return@mapNotNull null
                    Triple(otherId, doc.getString("lastMessage") ?: "", doc.getLong("timestamp") ?: 0L)
                }

                if (seeds.isEmpty()) {
                    chatUsers = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }

                // 2. For each peer, look it up. Try workers first, fall back to hirers.
                val collected = MutableList<ChatUser?>(seeds.size) { null }
                var done = 0
                seeds.forEachIndexed { index, (otherId, lastMessage, ts) ->
                    db.collection("workers").document(otherId).get()
                        .addOnSuccessListener { wDoc ->
                            if (wDoc.exists()) {
                                collected[index] = ChatUser(
                                    userId = otherId,
                                    name = wDoc.getString("name") ?: "",
                                    imageUrl = wDoc.getString("imageUrl") ?: "",
                                    lastMessage = lastMessage,
                                    lastMessageTime = ts,
                                    online = wDoc.getBoolean("online") ?: false
                                )
                                done++
                            } else {
                                db.collection("hirers").document(otherId).get()
                                    .addOnSuccessListener { hDoc ->
                                        if (hDoc.exists()) {
                                            collected[index] = ChatUser(
                                                userId = otherId,
                                                name = hDoc.getString("name") ?: "",
                                                imageUrl = hDoc.getString("imageUrl") ?: "",
                                                lastMessage = lastMessage,
                                                lastMessageTime = ts,
                                                online = hDoc.getBoolean("online") ?: false
                                            )
                                        }
                                        done++
                                        if (done == seeds.size) {
                                            chatUsers = collected.filterNotNull()
                                                .distinctBy { it.userId }
                                                .sortedByDescending { it.lastMessageTime }
                                            isLoading = false
                                        }
                                    }
                                    .addOnFailureListener {
                                        done++
                                        if (done == seeds.size) {
                                            chatUsers = collected.filterNotNull()
                                                .distinctBy { it.userId }
                                                .sortedByDescending { it.lastMessageTime }
                                            isLoading = false
                                        }
                                    }
                                return@addOnSuccessListener
                            }
                            if (done == seeds.size) {
                                chatUsers = collected.filterNotNull()
                                    .distinctBy { it.userId }
                                    .sortedByDescending { it.lastMessageTime }
                                isLoading = false
                            }
                        }
                        .addOnFailureListener {
                            done++
                            if (done == seeds.size) {
                                chatUsers = collected.filterNotNull()
                                isLoading = false
                            }
                        }
                }
            }

        onDispose { listeners.forEach { it.remove() } }
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
    ) {
        Text(
            text = stringResource(R.string.title_chats),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { ShimmerCard() }
                }
                chatUsers.isEmpty() -> EmptyState(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = stringResource(R.string.empty_chats),
                    subtitle = "Start a conversation from a worker's profile."
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                    items(chatUsers, key = { it.userId }) { user ->
                        ChatRow(user = user, onClick = {
                            val intent = Intent(context, ChatActivity::class.java)
                                .putExtra("receiverId", user.userId)
                                .putExtra("receiverName", user.name)
                            context.startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(user: ChatUser, onClick: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (user.imageUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(user.imageUrl),
                        contentDescription = "Profile photo of ${user.name}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                if (user.online) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp),
                        shape = CircleShape,
                        color = SuccessGreen,
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    if (user.lastMessageTime > 0L) {
                        Text(
                            text = formatChatTime(user.lastMessageTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = user.lastMessage.ifBlank { if (user.online) stringResource(R.string.chat_status_online) else stringResource(R.string.chat_status_offline) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
