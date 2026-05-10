package com.karthik.nammakelsa

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(

    receiverId: String
) {

    val context = LocalContext.current

    val currentUserId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    // UNIQUE CHAT ID
    val chatId =

        if (currentUserId < receiverId)
            "${currentUserId}_$receiverId"
        else
            "${receiverId}_$currentUserId"

    var messageText by remember {
        mutableStateOf("")
    }

    var messages by remember {
        mutableStateOf(listOf<Message>())
    }

    var editingMessageId by remember {
        mutableStateOf("")
    }

    // REALTIME MESSAGE LISTENER
    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")

            .orderBy(
                "timestamp",
                Query.Direction.ASCENDING
            )

            .addSnapshotListener { value, _ ->

                if (value != null) {

                    messages =
                        value.toObjects(
                            Message::class.java
                        )
                }
            }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Chat")
                },

                actions = {

                    // CLEAR ENTIRE CHAT
                    IconButton(

                        onClick = {

                            FirebaseFirestore
                                .getInstance()
                                .collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .get()

                                .addOnSuccessListener { result ->

                                    for (doc in result.documents) {

                                        doc.reference.delete()
                                    }

                                    FirebaseFirestore
                                        .getInstance()
                                        .collection("chats")
                                        .document(chatId)
                                        .delete()

                                    Toast.makeText(
                                        context,
                                        "Chat Cleared",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.DeleteForever,

                            contentDescription = null
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {

            // MESSAGE LIST
            LazyColumn(

                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp),

                contentPadding = PaddingValues(
                    bottom = 20.dp
                )
            ) {

                items(messages) { message ->

                    val isMine =
                        message.senderId ==
                                currentUserId

                    var expanded by remember {
                        mutableStateOf(false)
                    }

                    Row(

                        modifier = Modifier
                            .fillMaxWidth(),

                        horizontalArrangement =

                            if (isMine)
                                Arrangement.End
                            else
                                Arrangement.Start
                    ) {

                        ElevatedCard(

                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Row(

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(

                                    text = message.message,

                                    modifier = Modifier
                                        .padding(14.dp)
                                        .weight(1f)
                                )

                                // THREE DOT MENU
                                Box {

                                    IconButton(

                                        onClick = {
                                            expanded = true
                                        }
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.MoreVert,

                                            contentDescription = null
                                        )
                                    }

                                    DropdownMenu(

                                        expanded = expanded,

                                        onDismissRequest = {
                                            expanded = false
                                        }
                                    ) {

                                        // COPY
                                        DropdownMenuItem(

                                            text = {
                                                Text("Copy")
                                            },

                                            onClick = {

                                                val clipboard =
                                                    context.getSystemService(
                                                        Context.CLIPBOARD_SERVICE
                                                    ) as ClipboardManager

                                                val clip =
                                                    ClipData.newPlainText(
                                                        "message",
                                                        message.message
                                                    )

                                                clipboard.setPrimaryClip(
                                                    clip
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "Copied",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                expanded = false
                                            }
                                        )

                                        // EDIT ONLY FOR SENT MESSAGES
                                        if (isMine) {

                                            DropdownMenuItem(

                                                text = {
                                                    Text("Edit")
                                                },

                                                onClick = {

                                                    messageText =
                                                        message.message

                                                    editingMessageId =
                                                        message.messageId

                                                    expanded = false
                                                }
                                            )
                                        }

                                        // DELETE
                                        DropdownMenuItem(

                                            text = {
                                                Text("Delete")
                                            },

                                            onClick = {

                                                if (
                                                    message.messageId
                                                        .isNotBlank()
                                                ) {

                                                    FirebaseFirestore
                                                        .getInstance()
                                                        .collection("chats")
                                                        .document(chatId)
                                                        .collection("messages")
                                                        .document(
                                                            message.messageId
                                                        )
                                                        .delete()

                                                    Toast.makeText(
                                                        context,
                                                        "Message Deleted",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // INPUT AREA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                OutlinedTextField(

                    value = messageText,

                    onValueChange = {
                        messageText = it
                    },

                    label = {

                        if (
                            editingMessageId.isNotBlank()
                        ) {

                            Text("Edit Message")

                        } else {

                            Text("Message")
                        }
                    },

                    modifier = Modifier.weight(1f)
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Button(

                    onClick = {

                        if (messageText.isNotBlank()) {

                            // EDIT MESSAGE
                            if (
                                editingMessageId
                                    .isNotBlank()
                            ) {

                                FirebaseFirestore
                                    .getInstance()
                                    .collection("chats")
                                    .document(chatId)
                                    .collection("messages")
                                    .document(
                                        editingMessageId
                                    )

                                    .update(
                                        "message",
                                        messageText
                                    )

                                editingMessageId = ""
                            }

                            // SEND NEW MESSAGE
                            else {

                                val messageId =
                                    FirebaseFirestore
                                        .getInstance()
                                        .collection("chats")
                                        .document(chatId)
                                        .collection("messages")
                                        .document()
                                        .id

                                val message =
                                    Message(

                                        messageId =
                                            messageId,

                                        senderId =
                                            currentUserId,

                                        receiverId =
                                            receiverId,

                                        message =
                                            messageText
                                    )

                                val chatData = hashMapOf(

                                    "users" to listOf(
                                        currentUserId,
                                        receiverId
                                    ),

                                    "lastMessage" to messageText,

                                    "timestamp" to
                                            System.currentTimeMillis()
                                )

                                // CREATE/UPDATE CHAT DOC
                                FirebaseFirestore
                                    .getInstance()
                                    .collection("chats")
                                    .document(chatId)
                                    .set(chatData)

                                // SAVE MESSAGE
                                FirebaseFirestore
                                    .getInstance()
                                    .collection("chats")
                                    .document(chatId)
                                    .collection("messages")
                                    .document(messageId)
                                    .set(message)
                            }

                            messageText = ""
                        }
                    }
                ) {

                    if (
                        editingMessageId.isNotBlank()
                    ) {

                        Text("Update")

                    } else {

                        Text("Send")
                    }
                }
            }
        }
    }
}