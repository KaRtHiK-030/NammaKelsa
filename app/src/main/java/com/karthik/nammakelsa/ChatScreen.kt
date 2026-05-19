package com.karthik.nammakelsa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(receiverId: String) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val currentUserId = currentUser.uid

    val chatId =
        ChatRepository.getChatId(
            currentUserId,
            receiverId
        )

    var messages by remember {
        mutableStateOf<List<Message>>(emptyList())
    }

    var inputMessage by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    val listState =
        rememberLazyListState()

    DisposableEffect(Unit) {

        ChatRepository.setUserOnline(
            currentUserId
        )

        val listener: ListenerRegistration =
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .addSnapshotListener { snap, _ ->

                    if (snap == null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    messages =
                        snap.documents.mapNotNull {
                            it.toObject(
                                Message::class.java
                            )
                        }
                            .filter {
                                !it.deletedFor.contains(
                                    currentUserId
                                )
                            }
                            .sortedBy {
                                it.timestamp
                            }

                    ChatRepository.markMessagesSeen(
                        chatId,
                        currentUserId
                    )

                    isLoading = false
                }

        onDispose {
            listener.remove()

            ChatRepository.setUserOffline(
                currentUserId
            )
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush())
    ) {

        TopAppBar(
            title = {
                Text("Chat")
            },
            actions = {

                IconButton(
                    onClick = {
                        ChatRepository.clearChatForUser(
                            chatId,
                            currentUserId
                        ) {}
                    }
                ) {
                    Icon(
                        Icons.Default.ClearAll,
                        null
                    )
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment =
                    Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding =
                    PaddingValues(12.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(messages) { msg ->

                    val isMine =
                        msg.senderId ==
                                currentUserId

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            if (isMine)
                                Arrangement.End
                            else
                                Arrangement.Start
                    ) {

                        Card {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        12.dp
                                    )
                            ) {

                                Text(
                                    text = msg.message,
                                    textAlign =
                                        if (isMine)
                                            TextAlign.End
                                        else
                                            TextAlign.Start
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    if (isMine) {
                                        Text(
                                            if (
                                                msg.seenBy.contains(
                                                    receiverId
                                                )
                                            )
                                                "✓✓ Seen"
                                            else
                                                "✓ Sent",
                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.width(8.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            ChatRepository.deleteMessageForUser(
                                                chatId,
                                                msg.messageId,
                                                currentUserId
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = inputMessage,
                onValueChange = {
                    inputMessage = it
                },
                modifier =
                    Modifier.weight(1f),
                placeholder = {
                    Text("Type message")
                }
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            IconButton(
                onClick = {
                    if (
                        inputMessage.isNotBlank()
                    ) {
                        ChatRepository.sendMessage(
                            receiverId,
                            inputMessage.trim()
                        )

                        inputMessage = ""
                    }
                }
            ) {
                Icon(
                    Icons.Default.Send,
                    null
                )
            }
        }
    }
}