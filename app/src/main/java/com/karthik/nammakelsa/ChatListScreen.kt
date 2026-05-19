package com.karthik.nammakelsa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class ChatPreview(
    val userId: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)

@Composable
fun ChatListScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUser =
        FirebaseAuth.getInstance().currentUser ?: return

    val currentUserId = currentUser.uid

    var chats by remember {
        mutableStateOf<List<ChatPreview>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    DisposableEffect(Unit) {

        val listener: ListenerRegistration =
            db.collection("chats")
                .whereArrayContains(
                    "participants",
                    currentUserId
                )
                .addSnapshotListener { snap, _ ->

                    if (snap == null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (snap.documents.isEmpty()) {
                        chats = emptyList()
                        isLoading = false
                        return@addSnapshotListener
                    }

                    val tempChats =
                        mutableStateListOf<ChatPreview>()

                    snap.documents.forEach { chatDoc ->

                        val participants =
                            chatDoc.get("participants")
                                    as? List<String>
                                ?: emptyList()

                        val otherUserId =
                            participants.firstOrNull {
                                it != currentUserId
                            } ?: return@forEach

                        fun addChat(name: String) {
                            val preview =
                                ChatPreview(
                                    userId = otherUserId,
                                    name = name,
                                    lastMessage =
                                        chatDoc.getString("lastMessage")
                                            ?: "",
                                    lastMessageTime =
                                        chatDoc.getLong("lastMessageTime")
                                            ?: 0L
                                )

                            tempChats.removeAll {
                                it.userId == otherUserId
                            }

                            tempChats.add(preview)

                            chats =
                                tempChats.sortedByDescending {
                                    it.lastMessageTime
                                }

                            isLoading = false
                        }

                        db.collection("workers")
                            .document(otherUserId)
                            .get()
                            .addOnSuccessListener { workerDoc ->

                                if (workerDoc.exists()) {
                                    addChat(
                                        workerDoc.getString("name")
                                            ?: "Worker"
                                    )
                                } else {
                                    db.collection("hirers")
                                        .document(otherUserId)
                                        .get()
                                        .addOnSuccessListener { hirerDoc ->
                                            addChat(
                                                hirerDoc.getString("name")
                                                    ?: "Hirer"
                                            )
                                        }
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
            .padding(16.dp)
    ) {

        Text(
            text = "Chats",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No chats yet")
            }
            return@Column
        }

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            items(chats) { chat ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(
                                Intent(
                                    context,
                                    ChatActivity::class.java
                                ).apply {
                                    putExtra(
                                        "receiverId",
                                        chat.userId
                                    )
                                }
                            )
                        }
                ) {
                    Column(
                        modifier =
                            Modifier.padding(16.dp)
                    ) {

                        Text(
                            chat.name,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(chat.lastMessage)
                    }
                }
            }
        }
    }
}