package com.karthik.nammakelsa

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getChatId(
        user1: String,
        user2: String
    ): String {
        return if (user1 < user2)
            "${user1}_$user2"
        else
            "${user2}_$user1"
    }

    fun setUserOnline(userId: String) {
        db.collection("presence")
            .document(userId)
            .set(
                mapOf(
                    "online" to true,
                    "lastSeen" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }

    fun setUserOffline(userId: String) {
        db.collection("presence")
            .document(userId)
            .set(
                mapOf(
                    "online" to false,
                    "lastSeen" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }

    fun sendMessage(
        receiverId: String,
        messageText: String
    ) {
        val currentUser = auth.currentUser ?: return

        val senderId = currentUser.uid
        val chatId = getChatId(senderId, receiverId)

        val chatRef =
            db.collection("chats")
                .document(chatId)

        val messageRef =
            chatRef.collection("messages")
                .document()

        val timestamp =
            System.currentTimeMillis()

        val message = Message(
            messageId = messageRef.id,
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            timestamp = timestamp,
            deliveredTo = listOf(receiverId),
            seenBy = emptyList(),
            deletedFor = emptyList()
        )

        val batch = db.batch()

        batch.set(
            chatRef,
            mapOf(
                "participants" to listOf(
                    senderId,
                    receiverId
                ),
                "lastMessage" to messageText,
                "lastMessageTime" to timestamp
            ),
            SetOptions.merge()
        )

        batch.set(messageRef, message)

        batch.commit()
    }

    fun markMessagesSeen(
        chatId: String,
        userId: String
    ) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("receiverId", userId)
            .get()
            .addOnSuccessListener { snap ->

                val unseen =
                    snap.documents.filter {
                        !(it.get("seenBy") as? List<*>)
                            .orEmpty()
                            .contains(userId)
                    }

                if (unseen.isEmpty()) return@addOnSuccessListener

                val batch = db.batch()

                unseen.forEach { doc ->
                    batch.update(
                        doc.reference,
                        "seenBy",
                        FieldValue.arrayUnion(userId)
                    )
                }

                batch.commit()
            }
    }

    fun deleteMessageForUser(
        chatId: String,
        messageId: String,
        userId: String
    ) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                "deletedFor",
                FieldValue.arrayUnion(userId)
            )
    }

    fun clearChatForUser(
        chatId: String,
        userId: String,
        onDone: () -> Unit
    ) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .get()
            .addOnSuccessListener { snap ->

                if (snap.isEmpty) {
                    onDone()
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                snap.documents.forEach { doc ->
                    batch.update(
                        doc.reference,
                        "deletedFor",
                        FieldValue.arrayUnion(userId)
                    )
                }

                batch.commit()
                    .addOnSuccessListener {
                        onDone()
                    }
            }
    }
}