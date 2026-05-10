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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChatListScreen() {

    val context = LocalContext.current

    val currentUserId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var chatUsers by remember {
        mutableStateOf(listOf<ChatUser>())
    }

    // REALTIME CHAT LISTENER
    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("chats")

            .addSnapshotListener { chats, _ ->

                if (chats != null) {

                    val users =
                        mutableListOf<ChatUser>()

                    chats.documents.forEach { chatDoc ->

                        val ids =
                            chatDoc.id.split("_")

                        // CHECK IF CURRENT USER IS PART OF CHAT
                        if (ids.contains(currentUserId)) {

                            val otherUserId =

                                ids.first {
                                    it != currentUserId
                                }

                            // SEARCH WORKERS
                            FirebaseFirestore
                                .getInstance()
                                .collection("workers")
                                .document(otherUserId)
                                .get()

                                .addOnSuccessListener { workerDoc ->

                                    if (workerDoc.exists()) {

                                        val user =
                                            ChatUser(

                                                userId =
                                                    otherUserId,

                                                name =
                                                    workerDoc.getString("name")
                                                        ?: "",

                                                imageUrl =
                                                    workerDoc.getString("imageUrl")
                                                        ?: "",

                                                online =
                                                    workerDoc.getBoolean("online")
                                                        ?: false
                                            )

                                        users.add(user)

                                        chatUsers =
                                            users.distinctBy {
                                                it.userId
                                            }
                                    }

                                    else {

                                        // SEARCH HIRERS
                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("hirers")
                                            .document(otherUserId)
                                            .get()

                                            .addOnSuccessListener { hirerDoc ->

                                                if (hirerDoc.exists()) {

                                                    val user =
                                                        ChatUser(

                                                            userId =
                                                                otherUserId,

                                                            name =
                                                                hirerDoc.getString("name")
                                                                    ?: "",

                                                            imageUrl =
                                                                hirerDoc.getString("imageUrl")
                                                                    ?: "",

                                                            online =
                                                                hirerDoc.getBoolean("online")
                                                                    ?: false
                                                        )

                                                    users.add(user)

                                                    chatUsers =
                                                        users.distinctBy {
                                                            it.userId
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

    Column(
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
    ) {

        Text(

            text = "Chats",

            style = MaterialTheme
                .typography
                .headlineMedium,

            modifier = Modifier.padding(20.dp)
        )

        // EMPTY STATE
        if (chatUsers.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "No chats yet"
                )
            }
        }

        else {

            LazyColumn(

                contentPadding = PaddingValues(
                    bottom = 140.dp
                )
            ) {

                items(chatUsers) { user ->

                    ElevatedCard(

                        shape = RoundedCornerShape(20.dp),

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                            .clickable {

                                val intent =
                                    Intent(
                                        context,
                                        ChatActivity::class.java
                                    )

                                intent.putExtra(
                                    "receiverId",
                                    user.userId
                                )

                                context.startActivity(intent)
                            }
                    ) {

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Image(
                                painter =
                                    rememberAsyncImagePainter(
                                        user.imageUrl
                                    ),

                                contentDescription = null,

                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape),

                                contentScale =
                                    ContentScale.Crop
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(16.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text = user.name,

                                    style = MaterialTheme
                                        .typography
                                        .titleLarge
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        if (user.online)
                                            "🟢 Online"
                                        else
                                            "⚫ Offline"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}