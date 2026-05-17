package com.karthik.nammakelsa


import com.karthik.nammakelsa.ui.theme.brandBackground
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(receiverId: String, receiverName: String) {

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatId = chatIdFor(currentUserId, receiverId)

    var messages by remember { mutableStateOf(listOf<Message>()) }
    var messageText by remember { mutableStateOf("") }
    var editingMessageId by remember { mutableStateOf("") }
    var showClearChatDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val msgCleared = "Chat cleared"
    val msgCopied = stringResource(R.string.chat_message_copied)
    val msgDeleted = stringResource(R.string.chat_message_deleted)

    DisposableEffect(chatId) {
        if (currentUserId.isBlank() || receiverId.isBlank()) return@DisposableEffect onDispose {}
        val reg = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                messages = snap.toObjects(Message::class.java)
            }
        onDispose { reg.remove() }
    }

    // Auto-scroll to newest
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = receiverName.ifBlank { stringResource(R.string.title_chat) },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showClearChatDialog = true }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.chat_clear))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brandBackground())
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Say hi 👋",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages, key = { it.messageId.ifBlank { it.timestamp.toString() } }) { message ->
                        MessageRow(
                            message = message,
                            isMine = message.senderId == currentUserId,
                            onEdit = {
                                messageText = message.message
                                editingMessageId = message.messageId
                            },
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("message", message.message))
                                scope.launch { snackbar.showSnackbar(msgCopied) }
                            },
                            onDelete = {
                                if (message.messageId.isNotBlank()) {
                                    db.collection("chats").document(chatId)
                                        .collection("messages").document(message.messageId)
                                        .delete()
                                        .addOnSuccessListener { scope.launch { snackbar.showSnackbar(msgDeleted) } }
                                }
                            }
                        )
                    }
                }
            }

            // Input area
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = {
                            Text(
                                if (editingMessageId.isNotBlank()) stringResource(R.string.chat_edit_message_hint)
                                else stringResource(R.string.chat_message_hint)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val text = messageText.trim()
                            if (text.isBlank()) return@FilledIconButton
                            if (editingMessageId.isNotBlank()) {
                                db.collection("chats").document(chatId)
                                    .collection("messages").document(editingMessageId)
                                    .update(mapOf("message" to text, "edited" to true))
                                editingMessageId = ""
                            } else {
                                val ref = db.collection("chats").document(chatId)
                                    .collection("messages").document()
                                val now = System.currentTimeMillis()
                                val msg = Message(
                                    messageId  = ref.id,
                                    senderId   = currentUserId,
                                    receiverId = receiverId,
                                    message    = text,
                                    timestamp  = now
                                )
                                val chatMeta = mapOf(
                                    "users" to listOf(currentUserId, receiverId),
                                    "lastMessage" to text,
                                    "lastSenderId" to currentUserId,
                                    "timestamp" to now
                                )
                                db.collection("chats").document(chatId).set(chatMeta)
                                ref.set(msg)
                            }
                            messageText = ""
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (editingMessageId.isNotBlank()) Icons.Default.Check else Icons.AutoMirrored.Filled.Send,
                            contentDescription = if (editingMessageId.isNotBlank()) stringResource(R.string.action_update) else stringResource(R.string.action_send)
                        )
                    }
                }
            }
        }
    }

    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = { Text(stringResource(R.string.chat_clear_confirm_title)) },
            text  = { Text(stringResource(R.string.chat_clear_confirm_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("chats").document(chatId)
                            .collection("messages").get()
                            .addOnSuccessListener { result ->
                                for (doc in result.documents) doc.reference.delete()
                                db.collection("chats").document(chatId).delete()
                                scope.launch { snackbar.showSnackbar(msgCleared) }
                            }
                        showClearChatDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun MessageRow(
    message: Message,
    isMine: Boolean,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd   = if (isMine) 4.dp  else 18.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isMine) MaterialTheme.colorScheme.primaryContainer
                                 else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = message.message,
                        modifier = Modifier.weight(1f),
                        color = if (isMine) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Message options", modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = { menuExpanded = false; onCopy() }
                            )
                            // Edit + Delete restricted to sender
                            if (isMine) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = { menuExpanded = false; onEdit() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = { menuExpanded = false; onDelete() }
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.edited) {
                        Text(
                            text = stringResource(R.string.chat_edited_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = formatChatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
